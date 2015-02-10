package com.zt.simpledao.bean;

import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

public interface IBeanProxy<T> {
	String getDataBaseName();
	int getDataBaseVersion();
	String getTableName();
	String getTableCreator();
	Class<T> getBeanClass();
	ContentValues convertBeanToDatabase(T bean);
	List<T> convertDatabaseToBean(Cursor cursor);
}
