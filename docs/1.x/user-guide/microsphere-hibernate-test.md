# microsphere-hibernate-test (1.x)

[English](microsphere-hibernate-test.md) · [Chinese version](zh/microsphere-hibernate-test.md)

## 1. Module Positioning And Maven Coordinates

`microsphere-hibernate-test` is the Hibernate testing kit of Microsphere Hibernate 1.x. It is a **regular,
published JAR** (not a `test-jar`), so downstream projects put it in `test` scope and get:

- an abstract, H2-backed JUnit 5 test base class with ready-made integration tests (`AbstractHibernateH2Test`),
- an annotation-driven alternative (`@HibernateTest` + `@HibernateRuntime` + `HibernateExtension`) that manages
  `Configuration`, `SessionFactory`, `Session` and `StatelessSession` for you,
- a small four-entity domain model (`User`, `UserProfile`, `Order`, `Product`) used as fixture data.

| **Item** | **Value** |
|---|---|
| GroupId | `io.github.microsphere-projects` |
| ArtifactId | `microsphere-hibernate-test` |
| Version | `${revision}` → `0.1.11-SNAPSHOT` on `release-1.x`; latest release `0.1.10` |
| Packaging | `jar` |
| Name | `Microsphere :: Hibernate :: Test` |
| POM file | `microsphere-hibernate-test/pom.xml` |
| Java package | `io.microsphere.hibernate.test`, `io.microsphere.hibernate.test.entity` |
| Depends on core? | **No.** This module has no dependency on `microsphere-hibernate-core`; the core module depends on *this* one in `test` scope. |
| JavaDoc | https://javadoc.io/doc/io.github.microsphere-projects/microsphere-hibernate-test |

## 2. Version Information And Compatibility

| **Axis** | **Value** |
|---|---|
| Version line | `0.1.x`, branch `release-1.x`, latest release `0.1.10` |
| JDK | 8 – 25 (CI: JDK 8 with all seven Hibernate profiles; JDK 11/17/21/25 with `hibernate-5.3` … `hibernate-5.6`) |
| Hibernate Core | 5.0.x – 5.6.x, `org.hibernate:hibernate-core`, default `5.6.15.Final` |
| Spring Cloud | Hoxton – 2021.0.x (irrelevant at runtime: this module contains no Spring code) |
| JUnit | JUnit Jupiter 5 (the version comes from the `microsphere-spring-cloud` `0.1.26` parent chain, where `junit-jupiter.version` is `5.14.4`) |
| Persistence annotations | `javax.persistence` — entity scanning in `HibernateExtension` filters on `javax.persistence.Entity` |
| Database | H2 (in-memory), `org.h2.Driver`, `org.hibernate.dialect.H2Dialect` |

### Dependencies (all `optional`, from `microsphere-hibernate-test/pom.xml`)

| **Dependency** | **Why it is needed** |
|---|---|
| `io.github.microsphere-projects:microsphere-annotation-processor` | Configuration-metadata generation |
| `io.github.microsphere-projects:microsphere-java-core` | `io.microsphere.logging` used by `HibernateExtension` |
| `io.github.microsphere-projects:microsphere-java-test` | Optional convenience for downstream tests; no class in this module imports it (`SimpleClassScanner` and `io.microsphere.logging` come from `microsphere-java-core`) |
| `org.hibernate:hibernate-core` | `Configuration`, `SessionFactory`, `Session`, `StatelessSession` |
| `com.h2database:h2` | The in-memory database |
| `org.junit.jupiter:junit-jupiter` | `@Test`, `@Nested`, `@ExtendWith`, `assertDoesNotThrow` |
| `org.mockito:mockito-core`, `org.mockito:mockito-junit-jupiter` | Available to downstream tests |
| `ch.qos.logback:logback-classic` | Logging backend for the Hibernate SQL output |
| `javax.xml.bind:jaxb-api` | Added by the `java9+` profile (`[9,)`) |

**Because everything is optional, a consumer must declare what it actually uses.** A minimal, complete test
setup is:

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-hibernate-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-core</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

Versions come from the BOM import; see
[microsphere-hibernate-dependencies.md](microsphere-hibernate-dependencies.md).

## 3. API Reference

### 3.1 `AbstractHibernateTest` (package-private)

`io.microsphere.hibernate.test.AbstractHibernateTest`

