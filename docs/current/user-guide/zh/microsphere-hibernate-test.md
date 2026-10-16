# microsphere-hibernate-test

Microsphere Hibernate 的测试模块：一套小巧的 JUnit 5 工具，替你引导 Hibernate ORM、把
`Configuration` / `SessionFactory` / `Session` / `StatelessSession` 注入测试，并附带一组示例 JPA 实体和一个基于 H2
的抽象测试基类。

[← 返回用户文档索引](README.md) · 英文版本：[../microsphere-hibernate-test.md](../microsphere-hibernate-test.md)

## 1. 模块定位与 Maven 坐标

| **项目** | **取值** |
|----------|----------|
| `groupId` | `io.github.microsphere-projects` |
| `artifactId` | `microsphere-hibernate-test` |
| `version` | `${revision}` → `release` 分支上为 `0.2.11-SNAPSHOT`（最近发布版本 `0.2.10`） |
| `packaging` | `jar` |
| `name` | `Microsphere :: Hibernate :: Test` |
| 根包 | `io.microsphere.hibernate.test`（另有 `io.microsphere.hibernate.test.entity`） |
| 父工程 | [microsphere-hibernate-parent](microsphere-hibernate-parent.md) |
| 声明的依赖（全部 optional，compile 作用域） | `microsphere-annotation-processor`、`microsphere-java-core`、`microsphere-java-test`、`org.hibernate.orm:hibernate-core`、`com.h2database:h2`、`org.junit.jupiter:junit-jupiter`、`org.mockito:mockito-core`、`org.mockito:mockito-junit-jupiter`、`ch.qos.logback:logback-classic` |

注意：发布出去的 JAR 里这些依赖仍是 `compile` 作用域但被标记为 `optional`，因此没有任何传递依赖——这是有意为之：
本模块是一个**测试库**，不应替你决定测试技术栈。

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-hibernate-test</artifactId>
    <scope>test</scope>
</dependency>
```

你实际用到的部分需要自己补齐：

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>6.1.3</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>7.3.4.Final</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.3.232</version>
    <scope>test</scope>
</dependency>
<dependency>
    <!-- HibernateExtension 依赖它：SimpleClassScanner 支撑 scanEntityPackages -->
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-core</artifactId>
    <version>0.3.19</version>
    <scope>test</scope>
</dependency>
```

## 2. 版本信息与兼容范围

| **维度** | **取值 / 范围** | **`release` 分支中的依据** |
|----------|-----------------|-----------------------------|
| 版本线 | `0.2.x`；已发布 `0.2.10`，开发中 `0.2.11-SNAPSHOT` | 根 POM 的 `<revision>` 属性 |
| JDK | 17 – 25（基线 `17`） | `<java.version>17</java.version>`；CI 矩阵 `17`、`21`、`25` |
| Hibernate ORM | 6.2.x – 7.3.x | `hibernate-*` profile，默认 `7.3.4.Final` |
| JUnit | 由父工程链管理的 Jupiter 5 / 6 API（`0.2.10` 发布时为 `6.1.3`） | `junit-jupiter` optional 依赖 |
| 内置夹具使用的数据库 | 内存 H2（`org.h2.Driver` + `org.hibernate.dialect.H2Dialect`） | `AbstractHibernateH2Test#buildConfiguration()` |
| Jakarta Persistence | `jakarta.persistence.*`（`@Entity`、`@Id`、`@Table` 等） | 实体源码 |
| 下列所有类型的 `@since` | `1.0.0` | Javadoc 标签 |

扩展基于 JUnit 5（`org.junit.jupiter.api.extension`）编程模型，因此测试要跑在 JUnit Platform 上（Maven Surefire
2.22+ 或 `junit-platform` launcher）。

## 3. API 说明

### 3.1 类型总览

