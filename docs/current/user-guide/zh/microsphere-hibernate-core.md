# microsphere-hibernate-core

Microsphere Hibernate 的核心模块。它把 Hibernate ORM 二十多个事件监听接口收敛为一个通过 `ServiceLoader` 发现的回调
接口：**`EntityCallback`**。

[← 返回用户文档索引](README.md) · 英文版本：[../microsphere-hibernate-core.md](../microsphere-hibernate-core.md)

## 1. 模块定位与 Maven 坐标

| **项目** | **取值** |
|----------|----------|
| `groupId` | `io.github.microsphere-projects` |
| `artifactId` | `microsphere-hibernate-core` |
| `version` | `${revision}` → `release` 分支上为 `0.2.11-SNAPSHOT`（最近发布版本 `0.2.10`） |
| `packaging` | `jar` |
| `name` | `Microsphere :: Hibernate :: Core` |
| 根包 | `io.microsphere.hibernate`（`entity` + `constants`） |
| 父工程 | [microsphere-hibernate-parent](microsphere-hibernate-parent.md) |
| 声明的依赖 | `microsphere-annotation-processor`、`microsphere-java-core`、`org.hibernate.orm:hibernate-core` —— 全部为 **optional** |

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-hibernate-core</artifactId>
</dependency>
```

由于这三个编译期依赖都是 `optional`，你的应用必须自己声明所需的那些：

```xml
<!-- 运行期必需：本模块不会带入 Hibernate ORM -->
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>7.3.4.Final</version>
</dependency>
<!-- 运行期必需：代码里用到了 io.microsphere.collection.ListUtils / io.microsphere.lang.Prioritized -->
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-core</artifactId>
    <version>0.3.19</version>
</dependency>
```

`microsphere-annotation-processor` 只有在需要为 `PropertyConstants` 中标了 `@ConfigurationProperty` 的常量生成
配置元数据时，才需要在编译期引入。

模块自身 `pom.xml` 中声明的测试依赖：`junit-jupiter`、`mockito-core`、`mockito-junit-jupiter`、`h2`、
`logback-classic` 以及 `microsphere-hibernate-test`。

## 2. 版本信息与兼容范围

| **维度** | **取值 / 范围** | **`release` 分支中的依据** |
|----------|-----------------|-----------------------------|
| 版本线 | `0.2.x`；已发布 `0.2.10`，开发中 `0.2.11-SNAPSHOT` | 根 POM 中 `<revision>0.2.11-SNAPSHOT</revision>` |
| JDK | 17 – 25（基线 `17`） | `<java.version>17</java.version>`；CI 矩阵 `17`、`21`、`25` |
| Hibernate ORM | 6.2.x – 7.3.x | `hibernate-6.2` … `hibernate-7.3` profile，默认 `7.3.4.Final` |
| Spring Cloud | 2022.0.x – 2025.0.x | 继承的 `spring-cloud-*` profile（`microsphere-spring-cloud-parent:0.2.26`） |
| 使用的 Hibernate 构件 | `org.hibernate.orm:hibernate-core`、`hibernate-platform` | `microsphere-hibernate-parent/pom.xml` |
| 下列所有类型的 `@since` | `1.0.0` | Javadoc 标签 |

Hibernate 6.x 基于 Jakarta（`jakarta.persistence.*`），这正是 0.2.x 版本线无法用于 Hibernate 5.x 的原因——
Hibernate 5.x 请使用 `0.1.x` 版本线。

## 3. API 说明

### 3.1 类型总览

| **类型** | **包** | **可见性** | **种类** | **职责** |
|----------|--------|------------|----------|----------|
| `EntityCallback` | `io.microsphere.hibernate.entity` | `public` | 接口 | 你要实现的回调契约 |
| `CompositeEntityCallback` | `io.microsphere.hibernate.entity` | `public` | 类 | 把一次回调调用按顺序分发给一组回调 |
| `EntittyCallbackIntegrator` | `io.microsphere.hibernate.entity` | `public` | 类（`Integrator`） | Hibernate SPI 入口，负责注册监听器 |
| `EntityCallbackListener` | `io.microsphere.hibernate.entity` | 包级私有 | 类（实现 23 个 `*EventListener`） | 把 Hibernate 事件翻译成 `EntityCallback` 调用 |
| `EntityCallbackListenerDuplicationStrategy` | `io.microsphere.hibernate.entity` | 包级私有 | 类（`DuplicationStrategy`） | 保证每个 `SessionFactory` 只有一个监听器实例 |
| `PropertyConstants` | `io.microsphere.hibernate.constants` | `public` | 接口 | 配置属性名常量 |

> `EntittyCallbackIntegrator` 的类名里有一个拼写错误（`Entitty`）。它就是已发布并写进 SPI 注册文件的名字，手工引用
> 时必须一字不差。

### 3.2 `EntityCallback`

`public interface EntityCallback` —— 注释原文：“the lifecycle callback interface for entity; the implementation
class will be loaded by `ServiceLoader` (Java Services Loader)”。

**契约**

- 所有方法都是 `default` 空实现，实现类只需覆盖自己关心的那几个。
- 实现类通过 `ServiceLoader.load(EntityCallback.class)` 发现——必须把它登记在
  `META-INF/services/io.microsphere.hibernate.entity.EntityCallback` 中，并提供公共无参构造器。
- 实现类应当线程安全且尽量轻量：回调发生在 Hibernate 事件处理链路里，运行在执行持久化操作的线程上。
- 带 `Object[] state`、`String[] propertyNames`、`Type[] types` 参数的方法，Javadoc 明确写了
  *“不建议拦截器修改 state”*，请把这些数组当作只读。
- 回调不会否决（veto）Hibernate 的操作（见下文 `EntityCallbackListener`）。
- 多个实现之间的调用顺序通过实现 `io.microsphere.lang.Prioritized#getPriority()` 控制；在装配成组合回调之前，
  列表会用 `Prioritized.COMPARATOR` 排序。

