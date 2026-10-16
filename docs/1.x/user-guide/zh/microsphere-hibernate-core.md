# microsphere-hibernate-core（1.x）

[English](../microsphere-hibernate-core.md) · [中文](microsphere-hibernate-core.md)

## 1. 模块定位与 Maven 坐标

`microsphere-hibernate-core` 是 Microsphere Hibernate 1.x 的运行期模块，提供三项能力：

1. 基于 **Java `ServiceLoader` 的实体生命周期回调模型**（`EntityCallback`），由一个 `Integrator` 桥接到
   Hibernate ORM 5 的事件体系，因此你完全不需要编写 `EventListener` 样板代码。
2. **`DelegatingInterceptor`** —— `org.hibernate.Interceptor` 的 null 安全装饰器（仅存在于 1.x 线，0.2.x 已移除）。
3. **`PropertyConstants`** —— `microsphere.hibernate.*` 配置键命名空间。

| **项目** | **取值** |
|---|---|
| GroupId | `io.github.microsphere-projects` |
| ArtifactId | `microsphere-hibernate-core` |
| 版本 | `${revision}` → `release-1.x` 上为 `0.1.11-SNAPSHOT`；最新发布版本 `0.1.10` |
| 打包 | `jar` |
| Name | `Microsphere :: Hibernate :: Core` |
| POM 文件 | `microsphere-hibernate-core/pom.xml` |
| Java 包 | `io.microsphere.hibernate`、`io.microsphere.hibernate.entity`、`io.microsphere.hibernate.constants` |
| JavaDoc | https://javadoc.io/doc/io.github.microsphere-projects/microsphere-hibernate-core |

> 本模块所有类的 Javadoc 都标注 `@since 1.0.0`，指的是 **1.x 这条版本线**，而不是 Maven 的 `1.0.0` 版本。
> 本线发布的 Maven 版本形如 `0.1.x`。

## 2. 版本信息与兼容范围

| **维度** | **取值** |
|---|---|
| 版本线 | `0.1.x`，分支 `release-1.x`，最新发布版本 `0.1.10` |
| JDK | 8 – 25 |
| Hibernate Core | **5.0.x – 5.6.x**（`org.hibernate:hibernate-core`，默认构建为 `5.6.15.Final`） |
| Spring Cloud | Hoxton – 2021.0.x（`microsphere-spring-cloud` `0.1.26` 线） |
| 持久化注解 | `javax.persistence` |
| 不兼容 | Hibernate ORM **6.x/7.x** —— 本模块使用的 API 面（带 `Serializable id` 与 `EntityMode` 的 `Interceptor`、三参数的 `Integrator#integrate`、位于 `org.hibernate.event.spi` 的 `EventType` 常量）在那边已不存在 |

### 构建/测试依赖关系（摘自 `microsphere-hibernate-core/pom.xml`）

| **依赖** | **作用域** | **说明** |
|---|---|---|
| `io.github.microsphere-projects:microsphere-annotation-processor` | `optional` | 为 `@ConfigurationProperty`（`PropertyConstants` 上用到）生成配置元数据 |
| `io.github.microsphere-projects:microsphere-java-core` | `optional` | 提供 `ListUtils`、`Prioritized`、`DelegatingWrapper`、`SymbolConstants`、`io.microsphere.logging` |
| `org.hibernate:hibernate-core` | `optional` | 版本由 parent 中激活的 `hibernate-5.x` profile 决定 |
| `javax.xml.bind:jaxb-api` | `optional`，`java9+` profile（`[9,)`） | Java 9 起 JDK 不再内置 JAXB，而 Hibernate 5 仍需要它 |
| `org.junit.jupiter:junit-jupiter`、`org.mockito:mockito-core`、`org.mockito:mockito-junit-jupiter` | `test` | 供 `DelegatingInterceptorTest`、`EntityCallbackListenerDuplicationStrategyTest`、`PropertyConstantsTest` 使用 |
| `io.github.microsphere-projects:microsphere-hibernate-test` | `test`（`${revision}`） | 供 `EntityCallbackIntegratorTest` 使用 |
| `com.h2database:h2`、`ch.qos.logback:logback-classic` | `test` | integrator 测试所用的内存数据库与日志实现 |

**`optional` 的后果：** 不会传递引入任何依赖。应用必须自己声明 `org.hibernate:hibernate-core` 与
`io.github.microsphere-projects:microsphere-java-core`（版本可交由 BOM 管理，见
[microsphere-hibernate-dependencies.md](microsphere-hibernate-dependencies.md)）。

## 3. 工作机制

```
Hibernate ORM 5 SessionFactory 构建过程
        │
        │  ServiceLoader 发现 META-INF/services/org.hibernate.integrator.spi.Integrator
        ▼
EntittyCallbackIntegrator                      （随 microsphere-hibernate-core.jar 一起发布）
        │  注册 EntityCallbackListenerDuplicationStrategy
        │  把同一个 EntityCallbackListener 实例 append 到 23 个 EventType 上
        ▼
EntityCallbackListener（包级私有）              实现 23 个 Hibernate *EventListener 接口
        │  把每个 Hibernate 事件翻译成一次 EntityCallback 调用
        ▼
CompositeEntityCallback                        把调用按顺序分发给每一个被发现的回调
        ▲
        │  java.util.ServiceLoader.load(EntityCallback.class)
META-INF/services/io.microsphere.hibernate.entity.EntityCallback   <-- 你的应用在这里注册
```

