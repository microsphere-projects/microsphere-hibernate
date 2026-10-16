# microsphere-hibernate-core (1.x)

[English](microsphere-hibernate-core.md) · [Chinese version](zh/microsphere-hibernate-core.md)

## 1. Module Positioning And Maven Coordinates

`microsphere-hibernate-core` is the runtime module of Microsphere Hibernate 1.x. It provides:

1. A **Java `ServiceLoader`-based entity lifecycle callback model** (`EntityCallback`) that is bridged into
   Hibernate ORM 5's event system by an `Integrator`, so you never write `EventListener` boilerplate.
2. **`DelegatingInterceptor`** — a null-safe decorator for `org.hibernate.Interceptor` (1.x only; it was removed
   from the 0.2.x line).
3. **`PropertyConstants`** — the `microsphere.hibernate.*` configuration-key namespace.

| **Item**      | **Value**                                                                  |
|---------------|----------------------------------------------------------------------------|
| GroupId       | `io.github.microsphere-projects`                                            |
| ArtifactId    | `microsphere-hibernate-core`                                                |
| Version       | `${revision}` → `0.1.11-SNAPSHOT` on `release-1.x`; latest release `0.1.10`  |
| Packaging     | `jar`                                                                       |
| Name          | `Microsphere :: Hibernate :: Core`                                          |
| POM file      | `microsphere-hibernate-core/pom.xml`                                        |
| Java packages | `io.microsphere.hibernate`, `io.microsphere.hibernate.entity`, `io.microsphere.hibernate.constants` |
| JavaDoc       | https://javadoc.io/doc/io.github.microsphere-projects/microsphere-hibernate-core |

> Every class in this module is documented with `@since 1.0.0`, which refers to the **1.x line**, not to a Maven
> version `1.0.0`. The published Maven versions on this line are `0.1.x`.

## 2. Version Information And Compatibility

| **Axis**           | **Value**                                                                                   |
|--------------------|---------------------------------------------------------------------------------------------|
| Version line       | `0.1.x`, branch `release-1.x`, latest release `0.1.10`                                       |
| JDK                | 8 – 25                                                                                       |
| Hibernate Core     | **5.0.x – 5.6.x** (`org.hibernate:hibernate-core`; default build `5.6.15.Final`)              |
| Spring Cloud       | Hoxton – 2021.0.x (`microsphere-spring-cloud` `0.1.26` line)                                  |
| Persistence API    | `javax.persistence`                                                                          |
| Incompatible with  | Hibernate ORM **6.x/7.x** — the API surface used here (`Interceptor` with `Serializable id` and `EntityMode`, the three-argument `Integrator#integrate`, `EventType` constants in `org.hibernate.event.spi`) does not exist there |

### Build/test dependency graph (from `microsphere-hibernate-core/pom.xml`)

| **Dependency** | **Scope** | **Notes** |
|----------------|-----------|-----------|
| `io.github.microsphere-projects:microsphere-annotation-processor` | `optional` | Generates configuration metadata for `@ConfigurationProperty` (used on `PropertyConstants`) |
| `io.github.microsphere-projects:microsphere-java-core` | `optional` | Supplies `ListUtils`, `Prioritized`, `DelegatingWrapper`, `SymbolConstants`, `io.microsphere.logging` |
| `org.hibernate:hibernate-core` | `optional` | Version comes from the active `hibernate-5.x` profile of the parent POM |
| `javax.xml.bind:jaxb-api` | `optional`, `java9+` profile (`[9,)`) | JAXB is no longer part of the JDK from Java 9, Hibernate 5 still needs it |
| `org.junit.jupiter:junit-jupiter`, `org.mockito:mockito-core`, `org.mockito:mockito-junit-jupiter` | `test` | Used by `DelegatingInterceptorTest`, `EntityCallbackListenerDuplicationStrategyTest`, `PropertyConstantsTest` |
| `io.github.microsphere-projects:microsphere-hibernate-test` | `test` (`${revision}`) | Used by `EntityCallbackIntegratorTest` |
| `com.h2database:h2`, `ch.qos.logback:logback-classic` | `test` | In-memory database and logging for the integrator test |

**Consequence of `optional`:** nothing is pulled in transitively. An application must declare
`org.hibernate:hibernate-core` and `io.github.microsphere-projects:microsphere-java-core` itself
(versions can come from the BOM — see
[microsphere-hibernate-dependencies.md](microsphere-hibernate-dependencies.md)).

## 3. How The Feature Works

```
Hibernate ORM 5 SessionFactory build
        │
        │  ServiceLoader discovers META-INF/services/org.hibernate.integrator.spi.Integrator
        ▼
EntittyCallbackIntegrator                      (shipped inside microsphere-hibernate-core.jar)
        │  adds EntityCallbackListenerDuplicationStrategy
        │  appends ONE EntityCallbackListener instance to 23 EventType constants
        ▼
EntityCallbackListener (package-private)       implements the 23 Hibernate *EventListener interfaces
        │  converts each Hibernate event into an EntityCallback call
        ▼
CompositeEntityCallback                        fans the call out to every discovered callback, in order
        ▲
        │  java.util.ServiceLoader.load(EntityCallback.class)
META-INF/services/io.microsphere.hibernate.entity.EntityCallback   <-- YOUR application registers itself here
```