**回调方法**

*有状态 Session 语义*

| **方法** | **触发时机** | **关键参数** |
|----------|--------------|--------------|
| `onLoad(Object id, String entityClassName, Object entity, LockMode lockMode, boolean isAssociationFetch, Object result, Boolean readOnly, LoadType loadType)` | `Session#find(Class, Object)` / `LOAD` 事件 | 请求的 id、实体类名、正在加载的实例、解析出的 `LockMode`、是否来自关联抓取、事件结果、只读标志以及 `LoadEventListener.LoadType` |
| `onPersist(Object entity, String entityName)` | `Session#persist(Object)` | 实体与其实体名 |
| `onDelete(Object entity, boolean cascadeDeleteEnabled, boolean orphanRemovalBeforeUpdates)` | `Session#remove(Object)` | 实体 + 级联删除 / 孤儿移除标志 |
| `onMerge(Object original, Object id, Object entity, String entityName, Object result)` | `Session#merge(Object)` | 游离源对象、请求的 id、受管实体、实体名、merge 结果 |
| `onReplicate(Object entity, ReplicationMode replicationMode, String entityName)` | `Session#replicate(Object, ReplicationMode)` | 实体、`ReplicationMode`、实体名 |
| `onFlushDirty(Object entity, Object id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types)` | `Session#flush()` 中检测到脏实体 | 当前状态与载入时状态、属性名与类型 |
| `onEvict(Object entity)` | `Session#evict(Object)` | 被剔除的实体 |
| `onLock(Object entity, LockMode lockMode, String entityName)` | `Session#lock(Object, LockMode)` | 实体与请求的 `LockMode` |
| `onRefresh(Object entity, String entityName, LockMode lockMode)` | `Session#refresh(Object)` | 实体、实体名、`LockMode` |

*无状态 Session / pre-post 语义*

| **方法** | **触发时机** |
|----------|--------------|
| `onPreLoad(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types)` | 在把属性值写入新加载实例之前（`StatelessSession#get`） |
| `onPostLoad(Object entity, Object id, String[] propertyNames, Type[] types)` | 实例完成初始化之后 |
| `onPreDelete` / `onPostDelete(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types)` | `StatelessSession#delete(Object)` 前后 |
| `onPreInsert` / `onPostInsert(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes)` | `StatelessSession#insert(Object)` 前后 |
| `onPreUpdate` / `onPostUpdate(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes)` | `StatelessSession#update(Object)` 前后 |

*集合语义*（参数一律为 `(Object collection, Object key)`）

