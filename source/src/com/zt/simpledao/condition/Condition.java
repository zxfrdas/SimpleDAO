package com.zt.simpledao.condition;

/**
 * SQL条件语句
 */
public class Condition {
	private String selection;
	private String[] selectionArgs;
	private String orderBy;
	private String groupby;
	
	/**
	 * 开始构造条件语句
	 * @return 条件语句构造器
	 */
	public static IConditionBuilder build() {
		return new SQLiteConditionBuilder();
	}
	
	protected Condition() {}

	public String getSelection() {
		return selection;
	}

	public void setSelection(String selection) {
		this.selection = selection;
	}

	public String[] getSelectionArgs() {
		return selectionArgs;
	}

	public void setSelectionArgs(String[] selectionArgs) {
		this.selectionArgs = selectionArgs;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public String getGroupby() {
		return groupby;
	}

	public void setGroupby(String groupby) {
		this.groupby = groupby;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("where = ").append(getSelection()).append(", ");
		for (String arg : getSelectionArgs()) {
			sb.append("args = ").append(arg).append(" ");
		}
		sb.append(", orderby = ").append(orderBy);
		sb.append(", groupby = ").append(groupby);
		return sb.toString();
	}
	
}
