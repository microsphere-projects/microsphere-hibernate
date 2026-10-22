# src/test Source Diff: `0.2.10` vs `0.1.10`

> 中文版: [test-diff-0.2.10-vs-0.1.10.md](test-diff-0.2.10-vs-0.1.10.md) ·
> src/main comparison: [main-diff-0.2.10-vs-0.1.10-en.md](main-diff-0.2.10-vs-0.1.10-en.md) ·
> [Docs home](../../README.md)

## 1. Scope and rules

Identical to section 1 of the [src/main diff](main-diff-0.2.10-vs-0.1.10-en.md): per Maven module, same-named modules
only, `src/test` documented separately, and Java 8 vs Java 17+ syntax/API differences plus Java EE (`javax.*`) vs
Jakarta EE (`jakarta.*`) differences are excluded.

| Item | `0.2.10` | `0.1.10` |
|------|----------|----------|
| Maven version | `0.2.10-SNAPSHOT` | `0.1.10-SNAPSHOT` |
| JDK / Hibernate | 17 / 6.2.x – 7.3.x | 8 / 5.0.x – 5.6.x |

`src/test/resources` is byte-identical across the two tags
(`META-INF/services/io.microsphere.hibernate.entity.EntityCallback` unchanged).

## 2. Module overview

| Maven module | `0.1.10` Java / resource files | `0.2.10` Java / resource files | Matched | Summary |
|--------------|--------------------------------|--------------------------------|---------|---------|
| microsphere-hibernate-parent | 0 / 0 | 0 / 0 | ✅ | no test sources |
| microsphere-hibernate-dependencies | 0 / 0 | 0 / 0 | ✅ | no test sources |
| microsphere-hibernate-core | 5 / 1 | 5 / 1 | ✅ | 1 deleted, 1 added, 1 modified, 3 unchanged; resource unchanged |
| microsphere-hibernate-test | 6 / 0 | 6 / 0 | ✅ | 1 modified, 5 unchanged |

> The bulk of the `microsphere-hibernate-test` module's test cases (the `Session`/`StatelessSession` scenarios inside
> `AbstractHibernateH2Test`) lives under that module's `src/main`; those differences are recorded in
> [main-diff section 3.2](main-diff-0.2.10-vs-0.1.10-en.md) and are not double-counted here.

## 3. File-level differences

### 3.1 microsphere-hibernate-core / src/test

Package root: `io.microsphere.hibernate`

| File | Change | Difference | Category | Why |
|------|--------|------------|----------|-----|
| `DelegatingInterceptorTest.java` | **Deleted** (202 lines, 15 `@Test` methods) | Full `MockitoExtension`-based verification of the decorator: `null` delegate falling back to `EmptyInterceptor.INSTANCE`, plus per-method forwarding assertions for `onLoad`/`onFlushDirty`/`onSave`/`onDelete`/collection callbacks/`preFlush`/`postFlush`/`isTransient`/`findDirty`/`instantiate`/`getEntityName`/`getEntity`/the three transaction callbacks/`onPrepareStatement`/`getDelegate` (signatures use `Serializable` identifiers and `EntityMode.POJO`) | Capability removal + API compatibility | Removed together with the class under test: with the Hibernate 6+ `Interceptor` shape these assertions no longer compile (`EntityMode` is gone, identifier parameters became `Object`). On 0.2.x a custom interceptor implements the interface directly, so there is no decorator to test. |
| `entity/EntityCallbackListenerTest.java` | **Added** (46 lines, 1 `@Test`) | `@BeforeEach` constructs `new EntityCallbackListener()` (package-private class, same package), then asserts `requiresPostCommitHandling(null)` returns `false` | Test coverage (regression) | 0.2.10 deleted the misspelled `requiresPostCommitHanding(...)` override from `EntityCallbackListener` (Hibernate 6 removed that default method). Previously the surviving behaviour was only covered indirectly; this test pins the retained `requiresPostCommitHandling` contract as a direct regression case, confirming that "no post-commit handling" still holds after the removal. |
| `entity/TestEntityCallback.java` | Modified | The `onLoad(...)` override gains the `Boolean readOnly` parameter; the `logger.trace` format string gains `readOnly : {}` with the matching argument; the `super.onLoad(...)` forward passes it on | API compatibility | Strictly follows the `EntityCallback#onLoad` contract change (see main-diff 3.1). This file is the `META-INF/services`-registered test implementation, so a signature mismatch would silently break ServiceLoader dispatch. |
| `constants/PropertyConstantsTest.java` | Unchanged | — | — | Constant contract unchanged. |
| `entity/EntityCallbackIntegratorTest.java` | Unchanged | — | — | Assertions about integrator registration remain valid against the new `integrate(Metadata, BootstrapContext, …)` entry point. |
| `entity/EntityCallbackListenerDuplicationStrategyTest.java` | Unchanged | — | — | Duplication-strategy behaviour unchanged. |

