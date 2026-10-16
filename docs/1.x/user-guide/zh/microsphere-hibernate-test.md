# microsphere-hibernate-test（1.x）

[English](../microsphere-hibernate-test.md) · [中文](microsphere-hibernate-test.md)

## 1. 模块定位与 Maven 坐标

`microsphere-hibernate-test` 是 Microsphere Hibernate 1.x 的 Hibernate 测试套件。它是一个**普通的、对外发布的
JAR**（不是 `test-jar`），下游项目以 `test` 作用域引入即可获得：

- 一个基于 H2 的抽象 JUnit 5 测试基类，自带现成的集成测试（`AbstractHibernateH2Test`）；
- 一种注解驱动的替代方案（`@HibernateTest` + `@HibernateRuntime` + `HibernateExtension`），由它替你管理
  `Configuration`、`SessionFactory`、`Session` 与 `StatelessSession`；
- 一套由四个实体组成的小型领域模型（`User`、`UserProfile`、`Order`、`Product`），用作测试数据。

| **项目** | **取值** |
|---|---|
| GroupId | `io.github.microsphere-projects` |
| ArtifactId | `microsphere-hibernate-test` |
| 版本 | `${revision}` → `release-1.x` 上为 `0.1.11-SNAPSHOT`；最新发布版本 `0.1.10` |
| 打包 | `jar` |
| Name | `Microsphere :: Hibernate :: Test` |
| POM 文件 | `microsphere-hibernate-test/pom.xml` |
| Java 包 | `io.microsphere.hibernate.test`、`io.microsphere.hibernate.test.entity` |
| 是否依赖 core？ | **否。** 本模块不依赖 `microsphere-hibernate-core`；是 core 模块在 `test` 作用域依赖了本模块。 |
| JavaDoc | https://javadoc.io/doc/io.github.microsphere-projects/microsphere-hibernate-test |

## 2. 版本信息与兼容范围

| **维度** | **取值** |
|---|---|
| 版本线 | `0.1.x`，分支 `release-1.x`，最新发布版本 `0.1.10` |
| JDK | 8 – 25（CI：JDK 8 配全部 7 个 Hibernate profile；JDK 11/17/21/25 配 `hibernate-5.3` … `hibernate-5.6`） |
| Hibernate Core | 5.0.x – 5.6.x，`org.hibernate:hibernate-core`，默认 `5.6.15.Final` |
| Spring Cloud | Hoxton – 2021.0.x（运行期无关：本模块不含任何 Spring 代码） |
| JUnit | JUnit Jupiter 5（版本由 `microsphere-spring-cloud` `0.1.26` parent 链提供，其中 `junit-jupiter.version` 为 `5.14.4`） |
| 持久化注解 | `javax.persistence` —— `HibernateExtension` 的实体扫描按 `javax.persistence.Entity` 过滤 |
| 数据库 | H2（内存模式），`org.h2.Driver`，`org.hibernate.dialect.H2Dialect` |

### 依赖（全部为 `optional`，摘自 `microsphere-hibernate-test/pom.xml`）

| **依赖** | **用途** |
|---|---|
| `io.github.microsphere-projects:microsphere-annotation-processor` | 生成配置元数据 |
| `io.github.microsphere-projects:microsphere-java-core` | `HibernateExtension` 使用的 `io.microsphere.logging` |
| `io.github.microsphere-projects:microsphere-java-test` | 供下游测试可选使用；本模块的类并未 import 它（`SimpleClassScanner` 与 `io.microsphere.logging` 均来自 `microsphere-java-core`） |
| `org.hibernate:hibernate-core` | `Configuration`、`SessionFactory`、`Session`、`StatelessSession` |
| `com.h2database:h2` | 内存数据库 |
| `org.junit.jupiter:junit-jupiter` | `@Test`、`@Nested`、`@ExtendWith`、`assertDoesNotThrow` |
| `org.mockito:mockito-core`、`org.mockito:mockito-junit-jupiter` | 供下游测试使用 |
| `ch.qos.logback:logback-classic` | Hibernate SQL 输出的日志实现 |
| `javax.xml.bind:jaxb-api` | 由 `java9+` profile（`[9,)`）添加 |

