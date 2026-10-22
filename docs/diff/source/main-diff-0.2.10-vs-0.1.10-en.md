# src/main Source Diff: `0.2.10` vs `0.1.10`

> 中文版: [main-diff-0.2.10-vs-0.1.10.md](main-diff-0.2.10-vs-0.1.10.md) ·
> src/test comparison: [test-diff-0.2.10-vs-0.1.10-en.md](test-diff-0.2.10-vs-0.1.10-en.md) ·
> [Docs home](../../README.md)

## 1. Scope and rules

| Item | `0.2.10` | `0.1.10` |
|------|----------|----------|
| Maven version (`revision`) | `0.2.10-SNAPSHOT` | `0.1.10-SNAPSHOT` |
| Version line | current (0.2.x) | 1.x (0.1.x) |
| JDK baseline | 17 | 8 |
| Hibernate range | 6.2.x – 7.3.x (`org.hibernate.orm`) | 5.0.x – 5.6.x (`org.hibernate`) |
| `microsphere-spring-cloud` | `0.2.26` | `0.1.26` |

Comparison rules:

- Compared **per Maven module**, matching **same-named modules** only; both tags declare the identical module list,
  so no module had to be dropped.
- `src/main` and `src/test` are compared **separately** (this file covers `src/main` only).
- **Excluded** difference classes (see section 4): Java 8 vs Java 17+ syntax and API differences, and Java EE
  (`javax.*`) vs Jakarta EE (`jakarta.*`) differences.
- `src/main/resources` is byte-identical across the two tags (`META-INF/services/org.hibernate.integrator.spi.Integrator`
  unchanged).

## 2. Module overview

| Maven module | `0.1.10` Java / resource files | `0.2.10` Java / resource files | Matched | Summary |
|--------------|--------------------------------|--------------------------------|---------|---------|
| microsphere-hibernate-parent | 0 / 0 (POM-only) | 0 / 0 (POM-only) | ✅ | `pom.xml` only, see section 5 |
| microsphere-hibernate-dependencies | 0 / 0 (BOM) | 0 / 0 (BOM) | ✅ | no source differences |
| microsphere-hibernate-core | 7 / 1 | 6 / 1 | ✅ | 1 class deleted, 4 modified, 2 unchanged; resource unchanged |
| microsphere-hibernate-test | 9 / 0 | 9 / 0 | ✅ | 7 classes modified, 2 unchanged |
| microsphere-hibernate (root aggregator POM) | 0 / 0 | 0 / 0 | ✅ | `pom.xml` only |

## 3. File-level differences

### 3.1 microsphere-hibernate-core / src/main

Package root: `io.microsphere.hibernate`

