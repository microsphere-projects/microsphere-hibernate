# microsphere-hibernate-parent

Maven **parent POM** of the Microsphere Hibernate project. It contains no Java code: its job is to centralize
dependency management, to expose the Hibernate ORM version as a switchable Maven profile, and to provide one
consistent build configuration for every module of the project.

[← Back to the User Guide index](README.md) · Chinese version: [zh/microsphere-hibernate-parent.md](zh/microsphere-hibernate-parent.md)

## 1. Module Positioning and Maven Coordinates

| **Item** | **Value** |
|----------|-----------|
| `groupId` | `io.github.microsphere-projects` |
| `artifactId` | `microsphere-hibernate-parent` |
| `version` | `${revision}` → `0.2.11-SNAPSHOT` on the `release` branch (last release: `0.2.10`) |
| `packaging` | `pom` |
| `name` | `Microsphere :: Hibernate :: Parent` |
| Source location | `microsphere-hibernate-parent/pom.xml` |
| Its own parent | `io.github.microsphere-projects:microsphere-hibernate` (the root aggregator POM) |
| Root aggregator's parent | `io.github.microsphere-projects:microsphere-spring-cloud-parent:0.2.26` |

The POM is published to Maven Central, so the coordinates above can be used directly:

```xml
<parent>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-hibernate-parent</artifactId>
    <version>0.2.10</version>
</parent>
```

Inside the reactor, all modules (`microsphere-hibernate-dependencies`, `microsphere-hibernate-core`,
`microsphere-hibernate-test`) declare it as their parent via a `<relativePath>`:

```xml
<parent>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-hibernate-parent</artifactId>
    <version>${revision}</version>
    <relativePath>../microsphere-hibernate-parent/pom.xml</relativePath>
</parent>
```

## 2. Version Information and Compatibility

| **Dimension** | **Supported Range** | **How It Is Enforced** |
|---------------|---------------------|------------------------|
| Version line | `0.2.x` (`0.2.10` released, `0.2.11-SNAPSHOT` in development) | `revision` property in the root POM |
| JDK | 17 – 25 | `java.version=17` in the root POM; CI matrix runs JDK `17`, `21`, `25` |
| Hibernate Core | 6.2.x – 7.3.x | `hibernate-6.2` … `hibernate-7.3` profiles → `hibernate-platform` BOM import |
| Spring Cloud | 2022.0.x – 2025.0.x | inherited `spring-cloud-2022` … `spring-cloud-2025` profiles from `microsphere-spring-cloud-parent` |
| Microsphere Spring Cloud artifacts | `0.2.26` | `microsphere-spring-cloud.version` property |
| Maven | the project ships a wrapper (`mvnw`, `mvnw.cmd`) | `.mvn/wrapper/maven-wrapper.properties` |

Because the `0.1.x` line targets Hibernate ORM 5.x and JDK 8, it uses a different parent (`release-1.x` branch).
Do not mix the two lines.

## 3. What the Parent POM Provides (its "API")

For a POM-only module, the contract is the set of properties, managed dependencies and profiles it contributes
to its children.

### 3.1 Properties

| **Property** | **Value / Default** | **Purpose** |
|--------------|---------------------|-------------|
| `microsphere-spring-cloud.version` | `0.2.26` | version of `microsphere-spring-cloud-dependencies` that is imported |
| `hibernate.version` | set by the active `hibernate-*` profile (default `7.3.4.Final`) | version of `hibernate-platform` that is imported |

### 3.2 Imported BOMs (`dependencyManagement`)

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

The consequence for every child module: `org.hibernate.orm:hibernate-core`, `com.h2database:h2`,
`org.junit.jupiter:junit-jupiter`, `org.mockito:mockito-core`, `ch.qos.logback:logback-classic`,
`io.github.microsphere-projects:microsphere-java-core`,
`io.github.microsphere-projects:microsphere-annotation-processor` and friends can be declared **without a
`<version>`** — which is exactly how `microsphere-hibernate-core/pom.xml` and
`microsphere-hibernate-test/pom.xml` are written.

