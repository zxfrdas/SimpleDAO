package com.zt.simpledao.condition.sqlite;

import java.util.ArrayList;
import java.util.List;

import com.zt.simpledao.condition.Condition;
import com.zt.simpledao.condition.IConditionBuilder;
import com.zt.simpledao.condition.IOrderby;
import com.zt.simpledao.condition.IWhere;

@SuppressWarnings("unchecked")
public class SQLiteConditionBuilder implements IConditionBuilder {
	private static final String PLACE_HOLDER_NUMBER = "? ";
	private static final String AND = "AND ";
	private List<Where> mWheres;
	private List<Orderby> mOrderbys;
	private List<Groupby> mGroupbys;
	private Where where;
	
	public SQLiteConditionBuilder() {
		mWheres = new ArrayList<Where>();
		mOrderbys = new ArrayList<Orderby>();
		mGroupbys = new ArrayList<Groupby>();
	}
	
	@Override
	public IWhere where(String column) {
		where = new Where(this);
		where.column = column;
		return where;
	}
	
	@Override
	public IOrderby orderby(String column) {
		Orderby orderby = new Orderby(this);
		orderby.column = column;
		mOrderbys.add(orderby);
		return orderby;
	}
	
	@Override
	public IConditionBuilder groupby(String column) {
		Groupby groupby = new Groupby();
		groupby.column = column;
		mGroupbys.add(groupby);
		return this;
	}
	
	@Override
	public IConditionBuilder and() {
		if (null != where) {
			mWheres.add(where);
		}
		return this;
	}
	
	@Override
	public Condition sql(String selection, String[] selectionArgs,
			String orderby, String groupby) {
		Condition condition = new Condition();
		condition.setSelection(selection);
		condition.setSelectionArgs(selectionArgs);
		condition.setOrderBy(orderby);
		condition.setGroupby(groupby);
		return condition;
	}

	@Override
	public Condition buildDone() {
		and();
		Condition condition = new Condition();
		condition.setSelection(createSelection());
		condition.setSelectionArgs(createSelectionArgs());
		condition.setOrderBy(createOrderby());
		condition.setGroupby(createGroupby());
		clear();
		return condition;
	}
	
	private String createSelection() {
		StringBuilder sb = new StringBuilder();			
		for (Where where : mWheres) {
			sb.append(where.column);
			sb.append(where.condition.value);
			int argNumber = 0;
			if (Where.EnumCondition.BETWEEN == where.condition) {
				argNumber = 2;
			} else {
				argNumber = 1;
			}
			for (int i = 0; i < argNumber; i ++) {
				sb.append(PLACE_HOLDER_NUMBER);
				sb.append(AND);
			}
		}
		int lastAndIndex = sb.lastIndexOf(AND);
		String result = null;
		if (-1 != lastAndIndex) {
			result = sb.substring(0, lastAndIndex).toString();
		}
		return result;
	}
	
	private String[] createSelectionArgs() {
		List<String> args = new ArrayList<String>();
		for (Where where : mWheres) {
			for (String arg : where.args) {
				// if arg is boolean and type is number
				// we make true -> 1 & false -> 0
				if (Where.EnumArgType.NUMBER == where.argType) {
					if ("true".equals(arg) || "false".equals(arg)) {
						arg = (Boolean.valueOf(arg) ? 1 : 0) + "";
					}
				}
				args.add(arg);
			}
		}
		String[] result = null;
		if (!args.isEmpty()) {
			result = args.toArray(new String[args.size()]);
		}
		return result;
	}
	
	private String createOrderby() {
		StringBuilder sb = new StringBuilder();	
		for (Orderby orderby : mOrderbys) {
			sb.append(orderby.column);
			if (orderby.asc) {
				sb.append(" ASC, ");
			} else {
				sb.append(" DESC, ");
			}
		}
		int lastAndIndex = sb.lastIndexOf(",");
		String result = null;
		if (-1 != lastAndIndex) {
			result = sb.substring(0, lastAndIndex).toString();
		}
		return result;
	}
	
	private String createGroupby() {
		StringBuilder sb = new StringBuilder();	
		for (Groupby groupby : mGroupbys) {
			sb.append(groupby.column);
			sb.append(", ");
		}
		int lastAndIndex = sb.lastIndexOf(",");
		String result = null;
		if (-1 != lastAndIndex) {
			result = sb.substring(0, lastAndIndex).toString();
		}
		return result;
	}
	
	private void clear() {
		where = null;
		if (null != mWheres) {
			mWheres.clear();
		}
		if (null != mGroupbys) {
			mGroupbys.clear();
		}
		if (null != mOrderbys) {
			mOrderbys.clear();
		}
	}
	
	private static class Where implements IWhere {
		private IConditionBuilder builder;
		private String column;
		private EnumCondition condition;
		private List<String> args;
		private EnumArgType argType;
		
		enum EnumArgType {
			NUMBER,
			TEXT;
		}
		
		enum EnumCondition {
			LESS(" < "),
			LESS_EQUAL(" <= "),
			EQUAL(" = "),
			MORE_EQUAL(" >= "),
			MORE(" > "),
			NOT_EQUAL(" <> "),
			BETWEEN(" BETWEEN "),
			LIKE(" LIKE ");
			
			private String value;
			private EnumCondition(String value) {
				this.value = value;
			}
		}
		
		public Where(IConditionBuilder builder) {
			this.builder = builder;
		}

		@Override
		public <E> IConditionBuilder less(E arg) {
			condition = Where.EnumCondition.LESS;
			putArg(arg);
			return builder;
		}

		@Override
		public <E> IConditionBuilder lessEqual(E arg) {
			condition = Where.EnumCondition.LESS_EQUAL;
			putArg(arg);
			return builder;
		}

		@Override
		public <E> IConditionBuilder equal(E arg) {
			condition = Where.EnumCondition.EQUAL;
			putArg(arg);
			return builder;
		}

		@Override
		public <E> IConditionBuilder notEqual(E arg) {
			condition = Where.EnumCondition.NOT_EQUAL;
			putArg(arg);
			return builder;
		}

		@Override
		public <E> IConditionBuilder moreEqual(E arg) {
			condition = Where.EnumCondition.MORE_EQUAL;
			putArg(arg);
			return builder;
		}

		@Override
		public <E> IConditionBuilder more(E arg) {
			condition = Where.EnumCondition.MORE;
			putArg(arg);
			return builder;
		}

		@Override
		public <E> IConditionBuilder between(E min, E max) {
			if (!min.getClass().equals(max.getClass())) {
				throw new IllegalArgumentException("between args need same class");
			}
			condition = Where.EnumCondition.BETWEEN;
			putArg(min, max);
			return builder;
		}

		@Override
		public <E> IConditionBuilder like(E pattern) {
			condition = Where.EnumCondition.LIKE;
			putArg(pattern);
			return builder;
		}
		
		private <T> void putArg(T... args) {
			if (args[0] instanceof String) {
				argType = Where.EnumArgType.TEXT;
			} else {
				argType = Where.EnumArgType.NUMBER;
			}
			this.args = new ArrayList<String>();
			for (T arg : args) {
				this.args.add(arg.toString());
			}
		}
		
	}
	
	private class Orderby implements IOrderby {
		private IConditionBuilder builder;
		private String column;
		private boolean asc;
		
		public Orderby(IConditionBuilder builder) {
			this.builder = builder;
		}
		
		@Override
		public IConditionBuilder ascend() {
			this.asc = true;
			return builder;
		}
		
		@Override
		public IConditionBuilder descend() {
			asc = false;
			return builder;
		}
	}

	private static class Groupby {
		private String column;
	}

}
