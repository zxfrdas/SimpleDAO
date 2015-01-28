package com.zt.simpledao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明该字段为数据库的列
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
	/**
	 * 指定该字段代表的列在数据库中的index，不指定则由本类库进行排序指定
	 * @return 该列在数据库中的index
	 */
	int index() default -1;
	/**
	 * 指定该字段代表的列在数据库中的名称，不指定则使用变量本身名称
	 * @return 名称
	 */
	String name() default "";
	/**
	 * 指定该字段代表的列在数据库中的类型
	 * @return {@code SQLDataType} 数值类型
	 */
	SQLDataType type();
	/**
	 * 指定该字段代表的列是否为主键，不指定则不是
	 * @return 是否为主键
	 */
	boolean primary() default false;
}
