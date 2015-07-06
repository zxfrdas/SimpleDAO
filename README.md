# SimpleDAO简介 #
- SimpleDAO是一个非常简洁、高效的Android数据库ORM框架，用于简化Android APP工程中SQLite数据库相关开发。
- SimpleDAO的宗旨即Simple，数据库相关操作（初始化、CRUD）一句话完成。

---

# SimpleDAO特性 #
- 一行代码完成数据库操作
- 支持事务
- 支持自定义主键、多主键
- 支持自定义数据库名、表名、列名、列数据类型
- 支持自定义删除、更新、查找条件
- 使用SQL语句缓存
- 使用注解进行Object-Database关系映射
- 使用APT（Annotation Processing Tool）动态生成所需代码
- 轻巧，JAR包仅50KB大小
- ***摒弃运行时反射，利用SQL语句缓存，运行速度快！***

---

# SimpleDAO优势 #
- 在SimpleDAO的编写过程中，借鉴了许多非常优秀的Android ORM开源框架，包括：DBExecutor、xUtils、Afinal、GreenDao等。
- SimpleDAO的优势在于：
  - 不使用运行时反射，十万级数据操作时比使用反射快一个数量级
  - 删除、更新、查找时可以自由指定条件
  - 构造SQL条件语句时，用户无需记忆Object中字段在数据库中对应的列名，自动生成的代理类完成了映射，用户使用体验就像直接调用该字段

---

# SimpleDAO原理 #
- SimpleDAO核心原理，是使用了Annotation Processing Tools（APT）及Java Annotation（注解）功能。
- APT功能大体上是在预编译阶段对含有指定规则（如包含特殊Annotation）的Java文件进行处理，在此不多赘述。SimpleDAO利用APT功能以及Java Annotation功能，在用户使用Annotation配置Java Bean后，APT自动生成相应的数据库DAO、Proxy类。从而避免了运行时的反射调用（现有大部分ORM框架做法），大幅提高了执行速度。
- Java Annotation在SimpleDAO中用于对Java Bean进行数据库映射指定，如要映射的数据库名称、表名、要映射的列名及数据类型等。

---

# 开始使用SimpleDAO #
## Eclipse ##
### step 1 ###
- 在需要使用的Android工程中导入SimpleDAO.jar（直接放入libs文件夹）。

### step 2 ###
- 右键工程--properties--Java Compiler--Annotation Processing
- 在该页面下，勾选所有Enable项（一共三项）。Generated source directory是设置APT自动生成的代码所在文件夹，可以自由指定。
- 进入Factory Path项，勾选Enable项，点击Add JARs...选择项目libs目录下的SimpleDAO.jar文件。之后点击Advanced...应该在弹出窗口中看到`com.zt.simpledao.apt.DAOProcessor`，这就是SimpleDAO提供的APT处理器，点击选中后OK即可

## Android Studio ##
### step 1 ###
- 打开Android Studio里Project的build.gradle，加入此行：classpath 'com.neenbedankt.gradle.plugins:android-apt:1.+'

### step 2 ###
- 打开Module的build.grade，文件开头加上依赖 apply plugin: 'android-apt'
- 依赖中加入你要使用的带APT注解处理器的jar包：apt files(libs/SimpleDAO.jar)

### 配置完毕开始使用 ###
```java
// 假设已经写好了一个要映射为数据库的类并加上了正确注解
// APT会自动生成两个文件TestBeanDAO和TestBeanProxy
// 其中TestBeanDAO是数据库CRUD操作类
// TestBeanProxy提供了数据库的表名、列名、列ID供用户使用
// 初始化数据库
IDAO<TestBean> dao = TestBeanDAO.getInstance(context);
// 插入
dao.insert(new TestBean(...));
// 删除
// 构造删除条件
// 条件为：TestBean中字段‘bool’映射到数据库中的列的值=true
Condition c = Condition.build().where(TestBeanProxy.bool).equal(true)
				.buildDone();
// 删除符合该条件的数据
dao.delete(c);
// 更新
c = Condition.build().where(TestBeanProxy.number).equal(1).buildDone();
dao.update(new TestBean(...), c);
// 查找
c = Condition.build().where(TestBeanProxy.text).notEqual("text")
    .orderby(TestBeanProxy.number).ascend().buildDone();
List<TestBean> results = dao.query(c);
// 还有对应的批量操作，基本类似，不赘述了。
```
