package com.zt.simpledao.bean;

import java.lang.reflect.Field;

import com.zt.simpledao.SQLDataType;

public class ColumnItem {
	public int index;
	public String fieldName;
	public String columnName;
	public SQLDataType sqlType;
	public boolean primary;
	public Field field;
	
	public ColumnItem() {};

	public ColumnItem(int index, String columnName, SQLDataType sqlType, boolean primary,
			Field field) {
		this.index = index;
		this.columnName = columnName;
		this.sqlType = sqlType;
		this.primary = primary;
		this.field = field;
	}
}