| **方法** | **含义** |
|----------|----------|
| `onPreRecreateCollection` / `onPostRecreateCollection` | 集合被（重新）创建前 / 后 |
| `onPreCollectionRemove` / `onPostRemoveCollection` | 集合被删除前 / 后 |
| `onPreUpdateCollection` / `onPostUpdateCollection` | 集合被更新前 / 后 |

### 3.3 `CompositeEntityCallback`

`public class CompositeEntityCallback implements EntityCallback`

```java
public CompositeEntityCallback(List<EntityCallback> callbacks)
```

- 按传入 `List` 的顺序持有这些回调。
- 覆盖了 `EntityCallback` 的每个方法，并用
  `io.microsphere.collection.ListUtils.forEach(callbacks, callback -> callback.xxx(...))` 分发，也就是说某个回调
  抛异常会中断该事件上后续回调的执行。
- 它**不会**再按优先级排序——排序只在 `EntityCallbackListener` 的构造器里做一次。
- 当你需要自己驱动回调链时（单元测试、构建在其上的框架）非常有用。

### 3.4 `EntittyCallbackIntegrator`

`public class EntittyCallbackIntegrator implements org.hibernate.integrator.spi.Integrator`

它通过 JAR 自带的 SPI 注册文件生效，普通应用无需任何显式装配。

```java
// Hibernate 在 6.2 – 7.3 上调用的、基于 BootstrapContext 的入口：
@Override
public void integrate(Metadata metadata, BootstrapContext bootstrapContext,
                      SessionFactoryImplementor sessionFactory) {
    integrate(metadata, sessionFactory, (SessionFactoryServiceRegistry) sessionFactory.getServiceRegistry());
}

// 保留为公共方法的旧三参数形式，便于直接调用：
public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactory,
                      SessionFactoryServiceRegistry serviceRegistry) { ... }
```

执行顺序：

1. 从 `SessionFactoryServiceRegistry` 取得 `EventListenerRegistry`。
2. `eventListenerRegistry.addDuplicationStrategy(new EntityCallbackListenerDuplicationStrategy())`。
3. 创建**一个** `EntityCallbackListener`，并把它 append 到所有受支持的 `EventType` 上。
4. `disintegrate(...)` 是空实现——清理工作交给 `SessionFactory` 关闭流程。

### 3.5 `EntityCallbackListener`（包级私有）

你无法在自己的代码里引用这个类型；之所以要写进文档，是因为 `EntityCallback` 的可观察行为正是由它定义的。

- 实现了 23 个相关的 Hibernate 监听接口：`LoadEventListener`、`PersistEventListener`、`MergeEventListener`、
  `DeleteEventListener`、`ReplicateEventListener`、`FlushEntityEventListener`、`EvictEventListener`、
  `LockEventListener`、`RefreshEventListener`、`PreLoadEventListener`、`PostLoadEventListener`、
  `PreDeleteEventListener`、`PostDeleteEventListener`、`PreUpdateEventListener`、`PostUpdateEventListener`、
  `PreInsertEventListener`、`PostInsertEventListener`、`PreCollectionRecreateEventListener`、
  `PostCollectionRecreateEventListener`、`PreCollectionRemoveEventListener`、`PostCollectionRemoveEventListener`、
  `PreCollectionUpdateEventListener`、`PostCollectionUpdateEventListener`。
- 构造器是包级私有的：`EntityCallbackListener()`（通过 `ServiceLoader.load(EntityCallback.class)` 加载）和
  `EntityCallbackListener(Iterable<EntityCallback>)`（用 `ListUtils.newLinkedList` 拷成 `LinkedList`，按
  `Prioritized.COMPARATOR` 排序，再包成 `CompositeEntityCallback`）。
- 事件到回调的映射是一一对应的；`LockMode` 统一通过 `protected LockMode getLockMode(LockOptions)` 这个钩子从
  `LockOptions` 中提取。
- `onPreDelete`、`onPreInsert`、`onPreUpdate` 返回 `false`，即它们从不否决 Hibernate 的操作。
- `requiresPostCommitHandling(EntityPersister)` 返回 `false`，因此 post-insert / post-update / post-delete 回调在
  常规的后置阶段执行，而不是等到提交之后。

**该 integrator 注册的事件类型**