**由于所有依赖都是 optional，使用者必须自己声明真正要用的东西。** 最小且完整的测试装配是：

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-hibernate-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-core</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

版本交由 BOM 管理，见 [microsphere-hibernate-dependencies.md](microsphere-hibernate-dependencies.md)。

## 3. API 说明

### 3.1 `AbstractHibernateTest`（包级私有）

`io.microsphere.hibernate.test.AbstractHibernateTest`

> **可见性：** 该类声明为 `abstract class AbstractHibernateTest`，**没有 `public` 修饰符**。因此它*不能*在你自己的
> 包中被继承；请使用 `AbstractHibernateH2Test`（public）或 `@HibernateTest`。它的 `protected` 字段与方法仍可通过
> public 子类继承使用 —— `AbstractHibernateH2Test`（以及间接地 core 模块的测试）正是这样做的。

| **成员** | **类型** | **契约** |
|---|---|---|
| `configuration` | `protected Configuration` | 每个测试方法构建一次 |
| `sessionFactory` | `protected SessionFactory` | 由 `configuration` 构建 |
| `session` | `protected Session` | `sessionFactory.openSession()` |
| `statelessSession` | `protected StatelessSession` | `sessionFactory.openStatelessSession()` |
| `setUp()` | `final`、`@BeforeEach` | `buildConfiguration()` → `customizeConfiguration(...)` → `buildSessionFactory()` → 打开两个 session → `init()` |
| `tearDown()` | `final`、`@AfterEach` | `destroy()` → 关闭 `session` → 关闭 `statelessSession` → 关闭 `sessionFactory` |
| `buildConfiguration()` | `protected abstract` | **唯一必须由你实现的方法** —— 返回测试所用的 `Configuration` |
| `customizeConfiguration(Configuration)` | `protected`，空实现 | 在构建 factory 之前修改属性/追加实体类的钩子 |
| `init()` / `destroy()` | `protected`，空实现 | 围绕 session 生命周期的每个测试 setup/teardown 钩子 |

同包内的具体实现长这样（摘自该类的 Javadoc，也是本模块自身遵循的写法）：

```java
class MyHibernateTest extends AbstractHibernateTest {

    @Override
    protected Configuration buildConfiguration() {
        return new Configuration()
                .addAnnotatedClass(User.class)
                .setProperty("hibernate.connection.url", "jdbc:h2:mem:testdb");
    }

    @Test
    void testPersist() {
        User user = new User();
        user.setName("Alice");
        Transaction tx = session.beginTransaction();
        session.persist(user);
        tx.commit();
    }
}
```

### 3.2 `AbstractHibernateH2Test`（public、abstract）

`io.microsphere.hibernate.test.AbstractHibernateH2Test extends AbstractHibernateTest` —— 多数使用者真正接触的类。

**固定的 `Configuration`（`buildConfiguration()`）：**

```java
new Configuration()
        .addAnnotatedClass(User.class)
        .addAnnotatedClass(UserProfile.class)
        .addAnnotatedClass(Order.class)
        .addAnnotatedClass(Product.class)
        .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
        .setProperty("hibernate.connection.url", "jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1")
        .setProperty("hibernate.connection.username", "sa")
        .setProperty("hibernate.connection.password", "")
        .setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
        .setProperty("hibernate.format_sql", "true")
        .setProperty("hibernate.show_sql", "true")
        .setProperty("hibernate.hbm2ddl.auto", "update");
```

**测试数据钩子：**

