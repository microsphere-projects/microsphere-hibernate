# src/test 源码差异：`0.2.10` vs `0.1.10`

> English version: [test-diff-0.2.10-vs-0.1.10-en.md](test-diff-0.2.10-vs-0.1.10-en.md) ·
> src/main 对比：[main-diff-0.2.10-vs-0.1.10.md](main-diff-0.2.10-vs-0.1.10.md) ·
> [文档主页](../../zh/README.md)

## 1. 对比口径

与 [src/main 差异文档](main-diff-0.2.10-vs-0.1.10.md) 第 1 节一致：逐 Maven 模块、只比较同名模块、`src/test` 独立成文，
并排除 Java 8 / Java 17+ 语法与 API 差异、Java EE（`javax.*`）与 Jakarta EE（`jakarta.*`）差异。

| 项 | `0.2.10` | `0.1.10` |
|----|----------|----------|
| Maven 版本 | `0.2.10-SNAPSHOT` | `0.1.10-SNAPSHOT` |
| JDK / Hibernate | 17 / 6.2.x – 7.3.x | 8 / 5.0.x – 5.6.x |

`src/test/resources` 两个 tag 完全一致（`META-INF/services/io.microsphere.hibernate.entity.EntityCallback` 未变）。

## 2. 模块级总览

| Maven 模块 | `0.1.10` Java 文件 / 资源文件 | `0.2.10` Java 文件 / 资源文件 | 匹配 | 差异概况 |
|------------|------------------------------|------------------------------|------|----------|
| microsphere-hibernate-parent | 0 / 0 | 0 / 0 | ✅ | 无测试源码 |
| microsphere-hibernate-dependencies | 0 / 0 | 0 / 0 | ✅ | 无测试源码 |
| microsphere-hibernate-core | 5 / 1 | 5 / 1 | ✅ | 删除 1、新增 1、修改 1、3 个无差异；资源文件无差异 |
| microsphere-hibernate-test | 6 / 0 | 6 / 0 | ✅ | 修改 1、5 个无差异 |

> `microsphere-hibernate-test` 模块的测试用例主体（`AbstractHibernateH2Test` 内嵌的 `Session`/`StatelessSession`
> 场景）位于该模块的 `src/main`，其差异记录在
> [main-diff 第 3.2 节](main-diff-0.2.10-vs-0.1.10.md)，本文不重复计入。

## 3. 文件级差异

### 3.1 microsphere-hibernate-core / src/test

包根：`io.microsphere.hibernate`

| 文件 | 变更 | 差异要点 | 归类 | 原因解释 |
|------|------|----------|------|----------|
| `DelegatingInterceptorTest.java` | **删除**（202 行，15 个 `@Test`） | 基于 `MockitoExtension` 的装饰器全量验证：`null` 委托回落 `EmptyInterceptor.INSTANCE`，以及 `onLoad`/`onFlushDirty`/`onSave`/`onDelete`/集合回调/`preFlush`/`postFlush`/`isTransient`/`findDirty`/`instantiate`/`getEntityName`/`getEntity`/三个事务回调/`onPrepareStatement`/`getDelegate` 的逐方法转发断言（签名使用 `Serializable` 主键与 `EntityMode.POJO`） | 功能裁剪 + API 兼容 | 与被测类 `DelegatingInterceptor` 一同移除：Hibernate 6+ 的 `Interceptor` 接口形态使这批断言无法编译（`EntityMode` 不存在、主键形参改为 `Object`）。0.2.x 上自定义拦截器可直接实现接口，无需装饰器，也就没有对应的委托测试。 |
| `entity/EntityCallbackListenerTest.java` | **新增**（46 行，1 个 `@Test`） | `@BeforeEach` 直接 `new EntityCallbackListener()`（同包可访问包级类），断言 `requiresPostCommitHandling(null)` 返回 `false` | 测试覆盖（回归） | 0.2.10 删除了 `EntityCallbackListener` 中拼写错误的 `requiresPostCommitHanding(...)` 覆写（Hibernate 6 已移除该 default 方法），此前该断言仅被间接覆盖；新增测试把保留下来的 `requiresPostCommitHandling` 契约固化为直接回归用例，确保删除旧覆写后行为仍为"不参与 post-commit 处理"。 |
| `entity/TestEntityCallback.java` | 修改 | `onLoad(...)` 覆写签名新增 `Boolean readOnly` 形参；`logger.trace` 格式串增加 `readOnly : {}` 并补一个实参；`super.onLoad(...)` 转发同步补参 | API 兼容 | 与 `EntityCallback#onLoad` 契约变更（见 main-diff 3.1）严格同步。该文件是 `META-INF/services` 注册的测试实现，签名不匹配会导致 ServiceLoader 加载后无法回调。 |
| `constants/PropertyConstantsTest.java` | 无差异 | — | — | 常量契约未变。 |
| `entity/EntityCallbackIntegratorTest.java` | 无差异 | — | — | 集成器注册结果的断言与新增的 `integrate(Metadata, BootstrapContext, …)` 入口兼容，无需修改。 |
| `entity/EntityCallbackListenerDuplicationStrategyTest.java` | 无差异 | — | — | 去重策略行为未变。 |