| **`EventType`** | **对应的 `EntityCallback` 方法** |
|-----------------|----------------------------------|
| `LOAD` | `onLoad` |
| `PERSIST` | `onPersist` |
| `MERGE` | `onMerge` |
| `DELETE` | `onDelete` |
| `REPLICATE` | `onReplicate` |
| `FLUSH_ENTITY` | `onFlushDirty` |
| `EVICT` | `onEvict` |
| `LOCK` | `onLock` |
| `REFRESH` | `onRefresh` |
| `PRE_LOAD` / `POST_LOAD` | `onPreLoad` / `onPostLoad` |
| `PRE_DELETE` / `POST_DELETE` | `onPreDelete` / `onPostDelete` |
| `PRE_UPDATE` / `POST_UPDATE` | `onPreUpdate` / `onPostUpdate` |
| `PRE_INSERT` / `POST_INSERT` | `onPreInsert` / `onPostInsert` |
| `PRE_COLLECTION_RECREATE` / `POST_COLLECTION_RECREATE` | `onPreRecreateCollection` / `onPostRecreateCollection` |
| `PRE_COLLECTION_REMOVE` / `POST_COLLECTION_REMOVE` | `onPreCollectionRemove` / `onPostRemoveCollection` |
| `PRE_COLLECTION_UPDATE` / `POST_COLLECTION_UPDATE` | `onPreUpdateCollection` / `onPostUpdateCollection` |

### 3.6 `EntityCallbackListenerDuplicationStrategy`（包级私有）

实现 `org.hibernate.event.service.spi.DuplicationStrategy`：

```java
@Override
public boolean areMatch(Object listener, Object original) {
    Class<?> listenerClass = listener.getClass();
    return Objects.equals(listenerClass, original.getClass())
            && EntityCallbackListener.class.equals(listenerClass);
}

@Override
public Action getAction() {
    return KEEP_ORIGINAL;
}
```

- 只有当两个对象都恰好是 `EntityCallbackListener` 实例时，`areMatch` 才返回 `true`。
- `getAction()` 返回 `KEEP_ORIGINAL`：当 integrator 被执行多次（例如同一 JVM 中多次引导 `SessionFactory`，或者服务
  被重复注册），已注册的监听器会被保留、新来的被丢弃，因此一个事件不会触发两次回调。
- 由 `EntityCallbackListenerDuplicationStrategyTest`（核心模块 `src/test`）验证：
  `areMatch(listener, listener) == true`、与 Hibernate 自带 `DefaultLoadEventListener` 比较时为 `false`、
  `getAction() == KEEP_ORIGINAL`。

### 3.7 `PropertyConstants`

`io.microsphere.hibernate.constants` 包下的 `public interface PropertyConstants` —— “the interface to declare the
property constants of Microsphere Hibernate”：

| **常量** | **取值** | **说明** |
|----------|----------|----------|
| `MICROSPHERE_HIBERNATE_PROPERTY_NAME_PREFIX` | `microsphere.hibernate.` | 由 `microsphere.` + `hibernate` + `.` 拼出 |
| `DEFAULT_MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_VALUE` | `"true"` | 字符串形式 |
| `DEFAULT_MICROSPHERE_HIBERNATE_ENABLED` | `true` | 上一行经 `Boolean.parseBoolean` 得到 |
| `MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_NAME` | `microsphere.hibernate.enabled` | 标注了 `@ConfigurationProperty(type = boolean.class, defaultValue = "true", source = SYSTEM_PROPERTIES_SOURCE)` |

`@ConfigurationProperty`（`io.microsphere.annotation`）正是 `microsphere-annotation-processor` 用来生成配置元数据
的注解。

> **0.2.x 的现状：** `microsphere.hibernate.enabled` 这个键只是声明和文档化了，`EntityCallback` 的 integrator
> 并**不会**去读取它——只要 JAR 在 classpath 上，integrator 就会注册。因此请把它当作命名约定（以及未来的扩展点），
> 而不是一个真正可用的开关：想关闭该特性，请删除 `META-INF/services/io.microsphere.hibernate.entity.EntityCallback`
> 文件，或直接去掉 `microsphere-hibernate-core` 依赖。

## 4. 取自项目自身测试的示例

### 4.1 一个完整的回调实现 —— `TestEntityCallback`