### 3.3 Hibernate Version Profiles

Declared in `microsphere-hibernate-parent/pom.xml`; each profile only sets `hibernate.version`:

| **Profile id** | **`hibernate.version`** | **Hibernate ORM line** | **Activation** |
|----------------|-------------------------|------------------------|----------------|
| `hibernate-7.3` | `7.3.4.Final` | 7.3.x | `activeByDefault` |
| `hibernate-7.2` | `7.2.14.Final` | 7.2.x | manual |
| `hibernate-7.1` | `7.1.25.Final` | 7.1.x | manual |
| `hibernate-7.0` | `7.0.10.Final` | 7.0.x | manual |
| `hibernate-6.6` | `6.6.50.Final` | 6.6.x | manual |
| `hibernate-6.5` | `6.5.3.Final` | 6.5.x | manual |
| `hibernate-6.4` | `6.4.10.Final` | 6.4.x | manual |
| `hibernate-6.3` | `6.3.2.Final` | 6.3.x | manual |
| `hibernate-6.2` | `6.2.52.Final` | 6.2.x | manual |

Two rules worth remembering:

- `mvn package` with **no** `-P` builds against Hibernate `7.3.4.Final`, because `hibernate-7.3` is
  `activeByDefault`.
- Activating a different `hibernate-*` profile replaces that default, e.g. `-Phibernate-6.2` builds against
  `6.2.52.Final`. Activate **exactly one** `hibernate-*` profile per build: `hibernate.version` has no value
  outside the profiles, and the project's CI matrix therefore always lists one explicit `hibernate-*` profile per
  job rather than relying on the default.

### 3.4 What the Parent Does **Not** Do

- It does not register Hibernate SPI services and it does not contain classes; adding it as `<parent>` changes
  nothing at runtime.
- It does not pin the Microsphere Hibernate artifacts themselves — that is
  [microsphere-hibernate-dependencies](microsphere-hibernate-dependencies.md).

## 4. Examples

### 4.1 Use the parent as your project's parent

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
        <!-- version comes from hibernate-platform, no <version> needed -->
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-core</artifactId>
        </dependency>
    </dependencies>
</project>
```

### 4.2 Override the Hibernate version without a profile

The version is a plain property, so you can also drive it directly — useful in a CI matrix:

```xml
<properties>
    <hibernate.version>6.6.50.Final</hibernate.version>
</properties>
```

```bash
mvn -Dhibernate.version=6.6.50.Final clean test
```

> Command-line/user properties take precedence over profile and POM properties, which makes `-Dhibernate.version=…`
> the most predictable way to pin a single build.

## 5. CI Usage

`.github/workflows/maven-build.yml` builds the whole matrix (JDK `17`/`21`/`25` ×
`spring-cloud-2022`…`spring-cloud-2025` × `hibernate-6.2`…`hibernate-7.3`) with:

```bash
mvn \
  --batch-mode \
  --update-snapshots \
  --file pom.xml \
  -Drevision=0.0.1-SNAPSHOT \
  test \
  --activate-profiles test,coverage,spring-cloud-2023,hibernate-6.4
```

- `-Drevision=0.0.1-SNAPSHOT` overrides the project version so CI never publishes a real version number.
- `test` and `coverage` are build profiles contributed by the upstream Microsphere build parent chain; they
  enable the test phase and the coverage reporting that is uploaded to Codecov.
- Reproduce a single cell of the matrix locally with the same flags.

## 6. Related Documentation

- [microsphere-hibernate-dependencies.md](microsphere-hibernate-dependencies.md) — the BOM built on top of this parent
- [microsphere-hibernate-core.md](microsphere-hibernate-core.md) — the core module
- [microsphere-hibernate-test.md](microsphere-hibernate-test.md) — the test kit
