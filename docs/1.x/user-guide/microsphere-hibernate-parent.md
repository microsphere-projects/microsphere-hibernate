# microsphere-hibernate-parent (1.x)

[English](microsphere-hibernate-parent.md) · [Chinese version](zh/microsphere-hibernate-parent.md)

## 1. Module Positioning And Maven Coordinates

`microsphere-hibernate-parent` is a **POM-only** module: it contains no source code, no resources and produces
no JAR. It is the *technical parent* of every buildable module in the repository
(`microsphere-hibernate-core`, `microsphere-hibernate-test`) and of the published BOM
(`microsphere-hibernate-dependencies`).

Its job is to turn "which Hibernate 5.x am I building against?" into a single Maven profile switch and to align
all modules with one upstream Microsphere Spring Cloud release line.

| **Item**       | **Value**                                                     |
|----------------|---------------------------------------------------------------|
| GroupId        | `io.github.microsphere-projects`                              |
| ArtifactId     | `microsphere-hibernate-parent`                                |
| Version        | `${revision}` → `0.1.11-SNAPSHOT` on `release-1.x`; latest release `0.1.10` |
| Packaging      | `pom`                                                         |
| Name           | `Microsphere :: Hibernate :: Parent`                           |
| POM file       | `microsphere-hibernate-parent/pom.xml`                         |
| Parent         | `io.github.microsphere-projects:microsphere-hibernate:${revision}` (root aggregator) |

The inheritance chain is:

```
microsphere-spring-cloud-parent:0.1.26          (upstream: Spring Boot / Spring Cloud / test tooling)
  └── microsphere-hibernate:${revision}         (root aggregator, declares <revision> and the 4 modules)
        ├── microsphere-hibernate-parent:${revision}     <-- this module (version profiles + dependencyManagement)
        │     ├── microsphere-hibernate-dependencies     (BOM, inherits from this module)
        │     ├── microsphere-hibernate-core             (inherits from this module)
        │     └── microsphere-hibernate-test             (inherits from this module)
        └── (aggregates the four modules above for reactor builds)
```

## 2. Version Information And Compatibility

| **Axis**         | **Range enforced by this POM**                                                                 |
|------------------|------------------------------------------------------------------------------------------------|
| JDK              | 8 – 25 (Java 8 is the baseline; JDK 8 is the only JDK that exercises all seven Hibernate profiles in CI) |
| Hibernate Core   | 5.0.x – 5.6.x, one Maven profile per minor version, pinned through `${hibernate.version}`        |
| Spring Cloud     | Hoxton – 2021.0.x, inherited from `microsphere-spring-cloud-dependencies:0.1.26`                  |
| Microsphere      | `microsphere-spring-cloud.version = 0.1.26`                                                     |
| Hibernate `groupId` | `org.hibernate` (Hibernate 5 coordinates)                                                   |
| Default build    | `hibernate-5.6` → `org.hibernate:hibernate-core:5.6.15.Final` (`activeByDefault`)                |

The `spring-cloud-hoxton`, `spring-cloud-2020` and `spring-cloud-2021` profiles are **not** declared here; they
come from `microsphere-spring-cloud-parent:0.1.26`, where `spring-cloud-2021` (Spring Boot `2.6.15`,
Spring Cloud `2021.0.9`) is the default-active one.

## 3. What The POM Exposes (Its "API")

### 3.1 Properties

| **Property**                     | **Value / source**        | **Purpose**                                                     |
|----------------------------------|---------------------------|-----------------------------------------------------------------|
| `microsphere-spring-cloud.version` | `0.1.26` (literal)      | Version of the imported Microsphere Spring Cloud BOM             |
| `hibernate.version`              | Set by the active `hibernate-5.x` profile | Version of `org.hibernate:hibernate-core`            |
| `revision`                       | Inherited from the root POM (`0.1.11-SNAPSHOT`) | CI-friendly version used by every module      |

### 3.2 `dependencyManagement`

Two entries only:

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

The imported Spring Cloud BOM is what supplies versions for JUnit Jupiter, Mockito, H2, Logback,
`microsphere-java-core`, `microsphere-java-test`, `microsphere-annotation-processor` and `jaxb-api` — which is
why the child POMs declare those dependencies **without** a `<version>` element (see
`microsphere-hibernate-core/pom.xml` and `microsphere-hibernate-test/pom.xml`).

> Note the groupId: on this version line Hibernate is `org.hibernate:hibernate-core`. The
> `org.hibernate.orm:hibernate-platform` alignment used by the 0.2.x line does not exist here.

### 3.3 Hibernate 5.x profiles