### 3.2 microsphere-hibernate-test / src/test

包根：`io.microsphere.hibernate.test`

| 文件 | 变更 | 差异要点 | 归类 | 原因解释 |
|------|------|----------|------|----------|
| `HibernateAnnotationTest.java` | 修改 | 把顶层类上的 4 个 `@HibernateRuntime` **静态字段**（`Configuration`、`SessionFactory`、`Session`、`StatelessSession`）及其 `test()`（断言前两者非空、后两者为 `null`）整体下沉到新增的 `@Nested class StaticFieldTest`；顶层类不再持有任何 `@HibernateRuntime` 字段；断言内容与注释（"Session 与 StatelessSession 不得注入静态字段"）保持不变，`@Nested` 顺序为 `FieldTest` → `StaticFieldTest` → `ParameterTest` | 测试覆盖 + 结构重构 | `HibernateExtension#beforeAll` 的注释即"注入**当前**测试类（可能是嵌套类）的静态字段"：下沉到 `StaticFieldTest` 后，静态注入路径改为在嵌套类的 `beforeAll` 中执行，覆盖到了"嵌套测试类的静态字段注入"这一此前未被验证的分支；同时把静态可变状态从外层类隔离出去，与 `FieldTest`/`ParameterTest` 形成三种注入方式（实例字段 / 静态字段 / 方法参数）对称并列的结构，避免外层静态字段在嵌套类生命周期之间残留。断言未变，说明这是纯覆盖范围与组织方式调整，而非行为修改。 |
| `HibernateH2Test.java` | 无差异 | — | — | 继承 `AbstractHibernateH2Test` 的 H2 场景装配方式不变。 |
| `entity/{User,Order,Product,UserProfile}Test.java` | 无差异 | — | — | 4 个实体单测只依赖 getter/setter 与 `equals/hashCode` 语义，与 Hibernate 版本线无关。 |

## 4. 按口径排除的差异

本次 `src/test` 的 4 处变更中，**没有**属于以下类别的差异需要排除：

- Java 8 与 Java 17+ 的语法/API 差异；
- `javax.*`（Java EE）与 `jakarta.*`（Jakarta EE）的导入差异。

（`javax` → `jakarta` 的迁移只出现在 `src/main` 的实体与扩展类中，见
[main-diff 第 4 节](main-diff-0.2.10-vs-0.1.10.md)。）

## 5. 结论

`src/test` 层面的差异是 `src/main` 差异的**镜像**，规模很小但方向明确：

1. **收缩**：`DelegatingInterceptorTest` 随被测类删除，是两条版本线之间唯一的**测试能力损失**（1.x 上仍有 15 个用例覆盖委托行为）。
2. **补强**：`EntityCallbackListenerTest` 新增，把 Hibernate 6 移除拼写错误方法后遗留的 `requiresPostCommitHandling` 契约固化为直接断言。
3. **跟随契约**：`TestEntityCallback` 的 `onLoad` 签名同步，属必要的编译级适配。
4. **覆盖分支扩展**：`HibernateAnnotationTest` 通过 `StaticFieldTest` 把静态字段注入下沉到嵌套类，覆盖 `HibernateExtension` 的嵌套类静态注入路径。

测试文件数（仅计 `.java`）：`core` 两侧均为 5 个，其中 `DelegatingInterceptorTest`（15 个用例）被 `EntityCallbackListenerTest`（1 个用例）替换；`microsphere-hibernate-test` 两侧均为 6 个文件，断言内容未变，仅调整结构。
