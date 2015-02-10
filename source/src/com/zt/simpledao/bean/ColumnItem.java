package com.zt.simpledao.bean;

import javax.lang.model.type.TypeKind;

import com.zt.simpledao.SQLDataType;

public class ColumnItem {
	public int index;
	public String fieldName;
	public String columnName;
	public SQLDataType sqlType;
	public TypeKind typeKind;
}