| File | Change | Difference | Category | Why |
|------|--------|------------|----------|-----|
| `DelegatingInterceptor.java` | **Deleted** (165 lines) | Decorator implementing Hibernate `Interceptor` plus `DelegatingWrapper`, hand-delegating every method (`onLoad`/`onSave`/`findDirty`/`instantiate`/transaction callbacks); the constructor falls back to `EmptyInterceptor.INSTANCE` on `null` | Capability removal + API compatibility | From Hibernate 6, `Interceptor` is an **interface with default methods** rather than an abstract class, `EntityMode` was removed, and identifier parameters were widened from `Serializable` to `Object`. Every override signature in this class ceases to exist on the new line, and the hand-written delegation boilerplate loses its purpose. The API is therefore not offered on 0.2.x; `DelegatingInterceptor` is a **1.x-only** capability. |
| `entity/EntityCallback.java` | Modified | ① `onLoad(...)` gains a `Boolean readOnly` parameter between `result` and `loadType`; ② javadoc `@see StatelessSession#get(String/Class, Serializable)` → `#get(String/Class, Object)`; ③ unused `java.io.Serializable` import removed | API compatibility + functional need | ① Hibernate 6 `LoadEvent` exposes `getReadOnly()`, so the callback contract must propagate the read-only loading intent to user code; ② Hibernate 6 widened entity identifier parameters from `Serializable` to `Object`, and `@see` targets must follow the real signature or the link no longer resolves. |
| `entity/CompositeEntityCallback.java` | Modified | The composite `onLoad` implementation gains `readOnly` and forwards it to each delegate; trailing blank line tidied | API compatibility | A Composite implementation must track the `EntityCallback` signature exactly, otherwise the new argument is silently dropped on the composite path. |
| `entity/EntittyCallbackIntegrator.java` | Modified | Adds an `integrate(Metadata, BootstrapContext, SessionFactoryImplementor)` override that adapts to the pre-existing three-argument `integrate(...)` via `sessionFactory.getServiceRegistry()`; javadoc added to `disintegrate(...)` documenting it as a no-op (cleanup handled by session factory shutdown) | API compatibility | Hibernate 6 made `BootstrapContext` part of the canonical `Integrator#integrate` method and Hibernate 7 dropped the form without it. The added entry point satisfies the new SPI abstract method while reusing the existing listener-registration logic instead of duplicating it. |
| `entity/EntityCallbackListener.java` | Modified | ① Cascade-context parameters replace raw collections with dedicated types: `onPersist(…, Map)` → `PersistContext`, `onMerge(…, Map)` → `MergeContext`, `onRefresh(…, Map)` → `RefreshContext`, `onDelete(…, Set)` → `DeleteContext`; ② `onLoad` propagates `event.getReadOnly()`; ③ six collection callbacks use `PersistentCollection<?>` instead of raw `PersistentCollection`; ④ the misspelled `requiresPostCommitHanding(EntityPersister)` override is deleted, keeping only `requiresPostCommitHandling(...)`; ⑤ unused `java.util.Map`/`Set` imports removed | API compatibility | ① Hibernate 6.2+ replaced the raw cascade-deduplication collections with type-safe context objects; ② required by the new `EntityCallback` parameter; ③ `PersistentCollection` became generic in Hibernate 6, so raw usage no longer compiles; ④ Hibernate 6 removed the misspelled default method, leaving the extra override without an interface target. |
| `constants/PropertyConstants.java` | Unchanged | — | — | Pure constants, independent of Hibernate API shape. |
| `entity/EntityCallbackListenerDuplicationStrategy.java` | Unchanged | — | — | The `DuplicationStrategy` contract is stable across 5.x/6.x/7.x. |

### 3.2 microsphere-hibernate-test / src/main

Package root: `io.microsphere.hibernate.test`

| File | Change | Difference | Category | Why |
|------|--------|------------|----------|-----|
| `AbstractHibernateH2Test.java` | Modified | ① `session.delete(x)` → `session.remove(x)`, and `Test : persist & delete` / `testPersistAndDelete` → `persist & remove` / `testPersistAndRemove`; ② `session.load(User.class, id)` → `session.find(User.class, id)`; ③ `createQuery("FROM io.microsphere…User").list()` → `createQuery("FROM User", User.class).getResultList()`; ④ explicit `(User)` casts on `session.merge(...)` and `statelessSession.get(...)` removed; ⑤ new tests `testPersistAndDetach` (`Session.detach`) and `testInsertAndFetch` (`StatelessSession.fetch`) | API compatibility + functional need | ① `Session#delete` was deprecated in Hibernate 6 and removed in 7.0 in favour of JPA-style `remove` — since 0.2.x targets 6.2–7.3, only `remove` compiles everywhere; ② `find` is the JPA-standard lookup (returns `null` when absent) whereas `load` in 6+ means reference loading with assumed existence; ③ the untyped `createQuery(String)` overload and `Query#list()` are deprecated in 6+, replaced by the typed overload plus `getResultList()`; ④ these methods became generic in 6+, so the cast is redundant; ⑤ the added tests cover `detach`/`fetch` behaviour that is stable on the new line and doubles as documentation samples. |
| `HibernateExtension.java` | Modified | Closing condition tightened from `statelessSession != null` to `statelessSession != null && statelessSession.isOpen()`; `LoggerFactory.getLogger` switched to a static import | Functional need (robustness) | The JUnit extension's `@AfterAll` cleanup can run after a test already closed the session; on Hibernate 7 a repeated `close()` throws and would mask the real failure, so `isOpen()` is checked first. The logger change is style only. |
| `HibernateRuntime.java` | Modified | `ElementType.FIELD/PARAMETER` and `RetentionPolicy.RUNTIME` now static imports | Code style | Consistent annotation-metadata formatting; no behavioural change. |
| `entity/Order.java` | Modified | Added class javadoc (`t_order` mapping + an `Example Usage` block), javadoc on every accessor, `@author/@see/@since`; `final` dropped from `equals` | Documentation | This module is the source of README and user-guide examples, so the test entities were promoted to citable documentation samples on the 0.2.x line. |
| `entity/Product.java` | Modified | Same pattern: class javadoc (including a `StatelessSession.insert/get` example), accessor javadoc, `final` dropped from `equals` | Documentation | As above. |
| `entity/User.java` | Modified | Added class javadoc (`t_user` bidirectional-relationship example), javadoc on `addOrder` describing back-reference maintenance, javadoc on all accessors | Documentation | As above; pins down the "cascade persist + bidirectional sync" usage contract. |
| `entity/UserProfile.java` | Modified | Added class javadoc (`t_user_profile`, cascade-to-profile note) and accessor javadoc | Documentation | As above. |
| `AbstractHibernateTest.java`, `HibernateTest.java` | Unchanged | — | — | Session factory bootstrap and annotation contracts are identical on both lines. |