| **类型** | **可见性** | **种类** | **用途** |
|----------|------------|----------|----------|
| `HibernateTest` | `public @interface`（`@Target(TYPE)`） | 注解 | 把测试类标记为 Hibernate 测试，并引入 `HibernateExtension` |
| `HibernateRuntime` | `public @interface`（`@Target({FIELD, PARAMETER})`） | 注解 | 标记需要注入 Hibernate 运行期组件的位置 |
| `HibernateExtension` | `public class` | JUnit 5 扩展 | 承担全部引导、注入与清理工作 |
| `AbstractHibernateTest` | 包级私有 `abstract class` | 测试基类 | 每个测试方法一套 `SessionFactory`/`Session`/`StatelessSession` 生命周期 + 模板钩子 |
| `AbstractHibernateH2Test` | `public abstract class` | 测试基类 | 预置 H2 配置的 `AbstractHibernateTest` 子类，自带 `@Nested` Session 测试 |
| `User`、`Order`、`Product`、`UserProfile` | `public class` | JPA 实体 | 可复用的示例实体图（`t_user`、`t_order`、`t_product`、`t_user_profile`） |

### 3.2 `@HibernateTest`

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

| **属性** | **类型** | **默认值** | **含义** |
|----------|----------|------------|----------|
| `properties` | `String[]` | `{}` | 以 `key=value` 形式给出的 Hibernate `Configuration` 设置，例如 `"hibernate.dialect=org.hibernate.dialect.H2Dialect"` |
| `scanEntityPackages` | `String[]` | `{}` | 要递归扫描的包，凡带 `jakarta.persistence.@Entity` 的类都会被 `Configuration#addAnnotatedClass(Class)` 注册 |

契约：

- `@ExtendWith(HibernateExtension.class)` 是元注解，因此你永远不需要自己再写 `@ExtendWith`。
- `@Inherited` + `@Documented`，只能标注在类型上。
- 标注之后你会得到：测试类执行前构建 `Configuration` 与 `SessionFactory`；每个测试方法前打开 `Session` 与
  `StatelessSession`、执行后关闭；测试类全部结束后关闭 `SessionFactory`。
- `properties` 中没有 `=`（或 `=` 位于下标 0）的条目会被**跳过并打警告**，而不是抛错。
- `scanEntityPackages` 使用线程上下文 `ClassLoader`；没有 `@Entity` 的类会被忽略。若某个实体不在被扫描的包里，请改用
  编程式引导（见 `AbstractHibernateH2Test`）自己注册它。

### 3.3 `@HibernateRuntime`

```java
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Documented
public @interface HibernateRuntime {
}
```

可注入类型与作用范围：

| **类型** | **实例字段** | **静态字段** | **方法参数** | **生命周期** |
|----------|--------------|--------------|--------------|--------------|
| `org.hibernate.cfg.Configuration` | 支持 | 支持 | 支持 | 每个测试类 |
| `org.hibernate.SessionFactory` | 支持 | 支持 | 支持 | 每个测试类 |
| `org.hibernate.Session` | 支持 | **不支持**（保持 `null`） | 支持 | 每个测试方法 |
| `org.hibernate.StatelessSession` | 支持 | **不支持**（保持 `null`） | 支持 | 每个测试方法 |

其它标了 `@HibernateRuntime` 的类型会被原样放过（字段保持既有值；不受支持的参数类型不会被该扩展解析）。

### 3.4 `HibernateExtension`

```java
public class HibernateExtension implements BeforeAllCallback, BeforeEachCallback,
        AfterEachCallback, AfterAllCallback, ParameterResolver
```

调试测试时需要了解的行为：

- **共享存储。** `Configuration` 与 `SessionFactory` 存放在*最外层*带 `@HibernateTest` 的上下文的 `Store` 里，
  命名空间为 `HibernateExtension`，键为 `configuration` / `sessionFactory`；`Session` / `StatelessSession` 存放在
  **方法级** `Store`，键为 `session` / `statelessSession`。