> **Visibility:** the class is declared `abstract class AbstractHibernateTest` — **no `public` modifier**. It is
> therefore *not* extendable from your own package; use `AbstractHibernateH2Test` (public) or `@HibernateTest`
> instead. Its `protected` fields and methods are still inherited and usable through a public subclass, which is
> what `AbstractHibernateH2Test` (and, transitively, the core module's tests) does.

| **Member** | **Type** | **Contract** |
|---|---|---|
| `configuration` | `protected Configuration` | Built once per test method |
| `sessionFactory` | `protected SessionFactory` | Built from `configuration` |
| `session` | `protected Session` | `sessionFactory.openSession()` |
| `statelessSession` | `protected StatelessSession` | `sessionFactory.openStatelessSession()` |
| `setUp()` | `final`, `@BeforeEach` | `buildConfiguration()` → `customizeConfiguration(...)` → `buildSessionFactory()` → open both sessions → `init()` |
| `tearDown()` | `final`, `@AfterEach` | `destroy()` → close `session` → close `statelessSession` → close `sessionFactory` |
| `buildConfiguration()` | `protected abstract` | **The one method you must provide** — return the `Configuration` for the test |
| `customizeConfiguration(Configuration)` | `protected`, empty | Hook to mutate properties/annotated classes before the factory is built |
| `init()` / `destroy()` | `protected`, empty | Per-test setup/teardown around the sessions |

A concrete implementation inside the same package looks like this (from the class Javadoc, and the pattern the
module itself follows):

```java
class MyHibernateTest extends AbstractHibernateTest {

    @Override
    protected Configuration buildConfiguration() {
        return new Configuration()
                .addAnnotatedClass(User.class)
                .setProperty("hibernate.connection.url", "jdbc:h2:mem:testdb");
    }

    @Test
    void testPersist() {
        User user = new User();
        user.setName("Alice");
        Transaction tx = session.beginTransaction();
        session.persist(user);
        tx.commit();
    }
}
```

### 3.2 `AbstractHibernateH2Test` (public, abstract)

`io.microsphere.hibernate.test.AbstractHibernateH2Test extends AbstractHibernateTest` — the class most consumers
actually use.

**Fixed `Configuration` (`buildConfiguration()`):**

```java
new Configuration()
        .addAnnotatedClass(User.class)
        .addAnnotatedClass(UserProfile.class)
        .addAnnotatedClass(Order.class)
        .addAnnotatedClass(Product.class)
        .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
        .setProperty("hibernate.connection.url", "jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1")
        .setProperty("hibernate.connection.username", "sa")
        .setProperty("hibernate.connection.password", "")
        .setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
        .setProperty("hibernate.format_sql", "true")
        .setProperty("hibernate.show_sql", "true")
        .setProperty("hibernate.hbm2ddl.auto", "update");
```

**Fixture hooks:**

| **Member** | **Contract** |
|---|---|
| `protected User user` | Populated by `init()` before every test method |
| `protected User createUser()` | Builds a graph: `User(name="Tom")` + `UserProfile(idCard="ID123456")` + one `Order(orderNo="ORD001")` containing one `Product(productName="Laptop")`, wired via `user.addOrder(order)` (which also sets `order.user`). Override to change the fixture. |
| `protected void doInAction(Runnable)` | `assertDoesNotThrow(action::run)` — used for operations that intentionally run outside an explicit transaction |

**Inherited test inventory** — extending this class runs these tests in your suite:

| **Nested class** | **Tests** |
|---|---|
| `SessionTest` | `testPersistAndGet`, `testPersistAndMerge`, `testPersistAndDelete`, `testPersistAndReplicate`, `testPersistAndFlush`, `testPersistAndEvict`, `testPersistAndLock`, `testPersistAndRefresh`, `testPersistAndLoad`, `testPersistAndGetManagedEntities` (`persist` + `FROM io.microsphere.hibernate.test.entity.User`) |
| `StatelessSessionTest` | `testInsertAndGet`, `testInsertAndDelete`, `testInsertAndUpdate`, `testInsertAndRefresh` |

Each nested class carries its own `doInTransaction(Runnable)` helper that wraps the action in
`assertDoesNotThrow` plus a `session`/`statelessSession` transaction begin–commit.

**The minimal consumer subclass** — this is verbatim what the repository itself does:

```java
package io.microsphere.hibernate.test;

import org.hibernate.cfg.Configuration;

/**
 * Default {@link AbstractHibernateH2Test}
 */
public class HibernateH2Test extends AbstractHibernateH2Test {

    @Override
    protected void customizeConfiguration(Configuration configuration) {
        super.customizeConfiguration(configuration);
    }
}
```

Source: `microsphere-hibernate-test/src/test/java/io/microsphere/hibernate/test/HibernateH2Test.java`.
And the cross-module one, from the core module, which uses the harness to prove the `EntityCallback` wiring
(see [microsphere-hibernate-core.md](microsphere-hibernate-core.md#6-worked-example-the-modules-own-tests)):

```java
class EntityCallbackIntegratorTest extends AbstractHibernateH2Test {
}
```

Source: `microsphere-hibernate-core/src/test/java/io/microsphere/hibernate/entity/EntityCallbackIntegratorTest.java`.

### 3.3 `@HibernateTest`

`io.microsphere.hibernate.test.HibernateTest`

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ExtendWith(HibernateExtension.class)
public @interface HibernateTest {
    String[] properties() default {};
    String[] scanEntityPackages() default {};
}
```

| **Attribute** | **Default** | **Semantics** |
|---|---|---|
| `properties` | `{}` | Hibernate settings in `key=value` form. The extension splits at the **first** `=`, trims both sides and calls `Configuration.setProperty`. A string without `=` (or starting with it) is **skipped with a `WARN` log**, not an error. Values may contain `=` themselves. |
| `scanEntityPackages` | `{}` | Packages scanned recursively for classes annotated with `javax.persistence.Entity`, each registered with `Configuration#addAnnotatedClass`. Scanning uses `io.microsphere.io.scanner.SimpleClassScanner` against the **thread context `ClassLoader`**. |

`@ExtendWith(HibernateExtension.class)` is meta-annotated, so a single `@HibernateTest` is all a class needs —
no `@ExtendWith` of your own. `@Inherited` means subclasses of an annotated class keep the annotation.

### 3.4 `@HibernateRuntime`

`io.microsphere.hibernate.test.HibernateRuntime` — `@Target({FIELD, PARAMETER})`, `@Retention(RUNTIME)`. No
attributes: the **declared type** of the field or parameter selects the component.

| **Requested type** | **Static field** | **Instance field** | **Method parameter** |
|---|---|---|---|
| `org.hibernate.cfg.Configuration` | injected | injected | resolved |
| `org.hibernate.SessionFactory` | injected | injected | resolved |
| `org.hibernate.Session` | **left `null`** | injected | resolved |
| `org.hibernate.StatelessSession` | **left `null`** | injected | resolved |
| any other type | ignored | ignored | not supported (`supportsParameter` returns `false`) |

Static `Session`/`StatelessSession` fields stay `null` on purpose: sessions are per-test-method resources, so
sharing them through static state would leak across tests. This is asserted, not incidental — see § 4.2.

### 3.5 `HibernateExtension`

`io.microsphere.hibernate.test.HibernateExtension implements BeforeAllCallback, BeforeEachCallback,
AfterEachCallback, AfterAllCallback, ParameterResolver`

| **Phase** | **Behavior** |
|---|---|
| `beforeAll` | Walks up the `ExtensionContext` parents to the class annotated with `@HibernateTest`; if not found, does nothing (the extension is inert on unannotated classes). Builds `Configuration` + `SessionFactory` **once**, stored under the namespace `Namespace.create(HibernateExtension.class)` with keys `configuration` / `sessionFactory`. Then injects `@HibernateRuntime` **static** fields of the current class. |
| `beforeEach` | Opens a fresh `Session` and `StatelessSession`, stores them in the **method-level** store (`session` / `statelessSession`), then injects `@HibernateRuntime` instance fields of the test instance. |
| `afterEach` | Removes both sessions from the store and closes them; failures are logged (`WARN`) and swallowed, and the `Session` is only closed when `session.isOpen()`. |
| `afterAll` | Only the class that *declares* `@HibernateTest` closes the `SessionFactory` (guarded by `isClosed()`), then removes `configuration`. |
| `supportsParameter` / `resolveParameter` | `true` only for `@HibernateRuntime`-annotated parameters of the four supported types; resolution reads `configuration`/`sessionFactory` from the class store and `session`/`statelessSession` from the method store. |
| Field discovery | `getAllDeclaredFields` walks the class hierarchy up to (excluding) `Object`, so annotated fields in superclasses and in `@Nested` classes are injected too. `setAccessible(true)` is used, so `private` fields work. |
| Nested classes | `findHibernateTestContext` climbs to the annotated outer class, so a `@Nested` class shares one `SessionFactory` and does **not** rebuild it. |

## 4. Sample Entities (`io.microsphere.hibernate.test.entity`)

| **Entity** | **Table** | **Identifier** | **Associations** |
|---|---|---|---|
| `User` | `t_user` | `Long id`, `IDENTITY` | `@OneToOne(mappedBy = "user", cascade = ALL, fetch = LAZY) UserProfile profile`; `@OneToMany(mappedBy = "user", cascade = ALL, fetch = LAZY) List<Order> orders` + convenience `addOrder(Order)` which sets the back-reference |
| `UserProfile` | `t_user_profile` | `Long id`, `IDENTITY` | `@OneToOne @JoinColumn(name = "user_id") User user`; field `String idCard` |
| `Order` | `t_order` | `Long id`, `IDENTITY` | `@ManyToOne @JoinColumn(name = "user_id") User user`; `@ManyToMany(cascade = {PERSIST, MERGE}) @JoinTable(name = "t_order_product", joinColumns = order_id, inverseJoinColumns = product_id) Set<Product> products`; field `String orderNo` |
| `Product` | `t_product` | `Long id`, `IDENTITY` | `@ManyToMany(mappedBy = "products", fetch = LAZY) Set<Order> orders`; field `String productName` |

All four use `javax.persistence` annotations and implement `equals`/`hashCode` on `id` plus one business field
(`User`: `name`, `UserProfile`: `idCard`, …), which is why the inherited assertions such as
`assertEquals(user, user1)` work.

### 4.1 Entity POJO tests in `src/test`

`UserTest`, `UserProfileTest`, `OrderTest`, `ProductTest` are plain JUnit 5 unit tests (no Hibernate, no H2) that
verify the fixture model itself:

```java
@Test
void testDefaultConstructor() {
    User user = new User();
    assertNotNull(user);
    assertNull(user.getId());
    assertNull(user.getName());
    assertNull(user.getProfile());
    assertNotNull(user.getOrders());
    assertTrue(user.getOrders().isEmpty());
}

@Test
void testGetterSetter() {
    User user = new User();
    user.setId(1L);
    assertEquals(1L, user.getId());
    user.setName("John Doe");
    assertEquals("John Doe", user.getName());
    UserProfile profile = new UserProfile();
    user.setProfile(profile);
    assertEquals(profile, user.getProfile());
    Order order = new Order();
    user.addOrder(order);
    // ...
}
```

Source: `microsphere-hibernate-test/src/test/java/io/microsphere/hibernate/test/entity/UserTest.java`. Reuse the
pattern when you extend `createUser()` in your own tests.

### 4.2 Annotation-driven usage: `HibernateAnnotationTest`

Verbatim from `microsphere-hibernate-test/src/test/java/io/microsphere/hibernate/test/HibernateAnnotationTest.java`:

```java
@HibernateTest(
        properties = {
                "hibernate.connection.driver_class=org.h2.Driver",
                "hibernate.connection.url=jdbc:h2:mem:hibernate_annotation_test_db;DB_CLOSE_DELAY=-1",
                "hibernate.connection.username=sa",
                "hibernate.connection.password=",
                "hibernate.dialect=org.hibernate.dialect.H2Dialect",
                "hibernate.hbm2ddl.auto=update"
        },
        scanEntityPackages = {"io.microsphere.hibernate.test.entity"}
)
class HibernateAnnotationTest {

    @HibernateRuntime
    private static Configuration configuration;

    @HibernateRuntime
    private static SessionFactory sessionFactory;

    @HibernateRuntime
    private static Session session;

    @HibernateRuntime
    private static StatelessSession statelessSession;

    @Test
    void test() {
        assertNotNull(configuration);
        assertNotNull(sessionFactory);
        // Session and StatelessSession must NOT be injected into static fields
        assertNull(session);
        assertNull(statelessSession);
    }

    @Nested
    class FieldTest {

        @HibernateRuntime
        private Configuration configuration;

        @HibernateRuntime
        private SessionFactory sessionFactory;

        @HibernateRuntime
        private Session session;

        @HibernateRuntime
        private StatelessSession statelessSession;

        @Test
        void test() {
            assertNotNull(configuration);
            assertNotNull(sessionFactory);
            assertNotNull(session);
            assertNotNull(statelessSession);
        }
    }

    @Nested
    class ParameterTest {

        @Test
        void test(@HibernateRuntime Configuration configuration,
                  @HibernateRuntime SessionFactory sessionFactory,
                  @HibernateRuntime Session session,
                  @HibernateRuntime StatelessSession statelessSession) {
            assertNotNull(configuration);
            assertNotNull(sessionFactory);
            assertNotNull(session);
            assertNotNull(statelessSession);
        }
    }
}
```

Three things to copy into your own tests: the static-field expectations (`null` for sessions), the instance-field
style, and the parameter-injection style (which keeps the test class free of fields).

## 5. Integration: Using The Kit In A Consumer Project

1. Import the BOM and add `microsphere-hibernate-test` in `test` scope (§ 2).
2. Pick a style:
   - **Fix-it-fast:** `class MyDaoTest extends AbstractHibernateH2Test { … }` — you inherit the H2 wiring, the
     sample entity graph and fourteen sanity tests. Override `createUser()` (and, if needed,
     `customizeConfiguration(...)`) rather than `buildConfiguration()`, because the latter is what registers the
     four sample entities.
   - **Own model:** annotate your class with `@HibernateTest(properties = …, scanEntityPackages = …)` and inject
     what you need via `@HibernateRuntime`. Entities must carry `javax.persistence.Entity` to be picked up.
3. If you also use `microsphere-hibernate-core`, register your `EntityCallback` implementations in
   `src/test/resources/META-INF/services/io.microsphere.hibernate.entity.EntityCallback` to observe every event
   fired by the inherited tests — the exact technique used by
   `EntityCallbackIntegratorTest` + `TestEntityCallback`.
4. Build with the Hibernate version you ship, so the harness is exercised on that version too:

```bash
./mvnw -Phibernate-5.4 test
```

## 6. Notes And Gotchas

- **`AbstractHibernateTest` is package-private.** Subclassing it from another package fails to compile. Public
  alternatives: `AbstractHibernateH2Test` (inherit) or `@HibernateTest` (annotate).
- **Inheriting means inheriting tests.** Extending `AbstractHibernateH2Test` runs its `@Nested` suites in your
  module. That is a feature for smoke coverage (it is exactly how the core module tests its integrator), but if
  you want only the plumbing, use `@HibernateTest` instead.
- **The H2 database name is fixed** (`jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1`) and `hibernate.hbm2ddl.auto` is
  `update`, so within one JVM the schema and the rows survive across test classes. Give each test its own data
  (override `createUser()`), or override `buildConfiguration()` in a subclass inside the same package to change
  the URL.
- **`show_sql` and `format_sql` are `true`** in the base configuration. Add `logback-classic` in `test` scope if
  you want the SQL visible through SLF4J rather than `System.out`.
- **Entity scanning is classpath-package based, not JAR based.** `scanEntityPackages` needs the package to be
  reachable from the thread context `ClassLoader`; in a split-module or shaded build, prefer explicit registration
  (`Configuration#addAnnotatedClass`) inside `customizeConfiguration(...)`.
- **No malformed property becomes an error.** A `properties` entry without `=` is logged as `WARN` and skipped —
  check your logs if a connection setting seems ignored.
- **Lazy associations require an open session.** `User.orders`, `User.profile` and `Product.orders` are `LAZY`;
  traverse them inside the transaction (as the inherited tests do) or you will hit
  `LazyInitializationException`.
- **`doInAction` is not a transaction.** `testPersistAndEvict` and `testPersistAndLock` deliberately run outside
  an explicit transaction and only assert "no exception thrown"; treat it as a smoke assertion helper, not a
  persistence boundary.
- **No Spring needed.** Nothing in this module touches the Spring context, so the kit works in plain JUnit 5
  builds and in Spring Boot 2.3–2.6 projects alike.

## 7. Related Documents

- [microsphere-hibernate-core.md](microsphere-hibernate-core.md) — the API this kit is usually paired with
- [microsphere-hibernate-dependencies.md](microsphere-hibernate-dependencies.md) — version management
- [microsphere-hibernate-parent.md](microsphere-hibernate-parent.md) — the `hibernate-5.x` profiles
- [README.md](README.md) — 1.x user guide index
- Source (branch `release-1.x`): `microsphere-hibernate-test/src/main/java/io/microsphere/hibernate/test/**`,
  `microsphere-hibernate-test/src/test/java/io/microsphere/hibernate/test/**`
