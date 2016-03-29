package com.simpledao.sample;

import com.zt.simpledao.annotation.Column;
import com.zt.simpledao.annotation.Column.SQLDataType;
import com.zt.simpledao.annotation.Database;
import com.zt.simpledao.annotation.Table;

// 此处指定对应的数据库名称、表名、数据库版本
@Database(name = "test.db", version = 1)
@Table(name = "TestBeanPublic")
public class TestBeanPublic {
	// 指定number字段在数据库中对应列名为默认（即字段名），数据类型为Integer，是主键
	// 如果一个Column指定了Index属性，则所有Column都需要指定
	@Column(type = SQLDataType.INTEGER, primary = true)
	public int number;
	@Column(type = SQLDataType.TEXT)
	public String text;
	@Column(type = SQLDataType.INTEGER)
	public boolean bool;
	@Column(type = SQLDataType.REAL)
	public double flo;
	@Column(type = SQLDataType.BLOB)
	public byte[] blob;
	// boolean 可以映射为integer，也可以映射为text
	@Column(type=SQLDataType.TEXT)
	public boolean booltext;
	
	public boolean test;
	
	// 必须有一个无参数的构造函数
	public TestBeanPublic() {
	}
	
}
