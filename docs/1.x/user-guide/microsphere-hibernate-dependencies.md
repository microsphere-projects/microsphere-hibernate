# microsphere-hibernate-dependencies (1.x)

[English](microsphere-hibernate-dependencies.md) · [Chinese version](zh/microsphere-hibernate-dependencies.md)

## 1. Module Positioning And Maven Coordinates

`microsphere-hibernate-dependencies` is the **Bill Of Materials (BOM)** of Microsphere Hibernate 1.x. It is a
POM-only module: no sources, no JAR, no `dependencyManagement` other than version pinning. Importing it means
"give every Microsphere Hibernate artifact of this release the matching version, and do not make me repeat
versions".

| **Item**   | **Value**                                                                       |
|------------|---------------------------------------------------------------------------------|
| GroupId    | `io.github.microsphere-projects`                                                 |
| ArtifactId | `microsphere-hibernate-dependencies`                                             |
| Version    | `${revision}` → `0.1.11-SNAPSHOT` on `release-1.x`; latest release `0.1.10`       |
| Packaging  | `pom`                                                                            |
| Name       | `Microsphere :: Hibernate :: Dependencies`                                       |
| POM file   | `microsphere-hibernate-dependencies/pom.xml`                                     |
| Parent     | `io.github.microsphere-projects:microsphere-hibernate-parent:${revision}`         |
| Scope of use | `<scope>import</scope>` + `<type>pom</type>` inside your `<dependencyManagement>` |

## 2. Version Information And Compatibility

| **Axis**           | **Value**                                                                        |
|--------------------|----------------------------------------------------------------------------------|
| Version line       | `0.1.x` (branch `release-1.x`), latest release `0.1.10`                            |
| JDK                | 8 – 25                                                                            |
| Hibernate Core     | 5.0.x – 5.6.x via `org.hibernate:hibernate-core` (default `5.6.15.Final`)          |
| Spring Cloud       | Hoxton – 2021.0.x (through `microsphere-spring-cloud-dependencies:0.1.26`)          |
| Artifacts managed  | `microsphere-hibernate-core`, `microsphere-hibernate-test`                          |

Because the BOM inherits `microsphere-hibernate-parent`, importing it also brings the Hibernate and Spring
Cloud dependency management defined there — see
[microsphere-hibernate-parent.md](microsphere-hibernate-parent.md) for the seven `hibernate-5.x` profiles.

## 3. What The BOM Declares

### 3.1 The complete managed list declared by this POM

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

Two entries, both `${revision}`: the two Microsphere Hibernate artifacts are always released together, so they
can never drift inside one application.

### 3.2 What you get *inherited* from the parent chain

| **Managed item**                                       | **Version source**                             | **Why it matters** |
|--------------------------------------------------------|------------------------------------------------|---------------------|
| `org.hibernate:hibernate-core`                          | `${hibernate.version}`, default `5.6.15.Final` | You can omit the Hibernate version when adding it explicitly (it is *not* added transitively — it is `optional` in the core module). |
| `io.github.microsphere-projects:microsphere-spring-cloud-dependencies` (imported BOM) | `0.1.26` | Supplies versions for `microsphere-java-core`, `microsphere-java-test`, `microsphere-annotation-processor`, JUnit Jupiter, Mockito, H2, Logback and `javax.xml.bind:jaxb-api`. |

## 4. Usage

### 4.1 Import the BOM

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

### 4.2 Then declare the modules without versions

Runtime extension:

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-hibernate-core</artifactId>
</dependency>
<!-- hibernate-core is optional upstream, so declare it yourself -->
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-core</artifactId>
</dependency>
```

Testing support:

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-hibernate-test</artifactId>
    <scope>test</scope>
</dependency>
```

### 4.3 How the project itself consumes the managed versions

`microsphere-hibernate-core/pom.xml` uses the very same mechanism inside the reactor (there, `${revision}` is
used directly because the module is built next to its sibling):

```xml
<!-- Microsphere Hibernate Test -->
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-hibernate-test</artifactId>
    <version>${revision}</version>
    <scope>test</scope>
</dependency>
```

Source: `microsphere-hibernate-core/pom.xml` (branch `release-1.x`). This is the pattern that lets
`EntityCallbackIntegratorTest` in the core module run on the H2 test harness from the test module.

### 4.4 Pin a different Hibernate 5.x

A dependency declared in your own `dependencyManagement` takes precedence over an imported BOM:

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

Stay inside 5.0.x – 5.6.x: the 1.x artifacts are compiled against Hibernate 5 signatures
(`Interceptor#onLoad(Object, Serializable, …)`, `Interceptor#instantiate(String, EntityMode, Serializable)`,
`Integrator#integrate(Metadata, SessionFactoryImplementor, SessionFactoryServiceRegistry)`), so Hibernate 6+
will fail at class-definition time.

## 5. Notes And Gotchas

- **The BOM does not add dependencies.** Import only affects version resolution; you still have to declare the
  modules you use.
- **`optional` upstream means you must add Hibernate and Microsphere Java yourself.** Both
  `microsphere-hibernate-core` and `microsphere-hibernate-test` mark `org.hibernate:hibernate-core`,
  `microsphere-java-core`, `microsphere-java-test`, JUnit Jupiter, Mockito, H2 and Logback as `optional`
  (or `test` scope), so nothing flows transitively.
- **Versionless local builds.** All modules use `<version>${revision}</version>`; if you build the BOM from
  source, pass `-Drevision=…` (CI uses `-Drevision=0.0.1-SNAPSHOT`).
- **Import order.** When you also import Spring Boot's or Spring Cloud's BOMs, put the BOMs whose versions you
  want to keep *first* — the first declaration of a managed coordinate wins in Maven.
- **No plugin management, no distribution management, no repositories** are declared here; those live in the
  upstream `microsphere-spring-cloud-parent` chain.
- **Do not mix version lines.** Importing `microsphere-hibernate-dependencies:0.1.10` together with any `0.2.x`
  artifact produces an incompatible classpath (`org.hibernate` vs `org.hibernate.orm`,
  `javax.persistence` vs `jakarta.persistence`).

## 6. Related Documents

- [microsphere-hibernate-parent.md](microsphere-hibernate-parent.md) — where the inherited management comes from
- [microsphere-hibernate-core.md](microsphere-hibernate-core.md) — the managed runtime module
- [microsphere-hibernate-test.md](microsphere-hibernate-test.md) — the managed test module
- [README.md](README.md) — 1.x user guide index
- Source: `microsphere-hibernate-dependencies/pom.xml`, `microsphere-hibernate-parent/pom.xml`,
  `microsphere-hibernate-core/pom.xml` (branch `release-1.x`)