无需在 `Configuration`、`SessionFactoryBuilder`、Spring 或 `persistence.xml` 中做任何装配：把 jar 放到 classpath
即启用集成。

## 4. API 说明

### 4.1 `EntityCallback`

`io.microsphere.hibernate.entity.EntityCallback`

> The lifecycle callback interface for entity. The implementation class will be loaded by `ServiceLoader`.

- **用途：** 一个与 Hibernate API 解耦（但类型仍来自 Hibernate）的统一接口，接收 `SessionFactory` 的每一次
  实体/集合生命周期事件。
- **契约：** **23** 个方法全部是 `default` 空实现，实现类只覆盖自己关心的部分。接口不要求任何状态；实例由 JDK 的
  `ServiceLoader` 创建，因此必须有 public 无参构造器。
- **发现方式：** 服务文件名固定为
  `META-INF/services/io.microsphere.hibernate.entity.EntityCallback`。

#### 4.1.1 方法清单

| **回调方法** | **Hibernate 监听器** | **`EventType`** | **典型触发点** |
|---|---|---|---|
| `onLoad(Object id, String entityClassName, Object entity, LockMode lockMode, boolean isAssociationFetch, Object result, LoadType loadType)` | `LoadEventListener` | `LOAD` | `Session#get`、`Session#load`、`Session#find`、隐式关联加载 |
| `onPersist(Object entity, String entityName)` | `PersistEventListener` | `PERSIST` | `Session#persist` |
| `onDelete(Object entity, boolean cascadeDeleteEnabled, boolean orphanRemovalBeforeUpdates)` | `DeleteEventListener` | `DELETE` | `Session#delete` / `Session#remove` |
| `onMerge(Object original, Object id, Object entity, String entityName, Object result)` | `MergeEventListener` | `MERGE` | `Session#merge` |
| `onReplicate(Object entity, ReplicationMode replicationMode, String entityName)` | `ReplicateEventListener` | `REPLICATE` | `Session#replicate` |
| `onFlushDirty(Object entity, Object id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types)` | `FlushEntityEventListener` | `FLUSH_ENTITY` | `Session#flush` |
| `onEvict(Object entity)` | `EvictEventListener` | `EVICT` | `Session#evict` |
| `onLock(Object entity, LockMode lockMode, String entityName)` | `LockEventListener` | `LOCK` | `Session#lock` |
| `onRefresh(Object entity, String entityName, LockMode lockMode)` | `RefreshEventListener` | `REFRESH` | `Session#refresh` |
| `onPreLoad(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types)` | `PreLoadEventListener` | `PRE_LOAD` | `StatelessSession#get` |
| `onPostLoad(Object entity, Object id, String[] propertyNames, Type[] types)` | `PostLoadEventListener` | `POST_LOAD` | 实体实例刚刚完成初始化之后 |
| `onPreDelete(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types)` | `PreDeleteEventListener` | `PRE_DELETE` | `StatelessSession#delete` |
| `onPostDelete(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types)` | `PostDeleteEventListener` | `POST_DELETE` | 删除写入库之后 |
| `onPreInsert(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes)` | `PreInsertEventListener` | `PRE_INSERT` | `StatelessSession#insert` |
| `onPostInsert(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes)` | `PostInsertEventListener` | `POST_INSERT` | 插入之后 |
| `onPreUpdate(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes)` | `PreUpdateEventListener` | `PRE_UPDATE` | `StatelessSession#update` |
| `onPostUpdate(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes)` | `PostUpdateEventListener` | `POST_UPDATE` | 更新之后 |
| `onPreRecreateCollection(Object collection, Object key)` | `PreCollectionRecreateEventListener` | `PRE_COLLECTION_RECREATE` | 集合首次持久化 |
| `onPostRecreateCollection(Object collection, Object key)` | `PostCollectionRecreateEventListener` | `POST_COLLECTION_RECREATE` | recreate 之后 |
| `onPreCollectionRemove(Object collection, Object key)` | `PreCollectionRemoveEventListener` | `PRE_COLLECTION_REMOVE` | 集合被删除 |
| `onPostRemoveCollection(Object collection, Object key)` | `PostCollectionRemoveEventListener` | `POST_COLLECTION_REMOVE` | remove 之后 |
| `onPreUpdateCollection(Object collection, Object key)` | `PreCollectionUpdateEventListener` | `PRE_COLLECTION_UPDATE` | 集合被更新 |
| `onPostUpdateCollection(Object collection, Object key)` | `PostCollectionUpdateEventListener` | `POST_COLLECTION_UPDATE` | update 之后 |

#### 4.1.2 实现之前必须知道的规则

- **默认只读。** `onFlushDirty`、`onPreLoad`、`onPreInsert`、`onPreUpdate`、`onPreDelete` 以及集合类回调拿到的
  是持久化模型的状态数组。Javadoc 明确说明*不建议*修改 `state`、`currentState`、`previousState`。
- **无法否决（veto）操作。** Hibernate 的 `Pre*EventListener` 接口返回 `boolean`，`true` 表示中止操作；
  `EntityCallbackListener` 恒返回 `false`，且 `EntityCallback` 的方法返回 `void`。需要否决语义时请自行实现
  `EventListener`，或使用 `DelegatingInterceptor`（见 4.6 节）。
