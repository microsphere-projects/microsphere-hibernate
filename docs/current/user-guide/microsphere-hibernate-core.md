# microsphere-hibernate-core

The core module of Microsphere Hibernate. It turns Hibernate ORM's two dozen event listener interfaces into a
single, `ServiceLoader`-discovered callback interface: **`EntityCallback`**.

[← Back to the User Guide index](README.md) · Chinese version: [zh/microsphere-hibernate-core.md](zh/microsphere-hibernate-core.md)

## 1. Module Positioning and Maven Coordinates

| **Item** | **Value** |
|----------|-----------|
| `groupId` | `io.github.microsphere-projects` |
| `artifactId` | `microsphere-hibernate-core` |
| `version` | `${revision}` → `0.2.11-SNAPSHOT` on the `release` branch (last release: `0.2.10`) |
| `packaging` | `jar` |
| `name` | `Microsphere :: Hibernate :: Core` |
| Root package | `io.microsphere.hibernate` (`entity` + `constants`) |
| Parent | [microsphere-hibernate-parent](microsphere-hibernate-parent.md) |
| Declared dependencies | `microsphere-annotation-processor`, `microsphere-java-core`, `org.hibernate.orm:hibernate-core` — all **optional** |

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-hibernate-core</artifactId>
</dependency>
```

Because the three compile-time dependencies are `optional`, your application must declare the ones it needs:

```xml
<!-- Required at runtime: the module itself does not bring Hibernate ORM -->
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>7.3.4.Final</version>
</dependency>
<!-- Required at runtime: io.microsphere.collection.ListUtils / io.microsphere.lang.Prioritized -->
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-core</artifactId>
    <version>0.3.19</version>
</dependency>
```

`microsphere-annotation-processor` is only needed at compile time if you want configuration-metadata generation
for the `@ConfigurationProperty`-annotated constants in `PropertyConstants`.

Test-scope dependencies of the module itself (from its own `pom.xml`): `junit-jupiter`, `mockito-core`,
`mockito-junit-jupiter`, `h2`, `logback-classic` and `microsphere-hibernate-test`.

## 2. Version Information and Compatibility

| **Dimension** | **Value / Range** | **Evidence in the `release` branch** |
|---------------|-------------------|--------------------------------------|
| Version line | `0.2.x`; released `0.2.10`, development `0.2.11-SNAPSHOT` | `<revision>0.2.11-SNAPSHOT</revision>` in the root POM |
| JDK | 17 – 25 (baseline `17`) | `<java.version>17</java.version>`; CI matrix `17`, `21`, `25` |
| Hibernate ORM | 6.2.x – 7.3.x | `hibernate-6.2` … `hibernate-7.3` profiles, default `7.3.4.Final` |
| Spring Cloud | 2022.0.x – 2025.0.x | inherited `spring-cloud-*` profiles (`microsphere-spring-cloud-parent:0.2.26`) |
| Hibernate artifacts used | `org.hibernate.orm:hibernate-core`, `hibernate-platform` | `microsphere-hibernate-parent/pom.xml` |
| `@since` of every type below | `1.0.0` | Javadoc tags |

Hibernate 6.x is Jakarta-based (`jakarta.persistence.*`), which is why the 0.2.x line cannot be used with
Hibernate 5.x — for Hibernate 5.x use the `0.1.x` line.

## 3. API Reference

### 3.1 Type Overview

| **Type** | **Package** | **Visibility** | **Kind** | **Role** |
|----------|-------------|----------------|----------|----------|
| `EntityCallback` | `io.microsphere.hibernate.entity` | `public` | interface | The callback contract you implement |
| `CompositeEntityCallback` | `io.microsphere.hibernate.entity` | `public` | class | Fans one callback call out to an ordered list of callbacks |
| `EntittyCallbackIntegrator` | `io.microsphere.hibernate.entity` | `public` | class (`Integrator`) | Hibernate SPI entry point that registers the listener |
| `EntityCallbackListener` | `io.microsphere.hibernate.entity` | package-private | class (23 `*EventListener`s) | Translates Hibernate events into `EntityCallback` calls |
| `EntityCallbackListenerDuplicationStrategy` | `io.microsphere.hibernate.entity` | package-private | class (`DuplicationStrategy`) | Keeps a single listener instance per `SessionFactory` |
| `PropertyConstants` | `io.microsphere.hibernate.constants` | `public` | interface | Configuration property name constants |

> `EntittyCallbackIntegrator` contains a typo (`Entitty`). It is the published, SPI-registered name; you must
> spell it exactly like this when you reference it manually.

### 3.2 `EntityCallback`

`public interface EntityCallback` — "the lifecycle callback interface for entity; the implementation class will
be loaded by `ServiceLoader` (Java Services Loader)".

**Contract**

- Every method is a `default` no-op, so an implementation overrides only what it needs.
- Implementations are discovered with `ServiceLoader.load(EntityCallback.class)` — they must be listed in
  `META-INF/services/io.microsphere.hibernate.entity.EntityCallback` and have a public no-arg constructor.
- Implementations should be thread-safe and fast: they are invoked inside Hibernate event processing, on the
  thread performing the persistence operation.
- Methods receiving `Object[] state`, `String[] propertyNames` and `Type[] types` are documented as *"it is not
  recommended that the interceptor modify the state"* — treat those arrays as read-only.
- Callbacks never veto an operation (see `EntityCallbackListener` below).
- Invocation order across multiple implementations is controlled by implementing
  `io.microsphere.lang.Prioritized#getPriority()`; the list is sorted with `Prioritized.COMPARATOR` before use.