| **成员** | **契约** |
|---|---|
| `protected User user` | 由 `init()` 在每个测试方法执行前填充 |
| `protected User createUser()` | 构建数据图：`User(name="Tom")` + `UserProfile(idCard="ID123456")` + 一个含单个 `Product(productName="Laptop")` 的 `Order(orderNo="ORD001")`，通过 `user.addOrder(order)` 建立关联（该方法同时设置 `order.user`）。可覆写以替换测试数据。 |
| `protected void doInAction(Runnable)` | 即 `assertDoesNotThrow(action::run)` —— 用于故意在显式事务之外执行的操作 |

**继承来的测试清单** —— 继承该类后，这些测试会在你的套件中一起运行：

| **`@Nested` 类** | **测试方法** |
|---|---|
| `SessionTest` | `testPersistAndGet`、`testPersistAndMerge`、`testPersistAndDelete`、`testPersistAndReplicate`、`testPersistAndFlush`、`testPersistAndEvict`、`testPersistAndLock`、`testPersistAndRefresh`、`testPersistAndLoad`、`testPersistAndGetManagedEntities`（`persist` + `FROM io.microsphere.hibernate.test.entity.User`） |
| `StatelessSessionTest` | `testInsertAndGet`、`testInsertAndDelete`、`testInsertAndUpdate`、`testInsertAndRefresh` |

两个 `@Nested` 类各自带有 `doInTransaction(Runnable)` 辅助方法，用 `assertDoesNotThrow` 包裹并在其中对
`session` / `statelessSession` 执行 begin–commit。

**最小使用者子类** —— 仓库自身就是这么写的：

```java
package io.microsphere.hibernate.test;

import org.hibernate.cfg.Configuration;

/**
 * Default {@link AbstractHibernateH2Test}
 */
public class HibernateH2Test extends AbstractHibernateH2Test {

    @Override
    protected void customizeConfiguration(Configuration configuration) {
        super.customizeConfiguration(configuration);
    }
}
```

出处：`microsphere-hibernate-test/src/test/java/io/microsphere/hibernate/test/HibernateH2Test.java`。
另一个跨模块的例子来自 core 模块，它用这套装置证明 `EntityCallback` 装配正确（见
[microsphere-hibernate-core.md](microsphere-hibernate-core.md)）：

```java
class EntityCallbackIntegratorTest extends AbstractHibernateH2Test {
}
```

出处：`microsphere-hibernate-core/src/test/java/io/microsphere/hibernate/entity/EntityCallbackIntegratorTest.java`。

### 3.3 `@HibernateTest`

`io.microsphere.hibernate.test.HibernateTest`

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ExtendWith(HibernateExtension.class)
public @interface HibernateTest {
    String[] properties() default {};
    String[] scanEntityPackages() default {};
}
```

| **属性** | **默认值** | **语义** |
|---|---|---|
| `properties` | `{}` | `key=value` 形式的 Hibernate 配置。扩展按**第一个** `=` 切分，两侧 trim 后调用 `Configuration.setProperty`。不含 `=`（或以 `=` 开头）的字符串会被**跳过并打 `WARN` 日志**，不会报错。值本身可以包含 `=`。 |
| `scanEntityPackages` | `{}` | 递归扫描这些包中标注了 `javax.persistence.Entity` 的类，逐个调用 `Configuration#addAnnotatedClass` 注册。扫描使用 `io.microsphere.io.scanner.SimpleClassScanner`，基于**线程上下文 `ClassLoader`**。 |

`@ExtendWith(HibernateExtension.class)` 是元注解，因此一个 `@HibernateTest` 就够了 —— 不需要你自己再写
`@ExtendWith`。`@Inherited` 表示被标注类的子类同样保持该注解。

### 3.4 `@HibernateRuntime`

`io.microsphere.hibernate.test.HibernateRuntime` —— `@Target({FIELD, PARAMETER})`、`@Retention(RUNTIME)`。没有
属性：由字段或参数的**声明类型**决定注入哪个组件。