- **回调是同步执行的**，运行在调用线程上、位于正在进行的会话操作内部；对 `*Post*` 与 `onFlushDirty` 而言还位于
  事务内部。不要阻塞、不要在同一连接上再开一个 `Session`，也不要修改其他实体后期待它们被 flush。
- **异常会向外传播。** Composite 不做任何捕获；回调抛出的异常会以 `HibernateException`（或你自己的运行时异常）
  形式从 `Session` 调用处冒出并导致操作失败。若回调属于"尽力而为"（审计、埋点、链路追踪），请自行 `try/catch`。
- **本线的 `onLoad` 没有 `readOnly` 参数。** 0.2.x 的签名新增了 `Boolean readOnly`。升级时需改签名，见
  [用户文档索引](README.md)中的迁移表。
- **顺序。** 回调在组合之前会用 `io.microsphere.lang.Prioritized.COMPARATOR` 排序。实现
  `io.microsphere.lang.Prioritized` 即可控制顺序：`getPriority()` 返回 `int`，**值越小越先执行**
  （`MAX_PRIORITY = Integer.MIN_VALUE`、`NORMAL_PRIORITY = 0`、`MIN_PRIORITY = Integer.MAX_VALUE`）。
- **实例只创建一次**，时机是 `SessionFactory` 构建时（监听器在构造器里执行
  `ServiceLoader.load(EntityCallback.class)`）。新增回调需要重建 `SessionFactory` 才会生效。

### 4.2 `CompositeEntityCallback`

`io.microsphere.hibernate.entity.CompositeEntityCallback`

| **成员** | **签名** | **行为** |
|---|---|---|
| 构造器 | `CompositeEntityCallback(List<EntityCallback> callbacks)` | 原样保存列表，不做防御性拷贝，也不做 `null` 校验。传入 `null` 会在首次调用回调时抛 `NullPointerException`。 |
| 全部 23 个回调 | 对每个 `EntityCallback` 方法加 `@Override` | `forEach(this.callbacks, callback -> callback.<同名方法>(…))` —— 按列表顺序、以原参数逐个调用。 |

- **用途：** 把 N 个回调收敛成*一个* `EntityCallback`，让后续机制只与单一对象打交道（典型*组合*模式）。
- **契约：** 顺序串行迭代，遇到第一个异常即中断（`ListUtils.forEach` 不捕获异常）。空列表是合法的空组合体。
- **内部使用**于 `EntityCallbackListener`；同时是 public 的，应用可自行组合回调 —— 下文 1.x 的
  `DelegatingInterceptor` 用法正是复用了它。

### 4.3 `EntityCallbackListener`（包级私有）

`io.microsphere.hibernate.entity.EntityCallbackListener` —— **`io.microsphere.hibernate.entity` 包之外不可访问**。

| **方面** | **细节** |
|---|---|
| 实现的接口 | 23 个 Hibernate 监听器接口：`Load`、`Persist`、`Merge`、`Delete`、`Replicate`、`FlushEntity`、`Evict`、`Lock`、`Refresh`、`PreLoad`、`PostLoad`、`PreDelete`、`PostDelete`、`PreUpdate`、`PostUpdate`、`PreInsert`、`PostInsert`、`Pre/PostCollectionRecreate`、`Pre/PostCollectionRemove`、`Pre/PostCollectionUpdate` |
| `EntityCallbackListener()` | 通过 `ServiceLoader.load(EntityCallback.class)` 加载回调 |
| `EntityCallbackListener(Iterable<EntityCallback> callbacks)` | 拷贝进 `LinkedList`，用 `Prioritized.COMPARATOR` 排序，再包成 `CompositeEntityCallback` |
| 事件 → 回调映射 | 直接字段抽取，例如 `onFlushDirty` 取 `event.getEntityEntry().getId()` / `getLoadedState()` 与 `persister.getPropertyNames()` / `getPropertyTypes()`；集合回调取 `event.getCollection()` 与 `collection.getKey()` |
| 否决 | `onPreDelete`、`onPreInsert`、`onPreUpdate` 返回 `false` |
| 提交后处理 | `requiresPostCommitHanding(EntityPersister)` 与 `requiresPostCommitHandling(EntityPersister)` 均返回 `false`，因此 Hibernate 不会把回调推迟到提交之后执行。同时实现两种拼写，是为了满足目标 Hibernate 5.x 小版本所声明的那个名字。 |
| `getLockMode(LockOptions)` | `protected` 抽取钩子（`lockOptions.getLockMode()`），也是该类唯一的扩展点；但由于类是包级私有，外部代码无法继承 |
| 带 `Map`/`Set` 的重载 | `onDelete(DeleteEvent)`、`onPersist(PersistEvent)`、`onMerge(MergeEvent)`、`onRefresh(RefreshEvent)` 会委托给携带 `Map`/`Set` 的重载，从而保留级联记账参数 |

### 4.4 `EntittyCallbackIntegrator`

`io.microsphere.hibernate.entity.EntittyCallbackIntegrator`

> Hibernate `Integrator` for `EntityCallback`

- **命名说明：** 类名确实是 `EntittyCallbackIntegrator`（多了一个 `t`）。它会原样出现在随包发布的 SPI 文件、异常
  堆栈以及你手动注册的代码里，请不要"顺手纠正"它。
