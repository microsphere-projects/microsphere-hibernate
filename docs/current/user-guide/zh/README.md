# Microsphere Hibernate —— 用户文档（0.2.x）

本文档是 [Microsphere Hibernate](https://github.com/microsphere-projects/microsphere-hibernate) **0.2.x**
版本线（`release` 分支）的用户文档，说明各个 Maven 模块的定位、公开 API，以及如何在自己的项目中集成它们。

英文版本：[../README.md](../README.md)

## Microsphere Hibernate 是什么？

Microsphere Hibernate 是围绕 [Hibernate ORM](https://hibernate.org/) 的一组轻量扩展，它在 Hibernate 事件体系之上
增加了基于 Java `ServiceLoader` 的**实体生命周期回调**模型，并同时提供一套轻量的 Hibernate JUnit 5 测试工具。

只要把 `microsphere-hibernate-core` 放到 classpath 上，你只需实现 `EntityCallback` 接口，并把实现类全限定名写入
`META-INF/services/io.microsphere.hibernate.entity.EntityCallback`，Hibernate ORM 就会在 load、persist、merge、
delete、replicate、flush、evict、lock、refresh、insert/update 以及集合类事件发生时回调你的实现——你既不用编写
`EventListener` 模板代码，也不用自己去 `SessionFactory` 上注册监听器。

`microsphere-hibernate-test` 则提供注解驱动的 `@HibernateTest` / `@HibernateRuntime` 测试装配方式（以及一个基于
H2 的抽象测试基类），由它替你管理 `Configuration`、`SessionFactory`、`Session` 与 `StatelessSession` 的生命周期。

## 适用版本

| **项目** | **取值** |
|----------|----------|
| 分支 | `release` |
| 版本线 | `0.2.x` |
| 已发布的最新版本 | `0.2.10` |
| 分支当前 revision | `0.2.11-SNAPSHOT`（见根 `pom.xml` 中的 `<revision>`） |
| Maven `groupId` | `io.github.microsphere-projects` |
| 根聚合 POM | `io.github.microsphere-projects:microsphere-hibernate` |
| 上游 parent | `microsphere-spring-cloud-parent:0.2.26` |

## 兼容性

| **0.2.x 版本线** | **最低版本** | **最高版本** |
|------------------|--------------|--------------|
| **JDK** | 17 | 25 |
| **Hibernate Core** | 6.2.x | 7.3.x |
| **Spring Cloud** | 2022.0.x | 2025.0.x |

上述兼容范围不是靠代码判断，而是靠构建 profile 来保证的：

- `microsphere-hibernate-parent` 中声明了 `hibernate-6.2` … `hibernate-7.3` 这一组 Maven profile，
  用于锁定 `org.hibernate.orm:hibernate-platform`。详见 [microsphere-hibernate-parent.md](microsphere-hibernate-parent.md)。
- `spring-cloud-2022` … `spring-cloud-2025` 这一组 profile 继承自 `microsphere-spring-cloud-parent`。
- 根 POM 中 `java.version` 设置为 `17`；GitHub 构建矩阵会在 JDK **17、21、25** 上编译并测试。

> 更早的 **0.1.x** 版本线（JDK 8、Hibernate ORM 5.0.x–5.6.x、Spring Cloud Hoxton–2021.0.x）在 `release-1.x`
> 分支上维护，文档见本 `docs` 目录下的 1.x 文档。请不要在同一个应用里混用 `0.1.x` 与 `0.2.x` 的构件。

## 模块列表

| **模块** | **artifactId** | **打包方式** | **文档** |
|----------|----------------|--------------|----------|
| 父工程 POM | `microsphere-hibernate-parent` | `pom` | [microsphere-hibernate-parent.md](microsphere-hibernate-parent.md) |
| BOM | `microsphere-hibernate-dependencies` | `pom` | [microsphere-hibernate-dependencies.md](microsphere-hibernate-dependencies.md) |
| 核心模块 | `microsphere-hibernate-core` | `jar` | [microsphere-hibernate-core.md](microsphere-hibernate-core.md) |
| 测试模块 | `microsphere-hibernate-test` | `jar` | [microsphere-hibernate-test.md](microsphere-hibernate-test.md) |

## 快速开始

### 1. 引入 BOM

BOM（`microsphere-hibernate-dependencies`）会把 `microsphere-hibernate-core` 与 `microsphere-hibernate-test`
锁定到同一个版本：

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

同时声明版本属性，例如：

```xml

<properties>
    <microsphere-hibernate.version>0.2.10</microsphere-hibernate.version>
</properties>
```

### 2. 引入核心模块

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-hibernate-core</artifactId>
</dependency>
```

`microsphere-hibernate-core` 把 `org.hibernate.orm:hibernate-core`、
`io.github.microsphere-projects:microsphere-java-core`、
`io.github.microsphere-projects:microsphere-annotation-processor` 都声明为 **optional** 依赖，因此它们**不会**被传递引入。
应用自身必须提供 `hibernate-core` 与 `microsphere-java-core`（回调机制在运行时会用到
`io.microsphere.collection.ListUtils` 与 `io.microsphere.lang.Prioritized`）：

```xml
<dependency>
    <groupId>org.hibernate.orm</groupId>
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

然后只实现你关心的回调方法：

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

无需任何 `SessionFactory` 装配代码：`microsphere-hibernate-core` 自带
`META-INF/services/org.hibernate.integrator.spi.Integrator` 注册项，Hibernate ORM 会自动发现
`EntittyCallbackIntegrator`，并为所有支持的事件类型注册监听器。

### 4.（可选）测试支持

以 `test` 作用域引入测试模块：

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

## 从源码构建

使用本库并不需要从源码构建。若要体验 `release` 分支的最新代码：

```bash
git clone https://github.com/microsphere-projects/microsphere-hibernate.git
cd microsphere-hibernate
./mvnw package                        # Linux/MacOS
# mvnw.cmd package                    # Windows
```

若要在指定的 Hibernate 版本上做验证，激活对应的 profile：

```bash
./mvnw -Phibernate-6.2 clean test
```

## 延伸阅读

- [microsphere-hibernate-parent.md](microsphere-hibernate-parent.md) —— 依赖管理与 Hibernate 版本 profile
- [microsphere-hibernate-dependencies.md](microsphere-hibernate-dependencies.md) —— BOM
- [microsphere-hibernate-core.md](microsphere-hibernate-core.md) —— `EntityCallback` API 与 SPI 集成
- [microsphere-hibernate-test.md](microsphere-hibernate-test.md) —— JUnit 5 测试支持与示例实体
- 项目 README（仓库根目录）：[../../../../README.md](../../../../README.md)
- [发布说明](../../../../release-notes.md)
- JavaDoc：[microsphere-hibernate-core](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-hibernate-core)、
  [microsphere-hibernate-test](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-hibernate-test)
