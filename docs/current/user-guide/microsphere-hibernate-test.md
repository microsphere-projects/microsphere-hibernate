# microsphere-hibernate-test

The testing module of Microsphere Hibernate: a small JUnit 5 toolkit that boots Hibernate ORM for you, injects
`Configuration` / `SessionFactory` / `Session` / `StatelessSession` into tests, and ships a set of sample JPA
entities plus an H2-backed abstract test class.

[← Back to the User Guide index](README.md) · Chinese version: [zh/microsphere-hibernate-test.md](zh/microsphere-hibernate-test.md)

## 1. Module Positioning and Maven Coordinates

| **Item** | **Value** |
|----------|-----------|
| `groupId` | `io.github.microsphere-projects` |
| `artifactId` | `microsphere-hibernate-test` |
| `version` | `${revision}` → `0.2.11-SNAPSHOT` on the `release` branch (last release: `0.2.10`) |
| `packaging` | `jar` |
| `name` | `Microsphere :: Hibernate :: Test` |
| Root package | `io.microsphere.hibernate.test` (+ `io.microsphere.hibernate.test.entity`) |
| Parent | [microsphere-hibernate-parent](microsphere-hibernate-parent.md) |
| Declared dependencies (all **optional**, compile scope) | `microsphere-annotation-processor`, `microsphere-java-core`, `microsphere-java-test`, `org.hibernate.orm:hibernate-core`, `com.h2database:h2`, `org.junit.jupiter:junit-jupiter`, `org.mockito:mockito-core`, `org.mockito:mockito-junit-jupiter`, `ch.qos.logback:logback-classic` |

Note that the *published* JAR keeps these dependencies in `compile` scope but marks them `optional`, so nothing is
transitive — this is deliberate: the module is a test **library** and must not dictate your test stack.

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-hibernate-test</artifactId>
    <scope>test</scope>
</dependency>
```

You must add the parts you actually use:

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>6.1.3</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>7.3.4.Final</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.3.232</version>
    <scope>test</scope>
</dependency>
<dependency>
    <!-- used by HibernateExtension: SimpleClassScanner for scanEntityPackages -->
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-core</artifactId>
    <version>0.3.19</version>
    <scope>test</scope>
</dependency>
```

## 2. Version Information and Compatibility

| **Dimension** | **Value / Range** | **Evidence in the `release` branch** |
|---------------|-------------------|--------------------------------------|
| Version line | `0.2.x`; released `0.2.10`, development `0.2.11-SNAPSHOT` | `<revision>` property in the root POM |
| JDK | 17 – 25 (baseline `17`) | `<java.version>17</java.version>`; CI matrix `17`, `21`, `25` |
| Hibernate ORM | 6.2.x – 7.3.x | `hibernate-*` profiles; default `7.3.4.Final` |
| JUnit | Jupiter 5 / 6 API as managed by the parent chain (`6.1.3` for the `0.2.10` release) | `junit-jupiter` optional dependency |
| Database for the built-in fixtures | H2 (in-memory) via `org.h2.Driver` + `org.hibernate.dialect.H2Dialect` | `AbstractHibernateH2Test#buildConfiguration()` |
| Jakarta Persistence | `jakarta.persistence.*` (`@Entity`, `@Id`, `@Table`, …) | entity sources |
| `@since` of every type below | `1.0.0` | Javadoc tags |

The extension API is the JUnit 5 (`org.junit.jupiter.api.extension`) programming model, so tests run on the JUnit
Platform (Maven Surefire 2.22+ or the `junit-platform` launcher).

## 3. API Reference

### 3.1 Type Overview

| **Type** | **Visibility** | **Kind** | **Purpose** |
|----------|----------------|----------|-------------|
| `HibernateTest` | `public @interface` (`@Target(TYPE)`) | annotation | Marks a test class as a Hibernate test; pulls in `HibernateExtension` |
| `HibernateRuntime` | `public @interface` (`@Target({FIELD, PARAMETER})`) | annotation | Marks where a Hibernate runtime component should be injected |
| `HibernateExtension` | `public class` | JUnit 5 extension | Does all the bootstrapping, injection and cleanup |
| `AbstractHibernateTest` | package-private `abstract class` | test base | Per-method `SessionFactory`/`Session`/`StatelessSession` lifecycle with template hooks |
| `AbstractHibernateH2Test` | `public abstract class` | test base | `AbstractHibernateTest` pre-configured for H2, with built-in `@Nested` session tests |
| `User`, `Order`, `Product`, `UserProfile` | `public class` | JPA entities | Reusable sample entity graph (`t_user`, `t_order`, `t_product`, `t_user_profile`) |

