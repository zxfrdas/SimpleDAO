package com.zt.simpledao.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;

import com.zt.simpledao.condition.Condition;

public interface IDAO<T> {
	
	/**
	 * 数据库单个插入操作。
	 * 
	 * @param item
	 *            试图插入的数据类
	 * @return 成功返回{@code true}，反之{@code false}
	 */
	boolean insert(T item);

	/**
	 * 数据库多条插入操作。
	 * 
	 * @param items
	 *            试图插入的数据类集合
	 * @return 成功返回{@code true}，反之{@code false}
	 */
	boolean insert(Collection<T> items);
	
	/**
	 * 数据库多条插入操作。
	 * 
	 * @param values
	 *            试图插入的ContentValues列表
	 * @return 成功返回{@code true}，反之{@code false}
	 */
	boolean insert(List<ContentValues> values);

	/**
	 * 数据库删除操作。
	 * 
	 * @param condition
	 *            操作的条件
	 * @return 成功返回{@code true}，反之{@code false}
	 */
	boolean delete(Condition condition);
	
	/**
	 * 数据库多条删除操作。 
	 * 
	 * @param conditions
	 *            操作的条件
	 * @return 成功返回{@code true}，反之{@code false}
	 */
	boolean delete(Collection<Condition> conditions);

	/**
	 * 数据库表删除所有条目操作
	 * 
	 * @return 成功返回{@code true}，反之{@code false}
	 */
	boolean deleteAll();

	/**
	 * 数据库单个更新操作
	 * 
	 * @param item
	 *            试图更新的数据类
	 * @param condition
	 *            操作的条件
	 * @return 成功返回{@code true}，反之{@code false}
	 */
	boolean update(T item, Condition condition);

	/**
	 * 数据库多条更新操作，单一条件
	 * 
	 * @param items
	 *            试图更新的数据类集合
	 * @param condition
	 *            操作的条件
	 * @return 成功返回{@code true}，反之{@code false}
	 */
	boolean update(Collection<T> items, Condition condition);

	/**
	 * 数据库多条更新操作，每个数据类对应一个条件
	 * 
	 * @param updates
	 *            试图更新的数据类和其对应条件的集合
	 * @return 成功返回{@code true}，反之{@code false}
	 */
	boolean update(Map<T, Condition> updates);
	
	/**
	 * 数据库查询操作
	 * 
	 * @param sql
	 *            需要执行的sql语句
	 * @return 所有符合条件的数据类
	 */
	Cursor query(String sql, String[] selectionArgs);
	
	/**
	 * 数据库查询操作
	 * 
	 * @param condition
	 *            操作的条件
	 * @return 所有符合条件的数据类
	 */
	List<T> query(Condition condition);
	
	/**
	 * 数据库查询操作
	 * 
	 * @param condition
	 *            操作的条件
	 * @return 所有符合条件的数据
	 */
	Cursor queryForCursor(Condition condition);

	/**
	 * 返回数据库所有结果
	 * 
	 * @return 数据库所有数据
	 */
	List<T> queryAll();
	
	/**
	 * 返回数据库所有结果
	 * 
	 * @return 数据库所有数据
	 */
	Cursor queryAllForCursor();

	/**
	 * 获取共有多少行数据
	 * 
	 * @return 表的行数
	 */
	int getCount();

}