| **请求的类型** | **static 字段** | **实例字段** | **方法参数** |
|---|---|---|---|
| `org.hibernate.cfg.Configuration` | 注入 | 注入 | 解析 |
| `org.hibernate.SessionFactory` | 注入 | 注入 | 解析 |
| `org.hibernate.Session` | **保持 `null`** | 注入 | 解析 |
| `org.hibernate.StatelessSession` | **保持 `null`** | 注入 | 解析 |
| 其他任意类型 | 忽略 | 忽略 | 不支持（`supportsParameter` 返回 `false`） |

static 的 `Session`/`StatelessSession` 字段故意为 `null`：session 是每个测试方法级别的资源，用静态字段共享会跨测试
泄漏。这是被断言过的行为，不是偶然 —— 见 4.2 节。

### 3.5 `HibernateExtension`

`io.microsphere.hibernate.test.HibernateExtension implements BeforeAllCallback, BeforeEachCallback,
AfterEachCallback, AfterAllCallback, ParameterResolver`

| **阶段** | **行为** |
|---|---|
| `beforeAll` | 沿 `ExtensionContext` 父链向上查找标注了 `@HibernateTest` 的类；找不到就什么都不做（该扩展在未标注的类上是惰性的）。**只构建一次** `Configuration` + `SessionFactory`，存入命名空间 `Namespace.create(HibernateExtension.class)`，键为 `configuration` / `sessionFactory`。随后注入当前类的 `@HibernateRuntime` **static** 字段。 |
| `beforeEach` | 打开全新的 `Session` 与 `StatelessSession`，存入**方法级** store（键 `session` / `statelessSession`），随后注入测试实例的 `@HibernateRuntime` 实例字段。 |
| `afterEach` | 从 store 中移除并关闭两个 session；关闭失败仅记录 `WARN` 并吞掉，且只有当 `session.isOpen()` 时才关闭 `Session`。 |
| `afterAll` | 只有*声明* `@HibernateTest` 的那个类负责关闭 `SessionFactory`（先判断 `isClosed()`），随后移除 `configuration`。 |
| `supportsParameter` / `resolveParameter` | 仅对四种受支持类型且带 `@HibernateRuntime` 的参数返回 `true`；解析时 `configuration`/`sessionFactory` 取自类级 store，`session`/`statelessSession` 取自方法级 store。 |
| 字段发现 | `getAllDeclaredFields` 沿类继承链向上直到（不含）`Object`，因此父类与 `@Nested` 类中的被标注字段同样会被注入。内部调用 `setAccessible(true)`，`private` 字段可用。 |
| 嵌套类 | `findHibernateTestContext` 会向上定位到被标注的外层类，因此 `@Nested` 类共享同一个 `SessionFactory`，**不会**重复构建。 |

## 4. 示例实体（`io.microsphere.hibernate.test.entity`）

| **实体** | **表名** | **主键** | **关联** |
|---|---|---|---|
| `User` | `t_user` | `Long id`，`IDENTITY` | `@OneToOne(mappedBy = "user", cascade = ALL, fetch = LAZY) UserProfile profile`；`@OneToMany(mappedBy = "user", cascade = ALL, fetch = LAZY) List<Order> orders`；便捷方法 `addOrder(Order)` 会同时设置反向引用 |
| `UserProfile` | `t_user_profile` | `Long id`，`IDENTITY` | `@OneToOne @JoinColumn(name = "user_id") User user`；字段 `String idCard` |
| `Order` | `t_order` | `Long id`，`IDENTITY` | `@ManyToOne @JoinColumn(name = "user_id") User user`；`@ManyToMany(cascade = {PERSIST, MERGE}) @JoinTable(name = "t_order_product", joinColumns = order_id, inverseJoinColumns = product_id) Set<Product> products`；字段 `String orderNo` |
| `Product` | `t_product` | `Long id`，`IDENTITY` | `@ManyToMany(mappedBy = "products", fetch = LAZY) Set<Order> orders`；字段 `String productName` |