## 4. Differences excluded by the stated rules (recorded, not counted)

| Difference | Files | Exclusion basis |
|------------|-------|-----------------|
| `javax.persistence.*` → `jakarta.persistence.*` (imports and static imports of `Entity`, `Table`, `Id`, `GeneratedValue`, `ManyToOne`, `ManyToMany`, `OneToOne`, `JoinColumn`, `JoinTable`, `CascadeType`, `FetchType`, `StrategyType`) | `microsphere-hibernate-test/src/main/java/.../entity/{Order,Product,User,UserProfile}.java`, `HibernateExtension.java` | Java EE → Jakarta EE namespace migration (Jakarta Persistence 3.x): a package rename, not an implementation difference. |
| `if (!(o instanceof X)) { … } X that = (X) o;` → `if (!(o instanceof X x)) return false;` (pattern matching, Java 16+) | `entity/{Order,Product,User,UserProfile}.java` | Java 8 vs Java 17+ syntax difference. |
| `Serializable` vs `Object` identifier parameters in the `Interceptor` overrides | `DelegatingInterceptor.java` (class deleted entirely) | Driven by Hibernate API evolution; it disappears with the deleted class and is already recorded in 3.1 as a capability removal. |

## 5. Build context (not `src`, but explains the differences above)

| POM | Difference | Effect |
|-----|------------|--------|
| Root `pom.xml` | `revision` `0.1.10-SNAPSHOT` → `0.2.10-SNAPSHOT`; adds `<java.version>17</java.version>`; parent `microsphere-spring-cloud-parent` `0.1.26` → `0.2.26` | Raises the compile baseline to JDK 17, making new syntax such as pattern matching available. |
| `microsphere-hibernate-parent/pom.xml` | Direct `hibernate-core` version management replaced by importing `org.hibernate.orm:hibernate-platform` (`type=pom`, `scope=import`); default profile `hibernate-5.6` → `hibernate-7.3`, 5.0–5.6 profiles replaced by 6.2–7.3 profiles | Moves the whole coordinate and version matrix to Hibernate 6/7 — the direct cause of every API-compatibility change in 3.1/3.2. |
| `microsphere-hibernate-core/pom.xml`, `microsphere-hibernate-test/pom.xml` | `hibernate-core` groupId `org.hibernate` → `org.hibernate.orm`; the `java9+` profile (`javax.xml.bind:jaxb-api`) deleted | After the Jakarta migration no external JAXB is needed for JDK 9+, and a JDK 17 baseline makes that profile meaningless. |

## 6. Conclusion

The substantive `src/main` differences fall into **two groups**:

1. **Hibernate 5 → 6/7 API adaptation** — the `Integrator` and event-listener signatures in `core`, and the
   `Session`/`Query`/`StatelessSession` call styles in `test`. This is the fundamental divide between the two lines.
2. **Capability removal and documentation** — `DelegatingInterceptor`, unique to 1.x, is gone; test entities gained
   javadoc and usage examples, and `Session.detach` / `StatelessSession.fetch` gained test coverage.

Apart from the deleted `DelegatingInterceptor`, `0.2.10` adds no new public class under `src/main`; the public API
surface of `core` **shrinks**, and `EntityCallback#onLoad` is a source-incompatible signature change that downstream
implementors must adopt.
