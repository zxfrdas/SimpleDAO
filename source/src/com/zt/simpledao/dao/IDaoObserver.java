package com.zt.simpledao.dao;

public interface IDaoObserver {
	<T> void onChange(IDAO<T> dao);
}
