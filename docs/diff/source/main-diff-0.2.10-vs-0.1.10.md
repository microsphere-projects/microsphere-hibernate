# src/main 源码差异：`0.2.10` vs `0.1.10`

> English version: [main-diff-0.2.10-vs-0.1.10-en.md](main-diff-0.2.10-vs-0.1.10-en.md) ·
> src/test 对比：[test-diff-0.2.10-vs-0.1.10.md](test-diff-0.2.10-vs-0.1.10.md) ·
> [文档主页](../../zh/README.md)

## 1. 对比口径

| 项 | `0.2.10` | `0.1.10` |
|----|----------|----------|
| Maven 版本（`revision`） | `0.2.10-SNAPSHOT` | `0.1.10-SNAPSHOT` |
| 版本线 | current（0.2.x） | 1.x（0.1.x） |
| JDK 基线 | 17 | 8 |
| Hibernate 范围 | 6.2.x – 7.3.x（`org.hibernate.orm`） | 5.0.x – 5.6.x（`org.hibernate`） |
| `microsphere-spring-cloud` | `0.2.26` | `0.1.26` |

比较规则：

- **逐 Maven 模块**对比，只比较**同名模块**；两个 tag 的模块清单完全一致，无需要忽略的模块。
- 每个模块的 `src/main` 与 `src/test` **独立成文**（本文件只覆盖 `src/main`）。
- **已排除**的差异类别（详见第 4 节）：Java 8 与 Java 17+ 的语法及 API 差异、Java EE（`javax.*`）与 Jakarta EE（`jakarta.*`）的差异。
- `src/main/resources` 下两个 tag 完全一致（`META-INF/services/org.hibernate.integrator.spi.Integrator` 未变）。

## 2. 模块级总览

| Maven 模块 | `0.1.10` Java 文件 / 资源文件 | `0.2.10` Java 文件 / 资源文件 | 匹配 | 差异概况 |
|------------|------------------------------|------------------------------|------|----------|
| microsphere-hibernate-parent | 0 / 0（POM-only） | 0 / 0（POM-only） | ✅ | 仅 `pom.xml` 差异，见第 5 节 |
| microsphere-hibernate-dependencies | 0 / 0（BOM） | 0 / 0（BOM） | ✅ | 无源码差异 |
| microsphere-hibernate-core | 7 / 1 | 6 / 1 | ✅ | 删除 1 个类、修改 4 个类、2 个类无差异；资源文件无差异 |
| microsphere-hibernate-test | 9 / 0 | 9 / 0 | ✅ | 修改 7 个类、2 个类无差异 |
| microsphere-hibernate（根聚合 POM） | 0 / 0 | 0 / 0 | ✅ | 仅 `pom.xml` 差异 |

## 3. 文件级差异

### 3.1 microsphere-hibernate-core / src/main

包根：`io.microsphere.hibernate`

