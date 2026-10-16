# microsphere-hibernate-parent

Microsphere Hibernate 项目的 Maven **父工程 POM**。它不包含任何 Java 代码，职责是集中管理依赖、把 Hibernate ORM
的版本做成可通过 Maven profile 切换的开关，并为项目的所有模块提供统一的构建配置。

[← 返回用户文档索引](README.md) · 英文版本：[../microsphere-hibernate-parent.md](../microsphere-hibernate-parent.md)

## 1. 模块定位与 Maven 坐标

| **项目** | **取值** |
|----------|----------|
| `groupId` | `io.github.microsphere-projects` |
| `artifactId` | `microsphere-hibernate-parent` |
| `version` | `${revision}` → `release` 分支上为 `0.2.11-SNAPSHOT`（最近发布版本 `0.2.10`） |
| `packaging` | `pom` |
| `name` | `Microsphere :: Hibernate :: Parent` |
| 源码位置 | `microsphere-hibernate-parent/pom.xml` |
| 它的 parent | `io.github.microsphere-projects:microsphere-hibernate`（根聚合 POM） |
| 根聚合 POM 的 parent | `io.github.microsphere-projects:microsphere-spring-cloud-parent:0.2.26` |

该 POM 已发布到 Maven Central，可以直接使用上面的坐标：

```xml
<parent>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-hibernate-parent</artifactId>
    <version>0.2.10</version>
</parent>
```

在项目 reactor 内部，`microsphere-hibernate-dependencies`、`microsphere-hibernate-core`、
`microsphere-hibernate-test` 都通过 `<relativePath>` 把它声明为自己的 parent：

```xml
<parent>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-hibernate-parent</artifactId>
    <version>${revision}</version>
    <relativePath>../microsphere-hibernate-parent/pom.xml</relativePath>
</parent>
```

## 2. 版本信息与兼容范围

| **维度** | **支持范围** | **保证方式** |
|----------|--------------|--------------|
| 版本线 | `0.2.x`（已发布 `0.2.10`，开发中 `0.2.11-SNAPSHOT`） | 根 POM 的 `revision` 属性 |
| JDK | 17 – 25 | 根 POM 中 `java.version=17`；CI 矩阵使用 JDK `17`、`21`、`25` |
| Hibernate Core | 6.2.x – 7.3.x | `hibernate-6.2` … `hibernate-7.3` profile → 导入 `hibernate-platform` BOM |
| Spring Cloud | 2022.0.x – 2025.0.x | 继承自 `microsphere-spring-cloud-parent` 的 `spring-cloud-2022` … `spring-cloud-2025` profile |
| Microsphere Spring Cloud 构件 | `0.2.26` | `microsphere-spring-cloud.version` 属性 |
| Maven | 项目自带 wrapper（`mvnw`、`mvnw.cmd`） | `.mvn/wrapper/maven-wrapper.properties` |

`0.1.x` 版本线面向 Hibernate ORM 5.x 与 JDK 8，使用的是另一套 parent（`release-1.x` 分支），两条版本线不要混用。

## 3. 父 POM 提供的能力（它的“API”）

对于纯 POM 模块，它的契约就是它向子模块贡献的属性、受管依赖与 profile。

### 3.1 属性

| **属性** | **取值 / 默认值** | **用途** |
|----------|-------------------|----------|
| `microsphere-spring-cloud.version` | `0.2.26` | 被导入的 `microsphere-spring-cloud-dependencies` 的版本 |
| `hibernate.version` | 由激活的 `hibernate-*` profile 决定（默认 `7.3.4.Final`） | 被导入的 `hibernate-platform` 的版本 |

### 3.2 导入的 BOM（`dependencyManagement`）

