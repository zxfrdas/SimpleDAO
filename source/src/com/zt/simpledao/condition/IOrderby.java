package com.zt.simpledao.condition;

/**
 * SQL中ORDERBY语句构建
 */
public interface IOrderby {
	/**
	 * 递增
	 * @return {@code IConditionBuilder}条件构建器
	 */
	IConditionBuilder ascend();
	/**
	 * 递减
	 * @return {@code IConditionBuilder}条件构建器
	 */
	IConditionBuilder descend();
}