出处：`microsphere-hibernate-core/src/test/java/io/microsphere/hibernate/entity/TestEntityCallback.java`
（通过 `microsphere-hibernate-core/src/test/resources/META-INF/services/io.microsphere.hibernate.entity.EntityCallback`
注册，该文件内容只有一行 `io.microsphere.hibernate.entity.TestEntityCallback`）。

```java
public class TestEntityCallback implements EntityCallback {

    private static final Logger logger = getLogger(TestEntityCallback.class);

    @Override
    public void onLoad(Object id, String entityClassName, Object entity, LockMode lockMode,
                       boolean isAssociationFetch, Object result, Boolean readOnly,
                       LoadEventListener.LoadType loadType) {
        logger.trace("onLoad - id : {}, entityClassName : {}, entity : {}, lockMode : {}, "
                        + "isAssociationFetch : {}, result : {}, readOnly : {}, loadType : {}",
                id, entityClassName, entity, lockMode, isAssociationFetch, result, readOnly, loadType);
        EntityCallback.super.onLoad(id, entityClassName, entity, lockMode, isAssociationFetch,
                result, readOnly, loadType);
    }

    @Override
    public void onPersist(Object entity, String entityName) {
        logger.trace("onPersist - entity : {}, entityName : {}", entity, entityName);
        EntityCallback.super.onPersist(entity, entityName);
    }

    // …… 每个回调方法都有对应的覆盖实现，并显式调用 EntityCallback.super
}
```

从这个参考实现值得借鉴两点：日志走 `io.microsphere.logging`（与本模块内部使用的日志门面一致）；显式调用
`EntityCallback.super.onXxx(...)`，这样即便接口默认实现将来变化，子类也能看清行为来源。

### 4.2 端到端集成测试 —— `EntityCallbackIntegratorTest`

出处：`microsphere-hibernate-core/src/test/java/io/microsphere/hibernate/entity/EntityCallbackIntegratorTest.java`

```java
class EntityCallbackIntegratorTest extends AbstractHibernateH2Test {
}
```

这个测试“空”是刻意的：继承
[AbstractHibernateH2Test](microsphere-hibernate-test.md) 会引导出一个 H2 `SessionFactory`，而由于测试 classpath 上
存在 `microsphere-hibernate-core`，Hibernate 会从
`META-INF/services/org.hibernate.integrator.spi.Integrator` 加载 `EntittyCallbackIntegrator`。于是所有继承而来的
`Session` / `StatelessSession` 用例都会走一遍完整回调链——`TestEntityCallback` 打出的 trace 日志就是它的断言输出。
要想看到这些日志，请在 Logback 配置里把日志级别调到 `TRACE`（本模块的测试依赖 `logback-classic`）：

```xml
<!-- src/test/resources/logback-test.xml -->
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    <logger name="io.microsphere.hibernate" level="TRACE"/>
    <root level="INFO">
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>
```

在你自己的项目里复现同样的装配方式：继承 `AbstractHibernateH2Test`（或用任意 Hibernate 引导方式）+ 你自己的
`EntityCallback` 服务注册文件。

### 4.3 自己组装回调链 —— `CompositeEntityCallback`

```java
EntityCallback logging = new LoggingEntityCallback();
EntityCallback audit = new AuditEntityCallback();
CompositeEntityCallback composite = new CompositeEntityCallback(Arrays.asList(logging, audit));

composite.onPersist(myEntity, "MyEntity");   // 两个回调都会执行，按 List 顺序
```

（形态取自 `CompositeEntityCallback` 的 Javadoc 示例。）

### 4.4 对常量做断言 —— `PropertyConstantsTest`

出处：`microsphere-hibernate-core/src/test/java/io/microsphere/hibernate/constants/PropertyConstantsTest.java`

```java
@Test
void test() {
    assertEquals("microsphere.hibernate.", MICROSPHERE_HIBERNATE_PROPERTY_NAME_PREFIX);
    assertEquals("true", DEFAULT_MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_VALUE);
    assertTrue(DEFAULT_MICROSPHERE_HIBERNATE_ENABLED);
    assertEquals("microsphere.hibernate.enabled", MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_NAME);
}
```

## 5. 如何集成

### 5.1 两个 SPI 文件