Nothing needs to be wired into `Configuration`, `SessionFactoryBuilder`, Spring or `persistence.xml`: adding the
jar to the classpath activates the integration.

## 4. API Reference

### 4.1 `EntityCallback`

`io.microsphere.hibernate.entity.EntityCallback`

> The lifecycle callback interface for entity. The implementation class will be loaded by `ServiceLoader`.

- **Purpose:** a single, Hibernate-agnostic interface that receives every entity/collection lifecycle event of a
  `SessionFactory`.
- **Contract:** all **23** methods are `default` no-ops, so implementations override only what they need. The
  interface has no state requirement; instances are created by the JDK `ServiceLoader`, so they must expose a
  public no-arg constructor.
- **Discovery:** the name of the service file is fixed:
  `META-INF/services/io.microsphere.hibernate.entity.EntityCallback`.

#### 4.1.1 Method catalogue

| **Callback** | **Hibernate listener** | **`EventType`** | **Typical trigger** |
|---|---|---|---|
| `onLoad(Object id, String entityClassName, Object entity, LockMode lockMode, boolean isAssociationFetch, Object result, LoadType loadType)` | `LoadEventListener` | `LOAD` | `Session#get`, `Session#load`, `Session#find`, implicit association loads |
| `onPersist(Object entity, String entityName)` | `PersistEventListener` | `PERSIST` | `Session#persist` |
| `onDelete(Object entity, boolean cascadeDeleteEnabled, boolean orphanRemovalBeforeUpdates)` | `DeleteEventListener` | `DELETE` | `Session#delete` / `Session#remove` |
| `onMerge(Object original, Object id, Object entity, String entityName, Object result)` | `MergeEventListener` | `MERGE` | `Session#merge` |
| `onReplicate(Object entity, ReplicationMode replicationMode, String entityName)` | `ReplicateEventListener` | `REPLICATE` | `Session#replicate` |
| `onFlushDirty(Object entity, Object id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types)` | `FlushEntityEventListener` | `FLUSH_ENTITY` | `Session#flush` |
| `onEvict(Object entity)` | `EvictEventListener` | `EVICT` | `Session#evict` |
| `onLock(Object entity, LockMode lockMode, String entityName)` | `LockEventListener` | `LOCK` | `Session#lock` |
| `onRefresh(Object entity, String entityName, LockMode lockMode)` | `RefreshEventListener` | `REFRESH` | `Session#refresh` |
| `onPreLoad(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types)` | `PreLoadEventListener` | `PRE_LOAD` | `StatelessSession#get` |
| `onPostLoad(Object entity, Object id, String[] propertyNames, Type[] types)` | `PostLoadEventListener` | `POST_LOAD` | just after an entity instance is initialized |
| `onPreDelete(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types)` | `PreDeleteEventListener` | `PRE_DELETE` | `StatelessSession#delete` |
| `onPostDelete(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types)` | `PostDeleteEventListener` | `POST_DELETE` | after the delete hits the datastore |
| `onPreInsert(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes)` | `PreInsertEventListener` | `PRE_INSERT` | `StatelessSession#insert` |
| `onPostInsert(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes)` | `PostInsertEventListener` | `POST_INSERT` | after the insert |
| `onPreUpdate(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes)` | `PreUpdateEventListener` | `PRE_UPDATE` | `StatelessSession#update` |
| `onPostUpdate(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes)` | `PostUpdateEventListener` | `POST_UPDATE` | after the update |
| `onPreRecreateCollection(Object collection, Object key)` | `PreCollectionRecreateEventListener` | `PRE_COLLECTION_RECREATE` | collection first persisted |
| `onPostRecreateCollection(Object collection, Object key)` | `PostCollectionRecreateEventListener` | `POST_COLLECTION_RECREATE` | after recreate |
| `onPreCollectionRemove(Object collection, Object key)` | `PreCollectionRemoveEventListener` | `PRE_COLLECTION_REMOVE` | collection deleted |
| `onPostRemoveCollection(Object collection, Object key)` | `PostCollectionRemoveEventListener` | `POST_COLLECTION_REMOVE` | after remove |
| `onPreUpdateCollection(Object collection, Object key)` | `PreCollectionUpdateEventListener` | `PRE_COLLECTION_UPDATE` | collection updated |
| `onPostUpdateCollection(Object collection, Object key)` | `PostCollectionUpdateEventListener` | `POST_COLLECTION_UPDATE` | after update |

#### 4.1.2 Rules you must know before implementing

- **Read-only by design.** `onFlushDirty`, `onPreLoad`, `onPreInsert`, `onPreUpdate`, `onPreDelete` and the
  collection callbacks receive the state arrays of the persistence model. The Javadoc explicitly states that
  modifying `state`, `currentState` or `previousState` is not recommended.
