package com.zt.simpledao.apt;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import com.zt.simpledao.Column;
import com.zt.simpledao.Database;
import com.zt.simpledao.SQLDataType;
import com.zt.simpledao.Table;
import com.zt.simpledao.bean.ColumnItem;
import com.zt.simpledao.bean.PropMethodItem;

public class DAOProcessor extends AbstractProcessor {
	private Filer filer;
	private List<ColumnItem> primaryKeys;
	private Map<Integer, ColumnItem> indexItemMap;
	
	@Override
	public void init(ProcessingEnvironment env) {
		filer = env.getFiler();
		indexItemMap = new HashMap<Integer, ColumnItem>();
		super.init(env);
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		Set<String> annotataions = new LinkedHashSet<String>();
        annotataions.add("com.zt.simpledao.Column");
        annotataions.add("com.zt.simpledao.Database");
        annotataions.add("com.zt.simpledao.Table");
        return annotataions;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		Set<TypeElement> dbfiles = (Set<TypeElement>) roundEnv
				.getElementsAnnotatedWith(Database.class);
		for (TypeElement dbfile : dbfiles) {
			// init database file path
			String packageName = dbfile.toString().substring(0,
					dbfile.toString().lastIndexOf("."));
			String autoAPTPackageName = packageName + ".auto";
			String proxyClassName = dbfile.getSimpleName() + "Proxy";
			// create file
			createBeanProxy(autoAPTPackageName, proxyClassName, dbfile);
			String daoClassName = dbfile.getSimpleName() + "DAO";
			createDAO(autoAPTPackageName, daoClassName, proxyClassName, dbfile);
		}
		return true;
	}