- **`integrate(Metadata, SessionFactoryImplementor, SessionFactoryServiceRegistry)`**（Hibernate 5.x 的三参数
  签名）：
  1. 从 `SessionFactoryServiceRegistry` 取出 `EventListenerRegistry`；
  2. `eventListenerRegistry.addDuplicationStrategy(new EntityCallbackListenerDuplicationStrategy())`；
  3. 创建**一个** `EntityCallbackListener`，并对 23 个 `EventType` 常量执行
     `appendListeners(...)`（`LOAD`、`PERSIST`、`MERGE`、`DELETE`、`REPLICATE`、`FLUSH_ENTITY`、`EVICT`、
     `LOCK`、`REFRESH`、`PRE_LOAD`、`POST_LOAD`、`PRE_DELETE`、`POST_DELETE`、`PRE_UPDATE`、`POST_UPDATE`、
     `PRE_INSERT`、`POST_INSERT`、`PRE_COLLECTION_RECREATE`、`POST_COLLECTION_RECREATE`、
     `PRE_COLLECTION_REMOVE`、`POST_COLLECTION_REMOVE`、`PRE_COLLECTION_UPDATE`、`POST_COLLECTION_UPDATE`）。
     采用 append 而非 set，意味着回调运行在 Hibernate 自带默认监听器*之后*，这也是 `*Post*` 事件能看到已写入
     状态的原因。
