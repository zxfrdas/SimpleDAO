package com.zt.simpledao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明该类对应一张数据库表
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Table {
	/**
	 * 该类对应的数据库表的名称
	 * @return 数据库表明
	 */
	String name();
}