### 3.2 microsphere-hibernate-test / src/test

Package root: `io.microsphere.hibernate.test`

| File | Change | Difference | Category | Why |
|------|--------|------------|----------|-----|
| `HibernateAnnotationTest.java` | Modified | The four `@HibernateRuntime` **static fields** on the top-level class (`Configuration`, `SessionFactory`, `Session`, `StatelessSession`) and their `test()` (first two non-null, last two null) were moved wholesale into a new `@Nested class StaticFieldTest`; the outer class no longer declares any `@HibernateRuntime` field. Assertions and the comment ("Session and StatelessSession must NOT be injected into static fields") are unchanged; the `@Nested` order is now `FieldTest` → `StaticFieldTest` → `ParameterTest` | Test coverage + structural refactor | `HibernateExtension#beforeAll` is documented as "inject static fields of the **current** test class (which may be a nested class)". Moving the case into a nested class routes static injection through the nested class's `beforeAll`, covering the nested-class static-injection branch that was previously unverified. It also isolates mutable static state out of the outer class and makes the three injection styles (instance field / static field / method parameter) symmetric siblings, avoiding outer-class static fields persisting across nested-class lifecycles. Because the assertions are identical, this is a coverage-and-organisation change, not a behaviour change. |
| `HibernateH2Test.java` | Unchanged | — | — | The H2 scenario wiring inherited from `AbstractHibernateH2Test` is unchanged. |
| `entity/{User,Order,Product,UserProfile}Test.java` | Unchanged | — | — | These four entity unit tests depend only on getter/setter and `equals/hashCode` semantics, independent of the Hibernate version line. |

## 4. Differences excluded by the stated rules

None of the four `src/test` changes falls into the excluded classes:

- Java 8 vs Java 17+ syntax/API differences;
- `javax.*` (Java EE) vs `jakarta.*` (Jakarta EE) import differences.

(The `javax` → `jakarta` migration only appears in `src/main` entities and the extension class; see
[main-diff section 4](main-diff-0.2.10-vs-0.1.10-en.md).)

## 5. Conclusion

The `src/test` differences are a **mirror** of the `src/main` ones — small in volume but consistent in direction:

1. **Shrinkage**: `DelegatingInterceptorTest` disappears with its subject and is the only **test capability lost**
   between the two lines (1.x still spends 15 cases on delegation behaviour).
2. **Reinforcement**: `EntityCallbackListenerTest` is added, turning the `requiresPostCommitHandling` contract — left
   behind after Hibernate 6 removed the misspelled method — into a direct assertion.
3. **Contract follow-through**: `TestEntityCallback`'s `onLoad` signature is a required compile-level adaptation.
4. **Wider branch coverage**: `HibernateAnnotationTest` moves static-field injection into `StaticFieldTest`, exercising
   the nested-class static injection path of `HibernateExtension`.

Java test-file counts are 5 on both tags for `microsphere-hibernate-core` (the 15-case delegation suite is replaced by a
one-case regression test) and 6 on both tags for `microsphere-hibernate-test` (assertions unchanged, structure only).