- **`disintegrate(SessionFactoryImplementor, SessionFactoryServiceRegistry)`** 是空实现 —— 没有需要回滚的东西。
- **激活方式自动**，见 [第 5 节](#5-集成spi-注册与配置键)。

### 4.5 `EntityCallbackListenerDuplicationStrategy`（包级私有）

`io.microsphere.hibernate.entity.EntityCallbackListenerDuplicationStrategy`

| **成员** | **行为** |
|---|---|
| `areMatch(Object listener, Object original)` | 仅当两者运行时类相同**且**该类恰为 `EntityCallbackListener` 时返回 `true` |
| `getAction()` | `DuplicationStrategy.Action.KEEP_ORIGINAL` |

- **用途：** 当两次 `Integrator` 注册（例如 classpath 上有两份，或在你手动注册之外又自动发现了一次）会添加同类
  监听器时，Hibernate 会询问该策略如何处理。
- **后果：** **第一个**注册的 `EntityCallbackListener` 被保留，第二个被丢弃，因此重复注册不会导致回调执行两次
  —— 但也意味着你无法通过重复注册追加第二组 `EntityCallback`。若启动之后还需要更多回调，请改用
  `DelegatingInterceptor` 分发。

### 4.6 `DelegatingInterceptor`

`io.microsphere.hibernate.DelegatingInterceptor` —— **该类只存在于 1.x 线**，0.2.x 已移除，因为其方法签名是
Hibernate 5 专属的。

```java
public class DelegatingInterceptor implements Interceptor, DelegatingWrapper {

    protected final Interceptor delegate;

    public DelegatingInterceptor(@Nullable Interceptor delegate) {
        this.delegate = delegate == null ? INSTANCE : delegate;   // org.hibernate.EmptyInterceptor.INSTANCE
    }

    @Override
    public Interceptor getDelegate() {
        return delegate;
    }
    // ... 18 个转发方法
}
```

#### 4.6.1 Interceptor 委托机制

- **null 安全包装。** 传入 `null` 委托对象时会自动替换为
  `org.hibernate.EmptyInterceptor.INSTANCE`，于是每个转发调用都是无害的空操作而不是
  `NullPointerException`。这使它可以在"还没有委托对象"时当作起始 interceptor 使用。
- **原样转发。** 每个 `Interceptor` 方法都以完全一致的参数调用委托对象，并原封不动返回委托的结果 ——
  装饰器从不吞掉返回值。这对返回 `boolean` 的回调（`onLoad`、`onFlushDirty`、`onSave`，其中 `true`
  表示"状态已被修改"）、`int[] findDirty(...)`、`Object instantiate(...)`、`String getEntityName(...)`、
  `Object getEntity(...)`、`Boolean isTransient(...)` 尤为关键。
- **`protected final Interceptor delegate`** 就是扩展点：子类可直接读取，`getDelegate()` 则对外暴露。
  `DelegatingWrapper#getDelegate()`（在 `io.microsphere.lang.DelegatingWrapper` 中声明返回 `Object`）在这里被
  收窄为 `Interceptor`，并且从 `io.microsphere.lang.Wrapper` 继承的默认方法 `unwrap(Class<T>)` /
  `isWrapperFor(Class<?>)` 让你能够检查或穿透多层包装的 interceptor 栈。
- **被转发的方法**（全部带 `@Override`）：`onLoad`、`onFlushDirty`、`onSave`、`onDelete`、
  `onCollectionRecreate`、`onCollectionRemove`、`onCollectionUpdate`、`preFlush`、`postFlush`、`isTransient`、
  `findDirty`、`instantiate`、`getEntityName`、`getEntity`、`afterTransactionBegin`、
  `beforeTransactionCompletion`、`afterTransactionCompletion`、`onPrepareStatement`。其中
  `onPrepareStatement(String)` 另外标注了 `@Deprecated`，因为 Hibernate 已不推荐通过 interceptor 改写 SQL，
  请改用 `StatementInspector`。
- **Hibernate 5 签名（它只能属于 1.x 的原因）：** `onLoad`/`onSave`/`onDelete` 等使用 `Serializable id`，
  `instantiate` 使用 `EntityMode`，`preFlush`/`postFlush` 使用原始 `Iterator`。Hibernate 6 把 id 改为
  `Object`、删除了 `EntityMode`、并为迭代器加了泛型，同一份源码无法在那里编译 —— 这正是 0.2.x 移除该类的原因。

#### 4.6.2 注册 interceptor

`DelegatingInterceptor` **不**由 integrator 装配，需要你按 `Session` 或按 `SessionFactory` 显式启用。在
Hibernate 5.x 上 `SessionFactory#openSession(Interceptor)` **不可用**，请用以下两种方式之一：

```java
// (a) 按 Session —— Hibernate 5.0 .. 5.6
Interceptor interceptor = new DelegatingInterceptor(thirdPartyInterceptor);
Session session = sessionFactory.withOptions().interceptor(interceptor).openSession();

// (b) 对该 SessionFactory 创建的所有 Session 生效
Configuration configuration = new Configuration();
configuration.setInterceptor(interceptor);
SessionFactory sessionFactory = configuration.buildSessionFactory();
```

#### 4.6.3 用它注册自定义回调

通过 integrator 发现的 `EntityCallback` 作用于整个 `SessionFactory`。当你需要*只作用于单个会话*的回调，或者回调
构造时需要 `ServiceLoader` 无参构造器无法提供的依赖，就可以用一个"把事件分派给 `CompositeEntityCallback`"的
interceptor 来实现：

```java
package com.acme.hibernate;

import io.microsphere.hibernate.DelegatingInterceptor;
import io.microsphere.hibernate.entity.CompositeEntityCallback;
import io.microsphere.hibernate.entity.EntityCallback;
import org.hibernate.Interceptor;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.util.Arrays;

/** Bridges Hibernate 5 Interceptor calls into EntityCallback calls for one Session. */
public class EntityCallbackInterceptor extends DelegatingInterceptor {

    private final EntityCallback callback;

    public EntityCallbackInterceptor(Interceptor delegate, EntityCallback... callbacks) {
        super(delegate);                                  // null delegate -> EmptyInterceptor.INSTANCE
        this.callback = new CompositeEntityCallback(Arrays.asList(callbacks));
    }

    @Override
    public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        callback.onPreLoad(entity, id, state, propertyNames, types);
        return super.onLoad(entity, id, state, propertyNames, types);   // keep the delegate's verdict
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        callback.onPreInsert(entity, id, state, propertyNames, types);
        return super.onSave(entity, id, state, propertyNames, types);
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
                                String[] propertyNames, Type[] types) {
        callback.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
        return super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
    }

    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        callback.onPreDelete(entity, id, state, propertyNames, types);
        super.onDelete(entity, id, state, propertyNames, types);
    }
}
```

```java
EntityCallback tenantAudit = new TenantAuditCallback(tenantContext);   // built with DI, no ServiceLoader
Interceptor interceptor = new EntityCallbackInterceptor(null, tenantAudit);

try (Session session = sessionFactory.withOptions().interceptor(interceptor).openSession()) {
    session.persist(order);   // -> onPreInsert on tenantAudit, then the (empty) delegate
}
```

映射取舍说明：`Interceptor#onLoad` ↔ `EntityCallback#onPreLoad`，`Interceptor#onSave` ↔
`EntityCallback#onPreInsert`，`Interceptor#onFlushDirty` ↔ `EntityCallback#onFlushDirty`（参数列表完全一致），
`Interceptor#onDelete` ↔ `EntityCallback#onPreDelete`，`Interceptor#onCollectionRecreate/Remove/Update` ↔
`EntityCallback#onPreRecreateCollection`、`onPreCollectionRemove`、`onPreUpdateCollection`。`Interceptor` 没有
对应 `*Post*` 事件以及 `onPersist`、`onMerge`、`onReplicate`、`onEvict`、`onLock`、`onRefresh` 的入口 ——
这些只能通过 integrator 那条路径获得。

另外两种常用套路：

- **不改现有 interceptor，直接接管。** 把第三方给你的对象包起来
  （`new DelegatingInterceptor(libraryInterceptor)`），只覆写你关心的事件，并始终
  `return super.<event>(...)`，从而保留被包装对象的否决/判定权。
- **分层装饰。** 由于 `delegate` 本身也可以是一个 `DelegatingInterceptor`，你可以构造链条，之后用
  `getDelegate()` 或 `unwrap(SomeInterceptor.class)` 检查某一层。

### 4.7 `PropertyConstants`

`io.microsphere.hibernate.constants.PropertyConstants` —— 常量接口，没有方法。

| **常量** | **取值** | **说明** |
|---|---|---|
| `MICROSPHERE_HIBERNATE_PROPERTY_NAME_PREFIX` | `"microsphere.hibernate."` | `microsphere.`（来自 `io.microsphere.constants.PropertyConstants.MICROSPHERE_PROPERTY_NAME_PREFIX`）+ `hibernate` + `.` |
| `MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_NAME` | `"microsphere.hibernate.enabled"` | 标注 `@ConfigurationProperty(type = boolean.class, defaultValue = "true", source = SYSTEM_PROPERTIES_SOURCE)` |
| `DEFAULT_MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_VALUE` | `"true"` | 字符串默认值 |
| `DEFAULT_MICROSPHERE_HIBERNATE_ENABLED` | `true` | 由上一个值 `Boolean.parseBoolean` 得到 |

```java
// Enable Microsphere Hibernate via system property
System.setProperty(MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_NAME, "true");

// Read the enabled flag
boolean enabled = Boolean.parseBoolean(
        System.getProperty(MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_NAME,
                           DEFAULT_MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_VALUE));

// Use the property name prefix to build custom property names
String customProperty = MICROSPHERE_HIBERNATE_PROPERTY_NAME_PREFIX + "myFeature.enabled";
```

**需要如实说明的一点：** 在 `0.1.x` 中，本模块没有任何生产代码读取 `microsphere.hibernate.enabled`。实体回调
集成的启用完全取决于*classpath 上是否存在*（即随包发布的 `Integrator` 服务文件）。该常量是这个命名空间的正式
声明与元数据锚点，供你自己的开关逻辑与工具链使用 —— 之所以发布它，是因为可选依赖
`microsphere-annotation-processor` 会把 `@ConfigurationProperty` 声明转换成生成的配置元数据。你可以用它来给自己的
回调加开关：

```java
public class AuditEntityCallback implements EntityCallback {

    private static final boolean enabled = Boolean.parseBoolean(
            System.getProperty(MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_NAME,
                               DEFAULT_MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_VALUE));

    @Override
    public void onPersist(Object entity, String entityName) {
        if (!enabled) {
            return;
        }
        // ... audit
    }
}
```

## 5. 集成：SPI 注册与配置键

### 5.1 开启该特性的服务文件

由 `microsphere-hibernate-core` 随包发布，你不需要自己创建：

```
# microsphere-hibernate-core/src/main/resources/META-INF/services/org.hibernate.integrator.spi.Integrator
io.microsphere.hibernate.entity.EntittyCallbackIntegrator
```

### 5.2 注册你自己的回调

在**你的**构件中创建一个按行分隔的文件：

```
# src/main/resources/META-INF/services/io.microsphere.hibernate.entity.EntityCallback
com.acme.order.logging.OrderEntityCallback
com.acme.order.audit.AuditEntityCallback
```

要求：public 类、public 无参构造器、位于构建 `SessionFactory` 所用的 classpath（即 Hibernate 使用的同一个
`ClassLoader`）；除 `Prioritized` 之外不要对顺序做其他假设。

### 5.3 Spring Boot / Spring Cloud 说明（1.x 线）

该集成与容器无关 —— Spring Boot 2.3/2.4/2.6（即 `microsphere-spring-cloud:0.1.26` 管理的 Spring Cloud
Hoxton…2021.0.x 线）不需要额外注册 Bean，因为 Hibernate 通过 `ServiceLoader` 发现 `Integrator`。
`microsphere-hibernate-core` 保持在 `compile` 作用域即可；如果你使用 JPA（`spring-boot-starter-data-jpa`），
Boot 构建的 Hibernate `SessionFactory` 同样会执行 integrator 发现流程，因此 JPA 管理的实体也会触发回调。

## 6. 实例：模块自带的测试

### 6.1 `EntityCallbackIntegratorTest` —— 端到端验证

```java
package io.microsphere.hibernate.entity;

import io.microsphere.hibernate.test.AbstractHibernateH2Test;

/**
 * {@link EntittyCallbackIntegrator} Test
 */
class EntityCallbackIntegratorTest extends AbstractHibernateH2Test {

}
```

出处：`microsphere-hibernate-core/src/test/java/io/microsphere/hibernate/entity/EntityCallbackIntegratorTest.java`。

这个类故意不写任何方法。继承 `AbstractHibernateH2Test`（来自 `microsphere-hibernate-test`，在本模块 POM 中以
`test` 作用域声明）就继承了它的两个 `@Nested` 测试类 —— `SessionTest` 与 `StatelessSessionTest`，共 14 个测试
方法 —— 它们在内存 H2 上执行 `persist`、`get`、`load`、`merge`、`delete`、`replicate`、`flush`、`evict`、
`lock`、`refresh` 以及整套 `StatelessSession` 操作。由于 integrator 来自 jar 内的 SPI 文件，而
`TestEntityCallback` 来自测试资源的 SPI 文件，只有当所有事件类型的 `EntityCallback` 链路都正确时这些继承来的
测试才会通过：

```
# microsphere-hibernate-core/src/test/resources/META-INF/services/io.microsphere.hibernate.entity.EntityCallback
io.microsphere.hibernate.entity.TestEntityCallback
```

### 6.2 `TestEntityCallback` —— 记录一切事件的回调

```java
public class TestEntityCallback implements EntityCallback {

    private static final Logger logger = getLogger(TestEntityCallback.class);

    @Override
    public void onLoad(Object id, String entityClassName, Object entity, LockMode lockMode, boolean isAssociationFetch,
                       Object result, LoadEventListener.LoadType loadType) {
        logger.trace("onLoad - id : {}, entityClassName : {}, entity : {}, lockMode : {}, isAssociationFetch : {}, result : {}, loadType : {}",
                id, entityClassName, entity, lockMode, isAssociationFetch, result, loadType);
        EntityCallback.super.onLoad(id, entityClassName, entity, lockMode, isAssociationFetch, result, loadType);
    }

    @Override
    public void onPersist(Object entity, String entityName) {
        logger.trace("onPersist - entity : {}, entityName : {}", entity, entityName);
        EntityCallback.super.onPersist(entity, entityName);
    }

    @Override
    public void onFlushDirty(Object entity, Object id, Object[] currentState, Object[] previousState,
                             String[] propertyNames, Type[] types) {
        logger.trace("onFlushDirty - entity : {}, id : {}, currentState : {}, previousState : {}, propertyNames : {}, types : {}",
                entity, id, currentState, previousState, propertyNames, types);
        EntityCallback.super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
    }
    // … 全部 23 个方法，同样的写法
}
```

出处：`microsphere-hibernate-core/src/test/java/io/microsphere/hibernate/entity/TestEntityCallback.java`。
`logger.trace(…, args)` 的可变参数形式来自 `io.microsphere.logging`（属于 optional 的
`microsphere-java-core`），而调用 `EntityCallback.super.<method>(…)` 是覆写时保留默认行为的既定写法。

打开日志运行：

```bash
./mvnw -Phibernate-5.6 -Drevision=0.1.11-SNAPSHOT \
       -Dorg.slf4j.simpleLogger.defaultLogLevel=trace \
       test -Dtest=EntityCallbackIntegratorTest
```

### 6.3 `DelegatingInterceptorTest` —— 逐条测试刻画委托契约

```java
@ExtendWith(MockitoExtension.class)
class DelegatingInterceptorTest {

    @Mock
    private Interceptor mockDelegate;

    private DelegatingInterceptor delegatingInterceptor;

    @BeforeEach
    void setUp() {
        delegatingInterceptor = new DelegatingInterceptor(mockDelegate);
    }

    @Test
    void testConstructorWithNullDelegate() {
        DelegatingInterceptor interceptor = new DelegatingInterceptor(null);
        assertNotNull(interceptor);
        assertSame(INSTANCE, interceptor.getDelegate());          // EmptyInterceptor.INSTANCE
    }

    @Test
    void testOnLoad() throws CallbackException {
        Object entity = new Object();
        Serializable id = 1L;
        Object[] state = new Object[0];
        String[] names = new String[0];
        Type[] types = new Type[0];

        when(mockDelegate.onLoad(eq(entity), eq(id), eq(state), eq(names), eq(types)))
                .thenReturn(true);

        assertTrue(delegatingInterceptor.onLoad(entity, id, state, names, types));
        verify(mockDelegate).onLoad(entity, id, state, names, types);
    }

    @Test
    void testTransactionCallbacks() {
        Transaction tx = mock(Transaction.class);

        delegatingInterceptor.afterTransactionBegin(tx);
        delegatingInterceptor.beforeTransactionCompletion(tx);
        delegatingInterceptor.afterTransactionCompletion(tx);

        verify(mockDelegate).afterTransactionBegin(tx);
        verify(mockDelegate).beforeTransactionCompletion(tx);
        verify(mockDelegate).afterTransactionCompletion(tx);
    }

    @Test
    void testGetDelegate() {
        assertSame(mockDelegate, delegatingInterceptor.getDelegate());
    }
}
```

出处：`microsphere-hibernate-core/src/test/java/io/microsphere/hibernate/DelegatingInterceptorTest.java`。
其余测试（`testOnFlushDirty`、`testOnSave`、`testOnDelete`、`testCollectionOperations`、`testPreAndPostFlush`、
`testIsTransient`、`testFindDirty`、`testInstantiate`、`testGetEntityName`、`testGetEntity`、
`testOnPrepareStatement`）针对单个方法断言同一条规则：**参数原样转发，委托对象的返回值原样返回。** 可以把它们
当作这个类的可执行规格 —— 包括"委托 `onSave` 返回 `false` 时必须仍然是 `false`"这一点（见 `testOnSave`）。

注意 `testOnLoad` / `testInstantiate` 里的 `Serializable id = 1L;` 与 `EntityMode.POJO` 实参：它们正是 Hibernate 5
的签名，也是这个类只能属于 1.x 的原因。

### 6.4 `EntityCallbackListenerDuplicationStrategyTest`

```java
@Test
void testAreMatch() {
    EntityCallbackListener listener = new EntityCallbackListener();
    assertTrue(strategy.areMatch(listener, listener));

    assertFalse(strategy.areMatch(listener, new DefaultLoadEventListener()));
    assertFalse(strategy.areMatch(new DefaultLoadEventListener(), new DefaultLoadEventListener()));
}

@Test
void testGetAction() {
    assertSame(KEEP_ORIGINAL, strategy.getAction());
}
```

出处：`microsphere-hibernate-core/src/test/java/io/microsphere/hibernate/entity/EntityCallbackListenerDuplicationStrategyTest.java`。
由于被测类与 `EntityCallbackListener` 都是包级私有，测试必须放在 `io.microsphere.hibernate.entity` 包中 —— 如果你
想在自己的仓库里对它们做单元测试，也需要用同样的技巧。

### 6.5 `PropertyConstantsTest`

```java
@Test
void test() {
    assertEquals("microsphere.hibernate.", MICROSPHERE_HIBERNATE_PROPERTY_NAME_PREFIX);
    assertEquals("true", DEFAULT_MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_VALUE);
    assertTrue(DEFAULT_MICROSPHERE_HIBERNATE_ENABLED);
    assertEquals("microsphere.hibernate.enabled", MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_NAME);
}
```

出处：`microsphere-hibernate-core/src/test/java/io/microsphere/hibernate/constants/PropertyConstantsTest.java`。
这四条断言就是配置键名的权威表述，若你依赖该命名空间，可以把它们复制到自己的构建中。

## 7. 测试你自己的回调

引入 `microsphere-hibernate-test` 到 `test` 作用域（见
[microsphere-hibernate-test.md](microsphere-hibernate-test.md)），然后二选一：

1. 把你的回调注册到 `src/test/resources/META-INF/services/io.microsphere.hibernate.entity.EntityCallback`，
   继承 `AbstractHibernateH2Test`，让继承来的 H2 用例触发你的回调（即
   `EntityCallbackIntegratorTest` 的做法）；
2. 使用 `@HibernateTest` + `@HibernateRuntime` 写只针对单个回调的聚焦测试：

```java
@HibernateTest(
        properties = {
                "hibernate.connection.driver_class=org.h2.Driver",
                "hibernate.connection.url=jdbc:h2:mem:callback_test_db;DB_CLOSE_DELAY=-1",
                "hibernate.connection.username=sa",
                "hibernate.connection.password=",
                "hibernate.dialect=org.hibernate.dialect.H2Dialect",
                "hibernate.hbm2ddl.auto=update"
        },
        scanEntityPackages = {"io.microsphere.hibernate.test.entity"}
)
class PersistCallbackTest {

    @HibernateRuntime
    private Session session;

    @Test
    void callbackShouldSeePersist() {
        org.hibernate.Transaction tx = session.beginTransaction();
        session.persist(new Order());
        tx.commit();
        assertTrue(CollectingCallback.persistedNames.contains("io.microsphere.hibernate.test.entity.Order"));
    }
}
```

如果只想用 Mockito 对回调做纯单元测试，则完全不需要 Hibernate 运行期 —— `EntityCallback` 是普通接口，
`CompositeEntityCallback` 可以直接驱动：

```java
EntityCallback callback = mock(EntityCallback.class);
CompositeEntityCallback composite = new CompositeEntityCallback(Collections.singletonList(callback));

composite.onPersist(entity, "Order");

verify(callback).onPersist(entity, "Order");
```

## 8. 常见问题排查

| **现象** | **原因 / 处理** |
|---|---|
| 回调从不触发 | `EntityCallback` 服务文件缺失或名字不对：文件名必须精确为 `io.microsphere.hibernate.entity.EntityCallback`，内容是实现类全限定名。另外确认 `microsphere-hibernate-core` 位于**运行期** classpath（不是 `test` 作用域），且 `microsphere-java-core` 已引入。 |
| Java 9+ 报 `NoClassDefFoundError: javax/xml/bind/...` | 你在 Java 9+ 上运行 Hibernate 5 却没有 JAXB 兜底依赖。上游用 `java9+` profile（`javax.xml.bind:jaxb-api`）处理；在你的应用里补上该依赖，或改用 JDK 8 运行。 |
| 与 `Interceptor`、`Integrator` 或 `EntityMode` 相关的 `NoSuchMethodError` / `AbstractMethodError` | classpath 上有 Hibernate 6+。本线仅支持 5.0.x–5.6.x，请把 `org.hibernate:hibernate-core` 保持在范围内，并统一使用 `0.1.x` 构件。 |
| 回调被执行多次 | `EntityCallbackListenerDuplicationStrategy` 返回 `KEEP_ORIGINAL`，由 integrator 造成的重复会被抑制。真正的重复通常来自服务文件里同一类写了两行，或你自己的 `EventListener` 做了同样的事。 |
| 第二次注册后回调丢失 | 同一策略下*第一个* `EntityCallbackListener` 胜出。生命周期后期追加的回调会被忽略 —— 请重建 `SessionFactory`，或改走 `DelegatingInterceptor` 分发。 |
| 你的异常导致业务代码失败 | 这是预期行为：回调在会话操作内部执行，异常向外传播。若回调必须"尽力而为"，请在回调内部捕获。 |
| 修改 `state` 数组无效（或破坏了 flush） | 契约规定：不要修改状态数组。确实需要改值请使用 `DelegatingInterceptor` —— 那才是 `Interceptor` API 支持的做法。 |
| 某个 Hibernate 5.x 小版本行为与默认构建不一致 | 5.0 – 5.6 全区间都在 CI 覆盖范围内（JDK 8 × 7 个 `hibernate-5.x` profile × 3 个 Spring Cloud profile），出现差异即为缺陷：请提交 issue，并附上 profile（`-Phibernate-5.0` … `-Phibernate-5.6`）、锁定的 `hibernate-core` 版本、JDK 与堆栈信息。 |

## 9. 相关文档

- [README.md](README.md) —— 1.x 用户文档索引与 0.2.x 迁移对照表
- [microsphere-hibernate-test.md](microsphere-hibernate-test.md) —— 上文测试所用的 H2 装置
- [microsphere-hibernate-dependencies.md](microsphere-hibernate-dependencies.md) —— 版本管理
- [microsphere-hibernate-parent.md](microsphere-hibernate-parent.md) —— `hibernate-5.x` profiles
- 源码（`release-1.x` 分支）：`microsphere-hibernate-core/src/main/java/io/microsphere/hibernate/**`、
  `microsphere-hibernate-core/src/main/resources/META-INF/services/org.hibernate.integrator.spi.Integrator`、
  `microsphere-hibernate-core/src/test/java/io/microsphere/hibernate/**`、
  `microsphere-hibernate-core/src/test/resources/META-INF/services/io.microsphere.hibernate.entity.EntityCallback`
