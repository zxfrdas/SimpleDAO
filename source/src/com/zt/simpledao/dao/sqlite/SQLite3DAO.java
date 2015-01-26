package com.zt.simpledao.dao.sqlite;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.SparseArray;

import com.zt.simpledao.SQLDataType;
import com.zt.simpledao.bean.ColumnItem;
import com.zt.simpledao.bean.IBeanProxy;
import com.zt.simpledao.condition.Condition;
import com.zt.simpledao.condition.IConditionBuilder;
import com.zt.simpledao.condition.sqlite.SQLiteConditionBuilder;
import com.zt.simpledao.dao.IDAO;

public abstract class SQLite3DAO<T> implements IDAO<T> {
	private final ReadLock mReadLock;
	private final WriteLock mWriteLock;
	private SQLiteDatabase mDatabase;
	private String tableName;
	private IBeanProxy mProxy;

	public SQLite3DAO(Context context, IBeanProxy proxy) {
		mProxy = proxy;
		tableName = mProxy.getTableName();
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		mReadLock = lock.readLock();
		mWriteLock = lock.writeLock();
		mDatabase = new SQLiteOpenHelper(context, mProxy.getDataBaseName(), null,
				mProxy.getDataBaseVersion()) {

			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
				SQLite3DAO.this.onUpgrade(db, oldVersion, newVersion, mProxy);
			}

			@Override
			public void onCreate(SQLiteDatabase db) {
				db.execSQL(mProxy.getTableCreator());
			}
		}.getWritableDatabase();
	}

	protected abstract void onUpgrade(SQLiteDatabase db, int oldVersion,
			int newVersion, IBeanProxy proxy);

	@Override
	public boolean insert(List<ContentValues> valuesList) {
		long ret = -1;
		mWriteLock.lock();
		mDatabase.beginTransaction();
		try {
			for (ContentValues v : valuesList) {
				ret = mDatabase.insert(tableName, null, v);
			}
			mDatabase.setTransactionSuccessful();
		} catch (SQLiteException e) {
			e.printStackTrace();
			ret = -1;
		} finally {
			mDatabase.endTransaction();
			mWriteLock.unlock();
		}
		if (-1 != ret) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean insert(T item) {
		long ret = -1;
		ContentValues values = setColumnToContentValue(mProxy.getAllColumns(),
				item);
		mWriteLock.lock();
		try {
			ret = mDatabase.insert(tableName, null, values);
		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			mWriteLock.unlock();
		}
		if (-1 != ret) {
			return true;
		}
		return false;
	}

	@Override
	public boolean insert(Collection<T> items) {
		long ret = -1;
		ArrayList<ContentValues> values = new ArrayList<ContentValues>();
		for (T item : items) {
			ContentValues value = setColumnToContentValue(
					mProxy.getAllColumns(), item);
			values.add(value);
		}
		mWriteLock.lock();
		mDatabase.beginTransaction();
		try {
			for (ContentValues v : values) {
				ret = mDatabase.insert(tableName, null, v);
			}
			mDatabase.setTransactionSuccessful();
		} catch (SQLiteException e) {
			e.printStackTrace();
			ret = -1;
		} finally {
			mDatabase.endTransaction();
			mWriteLock.unlock();
		}
		if (-1 != ret) {
			return true;
		}
		return false;
	}

	@Override
	public boolean delete(Collection<Condition> conditions) {
		long ret = 0;
		mWriteLock.lock();
		mDatabase.beginTransaction();
		try {
			for (Condition condition : conditions){
				ret = mDatabase.delete(tableName, condition.getSelection(),
						condition.getSelectionArgs());
			}
			mDatabase.setTransactionSuccessful();
		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			mDatabase.endTransaction();
			mWriteLock.unlock();
		}
		if (0 != ret) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean delete(Condition condition) {
		long ret = 0;
		mWriteLock.lock();
		try {
			ret = mDatabase.delete(tableName, condition.getSelection(),
					condition.getSelectionArgs());
		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			mWriteLock.unlock();
		}
		if (0 != ret) {
			return true;
		}
		return false;
	}

	@Override
	public boolean deleteAll() {
		long ret = 0;
		mWriteLock.lock();
		try {
			ret = mDatabase.delete(tableName, null, null);
		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			mWriteLock.unlock();
		}
		if (1 == ret) {
			return true;
		}
		return false;
	}

	@Override
	public boolean update(T item, Condition condition) {
		long ret = -1;
		ContentValues value = setColumnToContentValue(mProxy.getAllColumns(),
				item);
		mWriteLock.lock();
		try {
			ret = mDatabase.update(tableName, value, condition.getSelection(),
					condition.getSelectionArgs());
		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			mWriteLock.unlock();
		}
		if (-1 != ret) {
			return true;
		}
		return false;
	}

	@Override
	public boolean update(Collection<T> items, Condition condition) {
		long ret = -1;
		ArrayList<ContentValues> values = new ArrayList<ContentValues>();
		for (T item : items) {
			ContentValues value = setColumnToContentValue(
					mProxy.getAllColumns(), item);
			values.add(value);
		}
		mWriteLock.lock();
		mDatabase.beginTransaction();
		try {
			for (ContentValues value : values) {
				ret = mDatabase.update(tableName, value, condition.getSelection(),
						condition.getSelectionArgs());
			}
			mDatabase.setTransactionSuccessful();
		} catch (SQLiteException e) {
			e.printStackTrace();
			ret = -1;
		} finally {
			mDatabase.endTransaction();
			mWriteLock.unlock();
		}
		if (-1 != ret) {
			return true;
		}
		return false;
	}

	@Override
	public boolean update(Map<T, Condition> updates) {
		long ret = -1;
		Map<ContentValues, Condition> values = new HashMap<ContentValues, Condition>();
		for (Entry<T, Condition> update : updates.entrySet()) {
			ContentValues value = setColumnToContentValue(
					mProxy.getAllColumns(), update.getKey());
			values.put(value, update.getValue());
		}
		mWriteLock.lock();
		mDatabase.beginTransaction();
		try {
			for (Entry<ContentValues, Condition> value : values.entrySet()) {
				ret = mDatabase.update(tableName, value.getKey(), value.getValue()
						.getSelection(), value.getValue().getSelectionArgs());
			}
			mDatabase.setTransactionSuccessful();
		} catch (SQLiteException e) {
			e.printStackTrace();
			ret = -1;
		} finally {
			mDatabase.endTransaction();
			mWriteLock.unlock();
		}
		if (-1 != ret) {
			return true;
		}
		return false;
	}

	@Override
	public Cursor queryForCursor(Condition condition) {
		Cursor c = null;
		mReadLock.lock();
		try {
			c = mDatabase.query(tableName, //table name
							null, //columns
							condition.getSelection(), //selection
							condition.getSelectionArgs(), //selection args
							condition.getGroupby(), //groupby
							null, //having
							condition.getOrderBy()); //orderby
		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			mReadLock.unlock();
		}
		return c;
	}
	
	@Override
	public Cursor query(String sql, String[] selectionArgs) {
		Cursor c = null;
		mReadLock.lock();
		try {
			c = mDatabase.rawQuery(sql, selectionArgs);
		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			mReadLock.unlock();
		}
		return c;
	}
	
	@Override
	public List<T> query(Condition condition) {
		Cursor c = null;
		mReadLock.lock();
		try {
			c = mDatabase.query(tableName, //table name
							null, //columns
							condition.getSelection(), //selection
							condition.getSelectionArgs(), //selection args
							condition.getGroupby(), //groupby
							null, //having
							condition.getOrderBy()); //orderby
		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			mReadLock.unlock();
		}
		List<T> items = new ArrayList<T>();
		try {
			items = setCursorValueToBean(c, mProxy.getAllColumns());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return items;
	}

	@Override
	public Cursor queryAllForCursor() {
		Cursor c = null;
		mReadLock.lock();
		try {
			c = mDatabase.query(tableName, null, null, null, null, null, null);
		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			mReadLock.unlock();
		}
		return c;
	}
	
	@Override
	public List<T> queryAll() {
		Cursor c = null;
		mReadLock.lock();
		try {
			c = mDatabase.query(tableName, null, null, null, null, null, null);
		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			mReadLock.unlock();
		}
		List<T> items = new ArrayList<T>();
		try {
			items = setCursorValueToBean(c, mProxy.getAllColumns());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return items;
	}

	@Override
	public int getCount() {
		int count = 0;
		String sql = "SELECT COUNT(*) FROM " + tableName;
		Cursor c = null;
		mReadLock.lock();
		try {
			c = mDatabase.rawQuery(sql, null);
		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			mReadLock.unlock();
		}
		if (null != c && c.moveToFirst()) {
			count = c.getInt(0);
			c.close();
		}
		return count;
	}

	@Override
	public IConditionBuilder buildCondition() {
		return new SQLiteConditionBuilder();
	}

	private ContentValues setColumnToContentValue(SparseArray<ColumnItem> items,
			T bean) {
		ContentValues values = new ContentValues();
		final int size = items.size();
		for (int i = 0; i < size; i++) {
			ColumnItem item = items.get(i);
			final String name = item.columnName;
			final SQLDataType type = item.sqlType;
			final Field field = item.field;
			final Class<?> fieldType = field.getType();
			field.setAccessible(true);
			try {
				if (SQLDataType.BLOB == type) {
					values.put(name, (byte[]) field.get(bean));
				} else if (SQLDataType.INTEGER == type) {
					if (boolean.class.equals(fieldType)
							|| Boolean.class.equals(fieldType)) {
						values.put(name, field.getBoolean(bean) ? 1 : 0);
					} else if (int.class.equals(fieldType)
							|| Integer.class.equals(fieldType)) {
						values.put(name, field.getInt(bean));
					} else if (long.class.equals(fieldType)
							|| Long.class.equals(fieldType)) {
						values.put(name, field.getLong(bean));
					} else if (short.class.equals(fieldType)
							|| Short.class.equals(fieldType)) {
						values.put(name, field.getShort(bean));
					}
				} else if (SQLDataType.REAL == type) {
					if (float.class.equals(fieldType)
							|| Float.class.equals(fieldType)) {
						values.put(name, field.getFloat(bean));
					} else if (double.class.equals(fieldType)
							|| Double.class.equals(fieldType)) {
						values.put(name, field.getDouble(bean));
					}
				} else if (SQLDataType.TEXT == type) {
					values.put(name, String.valueOf(field.get(bean)));
				} else if (SQLDataType.NULL == type) {
					values.putNull(name);
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		return values;
	}

	@SuppressWarnings("unchecked")
	private List<T> setCursorValueToBean(Cursor cursor, SparseArray<ColumnItem> items)
			throws IllegalAccessException, IllegalArgumentException {
		List<T> beans = new ArrayList<T>();
		while (null != cursor && cursor.moveToNext()) {
			T item = null;
			try {
				item = (T) mProxy.getBeanClass().newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			}
			final int size = items.size();
			for (int i = 0; i < size; i++) {
				ColumnItem ci = items.get(i);
				final int index = ci.index;
				final SQLDataType type = ci.sqlType;
				final Field field = ci.field;
				final Class<?> fieldType = field.getType();
				field.setAccessible(true);
				if (SQLDataType.BLOB == type) {
					field.set(item, cursor.getBlob(index));
				} else if (SQLDataType.INTEGER == type) {
					if (boolean.class.equals(fieldType)
							|| Boolean.class.equals(fieldType)) {
						field.set(item, 1 == cursor.getInt(index) ? true : false);
					} else if (int.class.equals(fieldType)
							|| Integer.class.equals(fieldType)) {
						field.set(item, cursor.getInt(index));
					} else if (long.class.equals(fieldType)
							|| Long.class.equals(fieldType)) {
						field.set(item, cursor.getLong(index));
					} else if (short.class.equals(fieldType)
							|| Short.class.equals(fieldType)) {
						field.set(item, cursor.getShort(index));
					}
				} else if (SQLDataType.REAL == type) {
					if (float.class.equals(fieldType)
							|| Float.class.equals(fieldType)) {
						field.set(item, cursor.getFloat(index));
					} else if (double.class.equals(fieldType)
							|| Double.class.equals(fieldType)) {
						field.set(item, cursor.getDouble(index));
					}
				} else if (SQLDataType.TEXT == type) {
					field.set(item, cursor.getString(index));
				} else if (SQLDataType.NULL == type) {
					field.set(item, null);
				}
			}
			beans.add(item);
		}
		return beans;
	}

}