**Callback methods**

*Stateful-session semantics*

| **Method** | **Triggered by** | **Key arguments** |
|------------|------------------|-------------------|
| `onLoad(Object id, String entityClassName, Object entity, LockMode lockMode, boolean isAssociationFetch, Object result, Boolean readOnly, LoadType loadType)` | `Session#find(Class, Object)` / `LOAD` event | the requested id, entity class name, the instance being loaded, the resolved `LockMode`, whether the load came from an association fetch, the event result, the read-only flag and the `LoadEventListener.LoadType` |
| `onPersist(Object entity, String entityName)` | `Session#persist(Object)` | the entity and its entity name |
| `onDelete(Object entity, boolean cascadeDeleteEnabled, boolean orphanRemovalBeforeUpdates)` | `Session#remove(Object)` | the entity plus the cascade / orphan-removal flags |
| `onMerge(Object original, Object id, Object entity, String entityName, Object result)` | `Session#merge(Object)` | the detached source object, requested id, managed entity, entity name, merge result |
| `onReplicate(Object entity, ReplicationMode replicationMode, String entityName)` | `Session#replicate(Object, ReplicationMode)` | the entity, the `ReplicationMode`, entity name |
| `onFlushDirty(Object entity, Object id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types)` | `Session#flush()`, dirty-checked entity | current vs. load-time state, property names and types |
| `onEvict(Object entity)` | `Session#evict(Object)` | the evicted entity |
| `onLock(Object entity, LockMode lockMode, String entityName)` | `Session#lock(Object, LockMode)` | the entity and requested `LockMode` |
| `onRefresh(Object entity, String entityName, LockMode lockMode)` | `Session#refresh(Object)` | the entity, entity name, `LockMode` |

*Stateless-session / pre-post semantics*

| **Method** | **Triggered by** |
|------------|------------------|
| `onPreLoad(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types)` | before values are pushed into a newly loaded instance (`StatelessSession#get`) |
| `onPostLoad(Object entity, Object id, String[] propertyNames, Type[] types)` | just after the instance is initialized |
| `onPreDelete` / `onPostDelete(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types)` | around `StatelessSession#delete(Object)` |
| `onPreInsert` / `onPostInsert(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes)` | around `StatelessSession#insert(Object)` |
| `onPreUpdate` / `onPostUpdate(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes)` | around `StatelessSession#update(Object)` |

*Collection semantics* (all `(Object collection, Object key)`)

