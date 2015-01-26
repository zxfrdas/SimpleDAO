package com.zt.simpledao.bean;

import android.util.SparseArray;

public interface IBeanProxy {
	String getDataBaseName();
	int getDataBaseVersion();
	String getTableName();
	String getTableCreator();
	Class<?> getBeanClass();
	SparseArray<ColumnItem> getAllColumns();
}