```xml
<dependencyManagement>
    <dependencies>

        <!-- Hibernate Dependencies -->
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-platform</artifactId>
            <version>${hibernate.version}</version>
            <type>pom</type>
            <scope>import</scope>
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

对所有子模块的直接好处是：`org.hibernate.orm:hibernate-core`、`com.h2database:h2`、
`org.junit.jupiter:junit-jupiter`、`org.mockito:mockito-core`、`ch.qos.logback:logback-classic`、
`io.github.microsphere-projects:microsphere-java-core`、
`io.github.microsphere-projects:microsphere-annotation-processor` 等依赖都可以**不写 `<version>`**——
`microsphere-hibernate-core/pom.xml` 与 `microsphere-hibernate-test/pom.xml` 正是这样编写的。

### 3.3 Hibernate 版本 profile

在 `microsphere-hibernate-parent/pom.xml` 中声明；每个 profile 只设置 `hibernate.version`：

| **profile id** | **`hibernate.version`** | **Hibernate ORM 版本线** | **激活方式** |
|----------------|-------------------------|--------------------------|--------------|
| `hibernate-7.3` | `7.3.4.Final` | 7.3.x | `activeByDefault` |
| `hibernate-7.2` | `7.2.14.Final` | 7.2.x | 手动 |
| `hibernate-7.1` | `7.1.25.Final` | 7.1.x | 手动 |
| `hibernate-7.0` | `7.0.10.Final` | 7.0.x | 手动 |
| `hibernate-6.6` | `6.6.50.Final` | 6.6.x | 手动 |
| `hibernate-6.5` | `6.5.3.Final` | 6.5.x | 手动 |
| `hibernate-6.4` | `6.4.10.Final` | 6.4.x | 手动 |
| `hibernate-6.3` | `6.3.2.Final` | 6.3.x | 手动 |
| `hibernate-6.2` | `6.2.52.Final` | 6.2.x | 手动 |

两条需要记住的规则：

- 不带 `-P` 执行 `mvn package` 时会针对 Hibernate `7.3.4.Final` 构建，因为 `hibernate-7.3` 是
  `activeByDefault`。
- 激活另一个 `hibernate-*` profile 会取代这个默认值，例如 `-Phibernate-6.2` 会针对 `6.2.52.Final` 构建。
  每次构建请**只**激活一个 `hibernate-*` profile：`hibernate.version` 在 profile 之外没有取值，因此项目的 CI
  矩阵为每个作业显式指定一个 `hibernate-*` profile，而不是依赖默认激活。

### 3.4 父 POM **不**负责的事情

- 它不注册任何 Hibernate SPI 服务，也不含任何类；把它写成 `<parent>` 不会改变运行期行为。
- 它不锁定 Microsphere Hibernate 自身的构件版本——那是
  [microsphere-hibernate-dependencies](microsphere-hibernate-dependencies.md) 的职责。

## 4. 示例

### 4.1 把父 POM 作为自己项目的 parent

```xml
<project>
    <parent>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-hibernate-parent</artifactId>
        <version>0.2.10</version>
    </parent>

    <groupId>com.acme</groupId>
    <artifactId>acme-billing</artifactId>
    <version>1.0.0-SNAPSHOT</version>

    <dependencies>
        <!-- 版本由 hibernate-platform 提供，无需写 <version> -->
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-core</artifactId>
        </dependency>
    </dependencies>
</project>
```

### 4.2 不用 profile，直接覆盖 Hibernate 版本

版本只是一个普通属性，因此也可以直接驱动它——在 CI 矩阵中很方便：

```xml
<properties>
    <hibernate.version>6.6.50.Final</hibernate.version>
</properties>
```

```bash
mvn -Dhibernate.version=6.6.50.Final clean test
```

> 命令行 / 用户属性的优先级高于 profile 与 POM 中定义的属性，所以 `-Dhibernate.version=…` 是单次构建里最可控的
> 锁定方式。

## 5. CI 用法

`.github/workflows/maven-build.yml` 会构建完整矩阵（JDK `17`/`21`/`25` × `spring-cloud-2022`…`spring-cloud-2025`
× `hibernate-6.2`…`hibernate-7.3`），命令为：

```bash
mvn \
  --batch-mode \
  --update-snapshots \
  --file pom.xml \
  -Drevision=0.0.1-SNAPSHOT \
  test \
  --activate-profiles test,coverage,spring-cloud-2023,hibernate-6.4
```

- `-Drevision=0.0.1-SNAPSHOT` 覆盖工程版本，保证 CI 永远不会用到真实版本号。
- `test` 与 `coverage` 是上游 Microsphere 构建父工程链提供的 profile，分别用于启用测试阶段和上报给 Codecov 的
  覆盖率统计。
- 想在本地复现矩阵中的某一个组合，使用同样的参数即可。

## 6. 相关文档

- [microsphere-hibernate-dependencies.md](microsphere-hibernate-dependencies.md) —— 基于该父 POM 的 BOM
- [microsphere-hibernate-core.md](microsphere-hibernate-core.md) —— 核心模块
- [microsphere-hibernate-test.md](microsphere-hibernate-test.md) —— 测试模块