四个实体均使用 `javax.persistence` 注解，并以 `id` 加一个业务字段实现 `equals`/`hashCode`（`User` 用 `name`、
`UserProfile` 用 `idCard`……），这就是继承来的断言（如 `assertEquals(user, user1)`）能够成立的原因。

### 4.1 `src/test` 中的实体 POJO 测试

`UserTest`、`UserProfileTest`、`OrderTest`、`ProductTest` 是不依赖 Hibernate、不依赖 H2 的普通 JUnit 5 单元测试，
验证测试领域模型本身：

```java
@Test
void testDefaultConstructor() {
    User user = new User();
    assertNotNull(user);
    assertNull(user.getId());
    assertNull(user.getName());
    assertNull(user.getProfile());
    assertNotNull(user.getOrders());
    assertTrue(user.getOrders().isEmpty());
}

@Test
void testGetterSetter() {
    User user = new User();
    user.setId(1L);
    assertEquals(1L, user.getId());
    user.setName("John Doe");
    assertEquals("John Doe", user.getName());
    UserProfile profile = new UserProfile();
    user.setProfile(profile);
    assertEquals(profile, user.getProfile());
    Order order = new Order();
    user.addOrder(order);
    // ...
}
```

出处：`microsphere-hibernate-test/src/test/java/io/microsphere/hibernate/test/entity/UserTest.java`。当你在自己的
测试里覆写 `createUser()` 时，可以复用这一写法。

### 4.2 注解驱动用法：`HibernateAnnotationTest`

以下代码原样摘自
`microsphere-hibernate-test/src/test/java/io/microsphere/hibernate/test/HibernateAnnotationTest.java`：

```java
@HibernateTest(
        properties = {
                "hibernate.connection.driver_class=org.h2.Driver",
                "hibernate.connection.url=jdbc:h2:mem:hibernate_annotation_test_db;DB_CLOSE_DELAY=-1",
                "hibernate.connection.username=sa",
                "hibernate.connection.password=",
                "hibernate.dialect=org.hibernate.dialect.H2Dialect",
                "hibernate.hbm2ddl.auto=update"
        },
        scanEntityPackages = {"io.microsphere.hibernate.test.entity"}
)
class HibernateAnnotationTest {

    @HibernateRuntime
    private static Configuration configuration;

    @HibernateRuntime
    private static SessionFactory sessionFactory;

    @HibernateRuntime
    private static Session session;

    @HibernateRuntime
    private static StatelessSession statelessSession;

    @Test
    void test() {
        assertNotNull(configuration);
        assertNotNull(sessionFactory);
        // Session and StatelessSession must NOT be injected into static fields
        assertNull(session);
        assertNull(statelessSession);
    }

    @Nested
    class FieldTest {

        @HibernateRuntime
        private Configuration configuration;

        @HibernateRuntime
        private SessionFactory sessionFactory;

        @HibernateRuntime
        private Session session;

        @HibernateRuntime
        private StatelessSession statelessSession;

        @Test
        void test() {
            assertNotNull(configuration);
            assertNotNull(sessionFactory);
            assertNotNull(session);
            assertNotNull(statelessSession);
        }
    }

    @Nested
    class ParameterTest {

        @Test
        void test(@HibernateRuntime Configuration configuration,
                  @HibernateRuntime SessionFactory sessionFactory,
                  @HibernateRuntime Session session,
                  @HibernateRuntime StatelessSession statelessSession) {
            assertNotNull(configuration);
            assertNotNull(sessionFactory);
            assertNotNull(session);
            assertNotNull(statelessSession);
        }
    }
}
```

值得照搬到你自己测试里的三点：static 字段的预期（session 为 `null`）、实例字段风格，以及参数注入风格（它可以让
测试类不再持有字段）。

## 5. 集成：在消费者项目中使用该测试套件

