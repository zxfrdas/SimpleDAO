package com.zt.simpledao.apt;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import com.zt.simpledao.Column;
import com.zt.simpledao.Database;
import com.zt.simpledao.SQLDataType;
import com.zt.simpledao.Table;
import com.zt.simpledao.bean.ColumnItem;

@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes(value = { "com.zt.simpledao.Column",
		"com.zt.simpledao.Database", "com.zt.simpledao.Table" })
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
		// cache
		proxyContent.append("	private static final HashMap<Class<").append(element.getSimpleName())
				.append(">, String> mSqlCache = new HashMap<Class<")
				.append(element.getSimpleName()).append(">, String>();");
		
		// method
		appendMethods(proxyContent, element.getSimpleName().toString());
		// class end
		proxyContent.append("\n}");
		// write file
		JavaFileObject file = null;
		Writer writer = null;
		try {
			file = filer.createSourceFile(autoAPTPackageName + "/" + proxyClassName,
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
				} catch (IOException e) {}
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
		content.append("\nimport android.content.ContentValues;");
		content.append("\nimport android.database.Cursor;");
		content.append("\nimport android.database.sqlite.SQLiteDatabase;");
		content.append("\nimport android.database.sqlite.SQLiteStatement;");
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
		for (Element element2 : element.getEnclosedElements()) {
			if (element2.getKind().isField()) {
				Column c = element2.getAnnotation(Column.class);
				if (null == c) {
					error("database must have @Column!", element);
					return;
				}
				ColumnItem column = new ColumnItem();
				column.index = (-1 == c.index()) ? index : c.index();
				column.fieldName = element2.getSimpleName().toString();
				column.columnName = (null != c.name() && !c.name().isEmpty()) ? c
						.name() : column.fieldName;
				column.sqlType = c.type();
				if (c.primary()) {
					primaryKeys.add(column);
				}
				column.typeKind = element2.asType().getKind();
				indexItemMap.put(column.index, column);
				content.append("	public static final String ")
						.append(element2.getSimpleName()).append(" = ").append("\"")
						.append(column.columnName).append("\";\n");
				content.append("	public static final int ")
						.append(element2.getSimpleName()).append("_id")
						.append(" = ").append(column.index).append(";\n");
				index++;
			}
		}
	}
	
	private String crateTable(String table) {
		// Create table xxx (column type, column type, primary key (column));
		StringBuilder sb = new StringBuilder();
		sb.append("create table ").append(table).append("(");
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
					for (int pri = 0; pri < primaryTotal; pri ++) {
						ColumnItem primary = primaryKeys.get(pri);
						sb.append(primary.columnName);
						if (pri != (primaryTotal - 1)) {
							// 非最后一个
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
		
		appendConvertBeanToDB(sb, className);
		appendConvertDBToBean(sb, className);
		appendCreateInsertSQL(sb, className);
	}
	
	private void appendConvertBeanToDB(StringBuilder sb, String className) {
		sb.append("\n	@Override\n");
		sb.append("	public ContentValues convertBeanToDatabase").append("(")
				.append(className).append(" bean) {\n")
				.append("		ContentValues values = new ContentValues();\n");
		final Collection<ColumnItem> columns = indexItemMap.values();
		for (ColumnItem column : columns) {
			final String filedName = column.fieldName;
			final SQLDataType sqlType = column.sqlType;
			final TypeKind fieldType = column.typeKind;
			if (SQLDataType.INTEGER == sqlType && TypeKind.BOOLEAN == fieldType) {
				sb.append("		values.put").append("(").append(filedName).append(", ")
						.append("bean.").append(filedName)
						.append(" == true ? 1 : 0);\n");
			} else if (SQLDataType.TEXT == sqlType) {
				if (TypeKind.DECLARED == fieldType) {
					sb.append("		values.put").append("(").append(filedName)
							.append(", ").append("bean.").append(filedName)
							.append(".toString());\n");
				} else {
					sb.append("		values.put").append("(").append(filedName)
							.append(", ").append("bean.").append(filedName)
							.append(" + \"\");\n");
				}
			} else if (SQLDataType.NULL == sqlType) {
				sb.append("		values.putNull").append("(").append(filedName)
						.append(");\n");
			} else {
				sb.append("		values.put").append("(").append(filedName).append(", ")
						.append("bean.").append(filedName).append(");\n");
			}
		}
		sb.append("		return values;\n	}\n");
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
			if (SQLDataType.INTEGER == sqlType) {
				sb.append("					");
				if (TypeKind.BOOLEAN == fieldType) {
					sb.append("item.").append(fieldName).append(" = cursor.getInt(")
							.append(column.index).append(") == 1 ? true : false;\n");
				} else if (TypeKind.LONG == fieldType) {
					sb.append("item.").append(fieldName)
							.append(" = cursor.getLong(").append(column.index)
							.append(");\n");
				} else if (TypeKind.SHORT == fieldType) {
					sb.append("item.").append(fieldName)
							.append(" = cursor.getShort(").append(column.index)
							.append(");\n");
				} else if (TypeKind.INT == fieldType) {
					sb.append("item.").append(fieldName).append(" = cursor.getInt(")
							.append(column.index).append(");\n");
				}
			} else if (SQLDataType.TEXT == sqlType) {
				sb.append("					");
				if (TypeKind.BOOLEAN == fieldType) {
					sb.append("item.").append(fieldName)
							.append(" = Boolean.valueOf(cursor.getString(")
							.append(column.index).append("));\n");
				} else if (TypeKind.DOUBLE == fieldType) {
					sb.append("item.").append(fieldName)
							.append(" = Double.valueOf(cursor.getString(")
							.append(column.index).append("));\n");
				} else if (TypeKind.FLOAT == fieldType) {
					sb.append("item.").append(fieldName)
							.append(" = Float.valueOf(cursor.getString(")
							.append(column.index).append("));\n");
				} else if (TypeKind.INT == fieldType) {
					sb.append("item.").append(fieldName)
							.append(" = Integer.valueOf(cursor.getString(")
							.append(column.index).append("));\n");
				} else if (TypeKind.LONG == fieldType) {
					sb.append("item.").append(fieldName)
							.append(" = Long.valueOf(cursor.getString(")
							.append(column.index).append("));\n");
				} else if (TypeKind.SHORT == fieldType) {
					sb.append("item.").append(fieldName)
							.append(" = Short.valueOf(cursor.getString(")
							.append(column.index).append("));\n");
				} else {
					sb.append("item.").append(fieldName)
							.append(" = cursor.getString(").append(column.index)
							.append(");\n");
				}
			} else if (SQLDataType.NULL == sqlType) {
				sb.append("					");
				sb.append("item.").append(fieldName).append(" = null;\n");
			} else if (SQLDataType.BLOB == sqlType) {
				sb.append("					");
				sb.append("item.").append(fieldName).append(" = cursor.getBlob(")
						.append(column.index).append(");\n");
			} else if (SQLDataType.REAL == sqlType) {
				sb.append("					");
				if (TypeKind.FLOAT == fieldType) {
					sb.append("item.").append(fieldName)
							.append(" = cursor.getFloat(").append(column.index)
							.append(");\n");
				} else if (TypeKind.DOUBLE == fieldType) {
					sb.append("item.").append(fieldName)
							.append(" = cursor.getDouble(").append(column.index)
							.append(");\n");
				}
			}
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
		sb.append("		String sql = mSqlCache.get(getBeanClass());\n");
		sb.append("		if (null == sql) {\n");
		sb.append("			StringBuilder sb = new StringBuilder(\"insert into \");\n");
		sb.append("			sb.append(TABLE).append(\" (\");\n");
		final Collection<ColumnItem> columns = indexItemMap.values();
		final int count = columns.size();
		int index = 0;
		for (ColumnItem column : columns) {
			final String fieldName = column.fieldName;
			sb.append("			sb.append(").append(fieldName).append(")");
			if (index < (count - 1)) {
				sb.append(".append(\",\");\n");
			} else {
				sb.append(";\n");
			}
			index ++;
		}
		sb.append("			sb.append(\") values(\");\n");
		for (int i = 0; i < count; i ++) {
			sb.append("			sb.append(\"?\")");
			if (i < (count - 1)) {
				sb.append(".append(\",\");\n");
			} else {
				sb.append(";\n");
			}
		}
		sb.append("			sb.append(\");\");\n");
		sb.append("			sql = sb.toString();\n");
		sb.append("			mSqlCache.put(getBeanClass(), sql);\n		}\n");
		sb.append("		SQLiteStatement sqLiteStatement = database.compileStatement(sql);\n");
		for (ColumnItem column : columns) {
			final int bindId = column.index + 1;
			final SQLDataType sqlType = column.sqlType;
			final TypeKind fieldType = column.typeKind;
			final String fieldName = column.fieldName;
			if (SQLDataType.BLOB == sqlType) {
				sb.append("		");
				sb.append("sqLiteStatement.bindBlob(").append(bindId).append(", bean.")
						.append(fieldName).append(");\n");
			} else if (SQLDataType.INTEGER == sqlType) {
				sb.append("		");
				sb.append("sqLiteStatement.bindLong(").append(bindId).append(", bean.")
						.append(fieldName);
				if (TypeKind.BOOLEAN == fieldType) {
					sb.append(" ? 1 : 0);\n");
				} else {
					sb.append(");\n");
				}
			} else if (SQLDataType.REAL == sqlType) {
				sb.append("		");
				sb.append("sqLiteStatement.bindDouble(").append(bindId).append(", bean.")
						.append(fieldName).append(");\n");
			} else if (SQLDataType.TEXT == sqlType) {
				sb.append("		");
				sb.append("sqLiteStatement.bindString(").append(bindId).append(", bean.")
						.append(fieldName).append(");\n");
			} else if (SQLDataType.NULL == sqlType) {
				sb.append("		");
				sb.append("sqLiteStatement.bindNull(").append(bindId).append(");\n");
			}
		}
		sb.append("		return sqLiteStatement;\n	}\n");
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
			file = filer.createSourceFile(autoAPTPackageName + "/" + daoClassName,
					element);
			file.openWriter().append(daoContent).close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void error(String msg, Element e) {
		processingEnv.getMessager().printMessage(Kind.ERROR, msg, e);
	}

}