| **Method** | **Meaning** |
|------------|-------------|
| `onPreRecreateCollection` / `onPostRecreateCollection` | before / after a collection is (re)created |
| `onPreCollectionRemove` / `onPostRemoveCollection` | before / after a collection is deleted |
| `onPreUpdateCollection` / `onPostUpdateCollection` | before / after a collection is updated |

### 3.3 `CompositeEntityCallback`

`public class CompositeEntityCallback implements EntityCallback`

```java
public CompositeEntityCallback(List<EntityCallback> callbacks)
```

- Holds the callbacks in the given list order.
- Overrides every `EntityCallback` method and dispatches with
  `io.microsphere.collection.ListUtils.forEach(callbacks, callback -> callback.xxx(...))`, i.e. one callback
  failing stops the remaining ones for that event.
- It does **not** re-sort by priority — sorting happens once, in `EntityCallbackListener`'s constructor.
- Useful when you want to drive the callback chain yourself (unit tests, frameworks built on top of the module).

### 3.4 `EntittyCallbackIntegrator`

`public class EntittyCallbackIntegrator implements org.hibernate.integrator.spi.Integrator`

Registered through the JAR's own service file, so no explicit wiring is required in a normal application.

```java
// The BootstrapContext-based entry point Hibernate calls on 6.2 – 7.3:
@Override
public void integrate(Metadata metadata, BootstrapContext bootstrapContext,
                      SessionFactoryImplementor sessionFactory) {
    integrate(metadata, sessionFactory, (SessionFactoryServiceRegistry) sessionFactory.getServiceRegistry());
}

// The legacy three-argument form, kept public so it can also be invoked directly:
public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactory,
                      SessionFactoryServiceRegistry serviceRegistry) { ... }
```

What it does, in order:

1. Gets the `EventListenerRegistry` from the `SessionFactoryServiceRegistry`.
2. `eventListenerRegistry.addDuplicationStrategy(new EntityCallbackListenerDuplicationStrategy())`.
3. Creates **one** `EntityCallbackListener` and appends it to every supported `EventType`.
4. `disintegrate(...)` is a no-op — cleanup is left to `SessionFactory` close.

### 3.5 `EntityCallbackListener` (package-private)

You cannot reference this type from your own code; it is documented because it defines the observable behaviour of
`EntityCallback`.

- Implements all 23 relevant Hibernate listener interfaces: `LoadEventListener`, `PersistEventListener`,
  `MergeEventListener`, `DeleteEventListener`, `ReplicateEventListener`, `FlushEntityEventListener`,
  `EvictEventListener`, `LockEventListener`, `RefreshEventListener`, `PreLoadEventListener`,
  `PostLoadEventListener`, `PreDeleteEventListener`, `PostDeleteEventListener`, `PreUpdateEventListener`,
  `PostUpdateEventListener`, `PreInsertEventListener`, `PostInsertEventListener`,
  `PreCollectionRecreateEventListener`, `PostCollectionRecreateEventListener`,
  `PreCollectionRemoveEventListener`, `PostCollectionRemoveEventListener`,
  `PreCollectionUpdateEventListener`, `PostCollectionUpdateEventListener`.
- Package-private constructors: `EntityCallbackListener()` (loads via `ServiceLoader.load(EntityCallback.class)`)
  and `EntityCallbackListener(Iterable<EntityCallback>)` (copies into a `LinkedList` via
  `ListUtils.newLinkedList`, sorts with `Prioritized.COMPARATOR`, wraps in a `CompositeEntityCallback`).
- Event → callback mapping is 1:1; `LockMode` values are read from the event's `LockOptions` through the
  `protected LockMode getLockMode(LockOptions)` hook.
- `onPreDelete`, `onPreInsert`, `onPreUpdate` return `false`, i.e. they never veto the Hibernate operation.
- `requiresPostCommitHandling(EntityPersister)` returns `false`, so post-insert/update/delete callbacks run in the
  regular after-action phase rather than after commit.

**Event types registered by the integrator**

