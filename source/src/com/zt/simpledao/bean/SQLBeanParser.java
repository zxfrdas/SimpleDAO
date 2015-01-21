package com.zt.simpledao.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.zt.simpledao.Column;
import com.zt.simpledao.SQLDataType;


public class SQLBeanParser {
	private Map<String, ColumnItem> field_Item;
	
	public static class ColumnItem {
		public int index;
		public String name;
		public SQLDataType type;
		public Field field;
	}

	public SQLBeanParser() {
		field_Item = new HashMap<String, ColumnItem>();
	}

	public void analyze(Class<?> clazz) {
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			Annotation c = field.getAnnotation(Column.class);
			if (null != c) {
				ColumnItem column = new ColumnItem();
				column.index = ((Column) c).index();
				column.name = ((Column) c).name();
				column.type = ((Column) c).type();
				column.field = field;
				field_Item.put(field.getName(), column);
			}
		}
	}
	
	public Collection<ColumnItem> getAllColumnItem() {
		return field_Item.values();
	}

}
