package com.zt.simpledao.bean;

import javax.lang.model.type.TypeKind;

import com.zt.simpledao.SQLDataType;

public class ColumnItem {
	/**
	 * 该列指定在数据库中的index
	 */
	public int index;
	/**
	 * 该列对应的java字段的名称
	 */
	public String fieldName;
	/**
	 * 该列在数据库中的名称
	 */
	public String columnName;
	/**
	 * 该列在数据库中的数据类型
	 */
	public SQLDataType sqlType;
	/**
	 * 该列对应的java字段的数据类型
	 */
	public TypeKind typeKind;
	/**
	 * 该列对应的java字段的getter&setter方法
	 * 如果该字段是{@code public},则此值为{@code null}
	 */
	public PropMethodItem getterSetter;
}