| **`EventType`** | **`EntityCallback` method** |
|-----------------|------------------------------|
| `LOAD` | `onLoad` |
| `PERSIST` | `onPersist` |
| `MERGE` | `onMerge` |
| `DELETE` | `onDelete` |
| `REPLICATE` | `onReplicate` |
| `FLUSH_ENTITY` | `onFlushDirty` |
| `EVICT` | `onEvict` |
| `LOCK` | `onLock` |
| `REFRESH` | `onRefresh` |
| `PRE_LOAD` / `POST_LOAD` | `onPreLoad` / `onPostLoad` |
| `PRE_DELETE` / `POST_DELETE` | `onPreDelete` / `onPostDelete` |
| `PRE_UPDATE` / `POST_UPDATE` | `onPreUpdate` / `onPostUpdate` |
| `PRE_INSERT` / `POST_INSERT` | `onPreInsert` / `onPostInsert` |
| `PRE_COLLECTION_RECREATE` / `POST_COLLECTION_RECREATE` | `onPreRecreateCollection` / `onPostRecreateCollection` |
| `PRE_COLLECTION_REMOVE` / `POST_COLLECTION_REMOVE` | `onPreCollectionRemove` / `onPostRemoveCollection` |
| `PRE_COLLECTION_UPDATE` / `POST_COLLECTION_UPDATE` | `onPreUpdateCollection` / `onPostUpdateCollection` |

### 3.6 `EntityCallbackListenerDuplicationStrategy` (package-private)

Implements `org.hibernate.event.service.spi.DuplicationStrategy`:

```java
@Override
public boolean areMatch(Object listener, Object original) {
    Class<?> listenerClass = listener.getClass();
    return Objects.equals(listenerClass, original.getClass())
            && EntityCallbackListener.class.equals(listenerClass);
}

@Override
public Action getAction() {
    return KEEP_ORIGINAL;
}
```

- `areMatch` is `true` only when both objects are exactly `EntityCallbackListener` instances.
- `getAction()` is `KEEP_ORIGINAL`: when the integrator runs twice (for example, several `SessionFactory` bootstraps
  in one JVM or a duplicated service registration), the already-registered listener is kept and the new one is
  dropped, so a callback is never invoked twice for one event.
- Verified by `EntityCallbackListenerDuplicationStrategyTest` (core module, `src/test`), which asserts
  `areMatch(listener, listener) == true`, `false` against Hibernate's `DefaultLoadEventListener`, and
  `getAction() == KEEP_ORIGINAL`.

### 3.7 `PropertyConstants`

`public interface PropertyConstants` in `io.microsphere.hibernate.constants` — "the interface to declare the
property constants of Microsphere Hibernate":

| **Constant** | **Value** | **Notes** |
|--------------|-----------|-----------|
| `MICROSPHERE_HIBERNATE_PROPERTY_NAME_PREFIX` | `microsphere.hibernate.` | built from `microsphere.` + `hibernate` + `.` |
| `DEFAULT_MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_VALUE` | `"true"` | string form |
| `DEFAULT_MICROSPHERE_HIBERNATE_ENABLED` | `true` | `Boolean.parseBoolean` of the value above |
| `MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_NAME` | `microsphere.hibernate.enabled` | annotated with `@ConfigurationProperty(type = boolean.class, defaultValue = "true", source = SYSTEM_PROPERTIES_SOURCE)` |

The `@ConfigurationProperty` annotation (`io.microsphere.annotation`) is what
`microsphere-annotation-processor` reads to emit configuration metadata.

> **Current behaviour in 0.2.x:** the `microsphere.hibernate.enabled` key is declared and documented, but the
> `EntityCallback` integrator does **not** consult it — the integrator is registered whenever the JAR is on the
> classpath. Treat the constant as a naming convention (and as a forward hook) rather than as a working switch:
> to disable the feature, remove the `META-INF/services/io.microsphere.hibernate.entity.EntityCallback` file or
> the `microsphere-hibernate-core` dependency.

## 4. Examples From the Project's Own Tests

### 4.1 A complete callback implementation — `TestEntityCallback`