- **`@Nested` 内部类共用同一个 `SessionFactory`。** 内部类不会重新构建它：扩展会通过
  `findHibernateTestContext` 沿上下文层级向上查找，复用外层 `@HibernateTest` 类创建的实例。
- **静态字段 vs 实例字段。** `beforeAll` 只把 `Configuration`/`SessionFactory` 注入被标注的静态字段；
  `beforeEach` 会把四种类型都注入被标注的实例字段。字段收集会覆盖整个类层次（到 `Object` 之前为止），并通过
  `setAccessible(true)` 赋值。
- **参数解析。** `supportsParameter` 要求同时具备 `@HibernateRuntime` 与四种受支持类型之一；
  `resolveParameter` 会分别从类级 Store 或方法级 Store 读取。
- **清理。** `afterEach` 先关 `StatelessSession` 再关 `Session`，且只在 `isOpen()` 时关闭；关闭抛异常时只记录
  `warn` 而不让测试失败。`afterAll` 仅当正在结束的类本身就标了 `@HibernateTest` 时才关闭 `SessionFactory`，
  因此内部类不会提前把它关掉。
- **日志。** 创建/关闭事件用 `io.microsphere.logging.LoggerFactory` 的 `debug` 级别记录，属性格式错误与关闭失败
  用 `warn`。

### 3.5 `AbstractHibernateTest`

```java
abstract class AbstractHibernateTest {          // 包级私有！

    protected Configuration configuration;
    protected SessionFactory sessionFactory;
    protected Session session;
    protected StatelessSession statelessSession;

    @BeforeEach  final void setUp();
    @AfterEach   final void tearDown();

    protected abstract Configuration buildConfiguration();
    protected void customizeConfiguration(Configuration configuration) {}
    protected void init() {}
    protected void destroy() {}
}
```

- `setUp()` 是 `final` 的：构建 Configuration → `customizeConfiguration` → `buildSessionFactory()` →
  `openSession()` + `openStatelessSession()` → `init()`。`tearDown()` 同样是 `final`：`destroy()` → 关 session →
  关 statelessSession → 关 sessionFactory。
- **生命周期是“每个测试方法一次”** —— 每个 `@Test` 都会新建一套 `SessionFactory`。这是最省事的隔离方式，也正是
  大型测试套件不建议用它的原因。
- **可见性注意：** 该类本身是包级私有的，所以你**无法**在自己的包里继承 `AbstractHibernateTest`。请改为继承公共的
  `AbstractHibernateH2Test`，或使用 `@HibernateTest`；两种方式下 `protected` 字段都能通过继承访问到。

### 3.6 `AbstractHibernateH2Test`

```java
public abstract class AbstractHibernateH2Test extends AbstractHibernateTest
```

它既是基类，也是一套测试套件：内置的 `@Nested` 测试类会在**每个**子类中执行。

**夹具与配置**

| **成员** | **行为** |
|----------|----------|
| `protected User user` | 测试夹具，`init()` 在每个测试前创建 |
| `protected User createUser()` | 构造 `User("Tom")` + `UserProfile("ID123456")` + `Order("ORD001")` + `Product("Laptop")` 的实体图；可覆盖以替换 |
| `buildConfiguration()` | 注册四个实体，并设置 `org.h2.Driver`、`jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1`、`sa` / 空密码、`H2Dialect`、`show_sql=true`、`format_sql=true`、`hbm2ddl.auto=update` |
| `protected void doInAction(Runnable)` | 在 `assertDoesNotThrow` 中执行动作——用于非事务型操作 |

**继承而来的 `@Nested` “Session Test” 用例**（基于 `session`，写操作都包在事务里）：`testPersistAndGet`、
`testPersistAndMerge`、`testPersistAndRemove`、`testPersistAndReplicate`（`LATEST_VERSION`）、`testPersistAndFlush`、
`testPersistAndDetach`、`testPersistAndEvict`、`testPersistAndLock`（`LockMode.READ`）、`testPersistAndRefresh`、
`testPersistAndLoad`（`session.find`）、`testPersistAndGetManagedEntities`（`createQuery("FROM User", User.class)`）。

