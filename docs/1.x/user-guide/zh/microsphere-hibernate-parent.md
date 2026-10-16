# microsphere-hibernate-parent（1.x）

[English](../microsphere-hibernate-parent.md) · [中文](microsphere-hibernate-parent.md)

## 1. 模块定位与 Maven 坐标

`microsphere-hibernate-parent` 是一个**纯 POM 模块**：没有源码、没有资源文件，也不会产出 JAR。它是仓库中所有
可构建模块（`microsphere-hibernate-core`、`microsphere-hibernate-test`）以及对外发布的 BOM
（`microsphere-hibernate-dependencies`）的*技术 parent*。

它的作用是把"我到底针对哪个 Hibernate 5.x 构建"变成一次 Maven profile 切换，并让所有模块对齐同一个上游
Microsphere Spring Cloud 发布线。

| **项目** | **取值** |
|---|---|
| GroupId | `io.github.microsphere-projects` |
| ArtifactId | `microsphere-hibernate-parent` |
| 版本 | `${revision}` → `release-1.x` 上为 `0.1.11-SNAPSHOT`；最新发布版本 `0.1.10` |
| 打包 | `pom` |
| Name | `Microsphere :: Hibernate :: Parent` |
| POM 文件 | `microsphere-hibernate-parent/pom.xml` |
| 父 POM | `io.github.microsphere-projects:microsphere-hibernate:${revision}`（根聚合 POM） |

继承链：

```
microsphere-spring-cloud-parent:0.1.26          （上游：Spring Boot / Spring Cloud / 测试工具链）
  └── microsphere-hibernate:${revision}         （根聚合 POM，声明 <revision> 与 4 个模块）
        ├── microsphere-hibernate-parent:${revision}     <-- 本模块（版本 profile + dependencyManagement）
        │     ├── microsphere-hibernate-dependencies     （BOM，继承自本模块）
        │     ├── microsphere-hibernate-core             （继承自本模块）
        │     └── microsphere-hibernate-test             （继承自本模块）
        └── （聚合上述 4 个模块用于 reactor 构建）
```

## 2. 版本信息与兼容范围

| **维度** | **本 POM 所约束的范围** |
|---|---|
| JDK | 8 – 25（Java 8 为基线；只有 JDK 8 会在 CI 中跑满 7 个 Hibernate profile） |
| Hibernate Core | 5.0.x – 5.6.x，每个小版本一个 Maven profile，通过 `${hibernate.version}` 锁定 |
| Spring Cloud | Hoxton – 2021.0.x，经由 `microsphere-spring-cloud-dependencies:0.1.26` 引入 |
| Microsphere 版本 | `microsphere-spring-cloud.version = 0.1.26` |
| Hibernate `groupId` | `org.hibernate`（Hibernate 5 坐标） |
| 默认构建 | `hibernate-5.6` → `org.hibernate:hibernate-core:5.6.15.Final`（`activeByDefault`） |

`spring-cloud-hoxton`、`spring-cloud-2020`、`spring-cloud-2021` profile **不在本 POM 中声明**，它们来自
`microsphere-spring-cloud-parent:0.1.26`，其中默认激活的是 `spring-cloud-2021`（Spring Boot `2.6.15`、
Spring Cloud `2021.0.9`）。

## 3. 该 POM 暴露的内容（它的"API"）

### 3.1 属性

| **属性** | **取值 / 来源** | **用途** |
|---|---|---|
| `microsphere-spring-cloud.version` | `0.1.26`（字面量） | 被 import 的 Microsphere Spring Cloud BOM 版本 |
| `hibernate.version` | 由激活的 `hibernate-5.x` profile 设置 | `org.hibernate:hibernate-core` 的版本 |
| `revision` | 继承自根 POM（`0.1.11-SNAPSHOT`） | 所有模块共用的 CI 友好版本 |

### 3.2 `dependencyManagement`

只有两项：

```xml

<dependencyManagement>
    <dependencies>
        <!-- Hibernate Dependencies -->
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>${hibernate.version}</version>
        </dependency>

        <!-- Microsphere Spring Cloud Dependencies -->
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-spring-cloud-dependencies</artifactId>
            <version>${microsphere-spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

被 import 的 Spring Cloud BOM 提供了 JUnit Jupiter、Mockito、H2、Logback、`microsphere-java-core`、
`microsphere-java-test`、`microsphere-annotation-processor`、`jaxb-api` 的版本——这正是子模块 POM 中这些依赖
**不写** `<version>` 的原因（见 `microsphere-hibernate-core/pom.xml` 与
`microsphere-hibernate-test/pom.xml`）。

> 注意 groupId：在本版本线上 Hibernate 是 `org.hibernate:hibernate-core`。0.2.x 线使用的
> `org.hibernate.orm:hibernate-platform` 在这里并不存在。

### 3.3 Hibernate 5.x profile 一览

| **profile id** | **`hibernate.version`** | **是否激活** |
|---|---|---|
| `hibernate-5.6` | `5.6.15.Final` | 是 —— `activeByDefault` |
| `hibernate-5.5` | `5.5.9.Final` | 否 |
| `hibernate-5.4` | `5.4.33.Final` | 否 |
| `hibernate-5.3` | `5.3.38.Final` | 否 |
| `hibernate-5.2` | `5.2.18.Final` | 否 |
| `hibernate-5.1` | `5.1.9.Final` | 否 |
| `hibernate-5.0` | `5.0.9.Final` | 否 |

由于每个 profile 只做一件事——设置一个属性，你激活的 profile 决定了整个 reactor（包括
`microsphere-hibernate-core` 与 `microsphere-hibernate-test` 的测试）使用的 Hibernate 版本。这正是同一份源码
能够同时兼容 Hibernate 5.0 到 5.6 的原因。

## 4. 使用方式

### 4.1 作为使用者：通常不直接使用本 POM

应用应当 import BOM（见
[microsphere-hibernate-dependencies.md](microsphere-hibernate-dependencies.md)）。BOM 继承自本 POM，因此
`org.hibernate:hibernate-core` 与 Microsphere Spring Cloud 的依赖管理也会一并带入。

```xml
<!-- 推荐：依赖 BOM，而不是 parent -->
<dependencyManagement>
    <dependencies>
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