- **You cannot veto an operation.** Hibernate's `Pre*EventListener` interfaces return `boolean`, where `true`
  aborts the operation. `EntityCallbackListener` always returns `false`, and `EntityCallback` methods return
  `void`. If you need vetoing, write your own `EventListener` or use `DelegatingInterceptor`
  ([§ 4.6](#46-delegatinginterceptor)).
- **Callbacks run synchronously** on the calling thread, inside the ongoing session operation and — for
  `*Post*`/`onFlushDirty` — inside the transaction. Do not block, do not open a second `Session` on the same
  connection, and do not mutate other entities expecting the change to be flushed.
- **Exceptions propagate.** The composite does not catch anything; an exception thrown from your callback
  surfaces as a `HibernateException` (or your own runtime exception) from the `Session` call and the operation
  fails. Wrap your body in `try/catch` when the callback is best-effort (auditing, metrics, tracing).
- **`onLoad` has no `readOnly` argument on this line.** In 0.2.x the signature gained a `Boolean readOnly`
  parameter. Any code written against 1.x needs a signature change when you upgrade — see the migration note in
  the [guide index](README.md#differences-from-the-02x-current-line).
- **Ordering.** Callbacks are sorted with `io.microsphere.lang.Prioritized.COMPARATOR` before being composed.
  Implement `io.microsphere.lang.Prioritized` to control the order: `getPriority()` returns an `int` where
  smaller means earlier (`MAX_PRIORITY = Integer.MIN_VALUE`, `NORMAL_PRIORITY = 0`,
  `MIN_PRIORITY = Integer.MAX_VALUE`).
- **Instances are created once**, when the `SessionFactory` is built (the listener resolves
  `ServiceLoader.load(EntityCallback.class)` in its constructor). Rebuilding the `SessionFactory` is required to
  pick up newly added callbacks.

### 4.2 `CompositeEntityCallback`

`io.microsphere.hibernate.entity.CompositeEntityCallback`

| **Member** | **Signature** | **Behavior** |
|---|---|---|
| constructor | `CompositeEntityCallback(List<EntityCallback> callbacks)` | Stores the list as-is; no defensive copy, no `null` check. Passing `null` results in a `NullPointerException` on the first callback invocation. |
| all 23 callbacks | `@Override` on each `EntityCallback` method | `forEach(this.callbacks, callback -> callback.<sameMethod>(…))` — invokes every callback in list order with the unchanged arguments. |

- **Purpose:** turns N callbacks into one `EntityCallback`, so the rest of the machinery only ever talks to a
  single object (classic *Composite*).
- **Contract:** iteration is sequential and stops on the first exception (`ListUtils.forEach` does not catch).
  An empty list is a valid no-op composite.
- **Used internally** by `EntityCallbackListener`; it is public so applications can compose callbacks manually —
  which is exactly what the 1.x `DelegatingInterceptor` recipes below reuse.

### 4.3 `EntityCallbackListener` (package-private)

`io.microsphere.hibernate.entity.EntityCallbackListener` — **not accessible outside
`io.microsphere.hibernate.entity`**.

| **Aspect** | **Detail** |
|---|---|
| Implemented interfaces | 23 Hibernate listener interfaces: `Load`, `Persist`, `Merge`, `Delete`, `Replicate`, `FlushEntity`, `Evict`, `Lock`, `Refresh`, `PreLoad`, `PostLoad`, `PreDelete`, `PostDelete`, `PreUpdate`, `PostUpdate`, `PreInsert`, `PostInsert`, `Pre/PostCollectionRecreate`, `Pre/PostCollectionRemove`, `Pre/PostCollectionUpdate` |
| `EntityCallbackListener()` | Loads callbacks via `ServiceLoader.load(EntityCallback.class)` |
| `EntityCallbackListener(Iterable<EntityCallback> callbacks)` | Copies into a `LinkedList`, sorts with `Prioritized.COMPARATOR`, wraps in `CompositeEntityCallback` |
| Event → callback mapping | Straight field extraction, e.g. `onFlushDirty` reads `event.getEntityEntry().getId()` / `getLoadedState()` and `persister.getPropertyNames()` / `getPropertyTypes()`; collection callbacks use `event.getCollection()` and `collection.getKey()` |
| Vetoing | `onPreDelete`, `onPreInsert`, `onPreUpdate` return `false` |
| Post-commit | `requiresPostCommitHanding(EntityPersister)` and `requiresPostCommitHandling(EntityPersister)` both return `false`, so Hibernate never defers the callback to after commit. Both spellings are implemented so the class satisfies whichever name the target Hibernate 5.x minor declares. |
| `getLockMode(LockOptions)` | `protected` extraction hook (`lockOptions.getLockMode()`); the only extension point of the class, but subclassing is impossible for external code because the class is package-private |
| Overloads with `Map`/`Set` | `onDelete(DeleteEvent)`, `onPersist(PersistEvent)`, `onMerge(MergeEvent)`, `onRefresh(RefreshEvent)` delegate to their `Map`/`Set`-carrying overloads so cascade bookkeeping arguments are preserved |

### 4.4 `EntittyCallbackIntegrator`

`io.microsphere.hibernate.entity.EntittyCallbackIntegrator`

> Hibernate `Integrator` for `EntityCallback`

- **Name note:** the class name really is `EntittyCallbackIntegrator` (an extra `t`). It appears verbatim in the
  shipped SPI file, in stack traces and when you register it manually, so do not "correct" it.
- **`integrate(Metadata, SessionFactoryImplementor, SessionFactoryServiceRegistry)`** (the Hibernate 5.x
  three-argument signature):
  1. resolves `EventListenerRegistry` from the `SessionFactoryServiceRegistry`;
  2. `eventListenerRegistry.addDuplicationStrategy(new EntityCallbackListenerDuplicationStrategy())`;
  3. creates **one** `EntityCallbackListener` and `appendListeners(...)` it to 23 `EventType` constants
     (`LOAD`, `PERSIST`, `MERGE`, `DELETE`, `REPLICATE`, `FLUSH_ENTITY`, `EVICT`, `LOCK`, `REFRESH`,
     `PRE_LOAD`, `POST_LOAD`, `PRE_DELETE`, `POST_DELETE`, `PRE_UPDATE`, `POST_UPDATE`, `PRE_INSERT`,
     `POST_INSERT`, `PRE_COLLECTION_RECREATE`, `POST_COLLECTION_RECREATE`, `PRE_COLLECTION_REMOVE`,
     `POST_COLLECTION_REMOVE`, `PRE_COLLECTION_UPDATE`, `POST_COLLECTION_UPDATE`).
  Appending (rather than setting) means the callbacks run *after* Hibernate's own default listeners, which is
  why `*Post*` events observe already-written state.
- **`disintegrate(SessionFactoryImplementor, SessionFactoryServiceRegistry)`** is a no-op — nothing to unwind.
- **Activation is automatic** through the SPI file described in [§ 5](#5-integration-spi-registration-and-config-keys).

### 4.5 `EntityCallbackListenerDuplicationStrategy` (package-private)

`io.microsphere.hibernate.entity.EntityCallbackListenerDuplicationStrategy`

| **Member** | **Behavior** |
|---|---|
| `areMatch(Object listener, Object original)` | `true` only when both objects have the same runtime class **and** that class is exactly `EntityCallbackListener` |
| `getAction()` | `DuplicationStrategy.Action.KEEP_ORIGINAL` |

- **Purpose:** if two `Integrator` registrations (e.g. two classpath entries, or a manual registration on top of
  the automatic one) would add the same kind of listener, Hibernate asks this strategy what to do.
- **Consequence:** the **first** registered `EntityCallbackListener` is kept and the second is dropped, so a
  duplicate registration cannot cause double callbacks — but it also cannot add a second set of
  `EntityCallback`s. If you need extra callbacks after startup, register them through
  `DelegatingInterceptor` instead.

### 4.6 `DelegatingInterceptor`

`io.microsphere.hibernate.DelegatingInterceptor` — **this class exists only on the 1.x line** and was removed in
0.2.x, because its method signatures are Hibernate 5-specific.

```java
public class DelegatingInterceptor implements Interceptor, DelegatingWrapper {

    protected final Interceptor delegate;

    public DelegatingInterceptor(@Nullable Interceptor delegate) {
        this.delegate = delegate == null ? INSTANCE : delegate;   // org.hibernate.EmptyInterceptor.INSTANCE
    }

    @Override
    public Interceptor getDelegate() {
        return delegate;
    }
    // ... 18 forwarding methods
}
```

#### 4.6.1 The delegation mechanism

- **Null-safe wrapping.** A `null` delegate becomes `org.hibernate.EmptyInterceptor.INSTANCE`, so every
  forwarded call is a harmless no-op instead of a `NullPointerException`. This makes the class usable as a
  "starting point" interceptor when you have no delegate yet.
- **Verbatim forwarding.** Every `Interceptor` method passes all arguments unchanged and returns the delegate's
  result unchanged — the decorator never swallows a return value. That matters for `boolean`-returning
  callbacks (`onLoad`, `onFlushDirty`, `onSave`, where `true` means "the state was modified"), for
  `int[] findDirty(...)`, for `Object instantiate(...)`, `String getEntityName(...)`,
  `Object getEntity(...)` and `Boolean isTransient(...)`.
- **`protected final Interceptor delegate`** is the extension point: subclasses can read it directly, and
  `getDelegate()` exposes it publicly. `DelegatingWrapper#getDelegate()` (declared to return `Object` in
  `io.microsphere.lang.DelegatingWrapper`) is narrowed here to `Interceptor`, and the inherited
  `unwrap(Class<T>)` / `isWrapperFor(Class<?>)` defaults from `io.microsphere.lang.Wrapper` let you walk or test
  a stack of wrapped interceptors.
- **Forwarded methods** (all `@Override`): `onLoad`, `onFlushDirty`, `onSave`, `onDelete`,
  `onCollectionRecreate`, `onCollectionRemove`, `onCollectionUpdate`, `preFlush`, `postFlush`, `isTransient`,
  `findDirty`, `instantiate`, `getEntityName`, `getEntity`, `afterTransactionBegin`,
  `beforeTransactionCompletion`, `afterTransactionCompletion`, `onPrepareStatement`.
  `onPrepareStatement(String)` is additionally annotated `@Deprecated` because Hibernate deprecates SQL
  rewriting through interceptors; prefer a `StatementInspector`.
- **Hibernate 5 signatures (the reason it is 1.x-only):** `Serializable id` in `onLoad`/`onSave`/`onDelete`/…,
  `EntityMode` in `instantiate`, the raw `Iterator` in `preFlush`/`postFlush`. Hibernate 6 changed the ids to
  `Object`, dropped `EntityMode` and generified the iterators, so the same source cannot compile there — which
  is precisely why 0.2.x removed the class.

#### 4.6.2 Registering the interceptor

`DelegatingInterceptor` is *not* wired by the integrator; it is opt-in per `Session` or per `SessionFactory`.
On Hibernate 5.x, `SessionFactory#openSession(Interceptor)` is **not** available — use one of these two:

```java
// (a) per Session — Hibernate 5.0 .. 5.6
Interceptor interceptor = new DelegatingInterceptor(thirdPartyInterceptor);
Session session = sessionFactory.withOptions().interceptor(interceptor).openSession();

// (b) for every Session created by this SessionFactory
Configuration configuration = new Configuration();
configuration.setInterceptor(interceptor);
SessionFactory sessionFactory = configuration.buildSessionFactory();
```

#### 4.6.3 Using it to register custom callbacks

`EntityCallback` instances discovered through the integrator apply to the whole `SessionFactory`. When you need
callbacks that apply to a *single session* — or that are constructed with dependencies you cannot provide
through a no-arg `ServiceLoader` constructor — dispatch them from an interceptor that delegates to a
`CompositeEntityCallback`:

```java
package com.acme.hibernate;

import io.microsphere.hibernate.DelegatingInterceptor;
import io.microsphere.hibernate.entity.CompositeEntityCallback;
import io.microsphere.hibernate.entity.EntityCallback;
import org.hibernate.Interceptor;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.util.Arrays;

/** Bridges Hibernate 5 Interceptor calls into EntityCallback calls for one Session. */
public class EntityCallbackInterceptor extends DelegatingInterceptor {

    private final EntityCallback callback;

    public EntityCallbackInterceptor(Interceptor delegate, EntityCallback... callbacks) {
        super(delegate);                                  // null delegate -> EmptyInterceptor.INSTANCE
        this.callback = new CompositeEntityCallback(Arrays.asList(callbacks));
    }

    @Override
    public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        callback.onPreLoad(entity, id, state, propertyNames, types);
        return super.onLoad(entity, id, state, propertyNames, types);   // keep the delegate's verdict
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        callback.onPreInsert(entity, id, state, propertyNames, types);
        return super.onSave(entity, id, state, propertyNames, types);
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
                                String[] propertyNames, Type[] types) {
        callback.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
        return super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
    }

    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        callback.onPreDelete(entity, id, state, propertyNames, types);
        super.onDelete(entity, id, state, propertyNames, types);
    }
}
```

```java
EntityCallback tenantAudit = new TenantAuditCallback(tenantContext);   // built with DI, no ServiceLoader
Interceptor interceptor = new EntityCallbackInterceptor(null, tenantAudit);

try (Session session = sessionFactory.withOptions().interceptor(interceptor).openSession()) {
    session.persist(order);   // -> onPreInsert on tenantAudit, then the (empty) delegate
}
```

Mapping choices worth noting: `Interceptor#onLoad` ↔ `EntityCallback#onPreLoad`,
`Interceptor#onSave` ↔ `EntityCallback#onPreInsert`, `Interceptor#onFlushDirty` ↔
`EntityCallback#onFlushDirty` (identical argument list), `Interceptor#onDelete` ↔ `EntityCallback#onPreDelete`,
and `Interceptor#onCollectionRecreate/Remove/Update` ↔ `EntityCallback#onPreRecreateCollection`,
`onPreCollectionRemove`, `onPreUpdateCollection`. `Interceptor` has no counterpart for the `*Post*` events, for
`onPersist`, `onMerge`, `onReplicate`, `onEvict`, `onLock` or `onRefresh` — those only arrive through the
integrator path.

Two more recipes:

- **Adopt an existing interceptor without changing it.** Wrap whatever a library handed you
  (`new DelegatingInterceptor(libraryInterceptor)`) and override only the events you care about; always return
  `super.<event>(...)` so the wrapped object keeps its veto/decision power.
- **Layered decoration.** Because `delegate` may itself be a `DelegatingInterceptor`, you can build a chain and
  later inspect it with `getDelegate()` or `unwrap(SomeInterceptor.class)`.

### 4.7 `PropertyConstants`

`io.microsphere.hibernate.constants.PropertyConstants` — a constant interface, no methods.

| **Constant** | **Value** | **Notes** |
|---|---|---|
| `MICROSPHERE_HIBERNATE_PROPERTY_NAME_PREFIX` | `"microsphere.hibernate."` | `microsphere.` (from `io.microsphere.constants.PropertyConstants.MICROSPHERE_PROPERTY_NAME_PREFIX`) + `hibernate` + `.` |
| `MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_NAME` | `"microsphere.hibernate.enabled"` | Annotated `@ConfigurationProperty(type = boolean.class, defaultValue = "true", source = SYSTEM_PROPERTIES_SOURCE)` |
| `DEFAULT_MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_VALUE` | `"true"` | String default |
| `DEFAULT_MICROSPHERE_HIBERNATE_ENABLED` | `true` | `Boolean.parseBoolean` of the value above |

```java
// Enable Microsphere Hibernate via system property
System.setProperty(MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_NAME, "true");

// Read the enabled flag
boolean enabled = Boolean.parseBoolean(
        System.getProperty(MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_NAME,
                           DEFAULT_MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_VALUE));

// Use the property name prefix to build custom property names
String customProperty = MICROSPHERE_HIBERNATE_PROPERTY_NAME_PREFIX + "myFeature.enabled";
```

**Important honesty note:** in `0.1.x`, no production class in this module reads
`microsphere.hibernate.enabled`. Activation of the entity-callback integration is purely *classpath presence*
(the shipped `Integrator` service file). The constant is the documented namespace and metadata anchor for your
own gating logic and for tooling — the `microsphere-annotation-processor` optional dependency turns the
`@ConfigurationProperty` declaration into generated configuration metadata, which is why it is published even
though the framework does not consult it yet. Use it to guard your own callbacks:

```java
public class AuditEntityCallback implements EntityCallback {

    private static final boolean enabled = Boolean.parseBoolean(
            System.getProperty(MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_NAME,
                               DEFAULT_MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_VALUE));

    @Override
    public void onPersist(Object entity, String entityName) {
        if (!enabled) {
            return;
        }
        // ... audit
    }
}
```

## 5. Integration: SPI Registration And Config Keys

### 5.1 The service file that turns the feature on

`microsphere-hibernate-core` ships this resource; you never create it yourself:

```
# microsphere-hibernate-core/src/main/resources/META-INF/services/org.hibernate.integrator.spi.Integrator
io.microsphere.hibernate.entity.EntittyCallbackIntegrator
```

### 5.2 Registering your own callbacks

Create one line-separated file in **your** artifact:

```
# src/main/resources/META-INF/services/io.microsphere.hibernate.entity.EntityCallback
com.acme.order.logging.OrderEntityCallback
com.acme.order.audit.AuditEntityCallback
```

Requirements: public class, public no-arg constructor, on the classpath used to build the `SessionFactory`
(the same `ClassLoader` Hibernate uses), and no ordering assumption other than `Prioritized`.

### 5.3 Spring Boot / Spring Cloud notes (1.x line)

The integration is container-agnostic — Spring Boot 2.3/2.4/2.6 (the Spring Cloud Hoxton…2021.0.x lines managed
by `microsphere-spring-cloud:0.1.26`) needs no extra bean, because Hibernate discovers the `Integrator` via
`ServiceLoader`. Keep `microsphere-hibernate-core` in `compile` scope; if you use JPA
(`spring-boot-starter-data-jpa`), the Hibernate `SessionFactory` built by Boot still runs integrator discovery,
so callbacks fire for JPA-managed entities as well.

## 6. Worked Example: The Module's Own Tests

### 6.1 `EntityCallbackIntegratorTest` — the end-to-end proof

```java
package io.microsphere.hibernate.entity;

import io.microsphere.hibernate.test.AbstractHibernateH2Test;

/**
 * {@link EntittyCallbackIntegrator} Test
 */
class EntityCallbackIntegratorTest extends AbstractHibernateH2Test {

}
```

Source: `microsphere-hibernate-core/src/test/java/io/microsphere/hibernate/entity/EntityCallbackIntegratorTest.java`.

The class body is intentionally empty. Extending `AbstractHibernateH2Test` (from
`microsphere-hibernate-test`, declared in `test` scope in this module's POM) inherits its two `@Nested` test
classes — `SessionTest` and `StatelessSessionTest`, fourteen test methods in total — which run `persist`, `get`,
`load`, `merge`, `delete`, `replicate`, `flush`, `evict`, `lock`, `refresh` and the whole `StatelessSession`
suite against in-memory H2. Because the integrator is discovered from the jar's SPI
file and `TestEntityCallback` is registered from the test SPI file, those inherited tests only pass when the
`EntityCallback` wiring works for every event type:

```
# microsphere-hibernate-core/src/test/resources/META-INF/services/io.microsphere.hibernate.entity.EntityCallback
io.microsphere.hibernate.entity.TestEntityCallback
```

### 6.2 `TestEntityCallback` — a callback that logs everything

```java
public class TestEntityCallback implements EntityCallback {

    private static final Logger logger = getLogger(TestEntityCallback.class);

    @Override
    public void onLoad(Object id, String entityClassName, Object entity, LockMode lockMode, boolean isAssociationFetch,
                       Object result, LoadEventListener.LoadType loadType) {
        logger.trace("onLoad - id : {}, entityClassName : {}, entity : {}, lockMode : {}, isAssociationFetch : {}, result : {}, loadType : {}",
                id, entityClassName, entity, lockMode, isAssociationFetch, result, loadType);
        EntityCallback.super.onLoad(id, entityClassName, entity, lockMode, isAssociationFetch, result, loadType);
    }

    @Override
    public void onPersist(Object entity, String entityName) {
        logger.trace("onPersist - entity : {}, entityName : {}", entity, entityName);
        EntityCallback.super.onPersist(entity, entityName);
    }

    @Override
    public void onFlushDirty(Object entity, Object id, Object[] currentState, Object[] previousState,
                             String[] propertyNames, Type[] types) {
        logger.trace("onFlushDirty - entity : {}, id : {}, currentState : {}, previousState : {}, propertyNames : {}, types : {}",
                entity, id, currentState, previousState, propertyNames, types);
        EntityCallback.super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
    }
    // … all 23 methods, same pattern
}
```

Source: `microsphere-hibernate-core/src/test/java/io/microsphere/hibernate/entity/TestEntityCallback.java`. The
`logger.trace(…, args)` varargs form comes from `io.microsphere.logging` (part of the optional
`microsphere-java-core` dependency), and calling `EntityCallback.super.<method>(…)` is the documented way to keep
the default behavior when you override.

Run it with logging visible:

```bash
./mvnw -Phibernate-5.6 -Drevision=0.1.11-SNAPSHOT \
       -Dorg.slf4j.simpleLogger.defaultLogLevel=trace \
       test -Dtest=EntityCallbackIntegratorTest
```

### 6.3 `DelegatingInterceptorTest` — the delegation contract, test by test

```java
@ExtendWith(MockitoExtension.class)
class DelegatingInterceptorTest {

    @Mock
    private Interceptor mockDelegate;

    private DelegatingInterceptor delegatingInterceptor;

    @BeforeEach
    void setUp() {
        delegatingInterceptor = new DelegatingInterceptor(mockDelegate);
    }

    @Test
    void testConstructorWithNullDelegate() {
        DelegatingInterceptor interceptor = new DelegatingInterceptor(null);
        assertNotNull(interceptor);
        assertSame(INSTANCE, interceptor.getDelegate());          // EmptyInterceptor.INSTANCE
    }

    @Test
    void testOnLoad() throws CallbackException {
        Object entity = new Object();
        Serializable id = 1L;
        Object[] state = new Object[0];
        String[] names = new String[0];
        Type[] types = new Type[0];

        when(mockDelegate.onLoad(eq(entity), eq(id), eq(state), eq(names), eq(types)))
                .thenReturn(true);

        assertTrue(delegatingInterceptor.onLoad(entity, id, state, names, types));
        verify(mockDelegate).onLoad(entity, id, state, names, types);
    }

    @Test
    void testTransactionCallbacks() {
        Transaction tx = mock(Transaction.class);

        delegatingInterceptor.afterTransactionBegin(tx);
        delegatingInterceptor.beforeTransactionCompletion(tx);
        delegatingInterceptor.afterTransactionCompletion(tx);

        verify(mockDelegate).afterTransactionBegin(tx);
        verify(mockDelegate).beforeTransactionCompletion(tx);
        verify(mockDelegate).afterTransactionCompletion(tx);
    }

    @Test
    void testGetDelegate() {
        assertSame(mockDelegate, delegatingInterceptor.getDelegate());
    }
}
```

Source: `microsphere-hibernate-core/src/test/java/io/microsphere/hibernate/DelegatingInterceptorTest.java`.
The remaining tests (`testOnFlushDirty`, `testOnSave`, `testOnDelete`, `testCollectionOperations`,
`testPreAndPostFlush`, `testIsTransient`, `testFindDirty`, `testInstantiate`, `testGetEntityName`,
`testGetEntity`, `testOnPrepareStatement`) each assert the same rule for one method: **arguments forwarded
unchanged, delegate's return value returned verbatim.** Read them as the executable specification of the class —
including the fact that `onSave` returning `false` from the delegate must stay `false` (see `testOnSave`).

Note the `Serializable id = 1L;` and `EntityMode.POJO` arguments in `testOnLoad` / `testInstantiate`: they are
the Hibernate 5 signatures that make this class 1.x-only.

### 6.4 `EntityCallbackListenerDuplicationStrategyTest`

```java
@Test
void testAreMatch() {
    EntityCallbackListener listener = new EntityCallbackListener();
    assertTrue(strategy.areMatch(listener, listener));

    assertFalse(strategy.areMatch(listener, new DefaultLoadEventListener()));
    assertFalse(strategy.areMatch(new DefaultLoadEventListener(), new DefaultLoadEventListener()));
}

@Test
void testGetAction() {
    assertSame(KEEP_ORIGINAL, strategy.getAction());
}
```

Source: `microsphere-hibernate-core/src/test/java/io/microsphere/hibernate/entity/EntityCallbackListenerDuplicationStrategyTest.java`.
Because the class under test and `EntityCallbackListener` are package-private, the test must live in
`io.microsphere.hibernate.entity` — the same trick applies if you want to unit-test them in your own repository.

### 6.5 `PropertyConstantsTest`

```java
@Test
void test() {
    assertEquals("microsphere.hibernate.", MICROSPHERE_HIBERNATE_PROPERTY_NAME_PREFIX);
    assertEquals("true", DEFAULT_MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_VALUE);
    assertTrue(DEFAULT_MICROSPHERE_HIBERNATE_ENABLED);
    assertEquals("microsphere.hibernate.enabled", MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_NAME);
}
```

Source: `microsphere-hibernate-core/src/test/java/io/microsphere/hibernate/constants/PropertyConstantsTest.java`.
These four assertions are the authoritative statement of the key names — copy them into your own build if you
depend on the namespace.

## 7. Testing Your Own Callbacks

Put `microsphere-hibernate-test` in `test` scope (see
[microsphere-hibernate-test.md](microsphere-hibernate-test.md)) and either:

1. register your callback in `src/test/resources/META-INF/services/io.microsphere.hibernate.entity.EntityCallback`,
   extend `AbstractHibernateH2Test` and let the inherited H2 exercises fire your callbacks (the pattern used by
   `EntityCallbackIntegratorTest`), or
2. use `@HibernateTest` + `@HibernateRuntime` for a focused test that verifies one callback:

```java
@HibernateTest(
        properties = {
                "hibernate.connection.driver_class=org.h2.Driver",
                "hibernate.connection.url=jdbc:h2:mem:callback_test_db;DB_CLOSE_DELAY=-1",
                "hibernate.connection.username=sa",
                "hibernate.connection.password=",
                "hibernate.dialect=org.hibernate.dialect.H2Dialect",
                "hibernate.hbm2ddl.auto=update"
        },
        scanEntityPackages = {"io.microsphere.hibernate.test.entity"}
)
class PersistCallbackTest {

    @HibernateRuntime
    private Session session;

    @Test
    void callbackShouldSeePersist() {
        org.hibernate.Transaction tx = session.beginTransaction();
        session.persist(new Order());
        tx.commit();
        assertTrue(CollectingCallback.persistedNames.contains("io.microsphere.hibernate.test.entity.Order"));
    }
}
```

For a Mockito-style unit test of a callback, no Hibernate runtime is needed at all — `EntityCallback` is a plain
interface and `CompositeEntityCallback` can be driven directly:

```java
EntityCallback callback = mock(EntityCallback.class);
CompositeEntityCallback composite = new CompositeEntityCallback(Collections.singletonList(callback));

composite.onPersist(entity, "Order");

verify(callback).onPersist(entity, "Order");
```

## 8. Troubleshooting

| **Symptom** | **Cause / fix** |
|---|---|
| Callbacks never fire | The `EntityCallback` service file is missing or misnamed. The file name is exactly `io.microsphere.hibernate.entity.EntityCallback` and the content is the fully qualified class name. Also confirm `microsphere-hibernate-core` is on the **runtime** classpath (not `test` scope) and that `microsphere-java-core` is present. |
| `NoClassDefFoundError: javax/xml/bind/...` on Java 9+ | You are running Hibernate 5 without the JAXB fallback. Upstream this is handled by the `java9+` profile (`javax.xml.bind:jaxb-api`); in your application add the dependency (or run on JDK 8). |
| `NoSuchMethodError` / `AbstractMethodError` mentioning `Interceptor`, `Integrator` or `EntityMode` | Hibernate 6+ is on the classpath. This line supports 5.0.x–5.6.x only; switch to `org.hibernate:hibernate-core` in range and use `0.1.x` artifacts consistently. |
| Duplicate callbacks | `EntityCallbackListenerDuplicationStrategy` returns `KEEP_ORIGINAL`, so duplicates from the integrator are suppressed. Real duplicates come from registering the same class twice in the service file, or from your own `EventListener` doing the same work. |
| Missing callbacks after a second registration | By the same strategy, the *first* `EntityCallbackListener` wins. If you add more callbacks later in the lifecycle, they are ignored — rebuild the `SessionFactory` or dispatch via `DelegatingInterceptor`. |
| Your exception aborts business code | Expected: callbacks run inside the session operation and exceptions propagate. Catch inside the callback if it must be best-effort. |
| `state` array edits have no effect (or corrupt the flush) | Contract: never mutate the state arrays. Use `DelegatingInterceptor` if you truly need to change values (that is the `Interceptor` API's supported use). |
| A specific Hibernate 5.x minor behaves differently from the default build | The whole 5.0 – 5.6 range is covered by CI (JDK 8 × seven `hibernate-5.x` profiles × three Spring Cloud profiles), so a difference is a bug: file an issue with the profile (`-Phibernate-5.0` … `-Phibernate-5.6`), the pinned `hibernate-core` version, the JDK and the stack trace. |

## 9. Related Documents

- [README.md](README.md) — 1.x user guide index and the 0.2.x migration table
- [microsphere-hibernate-test.md](microsphere-hibernate-test.md) — the H2 harness used by the tests above
- [microsphere-hibernate-dependencies.md](microsphere-hibernate-dependencies.md) — version management
- [microsphere-hibernate-parent.md](microsphere-hibernate-parent.md) — the `hibernate-5.x` profiles
- Source (branch `release-1.x`): `microsphere-hibernate-core/src/main/java/io/microsphere/hibernate/**`,
  `microsphere-hibernate-core/src/main/resources/META-INF/services/org.hibernate.integrator.spi.Integrator`,
  `microsphere-hibernate-core/src/test/java/io/microsphere/hibernate/**`,
  `microsphere-hibernate-core/src/test/resources/META-INF/services/io.microsphere.hibernate.entity.EntityCallback`
