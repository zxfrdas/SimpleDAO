package com.zt.simpledao.entry;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;

import com.zt.simpledao.condition.Condition;
import com.zt.simpledao.dao.IDaoObserver;

public interface IDaoWrapper {
	/**
	 * 注册数据变更监听器
	 * 
	 * @param observer
	 *            监听器
	 * @param bean
	 *            数据类
	 */
	<T> void registObserver(IDaoObserver observer, Class<T> bean);

	/**
	 * 数据库单个插入操作。
	 * 
	 * @param item
	 *            试图插入的数据类
	 * @return 成功返回{@code true}，反之{@code false}
	 */
	<T> boolean insert(T item);

	/**
	 * 数据库多条插入操作。
	 * 
	 * @param items
	 *            试图插入的数据类集合
	 * @return 成功返回{@code true}，反之{@code false}
	 */
	<T> boolean insert(Collection<T> items);

	/**
	 * 数据库多条插入操作。
	 * 
	 * @param values
	 *            试图插入的ContentValues列表
	 * @param bean
	 *            数据类
	 * @return 成功返回{@code true}，反之{@code false}
	 */
	<T> boolean insert(List<ContentValues> values, Class<T> bean);

	/**
	 * 数据库删除操作。
	 * 
	 * @param condition
	 *            操作的条件
	 * @param bean
	 *            数据类
	 * @return 成功返回{@code true}，反之{@code false}
	 */
	<T> boolean delete(Condition condition, Class<T> bean);

	/**
	 * 数据库多条删除操作。
	 * 
	 * @param conditions
	 *            操作的条件
	 * @param bean
	 *            数据类
	 * @return 成功返回{@code true}，反之{@code false}
	 */
	<T> boolean delete(Collection<Condition> conditions, Class<T> bean);

	/**
	 * 数据库表删除所有条目操作
	 * 
	 * @param bean
	 *            数据类
	 * @return 成功返回{@code true}，反之{@code false}
	 */
	<T> boolean deleteAll(Class<T> bean);

	/**
	 * 数据库单个更新操作
	 * 
	 * @param item
	 *            试图更新的数据类
	 * @param condition
	 *            操作的条件
	 * @return 成功返回{@code true}，反之{@code false}
	 */
	<T> boolean update(T item, Condition condition);

	/**
	 * 数据库多条更新操作，单一条件
	 * 
	 * @param items
	 *            试图更新的数据类集合
	 * @param condition
	 *            操作的条件
	 * @return 成功返回{@code true}，反之{@code false}
	 */
	<T> boolean update(Collection<T> items, Condition condition);

	/**
	 * 数据库多条更新操作，每个数据类对应一个条件
	 * 
	 * @param updates
	 *            试图更新的数据类和其对应条件的集合
	 * @return 成功返回{@code true}，反之{@code false}
	 */
	<T> boolean update(Map<T, Condition> updates);

	/**
	 * 数据库查询操作
	 * 
	 * @param sql
	 *            需要执行的sql语句
	 * @param bean
	 *            数据类
	 * @return 所有符合条件的数据类
	 */
	<T> Cursor query(String sql, String[] selectionArgs, Class<T> bean);

	/**
	 * 数据库查询操作
	 * 
	 * @param condition
	 *            操作的条件
	 * @param bean
	 *            数据类
	 * @return 所有符合条件的数据类
	 */
	<T> List<T> query(Condition condition, Class<T> bean);

	/**
	 * 数据库查询操作
	 * 
	 * @param condition
	 *            操作的条件
	 * @param bean
	 *            数据类
	 * @return 所有符合条件的数据
	 */
	<T> Cursor queryForCursor(Condition condition, Class<T> bean);

	/**
	 * 返回数据库所有结果
	 * 
	 * @param bean
	 *            数据类
	 * @return 数据库所有数据
	 */
	<T> List<T> queryAll(Class<T> bean);

	/**
	 * 返回数据库所有结果
	 * 
	 * @param bean
	 *            数据类
	 * @return 数据库所有数据
	 */
	<T> Cursor queryAllForCursor(Class<T> bean);

	/**
	 * 获取共有多少行数据
	 * 
	 * @param bean
	 *            数据类
	 * @return 表的行数
	 */
	<T> int getCount(Class<T> bean);
}
