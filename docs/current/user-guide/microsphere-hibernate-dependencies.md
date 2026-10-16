# microsphere-hibernate-dependencies

The **Bill of Materials (BOM)** of Microsphere Hibernate. It is a POM-only module whose single job is to pin
the versions of the Microsphere Hibernate artifacts so that consumer projects never write `<version>` on them.

[← Back to the User Guide index](README.md) · Chinese version: [zh/microsphere-hibernate-dependencies.md](zh/microsphere-hibernate-dependencies.md)

## 1. Module Positioning and Maven Coordinates

| **Item** | **Value** |
|----------|-----------|
| `groupId` | `io.github.microsphere-projects` |
| `artifactId` | `microsphere-hibernate-dependencies` |
| `version` | `${revision}` → `0.2.11-SNAPSHOT` on the `release` branch (last release: `0.2.10`) |
| `packaging` | `pom` |
| `name` | `Microsphere :: Hibernate :: Dependencies` |
| Source location | `microsphere-hibernate-dependencies/pom.xml` |
| Its parent | `microsphere-hibernate-parent` (see [parent documentation](microsphere-hibernate-parent.md)) |
| Consumed with | `<type>pom</type> <scope>import</scope>` inside your `dependencyManagement` |

The complete content of its dependency management is:

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

`${revision}` is Maven's CI-friendly version placeholder, declared in the root POM (`0.2.11-SNAPSHOT` on
`release`); released POMs carry the concrete number, e.g. `0.2.10`.

## 2. Version Information and Compatibility

| **Dimension** | **Value / Range** | **Notes** |
|---------------|-------------------|-----------|
| Version line | `0.2.x` | released with the whole reactor, all four artifacts always share one version |
| JDK | 17 – 25 | same as the [parent](microsphere-hibernate-parent.md) |
| Hibernate Core | 6.2.x – 7.3.x | the BOM itself does not re-declare it; it inherits `hibernate-platform` through `microsphere-hibernate-parent` |
| Spring Cloud | 2022.0.x – 2025.0.x | inherited through `microsphere-spring-cloud-dependencies` (`0.2.26`) |
| Latest usable version | `0.2.10` | `<version>0.2.10</version>` in the import block |

Because all modules are released together, `microsphere-hibernate-core` and `microsphere-hibernate-test` always
have the *same* version. Never mix `0.1.x` and `0.2.x` artifacts.

For reference, the POMs published for `0.2.10` (built with the default `hibernate-7.3` profile) resolve to:

| **Dependency** | **Version in the `0.2.10` release** |
|----------------|-------------------------------------|
| `org.hibernate.orm:hibernate-core` | `7.3.4.Final` |
| `io.github.microsphere-projects:microsphere-java-core` | `0.3.19` |
| `io.github.microsphere-projects:microsphere-annotation-processor` | `0.3.19` |
| `com.h2database:h2` | `2.3.232` |
| `org.junit.jupiter:junit-jupiter` | `6.1.3` |
| `org.mockito:mockito-core` / `mockito-junit-jupiter` | `5.17.0` |
| `ch.qos.logback:logback-classic` | `1.5.34` |

These numbers follow from the active Maven profiles, so they shift whenever the upstream parents or the
`hibernate-*` profile change. Pin what your application actually needs if you must stay on a specific value.

## 3. What the BOM Provides (its "API")

### 3.1 Managed artifacts

| **Artifact** | **Purpose** | **Typical scope in your project** |
|--------------|-------------|-----------------------------------|
| `microsphere-hibernate-core` | `EntityCallback` model + Hibernate `Integrator` | `compile` |
| `microsphere-hibernate-test` | JUnit 5 support: `@HibernateTest`, `@HibernateRuntime`, `HibernateExtension`, abstract test bases, sample entities | `test` |

### 3.2 Contract rules

- The BOM declares **no** `<dependencies>` section: importing it adds nothing to your classpath.
- The BOM manages versions only. Its own `<dependencyManagement>` block lists exactly the two Microsphere
  Hibernate artifacts; everything else (Hibernate ORM, Spring Cloud) comes from the profile-driven parent chain
  (`microsphere-hibernate-parent` → `hibernate-platform` / `microsphere-spring-cloud-dependencies`), so declare
  your Hibernate ORM version explicitly rather than relying on an inherited value.
- Both managed artifacts declare their own dependencies (`hibernate-core`, `microsphere-java-core`,
  `junit-jupiter`, `h2`, …) as **`optional`**. Versions being managed does not mean they arrive transitively;
  see [microsphere-hibernate-core.md](microsphere-hibernate-core.md) for the list you must add.
- `microsphere-hibernate-test` is *not* self-sufficient either: the JUnit 5 and Hibernate ORM dependencies you
  need in your own test scope must be declared explicitly.

## 4. Examples

### 4.1 Recommended setup: BOM + explicit dependencies

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
    <!-- version managed by the BOM -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-hibernate-core</artifactId>
    </dependency>

    <!-- test kit, version managed by the BOM -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-hibernate-test</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- optional in microsphere-hibernate-core, therefore declared here -->
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
    <!-- not managed by this BOM: declare the version your project is built with -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>6.1.3</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 4.2 Verify what the BOM pinned

```bash
mvn -Dincludes=io.github.microsphere-projects dependency:tree
```

Expected output in a project that imported version `0.2.10`:

```
[INFO] +- io.github.microsphere-projects:microsphere-hibernate-core:jar:0.2.10:compile
[INFO] \- io.github.microsphere-projects:microsphere-hibernate-test:jar:0.2.10:test
```

### 4.3 Using the BOM together with other Microsphere BOMs

`microsphere-hibernate-dependencies` shares the `microsphere-spring-cloud-dependencies` chain through its parent.
When you import several Microsphere BOMs, put the more specific one first — Maven keeps the first declaration it
sees for a given `groupId:artifactId`.

## 5. How It Is Built

`microsphere-hibernate-dependencies` is a module of the reactor declared in the root `pom.xml`:

```xml
<modules>
    <module>microsphere-hibernate-parent</module>
    <module>microsphere-hibernate-dependencies</module>
    <module>microsphere-hibernate-core</module>
    <module>microsphere-hibernate-test</module>
</modules>
```

Installing/deploying the reactor publishes the BOM side by side with the JARs, which is why a single version
property (`${microsphere-hibernate.version}`) is enough in a consumer POM.

## 6. Related Documentation

- [microsphere-hibernate-parent.md](microsphere-hibernate-parent.md) — where the Hibernate / Spring Cloud versions come from
- [microsphere-hibernate-core.md](microsphere-hibernate-core.md) — the core API
- [microsphere-hibernate-test.md](microsphere-hibernate-test.md) — the test kit