**继承而来的 `@Nested` “Stateless Session Test” 用例**（基于 `statelessSession`）：`testInsertAndGet`、
`testInsertAndDelete`、`testInsertAndUpdate`、`testInsertAndRefresh`、`testInsertAndFetch`。

两个内部类各自定义了 `doInTransaction(Runnable)` 辅助方法：在对应 session 上开启事务、执行动作、在
`assertDoesNotThrow` 内提交。

由于这些用例是继承来的，在子类里追加自己的 `@Test` 才是常规用法——`microsphere-hibernate-core` 的
`EntityCallbackIntegratorTest` 正是这么做的：一个空的子类，目的只是让继承来的流程在回调 SPI 生效的情况下跑起来。

### 3.7 示例实体

一个“小而完整”的图：一对一、一对多、多对多，主键统一使用 `IDENTITY`。

| **实体** | **表** | **字段** | **关联** |
|----------|--------|----------|----------|
| `User` | `t_user` | `Long id`（`@Id @GeneratedValue(IDENTITY)`）、`String name` | `@OneToOne(mappedBy = "user", cascade = ALL, fetch = LAZY) UserProfile profile`；`@OneToMany(mappedBy = "user", cascade = ALL, fetch = LAZY) List<Order> orders`（初始化为空 `ArrayList`） |
| `UserProfile` | `t_user_profile` | `Long id`、`String idCard` | `@OneToOne @JoinColumn(name = "user_id") User user`（拥有方） |
| `Order` | `t_order` | `Long id`、`String orderNo` | `@ManyToOne @JoinColumn(name = "user_id") User user`；`@ManyToMany(cascade = {PERSIST, MERGE}) @JoinTable(name = "t_order_product", joinColumns = order_id, inverseJoinColumns = product_id) Set<Product> products` |
| `Product` | `t_product` | `Long id`、`String productName` | `@ManyToMany(mappedBy = "products", fetch = LAZY) Set<Order> orders` |

测试所依赖的契约细节：

- `User#addOrder(Order)` 会维护双向关联（`orders.add(order); order.setUser(this);`）——请调用它，而不是直接改集合。
- `equals`/`hashCode` 基于**非关联**字段做值比较：`User(id, name)`、`Order(id, orderNo)`、`Product(id, productName)`、
  `UserProfile(id, idCard)`。因此 `assertEquals` 可以安全地比较游离实例与新载入的实例；并且它们刻意排除了集合，
  避免比较过程触发懒加载。
- 关联的级联策略：`User` 到 `profile`/`orders` 为 `cascade = ALL`，`Order` 到 `products` 为 `{PERSIST, MERGE}`，
  所以持久化一个 `User` 会级联持久化整张图。

## 4. 取自项目自身测试的示例

### 4.1 注解驱动的测试 —— `HibernateAnnotationTest`

