package com.zt.simpledao.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;

import com.zt.simpledao.bean.IBeanProxy;
import com.zt.simpledao.condition.Condition;

public abstract class SQLite3DAO<T> implements IDAO<T> {
	private final ReadLock mReadLock;
	private final WriteLock mWriteLock;
	private SQLiteDatabase mDatabase;
	private String tableName;
	private IBeanProxy<T> mProxy;
	private String mQueryAll = "SELECT * FROM ";
	private String mQueryCount = "SELECT COUNT(*) FROM ";

	public SQLite3DAO(Context context, IBeanProxy<T> proxy) {
		mProxy = proxy;
		tableName = mProxy.getTableName();
		mQueryAll = mQueryAll + tableName;
		mQueryCount = mQueryCount + tableName;
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
			int newVersion, IBeanProxy<T> proxy);

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
		mWriteLock.lock();
		SQLiteStatement statement = mProxy.createInsertSQL(mDatabase, item);
		try {
			ret = statement.executeInsert();
		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			statement.close();
		}
		mWriteLock.unlock();
		if (-1 != ret) {
			return true;
		}
		return false;
	}

	@Override
	public boolean insert(Collection<T> items) {
		long ret = -1;
		mWriteLock.lock();
		mDatabase.beginTransaction();
		try {
			for (T item : items) {
				SQLiteStatement statement = mProxy.createInsertSQL(mDatabase, item);
				try {
					ret = statement.executeInsert();
				} finally {
					statement.close();
				}
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
				SQLiteStatement statement = mProxy.createDeleteSQL(mDatabase,
						condition.getSelection(), condition.getSelectionArgs());
				try {
					ret = statement.executeUpdateDelete();
				} finally {
					statement.close();
				}
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
		SQLiteStatement statement = mProxy.createDeleteSQL(mDatabase,
				condition.getSelection(), condition.getSelectionArgs());
		try {
			ret = statement.executeUpdateDelete();
		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			statement.close();
		}
		mWriteLock.unlock();
		if (0 != ret) {
			return true;
		}
		return false;
	}

	@Override
	public boolean deleteAll() {
		long ret = 0;
		mWriteLock.lock();
		SQLiteStatement statement = mProxy.createDeleteSQL(mDatabase, null, null);
		try {
			ret = statement.executeUpdateDelete();
		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			statement.close();
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
		mWriteLock.lock();
		SQLiteStatement statement = mProxy.createUpdateSQL(mDatabase, item,
				condition.getSelection(), condition.getSelectionArgs());
		try {
			ret = statement.executeUpdateDelete();
		} catch (SQLiteException e) {
			e.printStackTrace();
			ret = -1;
		} finally {
			statement.close();
		}
		mWriteLock.unlock();
		if (-1 != ret) {
			return true;
		}
		return false;
	}

	@Override
	public boolean update(Collection<T> items, Condition condition) {
		long ret = -1;
		mWriteLock.lock();
		mDatabase.beginTransaction();
		try {
			for (T item : items) {
				SQLiteStatement statement = mProxy.createUpdateSQL(mDatabase, item,
						condition.getSelection(), condition.getSelectionArgs());
				try {
					ret = statement.executeUpdateDelete();
				} finally {
					statement.close();
				}
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
		mWriteLock.lock();
		mDatabase.beginTransaction();
		try {
			final Set<Entry<T, Condition>> entrySet = updates.entrySet();
			for (Entry<T, Condition> entry : entrySet) {
				SQLiteStatement statement = mProxy.createUpdateSQL(mDatabase, entry
						.getKey(), entry.getValue().getSelection(), entry.getValue()
						.getSelectionArgs());
				try {
					ret = statement.executeUpdateDelete();
				} finally {
					statement.close();
				}
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
			String sql = SQLiteQueryBuilder.buildQueryString(false, tableName,
					null, condition.getSelection(), condition.getGroupby(),
					null, condition.getOrderBy(), null);
			
            c = mDatabase.rawQueryWithFactory(null, sql, condition.getSelectionArgs(),
            		SQLiteDatabase.findEditTable(tableName), null);
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
			c = mDatabase.rawQueryWithFactory(null, sql, selectionArgs, null,
					null);
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
			String sql = SQLiteQueryBuilder.buildQueryString(false, tableName,
					null, condition.getSelection(), condition.getGroupby(),
					null, condition.getOrderBy(), null);

            c = mDatabase.rawQueryWithFactory(null, sql, condition.getSelectionArgs(),
            		SQLiteDatabase.findEditTable(tableName), null);
		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			mReadLock.unlock();
		}
		return mProxy.convertDatabaseToBean(c);
	}

	@Override
	public Cursor queryAllForCursor() {
		Cursor c = null;
		mReadLock.lock();
		try {
			c = mDatabase.rawQueryWithFactory(null, mQueryAll, null, null, null);
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
			c = mDatabase.rawQueryWithFactory(null, mQueryAll, null, null, null);
		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			mReadLock.unlock();
		}
		return mProxy.convertDatabaseToBean(c);
	}

	@Override
	public int getCount() {
		int count = 0;
		Cursor c = null;
		mReadLock.lock();
		try {
			c = mDatabase.rawQuery(mQueryCount, null);
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

}
