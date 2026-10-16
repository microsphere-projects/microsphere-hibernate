# Microsphere Hibernate — User Guide (1.x / 0.1.x)

This is the user guide for the **0.1.x** version line of
[Microsphere Hibernate](https://github.com/microsphere-projects/microsphere-hibernate), published from the
`release-1.x` branch. It documents the Maven modules, the public API of each module and how to integrate them
into your own project.

Chinese version: [zh/README.md](zh/README.md)

## What Is Microsphere Hibernate?

Microsphere Hibernate is a small set of extensions for [Hibernate ORM](https://hibernate.org/) that adds a
Java `ServiceLoader`-driven **entity lifecycle callback** model on top of Hibernate's event system, an
**`Interceptor` delegation** helper, plus a lightweight JUnit 5 testing kit for Hibernate projects.

With `microsphere-hibernate-core` on the classpath, you implement `EntityCallback` once, register it in
`META-INF/services/io.microsphere.hibernate.entity.EntityCallback`, and Hibernate ORM notifies it for load,
persist, merge, delete, replicate, flush, evict, lock, refresh, insert/update and collection events — without
writing `EventListener` boilerplate and without registering anything on the `SessionFactory` yourself.

`DelegatingInterceptor` (specific to the 1.x line) lets you decorate any Hibernate
`org.hibernate.Interceptor` and add behavior around it, which is the recommended way to plug *session-scoped*
callbacks into a `SessionFactory` you do not own.

With `microsphere-hibernate-test`, you get a `@HibernateTest` / `@HibernateRuntime` annotation-driven test
setup (or an H2-backed abstract test class) that manages `Configuration`, `SessionFactory`, `Session` and
`StatelessSession` for you.

## Applicable Version

| **Item**                | **Value**                                                    |
|-------------------------|--------------------------------------------------------------|
| Branch                  | `release-1.x` (development branch: `dev-1.x`)                |
| Version line            | `0.1.x`                                                      |
| Latest released version | `0.1.10`                                                     |
| Branch revision         | `0.1.11-SNAPSHOT` (see `<revision>` in the root `pom.xml`)    |
| Maven `groupId`         | `io.github.microsphere-projects`                             |
| Root aggregator POM     | `io.github.microsphere-projects:microsphere-hibernate`        |
| Upstream parent         | `io.github.microsphere-projects:microsphere-spring-cloud-parent:0.1.26` |
| Hibernate `groupId`     | `org.hibernate` (Hibernate 5 coordinates, **not** `org.hibernate.orm`) |
| Persistence API         | `javax.persistence` (not `jakarta.persistence`)              |
| Java baseline           | 8 — no `java.version` override is declared in this repository; the JDK 8 build matrix entry is authoritative |

## Compatibility

| **0.1.x line**  | **Min Version** | **Max Version**                                   |
|-----------------|-----------------|---------------------------------------------------|
| **JDK**         | 8               | 25                                                |
| **Hibernate Core** | 5.0.x        | 5.6.x                                             |
| **Spring Cloud** | Hoxton         | 2021.0.x                                          |

The compatibility range is enforced by build profiles rather than by code:

- `hibernate-5.0` … `hibernate-5.6` Maven profiles are declared in `microsphere-hibernate-parent` and pin
  `org.hibernate:hibernate-core`. `hibernate-5.6` is active by default.
  See [microsphere-hibernate-parent](microsphere-hibernate-parent.md).
- `spring-cloud-hoxton`, `spring-cloud-2020` and `spring-cloud-2021` profiles are inherited from
  `microsphere-spring-cloud-parent:0.1.26`.
- The GitHub build matrices (`.github/workflows/maven-build.yml` and `maven-build-java11+.yml`) cover:
  - JDK **8** × Spring Cloud {hoxton, 2020, 2021} × Hibernate {5.0, 5.1, 5.2, 5.3, 5.4, 5.5, 5.6}
  - JDK **11, 17, 21, 25** × Spring Cloud {hoxton, 2020, 2021} × Hibernate {5.3, 5.4, 5.5, 5.6}
- On Java 9 and later, a `java9+` profile adds the `javax.xml.bind:jaxb-api` dependency, because Hibernate 5.x
  still needs JAXB classes that were removed from the JDK.

> **Do not mix `0.1.x` and `0.2.x` artifacts inside one application.** The 0.2.x line (current) targets
> Hibernate ORM 6.2.x–7.3.x, `org.hibernate.orm` coordinates, `jakarta.persistence` and JDK 17+; it is
> documented in [../../current/user-guide/README.md](../../current/user-guide/README.md).

## Modules

| **Module**   | **Artifact** | **Packaging** | **Documentation** |
|--------------|--------------|---------------|-------------------|
| Parent POM   | `microsphere-hibernate-parent` | `pom` | [microsphere-hibernate-parent.md](microsphere-hibernate-parent.md) |
| BOM          | `microsphere-hibernate-dependencies` | `pom` | [microsphere-hibernate-dependencies.md](microsphere-hibernate-dependencies.md) |
| Core         | `microsphere-hibernate-core` | `jar` | [microsphere-hibernate-core.md](microsphere-hibernate-core.md) |
| Test kit     | `microsphere-hibernate-test` | `jar` | [microsphere-hibernate-test.md](microsphere-hibernate-test.md) |

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
    <microsphere-hibernate.version>0.1.10</microsphere-hibernate.version>
</properties>
```

### 2. Add the core module and its runtime requirements

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-hibernate-core</artifactId>
</dependency>
```

`microsphere-hibernate-core` declares `org.hibernate:hibernate-core`,
`io.github.microsphere-projects:microsphere-java-core` and
`io.github.microsphere-projects:microsphere-annotation-processor` as **optional** dependencies, so they are
*not* pulled in transitively. Your application must provide `hibernate-core` and `microsphere-java-core`
(the callback machinery uses `io.microsphere.collection.ListUtils` and `io.microsphere.lang.Prioritized` at
runtime):

```xml
<dependency>
    <groupId>org.hibernate</groupId>
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

and implement only the callbacks you care about (all 23 methods are `default` no-ops):

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
`META-INF/services/org.hibernate.integrator.spi.Integrator` entry (content:
`io.microsphere.hibernate.entity.EntittyCallbackIntegrator`), so Hibernate ORM 5.x discovers the integrator
automatically and registers the listener for all supported event types.

### 4. (Optional) Session-scoped callbacks with `DelegatingInterceptor`

If you need callbacks that only apply to one `Session` — or you want to wrap an interceptor provided by a third
party library — use `DelegatingInterceptor`:

```java
Interceptor thirdParty = new AuditInterceptor();
Interceptor interceptor = new DelegatingInterceptor(thirdParty) {
    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state,
                          String[] propertyNames, Type[] types) {
        System.out.println("Saving " + entity);
        return super.onSave(entity, id, state, propertyNames, types);
    }
};
Session session = sessionFactory.withOptions().interceptor(interceptor).openSession();
```

See [microsphere-hibernate-core.md](microsphere-hibernate-core.md#delegatinginterceptor) for the full contract.

### 5. (Optional) Test support

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

## Differences From the 0.2.x (current) Line

| **Topic**                       | **1.x (this guide, `release-1.x`)**                  | **0.2.x (current, `release`)**                       |
|---------------------------------|------------------------------------------------------|------------------------------------------------------|
| Latest version                  | `0.1.10` (`0.1.11-SNAPSHOT` in development)          | `0.2.10`                                              |
| JDK                             | 8 – 25                                               | 17 – 25                                               |
| Hibernate ORM                   | 5.0.x – 5.6.x (`org.hibernate:hibernate-core`)        | 6.2.x – 7.3.x (`org.hibernate.orm`, `hibernate-platform`) |
| Persistence annotations         | `javax.persistence.*`                                | `jakarta.persistence.*`                              |
| Spring Cloud                    | Hoxton – 2021.0.x (`microsphere-spring-cloud` 0.1.26) | 2022.0.x – 2025.0.x                                   |
| `DelegatingInterceptor`         | **Available** (`io.microsphere.hibernate`)             | **Removed** — its `Interceptor` overrides use the Hibernate 5 signatures (`Serializable id`, `EntityMode`, raw `Iterator`), which no longer compile against Hibernate 6+ |
| `EntityCallback#onLoad`         | `onLoad(id, entityClassName, entity, lockMode, isAssociationFetch, result, loadType)` — **7 parameters** | adds a `Boolean readOnly` parameter — **8 parameters** |
| `PropertyConstants`             | Same keys (`microsphere.hibernate.` / `microsphere.hibernate.enabled`) | Same keys                                             |

Practical consequences when migrating 1.x → 0.2.x: drop `DelegatingInterceptor` usages (port them to a plain
`Interceptor` or Hibernate 6's `Interceptor` signatures), re-implement `EntityCallback#onLoad` with the extra
`readOnly` argument, and switch `javax.persistence` imports to `jakarta.persistence`.

## Configuration Keys

`io.microsphere.hibernate.constants.PropertyConstants` declares the property namespace used by the project:

| **Constant**                                          | **Value / default**                     | **Notes**                              |
|-------------------------------------------------------|-----------------------------------------|----------------------------------------|
| `MICROSPHERE_HIBERNATE_PROPERTY_NAME_PREFIX`          | `microsphere.hibernate.`                | Prefix for all Microsphere Hibernate keys |
| `MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_NAME`         | `microsphere.hibernate.enabled`         | Read from system properties             |
| `DEFAULT_MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_VALUE`| `"true"`                                | Default value of the enabled flag       |
| `DEFAULT_MICROSPHERE_HIBERNATE_ENABLED`               | `true`                                  | Parsed boolean form                     |

## Building From Source

You do not need to build from source to use the library. To try the latest `release-1.x` code:

```bash
git clone -b release-1.x https://github.com/microsphere-projects/microsphere-hibernate.git
cd microsphere-hibernate
./mvnw package                        # Linux/MacOS
# mvnw.cmd package                    # Windows
```

The project uses CI-friendly versions (`<version>${revision}</version>`), so pass `-Drevision` when you install
it locally:

```bash
./mvnw -Drevision=0.1.11-SNAPSHOT clean install
```

To verify against a specific Hibernate version, activate the matching profile (JDK 8 is required for
`hibernate-5.0` … `hibernate-5.2`):

```bash
./mvnw -Phibernate-5.4 clean test
```

## Further Reading

- [microsphere-hibernate-parent.md](microsphere-hibernate-parent.md) — dependency management and Hibernate 5.x version profiles
- [microsphere-hibernate-dependencies.md](microsphere-hibernate-dependencies.md) — the BOM
- [microsphere-hibernate-core.md](microsphere-hibernate-core.md) — `EntityCallback`, `EntittyCallbackIntegrator`, `DelegatingInterceptor`, `PropertyConstants`
- [microsphere-hibernate-test.md](microsphere-hibernate-test.md) — JUnit 5 test support and sample entities
- Project README (repository root): [../../../README.md](../../../README.md)
- [Release notes](../../../release-notes.md)
- JavaDoc: [microsphere-hibernate-core](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-hibernate-core),
  [microsphere-hibernate-test](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-hibernate-test)
