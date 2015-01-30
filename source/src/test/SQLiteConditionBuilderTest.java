package test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.zt.simpledao.condition.Condition;
import com.zt.simpledao.condition.IConditionBuilder;
import com.zt.simpledao.condition.sqlite.SQLiteConditionBuilder;

public class SQLiteConditionBuilderTest {
	IConditionBuilder builder;
	Condition condition;

	@Before
	public void setUp() throws Exception {
		builder = new SQLiteConditionBuilder();
		condition = null;
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testWhere() {
		// <
		condition = builder.where("a").less("b").buildDone();
		assertEquals("a < ? ", condition.getSelection());
		assertEquals("b", condition.getSelectionArgs()[0]);
		// <=
		condition = builder.where("a").lessEqual("b").buildDone();
		assertEquals("a <= ? ", condition.getSelection());
		assertEquals("b", condition.getSelectionArgs()[0]);
		// =
		condition = builder.where("a").equal("b").buildDone();
		assertEquals("a = ? ", condition.getSelection());
		assertEquals("b", condition.getSelectionArgs()[0]);
		// >=
		condition = builder.where("a").moreEqual("b").buildDone();
		assertEquals("a >= ? ", condition.getSelection());
		assertEquals("b", condition.getSelectionArgs()[0]);
		// >
		condition = builder.where("a").more("b").buildDone();
		assertEquals("a > ? ", condition.getSelection());
		assertEquals("b", condition.getSelectionArgs()[0]);
		// !=
		condition = builder.where("a").notEqual("b").buildDone();
		assertEquals("a <> ? ", condition.getSelection());
		assertEquals("b", condition.getSelectionArgs()[0]);
		// like
		condition = builder.where("a").like("%b").buildDone();
		assertEquals("a LIKE ? ", condition.getSelection());
		assertEquals("%b", condition.getSelectionArgs()[0]);
		// between
		condition = builder.where("a").between(1, 10).buildDone();
		assertEquals("a BETWEEN ? AND ? ", condition.getSelection());
		assertEquals("1", condition.getSelectionArgs()[0]);
		assertEquals("10", condition.getSelectionArgs()[1]);
		// all
		condition = builder.where("a").notEqual("b").and().where("c").between(1, 10)
				.and().where("d").like("%d").buildDone();
		assertEquals("a <> ? AND c BETWEEN ? AND ? AND d LIKE ? ", condition.getSelection());
		assertEquals("b", condition.getSelectionArgs()[0]);
		assertEquals("1", condition.getSelectionArgs()[1]);
		assertEquals("10", condition.getSelectionArgs()[2]);
		assertEquals("%d", condition.getSelectionArgs()[3]);
	}

	@Test
	public void testOrderby() {
		condition = builder.orderby("a").ascend().orderby("b").descend().buildDone();
		assertEquals("a ASC, b DESC", condition.getOrderBy());
	}

	@Test
	public void testGroupby() {
		fail("尚未实现");
	}

	@Test
	public void testAnd() {
		fail("尚未实现");
	}

	@Test
	public void testSql() {
		fail("尚未实现");
	}

	@Test
	public void testBuildDone() {
		fail("尚未实现");
	}

}
