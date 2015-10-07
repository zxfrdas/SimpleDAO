package com.zt.simpledao.entry;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.zt.simpledao.condition.Condition;
import com.zt.simpledao.dao.IDAO;
import com.zt.simpledao.dao.IDaoObserver;

public class Dao implements IDaoWrapper {
	private static WeakReference<Context> mContextRef;
	private Map<Class<?>, IDAO<?>> daoMap;
	
	public static Dao getInstance(Context context) {
		if (null == mContextRef || null == mContextRef.get()) {
			mContextRef = new WeakReference<Context>(context);
		} else if (!mContextRef.get().equals(context)) {
			mContextRef = new WeakReference<Context>(context);
		}
		return InstanceHolder.sInstance;
	}
	
	public <T> void registObserver(IDaoObserver observer, Class<T> bean) {
		findDao(bean).registObserver(observer);
	}
	
	@SuppressWarnings("unchecked")
	private IDAO<?> findDao(Class<?> clazz) {
		if (!daoMap.containsKey(clazz)) {
			String daoName = clazz.getName() + "DAO";
			System.out.println("daoName = " + daoName);
			try {
				Class<IDAO<?>> daoClass = (Class<IDAO<?>>) Class.forName(daoName);
				Method getInstance = daoClass.getMethod("getInstance", Context.class);
				IDAO<?> dao = (IDAO<?>) getInstance.invoke(null, mContextRef.get());
				daoMap.put(clazz, dao);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return daoMap.get(clazz);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> boolean insert(T item) {
		IDAO<T> dao = (IDAO<T>) findDao(item.getClass());
		return dao.insert(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> boolean insert(Collection<T> items) {
		IDAO<T> dao = null;
		for (T item : items) {
			dao = (IDAO<T>) findDao(item.getClass());
			break;
		}
		return (null == dao) ? false : dao.insert(items);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> boolean insert(List<ContentValues> values, Class<T> bean) {
		IDAO<T> dao = (IDAO<T>) findDao(bean);
		return dao.insert(values);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> boolean delete(Condition condition, Class<T> bean) {
		IDAO<T> dao = (IDAO<T>) findDao(bean);
		return dao.delete(condition);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> boolean delete(Collection<Condition> conditions, Class<T> bean) {
		IDAO<T> dao = (IDAO<T>) findDao(bean);
		return dao.delete(conditions);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> boolean deleteAll(Class<T> bean) {
		IDAO<T> dao = (IDAO<T>) findDao(bean);
		return dao.deleteAll();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> boolean update(T item, Condition condition) {
		IDAO<T> dao = (IDAO<T>) findDao(item.getClass());
		return dao.update(item, condition);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> boolean update(Collection<T> items, Condition condition) {
		IDAO<T> dao = null;
		for (T item : items) {
			dao = (IDAO<T>) findDao(item.getClass());
			break;
		}
		return (null == dao) ? false : dao.update(items, condition);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> boolean update(Map<T, Condition> updates) {
		IDAO<T> dao = null;
		for (Map.Entry<T, Condition> entry : updates.entrySet()) {
			dao = (IDAO<T>) findDao(entry.getKey().getClass());
			break;
		}
		return (null == dao) ? false : dao.update(updates);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Cursor query(String sql, String[] selectionArgs, Class<T> bean) {
		IDAO<T> dao = (IDAO<T>) findDao(bean);
		return dao.query(sql, selectionArgs);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> query(Condition condition, Class<T> bean) {
		IDAO<T> dao = (IDAO<T>) findDao(bean);
		return dao.query(condition);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Cursor queryForCursor(Condition condition, Class<T> bean) {
		IDAO<T> dao = (IDAO<T>) findDao(bean);
		return dao.queryForCursor(condition);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> queryAll(Class<T> bean) {
		IDAO<T> dao = (IDAO<T>) findDao(bean);
		return dao.queryAll();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Cursor queryAllForCursor(Class<T> bean) {
		IDAO<T> dao = (IDAO<T>) findDao(bean);
		return dao.queryAllForCursor();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> int getCount(Class<T> bean) {
		IDAO<T> dao = (IDAO<T>) findDao(bean);
		return dao.getCount();
	}
	
	private static final class InstanceHolder {
		private static Dao sInstance = new Dao();
	}
	
}
