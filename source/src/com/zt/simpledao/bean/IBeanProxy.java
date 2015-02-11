package com.zt.simpledao.bean;

import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public interface IBeanProxy<T> {
	String getDataBaseName();
	int getDataBaseVersion();
	String getTableName();
	String getTableCreator();
	Class<T> getBeanClass();
	ContentValues convertBeanToDatabase(T bean);
	List<T> convertDatabaseToBean(Cursor cursor);
	SQLiteStatement createInsertSQL(SQLiteDatabase database, T bean);
}