由于已发布的 BOM 的 parent 默认激活 `hibernate-5.6`，你继承到的受管 Hibernate 版本是 `5.6.15.Final`。如果应用
需要其他 Hibernate 5.x 小版本，请在你自己的 `dependencyManagement` 中显式声明（直接声明的条目总是优先于 import
进来的 BOM）：

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
        <!-- 为本应用覆盖受管的 Hibernate 版本 -->
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>5.4.33.Final</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 4.2 作为维护者：针对指定 Hibernate 版本构建 reactor

```bash
# 默认（Hibernate 5.6.15.Final）
./mvnw clean test

# Hibernate 5.4.33.Final
./mvnw -Phibernate-5.4 clean test

# CI 使用的完整矩阵组合（JDK 8 + Spring Cloud Hoxton + Hibernate 5.0）
./mvnw --batch-mode -Drevision=0.0.1-SNAPSHOT test \
       --activate-profiles test,coverage,spring-cloud-hoxton,hibernate-5.0
```

CI 的确切命令定义在 `.github/workflows/maven-build.yml`（JDK 8 × 7 个 Hibernate profile ×
{`spring-cloud-hoxton`、`spring-cloud-2020`、`spring-cloud-2021`}）与
`.github/workflows/maven-build-java11+.yml`（JDK 11/17/21/25 × `hibernate-5.3` … `hibernate-5.6`）。

### 4.3 继承本 POM（可选，用于与本项目对齐的多模块构建）

```xml
<parent>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-hibernate-parent</artifactId>
    <version>0.1.10</version>
</parent>
```

继承后可获得由 `${hibernate.version}` 管理的 `org.hibernate:hibernate-core`、Spring Cloud BOM 的 import 以及
全部 7 个 profile。代价是你的构建也会绑定到 Microsphere parent 链（编译器、测试、覆盖率与发布配置）。因此对
普通应用来说，§ 4.1 的 BOM import 是更轻量的选择。

## 5. 说明与注意事项

- **`${revision}` 无法从 POM 文件直接解析。** 所有模块都写的是 `<version>${revision}</version>`；依赖已发布
  构件时请使用具体版本（如 `0.1.10`）。本地构建时必须传 `-Drevision=...`，否则 Maven 无法构建模型。
- **本 POM 里没有 `java9+` profile。** Java 9+ 的 `javax.xml.bind:jaxb-api` 兜底依赖声明在
  `microsphere-hibernate-core/pom.xml` 与 `microsphere-hibernate-test/pom.xml` 中。
- **profile 只影响构建期。** 构建时用 `hibernate-5.0` 并不会改变*已发布* POM 的受管版本；BOM 是按默认 profile
  解析后发布的。应用若要锁定较旧的 5.x，请始终显式声明 Hibernate。
- **兼容性是被验证过的，而不是 POM 声称的。** core 与 test 模块按激活的 profile 编译；它们依赖的 API 面
  （带 `Serializable id` 与 `EntityMode` 的 `Interceptor`、三参数的
  `Integrator#integrate(Metadata, SessionFactoryImplementor, SessionFactoryServiceRegistry)`、
  `org.hibernate.event.spi.*`）在 5.0 – 5.6 之间保持稳定，这才是矩阵得以成立的前提。

## 6. 相关文档

- [microsphere-hibernate-dependencies.md](microsphere-hibernate-dependencies.md) —— 由本 parent 发布的 BOM
- [microsphere-hibernate-core.md](microsphere-hibernate-core.md) —— 针对这些 profile 构建的运行期 API
- [microsphere-hibernate-test.md](microsphere-hibernate-test.md) —— 针对这些 profile 构建的测试套件
- [README.md](README.md) —— 1.x 用户文档索引
- 源码：`microsphere-hibernate-parent/pom.xml`、`pom.xml`、`.github/workflows/maven-build.yml`、
  `.github/workflows/maven-build-java11+.yml`（`release-1.x` 分支）