1. import BOM，并以 `test` 作用域引入 `microsphere-hibernate-test`（见第 2 节）。
2. 选择一种风格：
   - **快速起步：** `class MyDaoTest extends AbstractHibernateH2Test { … }` —— 你继承了 H2 装配、示例实体图与
     14 个冒烟测试。请覆写 `createUser()`（必要时再加 `customizeConfiguration(...)`），而不要覆写
     `buildConfiguration()`，因为后者正是注册那四个示例实体的地方。
   - **使用自己的模型：** 在类上标注 `@HibernateTest(properties = …, scanEntityPackages = …)`，通过
     `@HibernateRuntime` 注入所需组件。实体必须带 `javax.persistence.Entity` 才会被扫描到。
3. 如果你同时使用 `microsphere-hibernate-core`，请把 `EntityCallback` 实现注册到
   `src/test/resources/META-INF/services/io.microsphere.hibernate.entity.EntityCallback`，就能观察继承测试触发的
   全部事件 —— 这正是 `EntityCallbackIntegratorTest` + `TestEntityCallback` 的技巧。
4. 用你实际发布的 Hibernate 版本构建，让这套装置也在该版本上被验证：

```bash
./mvnw -Phibernate-5.4 test
```

## 6. 说明与注意事项

- **`AbstractHibernateTest` 是包级私有的。** 从其他包继承它会编译失败。公开可用的替代方案：
  `AbstractHibernateH2Test`（继承）或 `@HibernateTest`（注解）。
- **继承意味着连测试一起继承。** 继承 `AbstractHibernateH2Test` 会在你的模块中运行它的 `@Nested` 套件。用来做冒烟
  覆盖是好事（core 模块正是这样测试 integrator 的），但若你只需要"管道"，请改用 `@HibernateTest`。
- **H2 数据库名是固定的**（`jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1`），且 `hibernate.hbm2ddl.auto` 为 `update`，
  因此同一个 JVM 内 schema 与数据会跨测试类保留。请为每个测试准备自己的数据（覆写 `createUser()`），或者在同包的
  子类中覆写 `buildConfiguration()` 来更换 URL。
- **基类配置里 `show_sql` 与 `format_sql` 都是 `true`。** 若希望 SQL 走 SLF4J 而不是 `System.out`，请在 `test`
  作用域加入 `logback-classic`。
- **实体扫描是按包（classpath-package）进行的，不是按 JAR。** `scanEntityPackages` 要求包能被线程上下文
  `ClassLoader` 访问到；在模块拆分或 shaded 构建中，更稳妥的做法是在 `customizeConfiguration(...)` 里显式调用
  `Configuration#addAnnotatedClass`。
- **非法的 `properties` 条目不会报错。** 不含 `=` 的条目会以 `WARN` 记录后被跳过 —— 如果某个连接设置看似没生效，
  请检查日志。
- **延迟关联需要开启的 session。** `User.orders`、`User.profile`、`Product.orders` 都是 `LAZY`；请在事务内访问
  （如继承来的测试那样），否则会遭遇 `LazyInitializationException`。
- **`doInAction` 不是事务。** `testPersistAndEvict` 与 `testPersistAndLock` 故意在显式事务之外执行，只断言"未抛异常"；
  把它当作冒烟断言辅助方法，而不是持久化边界。
- **不需要 Spring。** 本模块没有触及 Spring 上下文，因此在纯 JUnit 5 构建以及 Spring Boot 2.3–2.6 项目中都可用。

## 7. 相关文档

- [microsphere-hibernate-core.md](microsphere-hibernate-core.md) —— 与本套件搭配使用的 API
- [microsphere-hibernate-dependencies.md](microsphere-hibernate-dependencies.md) —— 版本管理
- [microsphere-hibernate-parent.md](microsphere-hibernate-parent.md) —— `hibernate-5.x` profiles
- [README.md](README.md) —— 1.x 用户文档索引
- 源码（`release-1.x` 分支）：`microsphere-hibernate-test/src/main/java/io/microsphere/hibernate/test/**`、
  `microsphere-hibernate-test/src/test/java/io/microsphere/hibernate/test/**`
