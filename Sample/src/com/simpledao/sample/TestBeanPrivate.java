package com.simpledao.sample;

import com.zt.simpledao.Column;
import com.zt.simpledao.Database;
import com.zt.simpledao.SQLDataType;
import com.zt.simpledao.Table;

//此处指定对应的数据库名称、表名、数据库版本
@Database(name = "test.db", version = 1)
@Table(name = "TestBeanPrivate")
public class TestBeanPrivate {
	// 指定number字段在数据库中对应列名为默认（即字段名），数据类型为Integer，是主键
	// 如果一个Column指定了Index属性，则所有Column都需要指定
	@Column(type = SQLDataType.INTEGER, primary = true)
	private int number;
	@Column(type = SQLDataType.TEXT)
	private String text;
	@Column(type = SQLDataType.INTEGER)
	private boolean bool;
	@Column(type = SQLDataType.REAL)
	private double flo;
	@Column(type = SQLDataType.BLOB)
	private byte[] blob;
	// boolean 可以映射为integer，也可以映射为text
	@Column(type=SQLDataType.TEXT)
	private boolean booltext;
	
	// 必须有一个无参数的构造函数
	public TestBeanPrivate() {
	}

	// 字段为private时，需要用IDE自动生成的getter&setter
	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isBool() {
		return bool;
	}

	public void setBool(boolean bool) {
		this.bool = bool;
	}

	public double getFlo() {
		return flo;
	}

	public void setFlo(double flo) {
		this.flo = flo;
	}

	public byte[] getBlob() {
		return blob;
	}

	public void setBlob(byte[] blob) {
		this.blob = blob;
	}

	public boolean isBooltext() {
		return booltext;
	}

	public void setBooltext(boolean booltext) {
		this.booltext = booltext;
	}
	
}
