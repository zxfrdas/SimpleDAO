package com.simpledao.sample;

import com.zt.simpledao.Column;
import com.zt.simpledao.Database;
import com.zt.simpledao.PropMethod;
import com.zt.simpledao.PropMethodType;
import com.zt.simpledao.SQLDataType;
import com.zt.simpledao.Table;

//此处指定对应的数据库名称、表名、数据库版本
@Database(name = "private.db", version = 0)
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

	// 字段为private时，需要生成getter&setter
	// 需要指定该函数对应的字段名称，是get还是set操作
	@PropMethod(name="number", type=PropMethodType.GET)
	public int getNumber() {
		return number;
	}

	@PropMethod(name="number", type=PropMethodType.SET)
	public void setNumber(int number) {
		this.number = number;
	}

	@PropMethod(name="text", type=PropMethodType.GET)
	public String getText() {
		return text;
	}

	@PropMethod(name="text", type=PropMethodType.SET)
	public void setText(String text) {
		this.text = text;
	}

	@PropMethod(name="bool", type=PropMethodType.GET)
	public boolean isBool() {
		return bool;
	}

	@PropMethod(name="bool", type=PropMethodType.SET)
	public void setBool(boolean bool) {
		this.bool = bool;
	}

	@PropMethod(name="flo", type=PropMethodType.GET)
	public double getFlo() {
		return flo;
	}

	@PropMethod(name="flo", type=PropMethodType.SET)
	public void setFlo(double flo) {
		this.flo = flo;
	}

	@PropMethod(name="blob", type=PropMethodType.GET)
	public byte[] getBlob() {
		return blob;
	}

	@PropMethod(name="blob", type=PropMethodType.SET)
	public void setBlob(byte[] blob) {
		this.blob = blob;
	}

	@PropMethod(name="booltext", type=PropMethodType.GET)
	public boolean isBooltext() {
		return booltext;
	}

	@PropMethod(name="booltext", type=PropMethodType.SET)
	public void setBooltext(boolean booltext) {
		this.booltext = booltext;
	}
	
}