### 3.2 `@HibernateTest`

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

| **Attribute** | **Type** | **Default** | **Meaning** |
|---------------|----------|-------------|-------------|
| `properties` | `String[]` | `{}` | Hibernate `Configuration` settings in `key=value` form, e.g. `"hibernate.dialect=org.hibernate.dialect.H2Dialect"` |
| `scanEntityPackages` | `String[]` | `{}` | Packages scanned (recursively) for classes annotated with `jakarta.persistence.@Entity`; each found class is registered with `Configuration#addAnnotatedClass(Class)` |

Contract:

- `@ExtendWith(HibernateExtension.class)` is meta-annotated, so you never declare `@ExtendWith` yourself.
- `@Inherited` + `@Documented`, target is a type only.
- Applying it gives you: `Configuration` + `SessionFactory` built before all tests of the class, `Session` +
  `StatelessSession` opened before each test method and closed after it, `SessionFactory` closed after all tests.
- Entries in `properties` that have no `=` (or `=` at index 0) are **skipped with a warning**, not an error.
- `scanEntityPackages` uses the thread context `ClassLoader`; classes without `@Entity` are ignored. If you need
  an entity that is not in a scanned package, register it yourself with a programmatic bootstrap (see
  `AbstractHibernateH2Test`).

### 3.3 `@HibernateRuntime`

```java
@Target({FIELD, PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HibernateRuntime {
}
```

Injectable types and their scope:

| **Type** | **Instance field** | **Static field** | **Method parameter** | **Lifetime** |
|----------|--------------------|------------------|----------------------|--------------|
| `org.hibernate.cfg.Configuration` | yes | yes | yes | per test class |
| `org.hibernate.SessionFactory` | yes | yes | yes | per test class |
| `org.hibernate.Session` | yes | **no** (stays `null`) | yes | per test method |
| `org.hibernate.StatelessSession` | yes | **no** (stays `null`) | yes | per test method |

Anything else annotated with `@HibernateRuntime` is left untouched (fields keep their value; unsupported parameter
types are not resolved by the extension).

### 3.4 `HibernateExtension`

```java
public class HibernateExtension implements BeforeAllCallback, BeforeEachCallback,
        AfterEachCallback, AfterAllCallback, ParameterResolver
```

Behaviour worth knowing when you debug tests:

- **Shared store.** `Configuration` and `SessionFactory` live in the JUnit `Store` of the *outermost* context whose
  class carries `@HibernateTest`, under namespace `HibernateExtension` and keys `configuration` / `sessionFactory`.
  `Session` / `StatelessSession` live in the **method-level** store under `session` / `statelessSession`.
- **Nested classes share one `SessionFactory`.** `@Nested` test classes do not rebuild it: the extension walks up
  the context hierarchy (`findHibernateTestContext`) and reuses what the outer `@HibernateTest` class created.
- **Static vs instance injection.** `beforeAll` injects only `Configuration`/`SessionFactory` into annotated static
  fields; `beforeEach` injects all four types into annotated instance fields. Fields are collected across the whole
  class hierarchy (up to but excluding `Object`) and set with `setAccessible(true)`.
- **Parameter resolution.** `supportsParameter` requires both `@HibernateRuntime` and one of the four supported
  types; `resolveParameter` reads from the class store or the method store accordingly.
- **Cleanup.** `afterEach` closes `StatelessSession` then `Session`, only if `isOpen()`, and logs a warning instead
  of failing the test when closing throws. `afterAll` closes the `SessionFactory` only when the class being finished
  is itself annotated with `@HibernateTest`, so nested classes never close it early.
- **Logging.** Uses `io.microsphere.logging.LoggerFactory` at `debug` for creation/closing events and `warn` for
  malformed properties and close failures.

### 3.5 `AbstractHibernateTest`

```java
abstract class AbstractHibernateTest {          // package-private!

    protected Configuration configuration;
    protected SessionFactory sessionFactory;
    protected Session session;
    protected StatelessSession statelessSession;

    @BeforeEach  final void setUp();
    @AfterEach   final void tearDown();

    protected abstract Configuration buildConfiguration();
    protected void customizeConfiguration(Configuration configuration) {}
    protected void init() {}
    protected void destroy() {}
}
```

