package com.zt.simpledao.bean;

public interface IBeanProxy {
	String getDataBaseName();
	int getDataBaseVersion();
	String getTableName();
	String getTableCreator();
	Class<?> getBeanClass();
}