| **Profile id**   | **`hibernate.version`** | **Active**        |
|------------------|-------------------------|-------------------|
| `hibernate-5.6`  | `5.6.15.Final`          | Yes — `activeByDefault` |
| `hibernate-5.5`  | `5.5.9.Final`           | No                |
| `hibernate-5.4`  | `5.4.33.Final`          | No                |
| `hibernate-5.3`  | `5.3.38.Final`          | No                |
| `hibernate-5.2`  | `5.2.18.Final`          | No                |
| `hibernate-5.1`  | `5.1.9.Final`           | No                |
| `hibernate-5.0`  | `5.0.9.Final`           | No                |

Because each profile only sets a property, the profile you activate decides the Hibernate version for the whole
reactor, including the tests of `microsphere-hibernate-core` and `microsphere-hibernate-test`. That is how a
single source tree stays compatible with Hibernate 5.0 through 5.6.

## 4. Usage

### 4.1 As a consumer: you normally do not use this POM directly

Applications should import the BOM instead (see
[microsphere-hibernate-dependencies.md](microsphere-hibernate-dependencies.md)). The BOM inherits from this POM,
so `org.hibernate:hibernate-core` and the Microsphere Spring Cloud management come along with it.

```xml
<!-- Recommended: depend on the BOM, not on the parent -->
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

Because `hibernate-5.6` is the default profile of the published BOM's parent, the managed Hibernate version you
inherit is `5.6.15.Final`. If your application needs a different Hibernate 5.x minor, pin it explicitly in your
own `dependencyManagement` (a direct entry always wins over an imported BOM):

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
        <!-- Override the managed Hibernate version for this application -->
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>5.4.33.Final</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 4.2 As a maintainer: building the reactor against a chosen Hibernate

```bash
# Default (Hibernate 5.6.15.Final)
./mvnw clean test

# Hibernate 5.4.33.Final
./mvnw -Phibernate-5.4 clean test

# A full matrix combination, as used by CI (JDK 8 + Spring Cloud Hoxton + Hibernate 5.0)
./mvnw --batch-mode -Drevision=0.0.1-SNAPSHOT test \
       --activate-profiles test,coverage,spring-cloud-hoxton,hibernate-5.0
```

The exact CI command is defined in `.github/workflows/maven-build.yml` (JDK 8 × all seven Hibernate profiles ×
{`spring-cloud-hoxton`, `spring-cloud-2020`, `spring-cloud-2021`}) and `.github/workflows/maven-build-java11+.yml`
(JDK 11/17/21/25 × `hibernate-5.3` … `hibernate-5.6`).

### 4.3 Inheriting the POM (optional, for aligned multi-module builds)

```xml
<parent>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-hibernate-parent</artifactId>
    <version>0.1.10</version>
</parent>
```

Inheriting gives you `${hibernate.version}`-managed `org.hibernate:hibernate-core`, the Spring Cloud BOM import
and the seven profiles. It also ties your build to the Microsphere parent chain (compiler, test, coverage and
publishing configuration), so for plain applications the BOM import in § 4.1 is the lighter choice.

## 5. Notes And Gotchas

- **`${revision}` is not resolvable from a POM file.** Every published module uses
  `<version>${revision}</version>`; when you depend on the released artifacts, use a concrete version such as
  `0.1.10`. When you build locally, pass `-Drevision=...`, otherwise Maven cannot build the model.
- **No `java9+` profile here.** The `javax.xml.bind:jaxb-api` fallback for Java 9+ is declared in
  `microsphere-hibernate-core/pom.xml` and `microsphere-hibernate-test/pom.xml`, not in this POM.
- **Profiles are build-time only.** Activating `hibernate-5.0` when you build the artifacts does not change what
  the *published* POMs manage; the BOM is published with the default profile resolution. Always pin Hibernate
  explicitly in applications when you target an older 5.x.
- **Runtime compatibility is verified, not asserted by the POM.** The core and test modules compile against
  whichever profile is active; the API surface they rely on (`Interceptor` with `Serializable id` and
  `EntityMode`, `Integrator#integrate(Metadata, SessionFactoryImplementor, SessionFactoryServiceRegistry)`,
  `org.hibernate.event.spi.*`) is stable across 5.0 – 5.6, which is what makes the matrix possible.

## 6. Related Documents

- [microsphere-hibernate-dependencies.md](microsphere-hibernate-dependencies.md) — the BOM published from this parent
- [microsphere-hibernate-core.md](microsphere-hibernate-core.md) — the runtime API built against these profiles
- [microsphere-hibernate-test.md](microsphere-hibernate-test.md) — the test kit built against these profiles
- [README.md](README.md) — 1.x user guide index
- Source: `microsphere-hibernate-parent/pom.xml`, `pom.xml`, `.github/workflows/maven-build.yml`,
  `.github/workflows/maven-build-java11+.yml` (branch `release-1.x`)
