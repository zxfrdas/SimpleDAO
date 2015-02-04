package com.zt.simpledao.bean;

import android.content.ContentValues;
import android.util.SparseArray;

public interface IBeanProxy<T> {
	String getDataBaseName();
	int getDataBaseVersion();
	String getTableName();
	String getTableCreator();
	Class<T> getBeanClass();
	SparseArray<ColumnItem> getAllColumns();
	ContentValues convertBeanToDatabase(T bean);
}
