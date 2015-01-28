package com.zt.simpledao.condition;

/**
 * SQL条件构建器，用于构造SQL增删改查相应条件语句。
 */
public interface IConditionBuilder {
	
	/**
	 * SQL WHERE 语句开始
	 * @param column 进行条件筛选的相关列 
	 * @return {@code IConditionBuilder}
	 */
	IWhere where(String column);
	
	/**
	 * SQL ORDERBY 排序条件
	 * @param column 指定要进行排序的列
	 * @return {@code IOrderby}
	 */
	IOrderby orderby(String column);
	
	/**
	 * SQL GROUPBY 分组条件
	 * @param column 指定要进行分组的列
	 * @return {@code IConditionBuilder}
	 */
	IConditionBuilder groupby(String column);
	
	/**
	 * SQL AND 语句
	 * @return {@code IConditionBuilder}
	 */
	IConditionBuilder and();
	
	/**
	 * 使用上层给出的条件构造完成SQL条件语句
	 * @param selection WHERE条件约束
	 * @param selectionArgs 条件约束值
	 * @param orderby 排序约束
	 * @param groupby 分组约束
	 * @return {@code Condition}构造完成的SQL条件语句
	 */
	Condition sql(String selection, String[] selectionArgs, String orderby,
			String groupby);
	
	/**
	 * SQL条件构造完成
	 * @return {@code Condition}构造完成的SQL条件语句
	 */
	Condition buildDone();
	
}
