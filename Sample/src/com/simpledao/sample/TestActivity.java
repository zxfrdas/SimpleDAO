package com.simpledao.sample;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;

import com.zt.simpledao.condition.Condition;
import com.zt.simpledao.dao.IDAO;
import com.zt.simpledao.entry.Dao;

public class TestActivity extends Activity {
	IDAO<TestBeanPublic> daoPublic;
	IDAO<TestBeanPrivate> daoPrivate;
	Dao dao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onStart() {
		super.onStart();
		// java bean 字段是public或者private对CRUD操作没影响
		// 如示例一样配置完注解，成功生成DAO类就OK
		// 后续演示就只使用一种了
		daoPublic = TestBeanPublicDAO.getInstance(getApplicationContext());
		daoPrivate = TestBeanPrivateDAO.getInstance(getApplicationContext());
		dao = Dao.getInstance(getApplicationContext());
	}
	
	private void insert() {
		List<TestBeanPublic> beans = new ArrayList<TestBeanPublic>(10);
		for (int i = 0; i < 10; i ++) {
			TestBeanPublic bean = new TestBeanPublic();
			bean.blob = new byte[] { Integer.valueOf(i).byteValue() };
			bean.bool = (i % 2 == 0) ? true : false;
			bean.flo = (double) (i * 0.01);
			bean.number = i;
			bean.text = i + "更新前";
			bean.booltext = !bean.bool;
		}
		// 插入一个
		daoPublic.insert(beans.get(0));
		dao.insert(beans.get(0));
		// 插入多个
		daoPublic.insert(beans);
		dao.insert(beans);
		// 还可以直接插入ContentValues，不过一般不使用
	}
	
	private void delete() {
		// 删除数据库中bean.bool对应列值为false的行
		Condition c = Condition.build().where(TestBeanPublicProxy.bool)
				.equal(false).buildDone();
		daoPublic.delete(c);
		dao.delete(c, TestBeanPublic.class);
		// 还可以将多个条件加入list，由库依次删除
		List<Condition> conditions = new ArrayList<Condition>(2);
		conditions.add(c);
		conditions.add(Condition.build().where(TestBeanPublicProxy.number)
				.lessEqual(5).buildDone());
		daoPublic.delete(conditions);
		dao.delete(conditions, TestBeanPublic.class);
		// 删除所有
		daoPublic.deleteAll();
		dao.deleteAll(TestBeanPublic.class);
	}
	
	private void update() {
		// 更新一个
//		daoPublic.update(item, condition);
		// 更新多个，同样条件
//		daoPublic.update(items, condition);
		// 更新多个，每个都有自己的条件
//		daoPublic.update(updates);
		// 比较懒，就不写完了。。
	}
	
	private void query() {
		// 查找数据库中bean.flo大于等于0.03的值以及bean.booltext为“true”的值
		// 根据bean.number降序排列
		Condition c = Condition.build().where(TestBeanPublicProxy.flo)
				.moreEqual(0.03).and().where(TestBeanPublicProxy.booltext)
				.equal("true").orderby(TestBeanPublicProxy.number).descend().buildDone();
		List<TestBeanPublic> results = daoPublic.query(c);
		results = dao.query(c, TestBeanPublic.class);
		// 查找全部
		results = daoPublic.queryAll();
		results = dao.queryAll(TestBeanPublic.class);
		// 还有些别的查找方法，返回值不同
	}
	

}