| 文件 | 变更 | 差异要点 | 归类 | 原因解释 |
|------|------|----------|------|----------|
| `DelegatingInterceptor.java` | **删除**（165 行） | 实现 Hibernate `Interceptor` + `DelegatingWrapper` 的装饰器，逐方法手工委托（`onLoad`/`onSave`/`findDirty`/`instantiate`/事务回调等），构造器允许 `null` 回落到 `EmptyInterceptor.INSTANCE` | 功能裁剪 + API 兼容 | Hibernate 6 起 `Interceptor` 由抽象类改为**带 default 方法的接口**，`EntityMode` 被移除、标识符形参由 `Serializable` 放宽为 `Object`：该类的所有覆写签名在新版本线上不存在，手写全量委托样板也失去意义。因此 0.2.x 不再提供该公共 API，`DelegatingInterceptor` 成为 **1.x-only** 能力。 |
| `entity/EntityCallback.java` | 修改 | ① `onLoad(...)` 在 `result` 与 `loadType` 之间新增 `Boolean readOnly` 形参；② javadoc `@see StatelessSession#get(String/Class, Serializable)` → `#get(String/Class, Object)`；③ 移除未使用的 `java.io.Serializable` 导入 | API 兼容 + 功能需要 | ① Hibernate 6 的 `LoadEvent` 暴露 `getReadOnly()`，回调契约需要把"只读加载"意图透传给业务侧；② Hibernate 6 把实体标识符形参从 `Serializable` 放宽为 `Object`，`@see` 必须跟随真实签名否则链接解析失败。 |
| `entity/CompositeEntityCallback.java` | 修改 | `onLoad` 组合实现同步新增 `readOnly` 形参并逐个转发；文件末尾空行整理 | API 兼容 | 组合（Composite）实现必须与 `EntityCallback` 契约保持同一签名，否则新增参数在组合路径上丢失。 |
| `entity/EntittyCallbackIntegrator.java` | 修改 | 新增 `integrate(Metadata, BootstrapContext, SessionFactoryImplementor)` 覆写，内部以 `sessionFactory.getServiceRegistry()` 转调原有三参 `integrate(...)` 逻辑；为 `disintegrate(...)` 补充 javadoc（说明其为 no-op，清理由 SessionFactory 关闭承担） | API 兼容 | Hibernate 6 把 `BootstrapContext` 纳入 `Integrator#integrate` 的规范方法，Hibernate 7 移除了不带 `BootstrapContext` 的旧形态；新增入口用于满足新 SPI 抽象方法，同时复用既有监听器注册逻辑，避免复制实现。 |
| `entity/EntityCallbackListener.java` | 修改 | ① 事件级联上下文参数由裸集合改为专用类型：`onPersist(…, Map)` → `PersistContext`、`onMerge(…, Map)` → `MergeContext`、`onRefresh(…, Map)` → `RefreshContext`、`onDelete(…, Set)` → `DeleteContext`；② `onLoad` 透传 `event.getReadOnly()`；③ 6 处集合回调中 `PersistentCollection` → `PersistentCollection<?>`；④ 删除拼写错误的 `requiresPostCommitHanding(EntityPersister)` 覆写，仅保留 `requiresPostCommitHandling(...)`；⑤ 移除未使用的 `java.util.Map`/`Set` 导入 | API 兼容 | ① Hibernate 6.2+ 用类型安全的上下文对象取代原先的 `Map`/`Set` 级联去重集合；② 对应 `EntityCallback` 新增参数；③ Hibernate 6 将 `PersistentCollection` 泛型化，裸类型无法通过编译；④ Hibernate 6 移除了带拼写错误的 default 方法，多余覆写不再对应任何接口方法。 |
| `constants/PropertyConstants.java` | 无差异 | — | — | 纯常量定义，不依赖 Hibernate API 形态。 |
| `entity/EntityCallbackListenerDuplicationStrategy.java` | 无差异 | — | — | 实现的 `DuplicationStrategy` 契约在 5.x/6.x/7.x 间保持稳定。 |

### 3.2 microsphere-hibernate-test / src/main

包根：`io.microsphere.hibernate.test`

| 文件 | 变更 | 差异要点 | 归类 | 原因解释 |
|------|------|----------|------|----------|
| `AbstractHibernateH2Test.java` | 修改 | ① `session.delete(x)` → `session.remove(x)`，测试显示名 `Test : persist & delete` / `testPersistAndDelete` → `persist & remove` / `testPersistAndRemove`；② `session.load(User.class, id)` → `session.find(User.class, id)`；③ `createQuery("FROM io.microsphere…User").list()` → `createQuery("FROM User", User.class).getResultList()`；④ `session.merge(...)`、`statelessSession.get(...)` 上的显式 `(User)` 强转全部移除；⑤ 新增用例 `testPersistAndDetach`（`Session.detach`）、`testInsertAndFetch`（`StatelessSession.fetch`） | API 兼容 + 功能需要 | ① Hibernate 6 弃用、7.0 移除 `Session#delete`，统一为 JPA 风格的 `remove`（0.2.x 需同时兼容 6.2–7.3，只能用 `remove`）；② `find` 为 JPA 标准查找语义（缺失返回 `null`），`load` 在 6+ 表示"假定存在"的引用加载；③ 无类型 `createQuery(String)` 与 `Query#list()` 在 6+ 弃用，改为带结果类型的重载 + `getResultList()`；④ 6+ 中这些方法已泛型化，返回值即静态类型，强转是冗余；⑤ 补齐新版本线上稳定可用的 `detach`/`fetch` 行为覆盖，作为文档示例。 |
| `HibernateExtension.java` | 修改 | 关闭 `StatelessSession` 的判空条件由 `statelessSession != null` 收紧为 `statelessSession != null && statelessSession.isOpen()`；`LoggerFactory.getLogger` 改为静态导入 `getLogger` | 功能需要（健壮性） | JUnit 扩展的 `@AfterAll` 清理可能撞上测试体内已自行关闭会话的情形，Hibernate 7 上重复 `close()` 会抛异常并掩盖真实失败原因，故先判 `isOpen()`。日志写法调整属风格统一。 |
| `HibernateRuntime.java` | 修改 | `ElementType.FIELD/PARAMETER`、`RetentionPolicy.RUNTIME` 改为静态导入 | 代码风格 | 注解元数据的书写方式统一，无行为差异。 |
| `entity/Order.java` | 修改 | 新增类级 javadoc（`t_order` 映射说明 + `Example Usage` 代码块）、全部 getter/setter javadoc、`@author/@see/@since`；`equals` 去掉 `final` | 文档 | 该模块是 README/用户文档示例的来源，0.2.x 把测试实体升级为可直接引用的文档样例。 |
| `entity/Product.java` | 修改 | 同上：类级 javadoc（含 `StatelessSession.insert/get` 示例）、方法 javadoc、`equals` 去掉 `final` | 文档 | 同上。 |
| `entity/User.java` | 修改 | 新增类级 javadoc（`t_user` 双向关系示例）、`addOrder` 的维护双向关联说明、各访问器 javadoc | 文档 | 同上，重点固化"级联持久化 + 双向同步"的用法契约。 |
| `entity/UserProfile.java` | 修改 | 新增类级 javadoc（`t_user_profile`、级联到 profile 的说明）与各访问器 javadoc | 文档 | 同上。 |
| `AbstractHibernateTest.java`、`HibernateTest.java` | 无差异 | — | — | 会话工厂构建与注解契约在两条版本线一致。 |