	private void createBeanProxy(String autoAPTPackageName, String proxyClassName,
			Element element) {
		StringBuilder proxyContent = new StringBuilder();
		appendBeanProxyPackage(autoAPTPackageName, proxyContent);
		appendBeanProxyImport(element, proxyContent);
		appendBeanProxyClassStart(proxyClassName, element, proxyContent);
		appendBeanProxyColumnConst(element, proxyContent);
		// field database
		Database db = element.getAnnotation(Database.class);
		if (db.version() < 1) {
			error("database version must > 1", element);
			return;
		}
		proxyContent.append("	private static final String DATABASE_NAME = ")
				.append("\"").append(db.name()).append("\"").append(";\n");
		proxyContent.append("	private static final int VERSION = ")
				.append(db.version()).append(";\n");
		// field table
		Table t = element.getAnnotation(Table.class);
		if (null == t) {
			error("database must have @Table!", element);
			return;
		}
		proxyContent.append("	private static final String TABLE = ").append("\"")
				.append(t.name()).append("\"").append(";\n");
		proxyContent.append("	private static final String TABLE_CREATOR = ")
				.append("\"").append(crateTable(t.name())).append("\"")
				.append(";\n");
		proxyContent.append(
				"	private static final HashMap<String, String> CACHE_UPDATE")
				.append(" = new HashMap<String, String>();\n");
		proxyContent.append(
				"	private static final HashMap<String, String> CACHE_DELETE")
				.append(" = new HashMap<String, String>();\n");
		// sql string
		// insert
		appendInsertSQLString(proxyContent, t.name());
		// update
		appendUpdateSQLString(proxyContent, t.name());
		// delete
		appendDelelteSQLString(proxyContent, t.name());
		// method
		appendMethods(proxyContent, element.getSimpleName().toString());
		// class end
		proxyContent.append("\n}");
		// write file
		JavaFileObject file = null;
		Writer writer = null;
		try {
			file = filer.createSourceFile(autoAPTPackageName + "." + proxyClassName,
					element);
			if (null != file) {
				writer = file.openWriter();
				writer.append(proxyContent).flush();
			}
		} catch (IOException e) {
		} finally {
			if (null != writer) {
				try {
					writer.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private void appendBeanProxyPackage(String autoAPTPackageName,
			StringBuilder content) {
		content.append("package ").append(autoAPTPackageName).append(";\n");
	}

	private void appendBeanProxyImport(Element element, StringBuilder content) {
		content.append("\nimport java.util.ArrayList;");
		content.append("\nimport java.util.HashMap;");
		content.append("\nimport java.util.List;\n");
		content.append("\nimport android.database.Cursor;");
		content.append("\nimport android.database.sqlite.SQLiteDatabase;");
		content.append("\nimport android.database.sqlite.SQLiteStatement;");
		content.append("\nimport android.text.TextUtils;");
		content.append("\nimport ").append(element.toString()).append(";\n");
		content.append("import com.zt.simpledao.bean.IBeanProxy;\n");
	}

	private void appendBeanProxyClassStart(String proxyClassName, Element element,
			StringBuilder content) {
		content.append("\npublic class ").append(proxyClassName)
				.append(" implements IBeanProxy").append("<")
				.append(element.getSimpleName()).append(">").append(" {\n");
		content.append("	// ").append(element.toString()).append("\n");
	}

	private void appendBeanProxyColumnConst(Element element, StringBuilder content) {
		if (null == primaryKeys) {
			primaryKeys = new ArrayList<ColumnItem>();
		} else {
			primaryKeys.clear();
		}
		indexItemMap.clear();
		int index = 0;
		boolean isUseUserDefinedIndex = false;
		int userDefinedIndexTotal = 0;
		List<VariableElement> variables = ElementFilter.fieldsIn(element
				.getEnclosedElements());
		for (VariableElement fieldElement : variables) {
			Column c = fieldElement.getAnnotation(Column.class);
			ColumnItem column = new ColumnItem();
			// 用户自定义了一个列index，那么所有列都需要自定义
			if (-1 != c.index()) {
				isUseUserDefinedIndex = true;
				userDefinedIndexTotal ++;
			}
			column.index = (-1 == c.index()) ? index : c.index();
			column.fieldName = fieldElement.getSimpleName().toString();
			column.columnName = (null != c.name() && !c.name().isEmpty()) ? c
					.name() : column.fieldName;
			column.sqlType = c.type();
			if (c.primary()) {
				primaryKeys.add(column);
			}
			column.typeKind = fieldElement.asType().getKind();
			Set<Modifier> modifiers = fieldElement.getModifiers();
			if (!modifiers.contains(Modifier.PUBLIC)) {
				// field isn't public, need getter&setter
				column.getterSetter = new PropMethodItem();
				final String old = column.fieldName;
				final String firstStr = new String(new char[]{old.charAt(0)});
				final String otherStr = old.substring(1);
				final String upperFirst = firstStr.toUpperCase();
				final String useName = upperFirst + otherStr;
				column.getterSetter.setterName = "set" + useName;
				if (TypeKind.BOOLEAN == column.typeKind) {
					column.getterSetter.getterName = "is" + useName;
				} else {
					column.getterSetter.getterName = "get" + useName;
				}
			}
			
			if (TypeKind.BOOLEAN == column.typeKind
					&& SQLDataType.TEXT == column.sqlType) {
				warning("Maybe set boolean's SQLDataType=Integer will better",
						fieldElement);
			}
			indexItemMap.put(column.index, column);
			content.append("	public static final String ")
					.append(fieldElement.getSimpleName()).append(" = ")
					.append("\"").append(column.columnName).append("\";\n");
			content.append("	public static final int ")
					.append(fieldElement.getSimpleName()).append("_id")
					.append(" = ").append(column.index).append(";\n");
			index++;
		}
		// check if user defined index right
		if (isUseUserDefinedIndex && index != userDefinedIndexTotal) {
			error("if you defined one index you should define all @Column index", element);
		}
	}

	private String crateTable(String table) {
		// Create table xxx (column type, column type, primary key (column));
		StringBuilder sb = new StringBuilder();
		sb.append("create table if not exists ").append(table).append("(");
		final int total = indexItemMap.size();
		// 转换为了按Column中声明的index顺序构造sql语句。
		for (int i = 0; i < total; i++) {
			ColumnItem item = indexItemMap.get(i);
			sb.append(item.columnName).append(" ").append(item.sqlType.toString());
			if (item.index == (total - 1)) {
				// 创建最后一列
				if (!primaryKeys.isEmpty()) {
					// 存在主键，在最后添加主键语句
					sb.append(", ").append("primary key (");
					final int primaryTotal = primaryKeys.size();
					for (int pri = 0; pri < primaryTotal; pri++) {
						ColumnItem primary = primaryKeys.get(pri);
						sb.append(primary.columnName);
						if (pri != (primaryTotal - 1)) {
							// 多个主键之间用逗号隔开
							sb.append(",");
						}
					}
					sb.append(")");
				}
				sb.append(");");
			} else {
				sb.append(", ");
			}
		}
		return sb.toString();
	}

	private void appendInsertSQLString(StringBuilder sb, String tableName) {
		sb.append("	private static final String INSERT = \"insert into ")
				.append(tableName).append(" (");
		final Collection<ColumnItem> columns = indexItemMap.values();
		final int count = columns.size();
		int index = 0;
		for (ColumnItem column : columns) {
			final String name = column.columnName;
			sb.append(name);
			if (index < (count - 1)) {
				sb.append(",");
			} else {
				sb.append(") ");
			}
			index++;
		}
		sb.append("values(");
		for (int i = 0; i < count; i++) {
			sb.append("?");
			if (i < (count - 1)) {
				sb.append(",");
			} else {
				sb.append(");");
			}
		}
		sb.append("\";\n");
	}

	private void appendUpdateSQLString(StringBuilder sb, String tableName) {
		sb.append("	private static final String UPDATE = \"update ")
				.append(tableName).append(" set ");
		final Collection<ColumnItem> columns = indexItemMap.values();
		final int count = columns.size();
		int index = 0;
		for (ColumnItem column : columns) {
			final String name = column.columnName;
			sb.append(name).append("=?");
			if (index < (count - 1)) {
				sb.append(", ");
			} else {
				sb.append(" ");
			}
			index++;
		}
		sb.append("\";\n");
	}
	
	private void appendDelelteSQLString(StringBuilder sb, String tableName) {
		sb.append("	private static final String DELETE = \"delete from ")
				.append(tableName).append(" \";\n");
	}

	private void appendMethods(StringBuilder sb, String className) {
		sb.append("\n	@Override\n");
		sb.append("	public String getDataBaseName() {\n").append(
				"		return DATABASE_NAME;\n	}\n");

		sb.append("\n	@Override\n");
		sb.append("	public int getDataBaseVersion() {\n").append(
				"		return VERSION;\n	}\n");

		sb.append("\n	@Override\n");
		sb.append("	public String getTableName() {\n").append(
				"		return TABLE;\n	}\n");

		sb.append("\n	@Override\n");
		sb.append("	public String getTableCreator() {\n").append(
				"		return TABLE_CREATOR;\n	}\n");

		sb.append("\n	@Override\n");
		sb.append("	public Class").append("<").append(className)
				.append("> getBeanClass() {\n").append("		return ")
				.append(className).append(".class;\n	}\n");

		appendConvertDBToBean(sb, className);
		appendCreateInsertSQL(sb, className);
		appendCreateUpdateSQL(sb, className);
		appendBindBeanArg(sb, className);
		appendCreateDeleteSQL(sb);
	}

	private void appendConvertDBToBean(StringBuilder sb, String className) {
		sb.append("\n	@Override\n");
		sb.append("	public List<").append(className).append(">")
				.append(" convertDatabaseToBean(Cursor cursor) {\n")
				.append("		List<").append(className)
				.append("> beans = new ArrayList<").append(className)
				.append(">();\n");
		sb.append("		if (null != cursor) {\n			while(cursor.moveToNext()) {\n");
		sb.append("				try {\n").append("					");
		sb.append(className).append(" item = getBeanClass().newInstance();\n");
		final Collection<ColumnItem> columns = indexItemMap.values();
		for (ColumnItem column : columns) {
			final String fieldName = column.fieldName;
			final SQLDataType sqlType = column.sqlType;
			final TypeKind fieldType = column.typeKind;
			final PropMethodItem propMethod = column.getterSetter;
			final String setter = (null == propMethod) ? null
					: propMethod.setterName;
			String getValue = "";
			// 开始构建设值语句。
			sb.append("					");
			sb.append("item.");
			if (SQLDataType.INTEGER == sqlType) {
				if (TypeKind.BOOLEAN == fieldType) {
					getValue = new StringBuilder("cursor.getInt(")
							.append(column.index).append(") == 1 ? true : false")
							.toString();
				} else if (TypeKind.LONG == fieldType) {
					getValue = new StringBuilder("cursor.getLong(")
							.append(column.index).append(")").toString();
				} else if (TypeKind.SHORT == fieldType) {
					getValue = new StringBuilder("cursor.getShort(")
							.append(column.index).append(")").toString();
				} else if (TypeKind.INT == fieldType) {
					getValue = new StringBuilder("cursor.getInt(")
							.append(column.index).append(")").toString();
				}
			} else if (SQLDataType.TEXT == sqlType) {
				if (TypeKind.BOOLEAN == fieldType) {
					getValue = new StringBuilder("Boolean.valueOf(cursor.getString(")
							.append(column.index).append("))").toString();
				} else if (TypeKind.DOUBLE == fieldType) {
					getValue = new StringBuilder("Double.valueOf(cursor.getString(")
							.append(column.index).append("))").toString();
				} else if (TypeKind.FLOAT == fieldType) {
					getValue = new StringBuilder("Float.valueOf(cursor.getString(")
							.append(column.index).append("))").toString();
				} else if (TypeKind.INT == fieldType) {
					getValue = new StringBuilder("Integer.valueOf(cursor.getString(")
							.append(column.index).append("))").toString();
				} else if (TypeKind.LONG == fieldType) {
					getValue = new StringBuilder("Long.valueOf(cursor.getString(")
							.append(column.index).append("))").toString();
				} else if (TypeKind.SHORT == fieldType) {
					getValue = new StringBuilder("Short.valueOf(cursor.getString(")
							.append(column.index).append("))").toString();
				} else {
					getValue = new StringBuilder("cursor.getString(")
							.append(column.index).append(")").toString();
				}
			} else if (SQLDataType.NULL == sqlType) {
				getValue = new StringBuilder("null").toString();
			} else if (SQLDataType.BLOB == sqlType) {
				getValue = new StringBuilder("cursor.getBlob(").append(column.index)
						.append(")").toString();
			} else if (SQLDataType.REAL == sqlType) {
				if (TypeKind.FLOAT == fieldType) {
					getValue = new StringBuilder("cursor.getFloat(").append(column.index)
							.append(")").toString();
				} else if (TypeKind.DOUBLE == fieldType) {
					getValue = new StringBuilder("cursor.getDouble(").append(column.index)
							.append(")").toString();
				}
			}
			if (null == setter) {
				sb.append(fieldName).append(" = ").append(getValue);
			} else {
				sb.append(setter).append("(").append(getValue).append(")");
			}
			sb.append(";\n");
		}
		sb.append("					");
		sb.append("beans.add(item);\n");
		sb.append("				");
		sb.append("} catch (InstantiationException e) {\n");
		sb.append("					");
		sb.append("e.printStackTrace();\n");
		sb.append("				");
		sb.append("} catch (IllegalAccessException e) {\n");
		sb.append("					");
		sb.append("e.printStackTrace();\n");
		sb.append("				");
		sb.append("}\n			}\n");
		sb.append("			cursor.close();\n		}\n		return beans;\n	}\n");
	}

	private void appendCreateInsertSQL(StringBuilder sb, String className) {
		sb.append("\n	@Override\n");
		sb.append("	public SQLiteStatement createInsertSQL(SQLiteDatabase database,")
				.append(className).append(" bean) {\n");
		sb.append("		SQLiteStatement sqLiteStatement = database.compileStatement(INSERT);\n");
		sb.append("		bindBeanArg(sqLiteStatement, bean);\n");
		sb.append("		return sqLiteStatement;\n	}\n");
	}
	
	private void appendCreateUpdateSQL(StringBuilder sb, String className) {
		sb.append("\n	@Override\n");
		sb.append("	public SQLiteStatement createUpdateSQL(SQLiteDatabase database,")
				.append(className)
				.append(" bean, String whereClause, String[] whereArgs) {\n");
		final int beanArgCount = indexItemMap.size();
		sb.append("		final int argCount = (null == whereArgs) ? ")
				.append(beanArgCount).append(" : ").append("(").append(beanArgCount)
				.append(" + whereArgs.length);\n");
		sb.append("		String sql = CACHE_UPDATE.get(whereClause);\n");
		sb.append("		if (null == sql) {\n");
		sb.append("			StringBuilder sb = new StringBuilder(UPDATE);\n");
		sb.append("			if (!TextUtils.isEmpty(whereClause)) {\n");
		sb.append("				sb.append(\" where \").append(whereClause);\n");
		sb.append("			}\n");
		sb.append("			sql = sb.toString();\n");
		sb.append("			CACHE_UPDATE.put(whereClause, sql);\n");
		sb.append("		}\n");
		sb.append("		SQLiteStatement statement = database.compileStatement(sql);\n");
		sb.append("		bindBeanArg(statement, bean);\n");
		sb.append("		for (int i = ").append(beanArgCount)
				.append("; i < argCount; i ++) {\n");
		sb.append("			statement.bindString(i + 1, whereArgs[i - ")
				.append(beanArgCount).append("]);\n");
		sb.append("		}\n");
		sb.append("		return statement;\n	}\n");
	}
	
	private void appendBindBeanArg(StringBuilder sb, String className) {
		sb.append("\n	private void bindBeanArg(SQLiteStatement statement, ")
				.append(className).append(" bean) {\n");
		final Collection<ColumnItem> columns = indexItemMap.values();
		for (ColumnItem column : columns) {
			final int bindId = column.index + 1;
			final SQLDataType sqlType = column.sqlType;
			final TypeKind fieldType = column.typeKind;
			final String fieldName = column.fieldName;
			final PropMethodItem propMethod = column.getterSetter;
			// 根据是否存在propMethod来决定是直接调用fieldName还是使用getter方法
			String useName = (null == propMethod || null == propMethod.getterName) ? 
					fieldName : propMethod.getterName + "()";
			
			sb.append("		");
			if (SQLDataType.BLOB == sqlType) {
				sb.append("statement.bindBlob(").append(bindId)
						.append(", bean.").append(useName).append(");\n");
			} else if (SQLDataType.INTEGER == sqlType) {
				sb.append("statement.bindLong(").append(bindId)
						.append(", bean.").append(useName);
				if (TypeKind.BOOLEAN == fieldType) {
					sb.append(" ? 1 : 0);\n");
				} else {
					sb.append(");\n");
				}
			} else if (SQLDataType.REAL == sqlType) {
				sb.append("statement.bindDouble(").append(bindId)
						.append(", bean.").append(useName).append(");\n");
			} else if (SQLDataType.TEXT == sqlType) {
				sb.append("statement.bindString(").append(bindId)
						.append(", ");
				if (TypeKind.DECLARED == fieldType) {
					sb.append("null == bean.").append(useName).append(" ? \"\" : ");
					sb.append("bean.").append(useName).append(".toString()");
				} else {
					sb.append("bean.").append(useName).append(" + \"\"");
				}
				sb.append(");\n");
			} else if (SQLDataType.NULL == sqlType) {
				sb.append("statement.bindNull(").append(bindId).append(");\n");
			}
		}
		sb.append("	}\n");
	}
	
	private void appendCreateDeleteSQL(StringBuilder sb) {
		sb.append("\n	@Override\n");
		sb.append("	public SQLiteStatement createDeleteSQL(SQLiteDatabase database,")
				.append(" String whereClause, String[] whereArgs) {\n");
		sb.append("		String sql = CACHE_DELETE.get(whereClause);\n");
		sb.append("		if (null == sql) {\n");
		sb.append("			StringBuilder sb = new StringBuilder(DELETE);\n");
		sb.append("			if (!TextUtils.isEmpty(whereClause)) {\n");
		sb.append("				sb.append(\" where \").append(whereClause);\n");
		sb.append("			}\n");
		sb.append("			CACHE_DELETE.put(whereClause, sql = sb.toString());\n");
		sb.append("		}\n");
		sb.append("		SQLiteStatement statement = database.compileStatement(sql);\n");
		sb.append("		if (null != whereArgs) {\n");
		sb.append("			int index = 0;\n");
		sb.append("			for (String s : whereArgs) {\n");
		sb.append("				statement.bindString(index + 1, s);\n");
		sb.append("				index++;\n");
		sb.append("			}\n");
		sb.append("		}\n");
		sb.append("		return statement;\n	}\n");
	}

	private void createDAO(String autoAPTPackageName, String daoClassName,
			String proxyClassName, Element element) {
		StringBuilder daoContent = new StringBuilder();
		// package
		daoContent.append("package ").append(autoAPTPackageName).append(";\n");
		// import
		daoContent.append("\nimport android.content.Context;\n");
		daoContent.append("import android.database.sqlite.SQLiteDatabase;\n");
		daoContent.append("import com.zt.simpledao.bean.IBeanProxy;\n");
		daoContent.append("import com.zt.simpledao.dao.SQLite3DAO;\n");
		daoContent.append("import ").append(element.toString()).append(";\n");
		daoContent.append("import ").append(autoAPTPackageName).append(".")
				.append(proxyClassName).append(";\n");
		// class
		daoContent.append("\npublic class ").append(daoClassName)
				.append(" extends SQLite3DAO<").append(element.getSimpleName())
				.append("> {\n");
		daoContent.append("	// ").append(element.toString()).append("\n");
		// field
		daoContent.append("	private static ").append(daoClassName)
				.append(" sInstance;\n");
		// getInstance
		daoContent.append("\n	public synchronized static ").append(daoClassName)
				.append(" getInstance(Context context) {\n");
		daoContent.append("		if (null == sInstance) {\n");
		daoContent.append("			sInstance = new ").append(daoClassName)
				.append("(context, new ").append(proxyClassName)
				.append("());\n		}\n");
		daoContent.append("		return sInstance;\n	}\n");
		// constructor
		daoContent.append("\n	private ").append(daoClassName)
				.append("(Context context, IBeanProxy").append("<")
				.append(element.getSimpleName()).append("> proxy) {\n");
		daoContent.append("		super(context, proxy);\n	}\n");
		// override
		daoContent.append("\n	@Override");
		daoContent
				.append("\n	protected void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion, IBeanProxy")
				.append("<").append(element.getSimpleName()).append("> proxy) {\n");
		daoContent
				.append("		db.execSQL(\"DROP TABLE IF EXISTS \" + proxy.getTableName());\n");
		daoContent.append("		db.execSQL(proxy.getTableCreator());\n	}\n");
		// end
		daoContent.append("\n}");
		// output
		JavaFileObject file = null;
		try {
			file = filer.createSourceFile(autoAPTPackageName + "." + daoClassName,
					element);
			file.openWriter().append(daoContent).close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void error(String msg, Element e) {
		processingEnv.getMessager().printMessage(Kind.ERROR, msg, e);
	}
	
	private void warning(String msg, Element e) {
		processingEnv.getMessager().printMessage(Kind.WARNING, msg, e);
	}

}