Source: `microsphere-hibernate-core/src/test/java/io/microsphere/hibernate/entity/TestEntityCallback.java`
(registered via `microsphere-hibernate-core/src/test/resources/META-INF/services/io.microsphere.hibernate.entity.EntityCallback`,
whose content is the single line `io.microsphere.hibernate.entity.TestEntityCallback`).

```java
public class TestEntityCallback implements EntityCallback {

    private static final Logger logger = getLogger(TestEntityCallback.class);

    @Override
    public void onLoad(Object id, String entityClassName, Object entity, LockMode lockMode,
                       boolean isAssociationFetch, Object result, Boolean readOnly,
                       LoadEventListener.LoadType loadType) {
        logger.trace("onLoad - id : {}, entityClassName : {}, entity : {}, lockMode : {}, "
                        + "isAssociationFetch : {}, result : {}, readOnly : {}, loadType : {}",
                id, entityClassName, entity, lockMode, isAssociationFetch, result, readOnly, loadType);
        EntityCallback.super.onLoad(id, entityClassName, entity, lockMode, isAssociationFetch,
                result, readOnly, loadType);
    }

    @Override
    public void onPersist(Object entity, String entityName) {
        logger.trace("onPersist - entity : {}, entityName : {}", entity, entityName);
        EntityCallback.super.onPersist(entity, entityName);
    }

    // … one override per callback method, each delegating to the default via EntityCallback.super
}
```

Two habits worth copying from the reference implementation: it logs through `io.microsphere.logging` (the same
logging façade the module uses internally), and it calls `EntityCallback.super.onXxx(...)` explicitly so a
sub-class can still see the default behaviour.

### 4.2 End-to-end integration test — `EntityCallbackIntegratorTest`

Source: `microsphere-hibernate-core/src/test/java/io/microsphere/hibernate/entity/EntityCallbackIntegratorTest.java`

```java
class EntityCallbackIntegratorTest extends AbstractHibernateH2Test {
}
```

