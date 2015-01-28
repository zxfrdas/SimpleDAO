package com.zt.simpledao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明存储该类的数据库文件
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Database {
	/**
	 * 指定该类对应的数据库的名称
	 * @return 数据库名称
	 */
	String name();
	/**
	 * 指定该类对应的数据库的版本
	 * @return 数据库版本
	 */
	int version();
}
