# microsphere-hibernate-dependencies

Microsphere Hibernate 的 **BOM（Bill of Materials，依赖清单）**。这是一个纯 POM 模块，唯一职责是锁定 Microsphere
Hibernate 各构件的版本，让使用方在自己的项目里永远不需要为它们写 `<version>`。

[← 返回用户文档索引](README.md) · 英文版本：[../microsphere-hibernate-dependencies.md](../microsphere-hibernate-dependencies.md)

## 1. 模块定位与 Maven 坐标

| **项目** | **取值** |
|----------|----------|
| `groupId` | `io.github.microsphere-projects` |
| `artifactId` | `microsphere-hibernate-dependencies` |
| `version` | `${revision}` → `release` 分支上为 `0.2.11-SNAPSHOT`（最近发布版本 `0.2.10`） |
| `packaging` | `pom` |
| `name` | `Microsphere :: Hibernate :: Dependencies` |
| 源码位置 | `microsphere-hibernate-dependencies/pom.xml` |
| 它的 parent | `microsphere-hibernate-parent`（见 [父工程文档](microsphere-hibernate-parent.md)） |
| 使用方式 | 在 `dependencyManagement` 中以 `<type>pom</type> <scope>import</scope>` 导入 |

它的依赖管理内容一览无余：

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

`${revision}` 是 Maven 的 CI 友好版本占位符，在根 POM 中声明（`release` 分支上为 `0.2.11-SNAPSHOT`）；发布出去的
POM 里则是具体版本号，例如 `0.2.10`。

## 2. 版本信息与兼容范围

| **维度** | **取值 / 范围** | **说明** |
|----------|-----------------|----------|
| 版本线 | `0.2.x` | 与整个 reactor 一起发布，四个构件永远共享同一版本 |
| JDK | 17 – 25 | 与[父工程](microsphere-hibernate-parent.md)一致 |
| Hibernate Core | 6.2.x – 7.3.x | BOM 自身没有重复声明，它通过 `microsphere-hibernate-parent` 继承 `hibernate-platform` |
| Spring Cloud | 2022.0.x – 2025.0.x | 通过 `microsphere-spring-cloud-dependencies`（`0.2.26`）继承而来 |
| 可直接使用的最新版本 | `0.2.10` | 导入块里写 `<version>0.2.10</version>` |

所有模块是一起发布的，因此 `microsphere-hibernate-core` 与 `microsphere-hibernate-test` 的版本始终**相同**。
绝不要混用 `0.1.x` 与 `0.2.x` 的构件。

作为参考，`0.2.10`（使用默认 `hibernate-7.3` profile 构建）发布的 POM 中解析出的版本如下：

| **依赖** | **`0.2.10` 发布时的版本** |
|----------|---------------------------|
| `org.hibernate.orm:hibernate-core` | `7.3.4.Final` |
| `io.github.microsphere-projects:microsphere-java-core` | `0.3.19` |
| `io.github.microsphere-projects:microsphere-annotation-processor` | `0.3.19` |
| `com.h2database:h2` | `2.3.232` |
| `org.junit.jupiter:junit-jupiter` | `6.1.3` |
| `org.mockito:mockito-core` / `mockito-junit-jupiter` | `5.17.0` |
| `ch.qos.logback:logback-classic` | `1.5.34` |

这些数字由当时激活的 Maven profile 决定，上游父工程或 `hibernate-*` profile 一变它们就会变。若你必须停留在某个
特定版本，请在自己项目中显式锁定。

## 3. BOM 提供的能力（它的“API”）

### 3.1 受管构件

| **构件** | **用途** | **在你项目中的常见 scope** |
|----------|----------|----------------------------|
| `microsphere-hibernate-core` | `EntityCallback` 模型 + Hibernate `Integrator` | `compile` |
| `microsphere-hibernate-test` | JUnit 5 支持：`@HibernateTest`、`@HibernateRuntime`、`HibernateExtension`、抽象测试基类、示例实体 | `test` |

### 3.2 契约要点

- BOM 没有 `<dependencies>` 段：仅仅导入它，你的 classpath 上不会多出任何东西。
- BOM 只管版本。它自己的 `<dependencyManagement>` 中只有上面两个 Microsphere Hibernate 构件；其余版本（Hibernate
  ORM、Spring Cloud）来自由 profile 驱动的父工程链（`microsphere-hibernate-parent` → `hibernate-platform` /
  `microsphere-spring-cloud-dependencies`），因此请显式声明你自己的 Hibernate ORM 版本，不要依赖继承值。
- 两个受管构件自身声明的依赖（`hibernate-core`、`microsphere-java-core`、`junit-jupiter`、`h2` 等）都是
  **`optional`** 的。版本被管理不等于会被传递引入；必须补齐的依赖清单见
  [microsphere-hibernate-core.md](microsphere-hibernate-core.md)。
- `microsphere-hibernate-test` 同样**不是**自包含的：你自己的 test 作用域里仍需显式声明所需的 JUnit 5 与
  Hibernate ORM 依赖。

## 4. 示例

### 4.1 推荐用法：BOM + 显式依赖

```xml

<properties>
    <microsphere-hibernate.version>0.2.10</microsphere-hibernate.version>
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

<dependencies>
    <!-- 版本由 BOM 管理 -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-hibernate-core</artifactId>
    </dependency>

    <!-- 测试模块，版本由 BOM 管理 -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-hibernate-test</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- 在 microsphere-hibernate-core 中是 optional，因此在这里显式声明 -->
    <dependency>
        <groupId>org.hibernate.orm</groupId>
        <artifactId>hibernate-core</artifactId>
        <version>7.3.4.Final</version>
    </dependency>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-java-core</artifactId>
        <version>0.3.19</version>
    </dependency>
    <!-- 不由该 BOM 管理：写你项目实际使用的版本 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>6.1.3</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 4.2 验证 BOM 锁定的版本

```bash
mvn -Dincludes=io.github.microsphere-projects dependency:tree
```

导入了 `0.2.10` 的项目，期望输出：

```
[INFO] +- io.github.microsphere-projects:microsphere-hibernate-core:jar:0.2.10:compile
[INFO] \- io.github.microsphere-projects:microsphere-hibernate-test:jar:0.2.10:test
```

### 4.3 与其他 Microsphere BOM 一同使用

`microsphere-hibernate-dependencies` 通过父工程共用 `microsphere-spring-cloud-dependencies` 这条依赖链。当你导入
多个 Microsphere BOM 时，请把更具体的那个放在前面——对同一个 `groupId:artifactId`，Maven 采用它看到的第一个声明。

## 5. 它是如何被构建的

`microsphere-hibernate-dependencies` 是根 `pom.xml` 声明的 reactor 模块之一：

```xml
<modules>
    <module>microsphere-hibernate-parent</module>
    <module>microsphere-hibernate-dependencies</module>
    <module>microsphere-hibernate-core</module>
    <module>microsphere-hibernate-test</module>
</modules>
```

install / deploy 整个 reactor 时，BOM 与两个 JAR 一起发布，这就是为什么使用方的 POM 里只需要一个版本属性
（`${microsphere-hibernate.version}`）。

## 6. 相关文档

- [microsphere-hibernate-parent.md](microsphere-hibernate-parent.md) —— Hibernate / Spring Cloud 版本的来源
- [microsphere-hibernate-core.md](microsphere-hibernate-core.md) —— 核心 API
- [microsphere-hibernate-test.md](microsphere-hibernate-test.md) —— 测试模块