The test is intentionally empty: inheriting [AbstractHibernateH2Test](microsphere-hibernate-test.md) boots an H2
`SessionFactory`, and because `microsphere-hibernate-core` is on the test classpath Hibernate loads
`EntittyCallbackIntegrator` from `META-INF/services/org.hibernate.integrator.spi.Integrator`. Every inherited
`Session`/`StatelessSession` test therefore exercises the whole callback chain — the `TestEntityCallback` trace
lines are the assertion output. To see them, enable `TRACE` for the logger in your Logback configuration (the
module's tests run with `logback-classic` on the classpath):

```xml
<!-- src/test/resources/logback-test.xml -->
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    <logger name="io.microsphere.hibernate" level="TRACE"/>
    <root level="INFO">
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>
```

To reproduce the same wiring in your own project, copy the pattern: a subclass of `AbstractHibernateH2Test` (or any
Hibernate bootstrap) plus your `EntityCallback` service file.

### 4.3 Building the callback chain yourself — `CompositeEntityCallback`

```java
EntityCallback logging = new LoggingEntityCallback();
EntityCallback audit = new AuditEntityCallback();
CompositeEntityCallback composite = new CompositeEntityCallback(Arrays.asList(logging, audit));

composite.onPersist(myEntity, "MyEntity");   // both callbacks, in list order
```

(Shape taken from the `CompositeEntityCallback` Javadoc example.)

### 4.4 Asserting the constants — `PropertyConstantsTest`

Source: `microsphere-hibernate-core/src/test/java/io/microsphere/hibernate/constants/PropertyConstantsTest.java`

```java
@Test
void test() {
    assertEquals("microsphere.hibernate.", MICROSPHERE_HIBERNATE_PROPERTY_NAME_PREFIX);
    assertEquals("true", DEFAULT_MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_VALUE);
    assertTrue(DEFAULT_MICROSPHERE_HIBERNATE_ENABLED);
    assertEquals("microsphere.hibernate.enabled", MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_NAME);
}
```

## 5. Integration

### 5.1 The two SPI files

| **File** | **Shipped by** | **Content** |
|----------|----------------|-------------|
| `META-INF/services/org.hibernate.integrator.spi.Integrator` | `microsphere-hibernate-core` (already in the JAR) | `io.microsphere.hibernate.entity.EntittyCallbackIntegrator` |
| `META-INF/services/io.microsphere.hibernate.entity.EntityCallback` | **your** project (`src/main/resources`) | one fully-qualified class name per line |

The integrator file is why adding the dependency is enough for Hibernate to pick the feature up. The
`EntityCallback` file is the only thing you must add yourself:

```
# src/main/resources/META-INF/services/io.microsphere.hibernate.entity.EntityCallback
com.acme.order.logging.OrderEntityCallback
com.acme.order.audit.AuditEntityCallback
```

Notes:

- Each listed class must implement `EntityCallback` and be publicly instantiable with a no-arg constructor.
- Classpath ordering determines `ServiceLoader` order; when you need deterministic sequencing, implement
  `Prioritized` instead of relying on file order.
- With GraalVM native-image or a custom `ClassLoader`, make sure both service files are visible to the class
  loader that loads `EntityCallback` (`ServiceLoader.load` uses the thread context / caller class loader).

### 5.2 Configuration properties

There is no Hibernate property to turn the feature on or off. The keys you control the surrounding behaviour with
are the ordinary Hibernate ones — see the sample configuration in
[AbstractHibernateH2Test](microsphere-hibernate-test.md#36-abstract-hibernate-h2test) and the
`@HibernateTest(properties = …)` example. The project-specific key defined by `PropertyConstants` is documented in
section 3.7 together with its current (non-binding) status.

### 5.3 Verifying that callbacks fire

```java
// any existing Hibernate bootstrap works; the callback is picked up automatically
SessionFactory sessionFactory = new Configuration()
        .addAnnotatedClass(User.class)
        .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
        .setProperty("hibernate.connection.url", "jdbc:h2:mem:verify;DB_CLOSE_DELAY=-1")
        .setProperty("hibernate.hbm2ddl.auto", "update")
        .buildSessionFactory();

try (Session session = sessionFactory.openSession()) {
    Transaction tx = session.beginTransaction();
    session.persist(new User());          // -> onPersist, plus pre/post insert callbacks
    tx.commit();
}
sessionFactory.close();
```

### 5.4 Troubleshooting

| **Symptom** | **Cause and fix** |
|-------------|-------------------|
| No callbacks at all | `microsphere-hibernate-core` (or its `Integrator` service file) missing from the runtime classpath; or your `EntityCallback` file is not under `META-INF/services/` of a classpath entry; or the class name / package is misspelled. `EntittyCallbackIntegrator` is the exact (typo-containing) name. |
| `NoClassDefFoundError: io/microsphere/collection/ListUtils` or `io/microsphere/lang/Prioritized` | `microsphere-java-core` is optional in this module — add it explicitly. |
| `ClassNotFoundException: org.hibernate…` | `hibernate-core` is optional in this module — add it explicitly, within 6.2.x–7.3.x. |
| Duplicate callback invocations | Not expected: `EntityCallbackListenerDuplicationStrategy` keeps the original listener. If it happens, you probably registered `EntityCallbackListener` yourself or have two copies of the JAR with different contents. |
| `UnsupportedOperationException` / state array mutated downstream | You modified the `Object[] state` / `previousState` arrays; the contract says treat them as read-only. |
| Stack trace from one callback hides the others | `CompositeEntityCallback` iterates in order and does not swallow exceptions — guard your own callback bodies. |

## 6. Related Documentation

- [microsphere-hibernate-test.md](microsphere-hibernate-test.md) — how `AbstractHibernateH2Test` boots the
  `SessionFactory` used by `EntityCallbackIntegratorTest`
- [microsphere-hibernate-parent.md](microsphere-hibernate-parent.md) — where the `hibernate-core` version comes from
- [microsphere-hibernate-dependencies.md](microsphere-hibernate-dependencies.md) — the BOM
