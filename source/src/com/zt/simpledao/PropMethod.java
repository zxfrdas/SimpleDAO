package com.zt.simpledao;

/**
 * 用于声明java bean中getter&setter方法
 * 已经无用
 */
@Deprecated
public @interface PropMethod {
	PropMethodType type();
	String name();
}