- `setUp()` is `final`: build configuration → `customizeConfiguration` → `buildSessionFactory()` → `openSession()` +
  `openStatelessSession()` → `init()`. `tearDown()` is `final`: `destroy()` → close session → close stateless
  session → close session factory.
- **Lifecycle is per test method** — a fresh `SessionFactory` for every `@Test`. That is the cheapest way to get
  isolation, and the reason this base class is not recommended for large suites.
- **Visibility caveat:** the type itself is package-private, so you cannot extend `AbstractHibernateTest` from your
  own package. Extend the public `AbstractHibernateH2Test` instead, or use `@HibernateTest`; subclasses inherit the
  `protected` fields either way.

### 3.6 `AbstractHibernateH2Test`

```java
public abstract class AbstractHibernateH2Test extends AbstractHibernateTest
```

It is both a base class and a test suite: the built-in `@Nested` classes run in **every** subclass.

**Fixture and configuration**

| **Member** | **Behaviour** |
|------------|---------------|
| `protected User user` | test fixture, created by `init()` before each test |
| `protected User createUser()` | builds the graph `User("Tom")` + `UserProfile("ID123456")` + `Order("ORD001")` + `Product("Laptop")`; override to change it |
| `buildConfiguration()` | registers the four entities and sets `org.h2.Driver`, `jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1`, `sa` / empty password, `H2Dialect`, `show_sql=true`, `format_sql=true`, `hbm2ddl.auto=update` |
| `protected void doInAction(Runnable)` | runs the action inside `assertDoesNotThrow` — used for non-transactional operations |

**Inherited `@Nested` "Session Test" cases** (`session`, each write wrapped in a transaction): `testPersistAndGet`,
`testPersistAndMerge`, `testPersistAndRemove`, `testPersistAndReplicate` (`LATEST_VERSION`), `testPersistAndFlush`,
`testPersistAndDetach`, `testPersistAndEvict`, `testPersistAndLock` (`LockMode.READ`), `testPersistAndRefresh`,
`testPersistAndLoad` (`session.find`), `testPersistAndGetManagedEntities` (`createQuery("FROM User", User.class)`).

**Inherited `@Nested` "Stateless Session Test" cases** (`statelessSession`): `testInsertAndGet`,
`testInsertAndDelete`, `testInsertAndUpdate`, `testInsertAndRefresh`, `testInsertAndFetch`.

Both nested classes define their own `doInTransaction(Runnable)` helper that begins a transaction on the relevant
session, runs the action and commits inside `assertDoesNotThrow`.

Because these tests are inherited, adding your own `@Test` methods in a subclass is the normal usage — which is
exactly what `microsphere-hibernate-core` does in `EntityCallbackIntegratorTest` (an empty subclass whose only
purpose is to run the inherited flow while the callback SPI is active).

### 3.7 Sample Entities

A small but realistic graph: one-to-one, one-to-many and many-to-many with `IDENTITY` ids.

| **Entity** | **Table** | **Fields** | **Associations** |
|------------|-----------|------------|------------------|
| `User` | `t_user` | `Long id` (`@Id @GeneratedValue(IDENTITY)`), `String name` | `@OneToOne(mappedBy = "user", cascade = ALL, fetch = LAZY) UserProfile profile`; `@OneToMany(mappedBy = "user", cascade = ALL, fetch = LAZY) List<Order> orders` (initialized to an empty `ArrayList`) |
| `UserProfile` | `t_user_profile` | `Long id`, `String idCard` | `@OneToOne @JoinColumn(name = "user_id") User user` (owning side) |
| `Order` | `t_order` | `Long id`, `String orderNo` | `@ManyToOne @JoinColumn(name = "user_id") User user`; `@ManyToMany(cascade = {PERSIST, MERGE}) @JoinTable(name = "t_order_product", joinColumns = order_id, inverseJoinColumns = product_id) Set<Product> products` |
| `Product` | `t_product` | `Long id`, `String productName` | `@ManyToMany(mappedBy = "products", fetch = LAZY) Set<Order> orders` |

Contract details that tests rely on:

- `User#addOrder(Order)` maintains the bidirectional link (`orders.add(order); order.setUser(this);`) — call it
  instead of touching the list directly.
