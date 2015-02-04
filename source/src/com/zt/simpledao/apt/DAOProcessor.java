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
		
		appendBeanProxyColumnMap(element, proxyContent);
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
		content.append("\nimport ").append("android.content.ContentValues;\n");
		content.append("\nimport ").append("android.util.SparseArray;\n");
		content.append("\nimport ").append(element.toString()).append(";\n");
		content.append("import com.zt.simpledao.bean.ColumnItem;\n");
		content.append("import com.zt.simpledao.bean.IBeanProxy;\n");
		content.append("import com.zt.simpledao.SQLDataType;\n");
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
				column.primary = c.primary();
				if (column.primary) {
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
	
	private void appendBeanProxyColumnMap(Element element, StringBuilder content) {
		// field column map
		content.append(
				"	private static final SparseArray<ColumnItem> ALL_COLUMNS = new SparseArray<ColumnItem>(")
				.append(indexItemMap.size()).append(");\n");
		// fill column map
		content.append("	static {\n").append("		Class<")
				.append(element.getSimpleName().toString()).append("> claz = ")
				.append(element.getSimpleName().toString()).append(".class;\n");
		content.append("		try {\n");
		final Set<Integer> keySet = indexItemMap.keySet();
		for (Integer key : keySet) {
			ColumnItem value = indexItemMap.get(key);
			content.append("			ColumnItem item").append(key)
					.append(" = new ColumnItem(").append(key).append(", \"")
					.append(value.columnName).append("\", ").append("SQLDataType.")
					.append(value.sqlType.toString()).append(", ")
					.append(value.primary).append(", ")
					.append("claz.getDeclaredField(\"").append(value.fieldName)
					.append("\"));\n");
			content.append("			ALL_COLUMNS.put(").append(key).append(", item")
					.append(key).append(");\n");
		}
		content.append("		} catch (NoSuchFieldException e) {\n")
				.append("			e.printStackTrace();\n").append("		}\n")
				.append("	}\n");
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
		
		sb.append("\n	@Override\n");
		sb.append("	public SparseArray<ColumnItem> getAllColumns() {\n")
				.append("		return ALL_COLUMNS").append(";\n	}\n");
		
		sb.append("\n	@Override\n");
		sb.append("	public ContentValues convertBeanToDatabase").append("(")
				.append(className).append(" bean) {\n")
				.append("ContentValues values = new ContentValues();\n");

		Collection<ColumnItem> columns = indexItemMap.values();
		for (ColumnItem column : columns) {
			final String columnName = column.columnName;
			final String filedName = column.fieldName;
			final SQLDataType sqlType = column.sqlType;
			final TypeKind fieldType = column.typeKind;
			if (SQLDataType.BLOB == sqlType) {
				sb.append("values.put").append("(").append(columnName).append(", ")
						.append("bean.").append(filedName).append(");\n");
			} else if (SQLDataType.INTEGER == sqlType) {
				if (TypeKind.BOOLEAN == fieldType) {
					sb.append("values.put").append("(").append(columnName)
							.append(", ").append("bean.").append(filedName)
							.append(" == true ? 1 : 0);\n");
				} else {
					sb.append("values.put").append("(").append(columnName)
							.append(", ").append("bean.").append(filedName)
							.append(");\n");
				}
			} else if (SQLDataType.REAL == sqlType) {
				sb.append("values.put").append("(").append(columnName).append(", ")
						.append("bean.").append(filedName).append(");\n");
			} else if (SQLDataType.TEXT == sqlType) {
				sb.append("values.put").append("(").append(columnName).append(", ")
						.append("bean.").append(filedName).append(".toString());\n");
			} else if (SQLDataType.NULL == sqlType) {
				sb.append("values.putNull").append("(").append(columnName)
						.append(");\n");
			}
		}
		sb.append("		return values;\n	}\n");
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
		daoContent.append("import com.zt.simpledao.dao.sqlite.SQLite3DAO;\n");
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

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return super.getSupportedSourceVersion();
	}
	
	private void error(String msg, Element e) {
		processingEnv.getMessager().printMessage(Kind.ERROR, msg, e);
	}

}
