# Microsphere Hibernate — User Guide (0.2.x)

This is the user guide for the **0.2.x** version line of
[Microsphere Hibernate](https://github.com/microsphere-projects/microsphere-hibernate), published from the
`release` branch. It documents the Maven modules, the public API of each module and how to integrate them into
your own project.

Chinese version: [zh/README.md](zh/README.md)

## What Is Microsphere Hibernate?

Microsphere Hibernate is a small set of extensions for [Hibernate ORM](https://hibernate.org/) that adds a
Java `ServiceLoader`-driven **entity lifecycle callback** model on top of Hibernate's event system, plus a
lightweight JUnit 5 testing kit for Hibernate projects.

With `microsphere-hibernate-core` on the classpath, you implement `EntityCallback` once, register it in
`META-INF/services/io.microsphere.hibernate.entity.EntityCallback`, and Hibernate ORM will notify it for load,
persist, merge, delete, replicate, flush, evict, lock, refresh, insert/update and collection events — without
writing `EventListener` boilerplate and without registering anything on the `SessionFactory` yourself.

With `microsphere-hibernate-test`, you get a `@HibernateTest` / `@HibernateRuntime` annotation-driven test
setup (or an H2-backed abstract test class) that manages `Configuration`, `SessionFactory`, `Session` and
`StatelessSession` for you.

## Applicable Version

| **Item**               | **Value**                                             |
|------------------------|-------------------------------------------------------|
| Branch                 | `release`                                             |
| Version line           | `0.2.x`                                               |
| Latest released version| `0.2.10`                                              |
| Branch revision        | `0.2.11-SNAPSHOT` (see `<revision>` in the root `pom.xml`) |
| Maven `groupId`        | `io.github.microsphere-projects`                      |
| Root aggregator POM    | `io.github.microsphere-projects:microsphere-hibernate` |
| Upstream parent        | `microsphere-spring-cloud-parent:0.2.26`               |

## Compatibility

| **0.2.x line**    | **Min Version** | **Max Version** |
|-------------------|-----------------|-----------------|
| **JDK**           | 17              | 25              |
| **Hibernate Core**| 6.2.x           | 7.3.x           |
| **Spring Cloud**  | 2022.0.x        | 2025.0.x        |

The compatibility range is enforced by build profiles rather than by code:

- `hibernate-6.2` … `hibernate-7.3` Maven profiles are declared in `microsphere-hibernate-parent` and pin
  `org.hibernate.orm:hibernate-platform`. See [microsphere-hibernate-parent](microsphere-hibernate-parent.md).
- `spring-cloud-2022` … `spring-cloud-2025` profiles are inherited from `microsphere-spring-cloud-parent`.
- `java.version` is set to `17` in the root POM; the GitHub build matrix compiles and tests on JDK **17, 21, 25**.

> The older **0.1.x** line (JDK 8, Hibernate ORM 5.0.x–5.6.x, Spring Cloud Hoxton–2021.0.x) is maintained on the
> `release-1.x` branch and is documented separately in the 1.x documentation of this `docs` tree. Do not mix
> `0.1.x` and `0.2.x` artifacts inside one application.

## Modules

| **Module** | **Artifact** | **Packaging** | **Documentation** |
|------------|--------------|---------------|-------------------|
| Parent POM | `microsphere-hibernate-parent` | `pom` | [microsphere-hibernate-parent.md](microsphere-hibernate-parent.md) |
| BOM        | `microsphere-hibernate-dependencies` | `pom` | [microsphere-hibernate-dependencies.md](microsphere-hibernate-dependencies.md) |
| Core       | `microsphere-hibernate-core` | `jar` | [microsphere-hibernate-core.md](microsphere-hibernate-core.md) |
| Test kit   | `microsphere-hibernate-test` | `jar` | [microsphere-hibernate-test.md](microsphere-hibernate-test.md) |

## Quick Start

### 1. Import the BOM

The BOM (`microsphere-hibernate-dependencies`) pins `microsphere-hibernate-core` and
`microsphere-hibernate-test` to the same version:

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

Declare the property, e.g.:

```xml

<properties>
    <microsphere-hibernate.version>0.2.10</microsphere-hibernate.version>
</properties>
```

### 2. Add the core module

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-hibernate-core</artifactId>
</dependency>
```

`microsphere-hibernate-core` declares `org.hibernate.orm:hibernate-core`,
`io.github.microsphere-projects:microsphere-java-core` and
`io.github.microsphere-projects:microsphere-annotation-processor` as **optional** dependencies, so they are
*not* pulled in transitively. Your application must provide `hibernate-core` and `microsphere-java-core`
(the callback machinery uses `io.microsphere.collection.ListUtils` and `io.microsphere.lang.Prioritized` at
runtime):

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

### 3. Register an `EntityCallback`

Create `src/main/resources/META-INF/services/io.microsphere.hibernate.entity.EntityCallback`:

```
com.acme.order.logging.OrderEntityCallback
```

and implement the callbacks you care about:

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

No `SessionFactory` wiring is required: `microsphere-hibernate-core` ships its own
`META-INF/services/org.hibernate.integrator.spi.Integrator` entry, so Hibernate ORM discovers
`EntittyCallbackIntegrator` automatically and registers the listener for all supported event types.

### 4. (Optional) Test support

Add the test kit in `test` scope:

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

See [microsphere-hibernate-test.md](microsphere-hibernate-test.md) for the full contract.

## Building From Source

You do not need to build from source to use the library. To try the latest `release` branch code:

```bash
git clone https://github.com/microsphere-projects/microsphere-hibernate.git
cd microsphere-hibernate
./mvnw package                        # Linux/MacOS
# mvnw.cmd package                    # Windows
```

To verify against a specific Hibernate version, activate the matching profile:

```bash
./mvnw -Phibernate-6.2 clean test
```

## Further Reading

- [microsphere-hibernate-parent.md](microsphere-hibernate-parent.md) — dependency management and Hibernate version profiles
- [microsphere-hibernate-dependencies.md](microsphere-hibernate-dependencies.md) — the BOM
- [microsphere-hibernate-core.md](microsphere-hibernate-core.md) — `EntityCallback` API and SPI integration
- [microsphere-hibernate-test.md](microsphere-hibernate-test.md) — JUnit 5 test support and sample entities
- Project README (repository root): [../../../README.md](../../../README.md)
- [Release notes](../../../release-notes.md)
- JavaDoc: [microsphere-hibernate-core](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-hibernate-core),
  [microsphere-hibernate-test](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-hibernate-test)
