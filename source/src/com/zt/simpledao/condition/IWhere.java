package com.zt.simpledao.condition;

/**
 * SQL中WHERE语句构建
 */
public interface IWhere {
	/**
	 * SQL WHERE 语句中约束条件：小于
	 * @param arg 比较的值
	 * @return {@code IConditionBuilder}
	 */
	<E> IConditionBuilder less(E arg);
	
	/**
	 * SQL WHERE 语句中约束条件：小于等于
	 * @param arg 比较的值
	 * @return {@code IConditionBuilder}
	 */
	<E> IConditionBuilder lessEqual(E arg);
	
	/**
	 * SQL WHERE 语句中约束条件：等于
	 * @param arg 比较的值
	 * @return {@code IConditionBuilder}
	 */
	<E> IConditionBuilder equal(E arg);
	
	/**
	 * SQL WHERE 语句中约束条件：不等于
	 * @param arg 比较的值
	 * @return {@code IConditionBuilder}
	 */
	<E> IConditionBuilder notEqual(E arg);
	
	/**
	 * SQL WHERE 语句中约束条件：大于等于
	 * @param arg 比较的值
	 * @return {@code IConditionBuilder}
	 */
	<E> IConditionBuilder moreEqual(E arg);
	
	/**
	 * SQL WHERE 语句中约束条件：大于
	 * @param arg 比较的值
	 * @return {@code IConditionBuilder}
	 */
	<E> IConditionBuilder more(E arg);
	
	/**
	 * SQL WHERE 语句中约束条件：在min,max之间
	 * @param min 大于该值
	 * @param max 小于该值
	 * @return {@code IConditionBuilder}
	 */
	<E> IConditionBuilder between(E min, E max);
	
	/**
	 * SQL WHERE 语句中约束条件：指定模式
	 * @param pattern 指定的匹配模式
	 * @return {@code IConditionBuilder}
	 */
	<E> IConditionBuilder like(E pattern);
}