- `equals`/`hashCode` are value-based on the *non-relationship* fields: `User(id, name)`, `Order(id, orderNo)`,
  `Product(id, productName)`, `UserProfile(id, idCard)`. They are safe for `assertEquals` between a detached
  instance and a freshly loaded one, and they deliberately exclude collections to avoid lazy-loading during
  comparison.
- All relationships use `cascade = ALL` (profile/orders) or `{PERSIST, MERGE}` (order → products), so persisting a
  `User` persists the whole graph.

## 4. Examples From the Project's Own Tests

### 4.1 Annotation-driven test — `HibernateAnnotationTest`

Source: `microsphere-hibernate-test/src/test/java/io/microsphere/hibernate/test/HibernateAnnotationTest.java`

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

    @Nested
    class FieldTest {

        @HibernateRuntime private Configuration configuration;
        @HibernateRuntime private SessionFactory sessionFactory;
        @HibernateRuntime private Session session;
        @HibernateRuntime private StatelessSession statelessSession;

        @Test
        void test() {
            assertNotNull(configuration);
            assertNotNull(sessionFactory);
            assertNotNull(session);
            assertNotNull(statelessSession);
        }
    }

    @Nested
    class StaticFieldTest {

        @HibernateRuntime private static Configuration configuration;
        @HibernateRuntime private static SessionFactory sessionFactory;
        @HibernateRuntime private static Session session;
        @HibernateRuntime private static StatelessSession statelessSession;

        @Test
        void test() {
            assertNotNull(configuration);
            assertNotNull(sessionFactory);
            // Session and StatelessSession must NOT be injected into static fields
            assertNull(session);
            assertNull(statelessSession);
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

This is the reference for all three injection styles (instance field, static field, method parameter) and shows the
static-field limitation as an assertion rather than a footnote.

### 4.2 Extending the H2 base class — `HibernateH2Test`

Source: `microsphere-hibernate-test/src/test/java/io/microsphere/hibernate/test/HibernateH2Test.java`

```java
public class HibernateH2Test extends AbstractHibernateH2Test {

    @Override
    protected void customizeConfiguration(Configuration configuration) {
        super.customizeConfiguration(configuration);
    }
}
```

Running it executes the inherited `Session Test` and `Stateless Session Test` suites. Your own additions look like
this (shape taken from the `AbstractHibernateH2Test` Javadoc example):

```java
class MyEntityTest extends AbstractHibernateH2Test {

    @Test
    void testPersistAndGet() {
        doInTransaction(() -> {
            session.persist(user);
            User found = session.get(User.class, user.getId());
            assertEquals(user, found);
        });
    }

    @Override
    protected User createUser() {
        User user = new User();
        user.setName("Alice");
        return user;
    }
}
```

(`doInTransaction` exists on the two `@Nested` classes of the base class; in your own subclass, wrap the action in
a `session.beginTransaction()` / `commit()` pair or reuse `doInAction(...)`.)

### 4.3 Using the kit from another module — `EntityCallbackIntegratorTest`

Source: `microsphere-hibernate-core/src/test/java/io/microsphere/hibernate/entity/EntityCallbackIntegratorTest.java`

```java
class EntityCallbackIntegratorTest extends AbstractHibernateH2Test {
}
```

A different module, a different package, zero test code: the inherited H2 suite plus the `EntityCallback` SPI from
[microsphere-hibernate-core](microsphere-hibernate-core.md) are enough. This is the pattern to copy when you want to
verify your own `EntityCallback` implementations.

### 4.4 Plain unit tests over the entities — `UserTest`

Source: `microsphere-hibernate-test/src/test/java/io/microsphere/hibernate/test/entity/UserTest.java`

```java
@Test
void testAddOrder() {
    User user = new User();
    Order order1 = new Order();
    Order order2 = new Order();

    user.addOrder(order1);
    user.addOrder(order2);

    assertEquals(2, user.getOrders().size());
    assertEquals(user, order1.getUser());
    assertEquals(user, order2.getUser());
}
```

`OrderTest`, `ProductTest` and `UserProfileTest` follow the same template: default-construction assertions, getter /
setter round-trips, `equals`/`hashCode` contract checks (reflexive, symmetric, null, different type, different id)
and null-field behaviour. Use them as a template for testing your own entity POJOs without booting Hibernate.

## 5. Integration

### 5.1 Minimum consumer setup

1. Add `microsphere-hibernate-test` in `test` scope (version from the
   [BOM](microsphere-hibernate-dependencies.md)).
2. Add your own `test`-scope `junit-jupiter`, `hibernate-core`, `h2` (or your database driver) and
   `microsphere-java-core` — see section 1.
3. Pick one style:
   - `@HibernateTest` + `@HibernateRuntime` on a plain class (recommended: one `SessionFactory` per class), or
   - extend `AbstractHibernateH2Test` when you want the ready-made H2 entity graph and inherited suites (and accept
     a new `SessionFactory` per test method).
4. Make sure your entities are reachable: list them in `scanEntityPackages`, or add them in
   `buildConfiguration()` / `customizeConfiguration(...)`.
5. Combine with `microsphere-hibernate-core` when you want to assert `EntityCallback` behaviour: registering your
   callback via `META-INF/services/io.microsphere.hibernate.entity.EntityCallback` is enough for the inherited tests
   to trigger it.

### 5.2 Property keys you will use

`@HibernateTest(properties = "key=value")` accepts any standard Hibernate setting; the ones used throughout this
project are:

| **Key** | **Example value** | **Where it appears** |
|---------|-------------------|----------------------|
| `hibernate.connection.driver_class` | `org.h2.Driver` | `HibernateAnnotationTest`, `AbstractHibernateH2Test` |
| `hibernate.connection.url` | `jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1` | same |
| `hibernate.connection.username` / `.password` | `sa` / `""` | same |
| `hibernate.dialect` | `org.hibernate.dialect.H2Dialect` | same |
| `hibernate.show_sql` / `hibernate.format_sql` | `true` / `true` | `AbstractHibernateH2Test` |
| `hibernate.hbm2ddl.auto` | `update` | both, so schemas exist without a migration step |

### 5.3 Isolation and performance guidance

- Give each test class its **own** in-memory database name (`jdbc:h2:mem:hibernate_annotation_test_db`,
  `jdbc:h2:mem:test_db`, …) when you keep `DB_CLOSE_DELAY=-1`; otherwise classes share state through the same
  schema.
- `hbm2ddl.auto=update` is convenient but never drops columns; for strict schema expectations use `create-drop`.
- Extending `AbstractHibernateH2Test` runs 16 inherited tests per subclass. That is intentional (it doubles as the
  integration harness for `microsphere-hibernate-core`) but it is not a lightweight unit test — prefer
  `@HibernateTest` or plain POJO tests when you only need the entity semantics.
- `HibernateExtension` closes resources defensively and logs warnings instead of throwing, so a leaked session shows
  up in the log rather than failing an unrelated assertion.

### 5.4 Troubleshooting

| **Symptom** | **Cause and fix** |
|-------------|-------------------|
| `@HibernateRuntime` field stays `null` | The class is missing `@HibernateTest`; the type is not one of the four supported types; or it is a `static` `Session`/`StatelessSession` field (not injected by design). |
| Entities never registered (`org.hibernate.MappingException` / unknown entity at bootstrap) | The package in `scanEntityPackages` does not contain `@Entity`-annotated classes on the thread context classpath — check the package name and that the classes are compiled into `target/test-classes` / `target/classes`. |
| Your property is silently ignored | `properties` entries must be `key=value`; entries without a valid separator are skipped with a `warn` log. |
| `ClassNotFoundException: io.microsphere.io.scanner.SimpleClassScanner` | `microsphere-java-core` is optional — add it to your test scope. |
| `NoClassDefFoundError: org/hibernate/…` or `org/junit/jupiter/…` | `hibernate-core` / `junit-jupiter` are optional in this module; declare them yourself. |
| Second test class sees stale data | Both classes use the same `jdbc:h2:mem:<name>` URL — see 5.3. |
| `AbstractHibernateTest` "not visible" at compile time | Expected: the class is package-private. Extend `AbstractHibernateH2Test` or use `@HibernateTest`. |

## 6. Related Documentation

- [microsphere-hibernate-core.md](microsphere-hibernate-core.md) — the `EntityCallback` machinery these tests exercise
- [microsphere-hibernate-dependencies.md](microsphere-hibernate-dependencies.md) — version pinning for this artifact
- [microsphere-hibernate-parent.md](microsphere-hibernate-parent.md) — the profiles that select the Hibernate version
