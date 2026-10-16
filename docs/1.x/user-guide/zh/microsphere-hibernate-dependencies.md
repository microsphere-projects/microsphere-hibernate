# microsphere-hibernate-dependencies（1.x）

[English](../microsphere-hibernate-dependencies.md) · [中文](microsphere-hibernate-dependencies.md)

## 1. 模块定位与 Maven 坐标

`microsphere-hibernate-dependencies` 是 Microsphere Hibernate 1.x 的 **BOM（Bill Of Materials，物料清单）**。
它是纯 POM 模块：没有源码、没有 JAR，除版本锁定外不做任何事情。import 它的含义就是"把本发行版所有 Microsphere
Hibernate 构件对齐到同一版本，并且不要让我重复写版本号"。

| **项目** | **取值** |
|---|---|
| GroupId | `io.github.microsphere-projects` |
| ArtifactId | `microsphere-hibernate-dependencies` |
| 版本 | `${revision}` → `release-1.x` 上为 `0.1.11-SNAPSHOT`；最新发布版本 `0.1.10` |
| 打包 | `pom` |
| Name | `Microsphere :: Hibernate :: Dependencies` |
| POM 文件 | `microsphere-hibernate-dependencies/pom.xml` |
| 父 POM | `io.github.microsphere-projects:microsphere-hibernate-parent:${revision}` |
| 使用方式 | 在你自己的 `<dependencyManagement>` 中 `<scope>import</scope>` + `<type>pom</type>` |

## 2. 版本信息与兼容范围

| **维度** | **取值** |
|---|---|
| 版本线 | `0.1.x`（分支 `release-1.x`），最新发布版本 `0.1.10` |
| JDK | 8 – 25 |
| Hibernate Core | 5.0.x – 5.6.x，坐标 `org.hibernate:hibernate-core`（默认 `5.6.15.Final`） |
| Spring Cloud | Hoxton – 2021.0.x（经由 `microsphere-spring-cloud-dependencies:0.1.26`） |
| 受管构件 | `microsphere-hibernate-core`、`microsphere-hibernate-test` |

由于 BOM 继承 `microsphere-hibernate-parent`，import 它同时会带来那里定义的 Hibernate 与 Spring Cloud 依赖管理
—— 7 个 `hibernate-5.x` profile 详见 [microsphere-hibernate-parent.md](microsphere-hibernate-parent.md)。

## 3. BOM 声明的内容

### 3.1 本 POM 自身声明的完整受管列表

```xml

<dependencyManagement>
    <dependencies>

        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-hibernate-core</artifactId>
            <version>${revision}</version>
        </dependency>

        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-hibernate-test</artifactId>
            <version>${revision}</version>
        </dependency>

    </dependencies>
</dependencyManagement>
```

只有两项，且都用 `${revision}`：两个 Microsphere Hibernate 构件始终一起发布，因此在一个应用内不可能出现版本漂移。

### 3.2 从 parent 链*继承*而来的受管内容

| **受管项** | **版本来源** | **为什么重要** |
|---|---|---|
| `org.hibernate:hibernate-core` | `${hibernate.version}`，默认 `5.6.15.Final` | 显式添加 Hibernate 时可以省略版本（注意它并**不会**被传递引入——core 模块中它是 `optional`）。 |
| `io.github.microsphere-projects:microsphere-spring-cloud-dependencies`（被 import 的 BOM） | `0.1.26` | 提供 `microsphere-java-core`、`microsphere-java-test`、`microsphere-annotation-processor`、JUnit Jupiter、Mockito、H2、Logback 以及 `javax.xml.bind:jaxb-api` 的版本。 |

## 4. 使用方式

### 4.1 import BOM

```xml

<properties>
    <microsphere-hibernate.version>0.1.10</microsphere-hibernate.version>
</properties>

<dependencyManagement>
    <dependencies>
        <!-- Microsphere Hibernate Dependencies -->
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-hibernate-dependencies</artifactId>
            <version>${microsphere-hibernate.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 4.2 随后按无版本方式声明模块

运行期扩展：

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-hibernate-core</artifactId>
</dependency>
<!-- hibernate-core 在上游是 optional，需要你自己声明 -->
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-core</artifactId>
</dependency>
```

测试支持：

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-hibernate-test</artifactId>
    <scope>test</scope>
</dependency>
```

### 4.3 项目自身如何使用这些受管版本

`microsphere-hibernate-core/pom.xml` 在 reactor 内部使用的是同一套机制（那里的模块与兄弟模块一起构建，所以直接
使用 `${revision}`）：

```xml
<!-- Microsphere Hibernate Test -->
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-hibernate-test</artifactId>
    <version>${revision}</version>
    <scope>test</scope>
</dependency>
```

出处：`microsphere-hibernate-core/pom.xml`（`release-1.x` 分支）。正是这一模式让 core 模块的
`EntityCallbackIntegratorTest` 能够跑在 test 模块提供的 H2 测试装置上。

### 4.4 锁定其他 Hibernate 5.x 版本

你自己 `dependencyManagement` 中声明的依赖优先于 import 进来的 BOM：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-hibernate-dependencies</artifactId>
            <version>0.1.10</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>5.2.18.Final</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

请把版本保持在 5.0.x – 5.6.x 区间：1.x 构件是针对 Hibernate 5 签名编译的
（`Interceptor#onLoad(Object, Serializable, …)`、
`Interceptor#instantiate(String, EntityMode, Serializable)`、
`Integrator#integrate(Metadata, SessionFactoryImplementor, SessionFactoryServiceRegistry)`），Hibernate 6+
会在类定义阶段就失败。

## 5. 说明与注意事项

- **BOM 不会添加依赖。** import 只影响版本解析；你仍然需要自己声明要用的模块。
- **上游的 `optional` 意味着 Hibernate 与 Microsphere Java 需要你自己引入。**
  `microsphere-hibernate-core` 与 `microsphere-hibernate-test` 都把
  `org.hibernate:hibernate-core`、`microsphere-java-core`、`microsphere-java-test`、JUnit Jupiter、Mockito、H2、
  Logback 标记为 `optional`（或 `test` 作用域），因此没有任何传递依赖。
- **无版本号的本地构建。** 所有模块使用 `<version>${revision}</version>`；从源码构建 BOM 时请传
  `-Drevision=…`（CI 使用 `-Drevision=0.0.1-SNAPSHOT`）。
- **import 顺序。** 如果你同时 import Spring Boot 或 Spring Cloud 的 BOM，请把你希望保留其版本的那个 BOM 放在
  *前面* —— Maven 中同一受管坐标以先声明者为准。
- **这里没有 pluginManagement、没有 distributionManagement、也没有 repositories**；它们位于上游
  `microsphere-spring-cloud-parent` 链中。
- **不要混用版本线。** 同时 import `microsphere-hibernate-dependencies:0.1.10` 与任何 `0.2.x` 构件会产生不兼容
  的 classpath（`org.hibernate` vs `org.hibernate.orm`、`javax.persistence` vs `jakarta.persistence`）。

## 6. 相关文档

- [microsphere-hibernate-parent.md](microsphere-hibernate-parent.md) —— 继承的依赖管理来自哪里
- [microsphere-hibernate-core.md](microsphere-hibernate-core.md) —— 被管理的运行期模块
- [microsphere-hibernate-test.md](microsphere-hibernate-test.md) —— 被管理的测试模块
- [README.md](README.md) —— 1.x 用户文档索引
- 源码：`microsphere-hibernate-dependencies/pom.xml`、`microsphere-hibernate-parent/pom.xml`、
  `microsphere-hibernate-core/pom.xml`（`release-1.x` 分支）