| **文件** | **由谁提供** | **内容** |
|----------|--------------|----------|
| `META-INF/services/org.hibernate.integrator.spi.Integrator` | `microsphere-hibernate-core`（JAR 内已带） | `io.microsphere.hibernate.entity.EntittyCallbackIntegrator` |
| `META-INF/services/io.microsphere.hibernate.entity.EntityCallback` | **你的**项目（`src/main/resources`） | 每行一个实现类全限定名 |

正因为 JAR 自带了 integrator 的注册文件，加上依赖就足以让 Hibernate 发现这个特性；你需要自己添加的只有
`EntityCallback` 注册文件：

```
# src/main/resources/META-INF/services/io.microsphere.hibernate.entity.EntityCallback
com.acme.order.logging.OrderEntityCallback
com.acme.order.audit.AuditEntityCallback
```

要点：

- 列出的每个类都必须实现 `EntityCallback`，并且能用公共无参构造器实例化。
- classpath 顺序决定了 `ServiceLoader` 的顺序；需要确定性的执行次序时，请实现 `Prioritized`，不要依赖文件里的行序。
- 在 GraalVM native-image 或自定义 `ClassLoader` 环境下，要保证两个服务文件对加载 `EntityCallback` 的类加载器可见
  （`ServiceLoader.load` 使用线程上下文 / 调用方类加载器）。

### 5.2 配置属性

本特性没有“开/关”型的 Hibernate 属性。你能用的是 Hibernate 的标准属性——参考
[AbstractHibernateH2Test](microsphere-hibernate-test.md) 中的示例配置以及
`@HibernateTest(properties = …)` 用法。项目自定义的键在 3.7 节，同时说明了它当前不生效的事实。

### 5.3 验证回调是否被触发

```java
// 任意一种既有 Hibernate 引导方式都可以，回调会被自动接管
SessionFactory sessionFactory = new Configuration()
        .addAnnotatedClass(User.class)
        .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
        .setProperty("hibernate.connection.url", "jdbc:h2:mem:verify;DB_CLOSE_DELAY=-1")
        .setProperty("hibernate.hbm2ddl.auto", "update")
        .buildSessionFactory();

try (Session session = sessionFactory.openSession()) {
    Transaction tx = session.beginTransaction();
    session.persist(new User());          // -> onPersist，另外还有 pre/post insert 回调
    tx.commit();
}
sessionFactory.close();
```

### 5.4 故障排查

| **现象** | **原因与处理** |
|----------|----------------|
| 完全没有回调 | 运行时 classpath 缺少 `microsphere-hibernate-core`（或缺少它的 `Integrator` 服务文件）；或者你的 `EntityCallback` 文件没有放在某个 classpath 条目的 `META-INF/services/` 下；或者类名 / 包名写错。注意 `EntittyCallbackIntegrator` 这个带拼写错误的名字就是准确名称。 |
| `NoClassDefFoundError: io/microsphere/collection/ListUtils` 或 `io/microsphere/lang/Prioritized` | 本模块中 `microsphere-java-core` 是 optional，需要显式添加。 |
| `ClassNotFoundException: org.hibernate…` | 本模块中 `hibernate-core` 是 optional，需要显式添加，版本落在 6.2.x–7.3.x。 |
| 回调被触发多次 | 正常情况下不会：`EntityCallbackListenerDuplicationStrategy` 会保留原有监听器。若出现了，多半是你自己又注册了一遍 `EntityCallbackListener`，或者 classpath 上有两份内容不同的 JAR。 |
| 抛 `UnsupportedOperationException`，或 state 数组被下游修改 | 你修改了 `Object[] state` / `previousState`；契约要求把它们视为只读。 |
| 一个回调抛出的堆栈“吞掉”了其他回调 | `CompositeEntityCallback` 按序遍历且不捕获异常——请在自己的回调方法内部做好保护。 |

## 6. 相关文档

- [microsphere-hibernate-test.md](microsphere-hibernate-test.md) —— `EntityCallbackIntegratorTest` 所使用的
  `SessionFactory` 是如何被引导起来的
- [microsphere-hibernate-parent.md](microsphere-hibernate-parent.md) —— `hibernate-core` 版本的来源
- [microsphere-hibernate-dependencies.md](microsphere-hibernate-dependencies.md) —— BOM