## 4. 按口径排除的差异（记录但不计入对比结论）

| 差异 | 涉及文件 | 排除理由 |
|------|----------|----------|
| `javax.persistence.*` → `jakarta.persistence.*`（含 `Entity`、`Table`、`Id`、`GeneratedValue`、`ManyToOne`、`ManyToMany`、`OneToOne`、`JoinColumn`、`JoinTable`、`CascadeType`、`FetchType`、`StrategyType` 的导入与静态导入） | `microsphere-hibernate-test/src/main/java/.../entity/{Order,Product,User,UserProfile}.java`、`HibernateExtension.java` | Java EE → Jakarta EE 命名空间迁移（Jakarta Persistence 3.x），属包名替换而非实现差异。 |
| `if (!(o instanceof X)) { … } X that = (X) o;` → `if (!(o instanceof X x)) return false;`（模式匹配，Java 16+） | `entity/{Order,Product,User,UserProfile}.java` | Java 8 与 Java 17+ 语法差异。 |
| `Interceptor` 覆写签名中的 `Serializable` 主键形参 vs `Object` 主键形参 | `DelegatingInterceptor.java`（该类整体已删除） | 由 Hibernate API 演进决定，随类删除一并消失；已在 3.1 首行以"功能裁剪"形式记录。 |

## 5. 构建上下文（非 src，但解释了上述差异）

| POM | 差异 | 影响 |
|-----|------|------|
| 根 `pom.xml` | `revision` `0.1.10-SNAPSHOT` → `0.2.10-SNAPSHOT`；新增 `<java.version>17</java.version>`；父 POM `microsphere-spring-cloud-parent` `0.1.26` → `0.2.26` | 抬高编译基线到 JDK 17，模式匹配等新语法可用。 |
| `microsphere-hibernate-parent/pom.xml` | `hibernate-core` 直接版本管理改为导入 `org.hibernate.orm:hibernate-platform`（`type=pom`, `scope=import`）；默认 profile `hibernate-5.6` → `hibernate-7.3`，5.0–5.6 profiles 替换为 6.2–7.3 profiles | 依赖坐标与版本矩阵整体切到 Hibernate 6+/7，是 3.1/3.2 中所有 API 兼容改造的直接动因。 |
| `microsphere-hibernate-core/pom.xml`、`microsphere-hibernate-test/pom.xml` | `hibernate-core` groupId `org.hibernate` → `org.hibernate.orm`；删除 `java9+` profile（`javax.xml.bind:jaxb-api`） | Jakarta 迁移后不再需要为 JDK 9+ 外挂 JAXB，同时 JDK 17 基线使该 profile 失去存在意义。 |

## 6. 结论

`src/main` 的实质差异集中在**两类**：

1. **Hibernate 5 → 6/7 的 API 适配**（`core` 的 `Integrator`/事件监听器签名、`test` 模块的 `Session`/`Query`/`StatelessSession` 调用方式）——这是两条版本线的根本分野；
2. **能力裁剪与文档化**：1.x 独有的 `DelegatingInterceptor` 在 0.2.x 消失；测试实体的 javadoc 与示例被补齐，`Session.detach`/`StatelessSession.fetch` 新增用例。

除删除的 `DelegatingInterceptor` 外，`0.2.10` 未新增 `src/main` 公开类，公开 API 面在 `core` 上是**收缩**的（`EntityCallback#onLoad` 签名破坏兼容，需下游同步）。
