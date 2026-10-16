# Microsphere Hibernate 用户文档（1.x / 0.1.x）

[English](../README.md) · [中文](README.md)

本文档是 [Microsphere Hibernate](https://github.com/microsphere-projects/microsphere-hibernate) **0.1.x**
版本线（由 `release-1.x` 分支发布）的用户文档，介绍各 Maven 模块、每个模块的公开 API 以及如何在自己的项目中集成。

## 项目简介

Microsphere Hibernate 是围绕 [Hibernate ORM](https://hibernate.org/) 的一组轻量扩展，在 Hibernate 事件体系之上
提供了一套基于 Java `ServiceLoader` 的**实体生命周期回调**模型，并额外提供 `Interceptor` 委托工具与
JUnit 5 测试套件。

把 `microsphere-hibernate-core` 放到 classpath 后，你只需实现 `EntityCallback`，在
`META-INF/services/io.microsphere.hibernate.entity.EntityCallback` 中注册，Hibernate ORM 就会在加载、持久化、
合并、删除、复制、flush、evict、lock、refresh、插入/更新以及集合事件时通知它——无需编写 `EventListener` 样板
代码，也不需要自己往 `SessionFactory` 上注册任何东西。

`DelegatingInterceptor`（**1.x 线特有**）可以装饰任意 Hibernate `org.hibernate.Interceptor` 并在其周围增加行为，
这也是把*会话级（session-scoped）*回调插入到你并不拥有的 `SessionFactory` 的推荐做法。

`microsphere-hibernate-test` 提供注解驱动的测试装配（`@HibernateTest` / `@HibernateRuntime`），或者基于 H2 的抽象
测试基类，由它们替你管理 `Configuration`、`SessionFactory`、`Session` 与 `StatelessSession`。

## 适用版本

| **项目** | **取值** |
|---|---|
| 分支 | `release-1.x`（开发分支：`dev-1.x`） |
| 版本线 | `0.1.x` |
| 已发布的最新版本 | `0.1.10` |
| 分支当前 revision | `0.1.11-SNAPSHOT`（见根 `pom.xml` 的 `<revision>`） |
| Maven `groupId` | `io.github.microsphere-projects` |
| 根聚合 POM | `io.github.microsphere-projects:microsphere-hibernate` |
| 上游 parent | `io.github.microsphere-projects:microsphere-spring-cloud-parent:0.1.26` |
| Hibernate `groupId` | `org.hibernate`（Hibernate 5 坐标，**不是** `org.hibernate.orm`） |
| 持久化注解 | `javax.persistence`（不是 `jakarta.persistence`） |
| Java 基线 | 8 —— 本仓库未声明 `java.version` 覆盖项，以 JDK 8 的 CI 矩阵条目为准 |

## 兼容范围

| **0.1.x 线** | **最低版本** | **最高版本** |
|---|---|---|
| **JDK** | 8 | 25 |
| **Hibernate Core** | 5.0.x | 5.6.x |
| **Spring Cloud** | Hoxton | 2021.0.x |

兼容范围由构建 profile 而非代码来保证：

- `microsphere-hibernate-parent` 中声明了 `hibernate-5.0` … `hibernate-5.6` Maven profile，用于锁定
  `org.hibernate:hibernate-core`，其中 `hibernate-5.6` 默认激活。详见
  [microsphere-hibernate-parent](microsphere-hibernate-parent.md)。
- `spring-cloud-hoxton`、`spring-cloud-2020`、`spring-cloud-2021` profile 继承自
  `microsphere-spring-cloud-parent:0.1.26`。
- GitHub 构建矩阵（`.github/workflows/maven-build.yml` 与 `maven-build-java11+.yml`）覆盖：
  - JDK **8** × Spring Cloud {hoxton, 2020, 2021} × Hibernate {5.0, 5.1, 5.2, 5.3, 5.4, 5.5, 5.6}
  - JDK **11、17、21、25** × Spring Cloud {hoxton, 2020, 2021} × Hibernate {5.3, 5.4, 5.5, 5.6}
- Java 9 及以上由 `java9+` profile 补充 `javax.xml.bind:jaxb-api` 依赖，因为 Hibernate 5.x 仍需要已被 JDK 移除
  的 JAXB 类。

> **不要在同一个应用中混用 `0.1.x` 与 `0.2.x` 构件。** 0.2.x（current）线面向 Hibernate ORM
> 6.2.x–7.3.x、`org.hibernate.orm` 坐标、`jakarta.persistence` 与 JDK 17+，文档见
> [../../../current/user-guide/README.md](../../../current/user-guide/README.md)。

## 模块文档

| **模块** | **Artifact** | **打包** | **文档** |
|---|---|---|---|
| Parent POM | `microsphere-hibernate-parent` | `pom` | [microsphere-hibernate-parent.md](microsphere-hibernate-parent.md) |
| BOM | `microsphere-hibernate-dependencies` | `pom` | [microsphere-hibernate-dependencies.md](microsphere-hibernate-dependencies.md) |
| 核心 | `microsphere-hibernate-core` | `jar` | [microsphere-hibernate-core.md](microsphere-hibernate-core.md) |
| 测试套件 | `microsphere-hibernate-test` | `jar` | [microsphere-hibernate-test.md](microsphere-hibernate-test.md) |

## 快速开始

### 1. 引入 BOM

BOM（`microsphere-hibernate-dependencies`）把 `microsphere-hibernate-core` 与
`microsphere-hibernate-test` 锁定到同一版本：

```xml

<dependencyManagement>
    <dependencies>
        ...
        <!-- Microsphere Hibernate Dependencies -->
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-hibernate-dependencies</artifactId>
            <version>${microsphere-hibernate.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        ...
    </dependencies>
</dependencyManagement>
```

声明版本属性，例如：

```xml

<properties>
    <microsphere-hibernate.version>0.1.10</microsphere-hibernate.version>
</properties>
```

### 2. 引入核心模块及其运行期依赖

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-hibernate-core</artifactId>
</dependency>
```

`microsphere-hibernate-core` 把 `org.hibernate:hibernate-core`、
`io.github.microsphere-projects:microsphere-java-core`、
`io.github.microsphere-projects:microsphere-annotation-processor` 声明为 **optional**，因此它们不会传递引入。
应用需要自行提供 `hibernate-core` 与 `microsphere-java-core`（回调机制在运行时会用到
`io.microsphere.collection.ListUtils` 与 `io.microsphere.lang.Prioritized`）：

```xml
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-core</artifactId>
</dependency>
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-core</artifactId>
</dependency>
```

### 3. 注册 `EntityCallback`

创建 `src/main/resources/META-INF/services/io.microsphere.hibernate.entity.EntityCallback`：

```
com.acme.order.logging.OrderEntityCallback
```

只实现你关心的回调（23 个方法全部是 `default` 空实现）：

```java
package com.acme.order.logging;

import io.microsphere.hibernate.entity.EntityCallback;

public class OrderEntityCallback implements EntityCallback {

    @Override
    public void onPersist(Object entity, String entityName) {
        System.out.println("Persisting " + entityName + " -> " + entity);
    }
}
```

无需任何 `SessionFactory` 装配：`microsphere-hibernate-core` 自带
`META-INF/services/org.hibernate.integrator.spi.Integrator`（内容为
`io.microsphere.hibernate.entity.EntittyCallbackIntegrator`），Hibernate ORM 5.x 会自动发现该 integrator，并把
监听器注册到全部受支持的事件类型上。

### 4.（可选）用 `DelegatingInterceptor` 做会话级回调

如果你只需要让回调作用于某个 `Session`，或者想包装第三方提供的 interceptor，使用 `DelegatingInterceptor`：

```java
Interceptor thirdParty = new AuditInterceptor();
Interceptor interceptor = new DelegatingInterceptor(thirdParty) {
    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state,
                          String[] propertyNames, Type[] types) {
        System.out.println("Saving " + entity);
        return super.onSave(entity, id, state, propertyNames, types);
    }
};
Session session = sessionFactory.withOptions().interceptor(interceptor).openSession();
```

完整契约见 [microsphere-hibernate-core.md](microsphere-hibernate-core.md)（`DelegatingInterceptor` 一节）。

### 5.（可选）测试支持

以 `test` 作用域引入测试套件：

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-hibernate-test</artifactId>
    <scope>test</scope>
</dependency>
```

```java
@HibernateTest(
        properties = {
                "hibernate.connection.driver_class=org.h2.Driver",
                "hibernate.connection.url=jdbc:h2:mem:demo_db;DB_CLOSE_DELAY=-1",
                "hibernate.connection.username=sa",
                "hibernate.connection.password=",
                "hibernate.dialect=org.hibernate.dialect.H2Dialect",
                "hibernate.hbm2ddl.auto=update"
        },
        scanEntityPackages = {"com.acme.order.entity"}
)
class OrderRepositoryTest {

    @HibernateRuntime
    private Session session;

    @Test
    void shouldPersistOrder() {
        assertNotNull(session);
    }
}
```

完整契约见 [microsphere-hibernate-test.md](microsphere-hibernate-test.md)。

## 与 0.2.x（current）线的差异

| **主题** | **1.x（本指南，`release-1.x`）** | **0.2.x（current，`release`）** |
|---|---|---|
| 最新版本 | `0.1.10`（开发中为 `0.1.11-SNAPSHOT`） | `0.2.10` |
| JDK | 8 – 25 | 17 – 25 |
| Hibernate ORM | 5.0.x – 5.6.x（`org.hibernate:hibernate-core`） | 6.2.x – 7.3.x（`org.hibernate.orm`、`hibernate-platform`） |
| 持久化注解 | `javax.persistence.*` | `jakarta.persistence.*` |
| Spring Cloud | Hoxton – 2021.0.x（`microsphere-spring-cloud` 0.1.26 线） | 2022.0.x – 2025.0.x |
| `DelegatingInterceptor` | **存在**（`io.microsphere.hibernate`） | **已移除** —— 它覆写的 `Interceptor` 方法使用 Hibernate 5 签名（`Serializable id`、`EntityMode`、原始 `Iterator`），无法在 Hibernate 6+ 上编译 |
| `EntityCallback#onLoad` | `onLoad(id, entityClassName, entity, lockMode, isAssociationFetch, result, loadType)`，**7 个参数** | 增加 `Boolean readOnly` 参数，**8 个参数** |
| `PropertyConstants` | 键完全相同（`microsphere.hibernate.` / `microsphere.hibernate.enabled`） | 键完全相同 |

从 1.x 迁移到 0.2.x 的实际影响：移除所有 `DelegatingInterceptor` 用法（改为普通 `Interceptor`，并按 Hibernate 6
的签名重写）、按新增的 `readOnly` 参数调整 `EntityCallback#onLoad` 实现，并把 `javax.persistence` 导入替换为
`jakarta.persistence`。

## 配置键

`io.microsphere.hibernate.constants.PropertyConstants` 声明了本项目使用的属性命名空间：

| **常量** | **取值 / 默认值** | **说明** |
|---|---|---|
| `MICROSPHERE_HIBERNATE_PROPERTY_NAME_PREFIX` | `microsphere.hibernate.` | 本项目所有配置键的前缀 |
| `MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_NAME` | `microsphere.hibernate.enabled` | 从系统属性读取 |
| `DEFAULT_MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_VALUE` | `"true"` | enabled 开关的默认值 |
| `DEFAULT_MICROSPHERE_HIBERNATE_ENABLED` | `true` | 上者的布尔形式 |

## 从源码构建

使用库本身并不需要构建源码。若要体验 `release-1.x` 最新代码：

```bash
git clone -b release-1.x https://github.com/microsphere-projects/microsphere-hibernate.git
cd microsphere-hibernate
./mvnw package                        # Linux/MacOS
# mvnw.cmd package                    # Windows
```

项目使用 CI 友好版本（`<version>${revision}</version>`），本地安装时请传入 `-Drevision`：

```bash
./mvnw -Drevision=0.1.11-SNAPSHOT clean install
```

要针对某个具体的 Hibernate 版本验证，激活对应 profile（`hibernate-5.0` … `hibernate-5.2` 需要 JDK 8）：

```bash
./mvnw -Phibernate-5.4 clean test
```

## 延伸阅读

- [microsphere-hibernate-parent.md](microsphere-hibernate-parent.md) —— 依赖管理与 Hibernate 5.x 版本 profile
- [microsphere-hibernate-dependencies.md](microsphere-hibernate-dependencies.md) —— BOM
- [microsphere-hibernate-core.md](microsphere-hibernate-core.md) —— `EntityCallback`、`EntittyCallbackIntegrator`、`DelegatingInterceptor`、`PropertyConstants`
- [microsphere-hibernate-test.md](microsphere-hibernate-test.md) —— JUnit 5 测试支持与示例实体
- 项目 README（仓库根目录）：[../../../../README.md](../../../../README.md)
- [发布说明](../../../../release-notes.md)
- JavaDoc：[microsphere-hibernate-core](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-hibernate-core)、
  [microsphere-hibernate-test](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-hibernate-test)