出处：`microsphere-hibernate-test/src/test/java/io/microsphere/hibernate/test/HibernateAnnotationTest.java`

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

    @Nested
    class FieldTest {

        @HibernateRuntime private Configuration configuration;
        @HibernateRuntime private SessionFactory sessionFactory;
        @HibernateRuntime private Session session;
        @HibernateRuntime private StatelessSession statelessSession;

        @Test
        void test() {
            assertNotNull(configuration);
            assertNotNull(sessionFactory);
            assertNotNull(session);
            assertNotNull(statelessSession);
        }
    }

    @Nested
    class StaticFieldTest {

        @HibernateRuntime private static Configuration configuration;
        @HibernateRuntime private static SessionFactory sessionFactory;
        @HibernateRuntime private static Session session;
        @HibernateRuntime private static StatelessSession statelessSession;

        @Test
        void test() {
            assertNotNull(configuration);
            assertNotNull(sessionFactory);
            // Session 与 StatelessSession 不应被注入到静态字段
            assertNull(session);
            assertNull(statelessSession);
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

这是三种注入方式（实例字段、静态字段、方法参数）的参考实现，并且把“静态字段不注入 Session”这一限制写成了断言而不
是脚注。

### 4.2 继承 H2 基类 —— `HibernateH2Test`

出处：`microsphere-hibernate-test/src/test/java/io/microsphere/hibernate/test/HibernateH2Test.java`

```java
public class HibernateH2Test extends AbstractHibernateH2Test {

    @Override
    protected void customizeConfiguration(Configuration configuration) {
        super.customizeConfiguration(configuration);
    }
}
```

运行它会执行继承来的 `Session Test` 与 `Stateless Session Test` 两套用例。你自己追加测试的写法如下（形态取自
`AbstractHibernateH2Test` 的 Javadoc 示例）：

```java
class MyEntityTest extends AbstractHibernateH2Test {

    @Test
    void testPersistAndGet() {
        doInTransaction(() -> {
            session.persist(user);
            User found = session.get(User.class, user.getId());
            assertEquals(user, found);
        });
    }

    @Override
    protected User createUser() {
        User user = new User();
        user.setName("Alice");
        return user;
    }
}
```

（`doInTransaction` 定义在基类的两个 `@Nested` 内部类上；在你自己的子类里，请手写
`session.beginTransaction()` / `commit()`，或复用 `doInAction(...)`。）

### 4.3 在其他模块中使用本测试工具 —— `EntityCallbackIntegratorTest`

出处：`microsphere-hibernate-core/src/test/java/io/microsphere/hibernate/entity/EntityCallbackIntegratorTest.java`

```java
class EntityCallbackIntegratorTest extends AbstractHibernateH2Test {
}
```

不同的模块、不同的包、零测试代码：继承来的 H2 测试套件 + 来自
[microsphere-hibernate-core](microsphere-hibernate-core.md) 的 `EntityCallback` SPI 就已经足够了。想验证自己的
`EntityCallback` 实现时，请照抄这个模式。

### 4.4 针对实体做纯单元测试 —— `UserTest`

出处：`microsphere-hibernate-test/src/test/java/io/microsphere/hibernate/test/entity/UserTest.java`

```java
@Test
void testAddOrder() {
    User user = new User();
    Order order1 = new Order();
    Order order2 = new Order();

    user.addOrder(order1);
    user.addOrder(order2);

    assertEquals(2, user.getOrders().size());
    assertEquals(user, order1.getUser());
    assertEquals(user, order2.getUser());
}
```

`OrderTest`、`ProductTest`、`UserProfileTest` 遵循同一模板：默认构造断言、getter/setter 往返、`equals`/`hashCode`
契约校验（自反、对称、null、不同类型、不同 id）以及空字段行为。把这些当作“不启动 Hibernate 也能测实体 POJO”的模板。

## 5. 如何集成

### 5.1 使用方的最小配置

1. 以 `test` 作用域引入 `microsphere-hibernate-test`（版本由 [BOM](microsphere-hibernate-dependencies.md) 管理）。
2. 补齐你自己 `test` 作用域下的 `junit-jupiter`、`hibernate-core`、`h2`（或你的数据库驱动）与
   `microsphere-java-core` —— 见第 1 节。
3. 二选一：
   - 在普通类上使用 `@HibernateTest` + `@HibernateRuntime`（推荐：每个测试类一套 `SessionFactory`）；或
   - 继承 `AbstractHibernateH2Test`，享受现成的 H2 实体图与继承用例（代价是每个测试方法新建一套 `SessionFactory`）。
4. 确保实体可被发现：写进 `scanEntityPackages`，或在 `buildConfiguration()` / `customizeConfiguration(...)` 里注册。
5. 若要断言 `EntityCallback` 行为，就配合 `microsphere-hibernate-core` 使用：把回调登记到
   `META-INF/services/io.microsphere.hibernate.entity.EntityCallback` 即可让继承用例触发它。

### 5.2 你会用到的属性键

`@HibernateTest(properties = "key=value")` 接受任意标准 Hibernate 设置；本项目里用到的有：

| **键** | **示例值** | **出现位置** |
|--------|------------|--------------|
| `hibernate.connection.driver_class` | `org.h2.Driver` | `HibernateAnnotationTest`、`AbstractHibernateH2Test` |
| `hibernate.connection.url` | `jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1` | 同上 |
| `hibernate.connection.username` / `.password` | `sa` / `""` | 同上 |
| `hibernate.dialect` | `org.hibernate.dialect.H2Dialect` | 同上 |
| `hibernate.show_sql` / `hibernate.format_sql` | `true` / `true` | `AbstractHibernateH2Test` |
| `hibernate.hbm2ddl.auto` | `update` | 两处都有，这样无需迁移脚本就能建表 |

### 5.3 隔离性与性能建议

- 保留 `DB_CLOSE_DELAY=-1` 时，请给每个测试类用**各自的**内存库名（`jdbc:h2:mem:hibernate_annotation_test_db`、
  `jdbc:h2:mem:test_db` …），否则多个类会通过同一 schema 共享数据。
- `hbm2ddl.auto=update` 很方便，但它永远不会删除列；对 schema 有严格要求时请用 `create-drop`。
- 继承 `AbstractHibernateH2Test` 会带出 16 个继承用例。这是刻意的（它同时充当 `microsphere-hibernate-core` 的集成
  测试载体），但它不是轻量单元测试——只需要实体语义时，请选用 `@HibernateTest` 或纯 POJO 测试。
- `HibernateExtension` 会防御性地关闭资源，并在失败时记 `warn` 而非抛异常，所以 session 泄漏会体现在日志里，而不是
  让某个不相干的断言失败。

### 5.4 故障排查

| **现象** | **原因与处理** |
|----------|----------------|
| 标了 `@HibernateRuntime` 的字段仍是 `null` | 类上没有 `@HibernateTest`；或者字段类型不属于那四种受支持类型；或者它是 `static` 的 `Session`/`StatelessSession` 字段（设计上就不注入）。 |
| 实体始终没被注册（引导期 `org.hibernate.MappingException` / unknown entity） | `scanEntityPackages` 指定的包在线程上下文 classpath 上没有 `@Entity` 标注的类——检查包名，以及类是否已编译进 `target/test-classes` / `target/classes`。 |
| 你写的属性被静默忽略 | `properties` 必须是 `key=value`；没有合法分隔符的条目会打 `warn` 日志后被跳过。 |
| `ClassNotFoundException: io.microsphere.io.scanner.SimpleClassScanner` | `microsphere-java-core` 是 optional，需要加到 test 作用域。 |
| `NoClassDefFoundError: org/hibernate/…` 或 `org/junit/jupiter/…` | 本模块中 `hibernate-core` / `junit-jupiter` 是 optional，需要你自己声明。 |
| 第二个测试类看到脏数据 | 两个类共用了同一个 `jdbc:h2:mem:<name>` —— 见 5.3。 |
| 编译期提示 `AbstractHibernateTest` 不可见 | 符合预期：该类是包级私有的。请改为继承 `AbstractHibernateH2Test` 或使用 `@HibernateTest`。 |

## 6. 相关文档

- [microsphere-hibernate-core.md](microsphere-hibernate-core.md) —— 这些测试所驱动的 `EntityCallback` 机制
- [microsphere-hibernate-dependencies.md](microsphere-hibernate-dependencies.md) —— 本构件的版本锁定方式
- [microsphere-hibernate-parent.md](microsphere-hibernate-parent.md) —— 选择 Hibernate 版本的 profile
