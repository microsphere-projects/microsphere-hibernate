# Microsphere Hibernate Documentation

This directory hosts the bilingual documentation for [Microsphere Hibernate](../README.md), organized by
**release branch line** and then by **guide type**. English is the default language; the Chinese version of every
guide document lives in a parallel `zh/` subdirectory with the same filename. The tag-to-tag source comparisons under
[`diff/source/`](diff/source/) use the opposite layout: the Chinese document is at the primary path and the English
version is the `-en.md` sibling.

## Documentation Structure

```
docs/
├── README.md                 # This index (English)
├── zh/README.md              # This index (Chinese)
├── current/                  # release branch     — 0.2.x line (Hibernate 6.2–7.3, JDK 17+)
│   ├── user-guide/           # User Guide (English) + zh/ (Chinese)
│   └── developer-guide/      # Developer Guide (English) + zh/ (Chinese)
├── 1.x/                      # release-1.x branch — 0.1.x line (Hibernate 5.0–5.6, JDK 8+)
│   ├── user-guide/           # User Guide (English) + zh/ (Chinese)
│   └── developer-guide/      # Developer Guide (English) + zh/ (Chinese)
└── diff/source/              # Tag-to-tag source comparisons (Chinese at the primary path,
                              # English as the `-en.md` sibling)
```

## Version Lines

| Line     | Source Branch | Latest Release | JDK | Hibernate Core        | Spring Cloud          |
|----------|---------------|----------------|-----|-----------------------|-----------------------|
| current  | `release`     | `0.2.10`       | 17  | 6.2.x – 7.3.x (`org.hibernate.orm`) | 2022.0.x – 2025.0.x |
| 1.x      | `release-1.x` | `0.1.10`       | 8   | 5.0.x – 5.6.x (`org.hibernate`)     | Hoxton – 2021.0.x   |

## Guides

### Current (0.2.x, `release` branch)

- [User Guide](current/user-guide/README.md) — module APIs, Maven coordinates, version compatibility, and code
  examples taken from the project test cases. 中文：[用户文档](current/user-guide/zh/README.md)
- [Developer Guide](current/developer-guide/README.md) — architecture design, source-code analysis, design patterns,
  and mermaid flow/sequence diagrams. 中文：[开发者文档](current/developer-guide/zh/README.md)

### 1.x (0.1.x, `release-1.x` branch)

- [User Guide](1.x/user-guide/README.md) — same coverage for the Hibernate 5 compatible line, including the
  1.x-only `DelegatingInterceptor` API. 中文：[用户文档](1.x/user-guide/zh/README.md)
- [Developer Guide](1.x/developer-guide/README.md) — architecture and source analysis for Hibernate 5 adaptation,
  with mermaid diagrams. 中文：[开发者文档](1.x/developer-guide/zh/README.md)

## Per-Module Documents

Each guide contains one document per Maven module:

| Module | Purpose |
|--------|---------|
| [microsphere-hibernate-parent](current/user-guide/microsphere-hibernate-parent.md) | Parent POM: dependency management and Hibernate version profiles |
| [microsphere-hibernate-dependencies](current/user-guide/microsphere-hibernate-dependencies.md) | BOM centralizing versions for all modules |
| [microsphere-hibernate-core](current/user-guide/microsphere-hibernate-core.md) | Core extension: entity callbacks via Hibernate `Integrator` SPI |
| [microsphere-hibernate-test](current/user-guide/microsphere-hibernate-test.md) | Testing framework: JUnit 5 extensions and H2 test fixtures |

Replace `current/user-guide` with `current/developer-guide`, `1.x/user-guide`, or `1.x/developer-guide`
(or their `zh/` counterparts) to read the same module in another guide, branch line, or language.

## Tag Source Diffs

Side-by-side comparisons of the two most recent tags, module by module, with `src/main` and `src/test` documented
separately. Java 8 vs Java 17+ syntax/API differences and Java EE (`javax.*`) vs Jakarta EE (`jakarta.*`) differences
are filtered out, so only substantive implementation deltas remain.

| Comparison | Scope | Documents |
|------------|-------|-----------|
| `0.2.10` vs `0.1.10` | `src/main` | [中文](diff/source/main-diff-0.2.10-vs-0.1.10.md) · [English](diff/source/main-diff-0.2.10-vs-0.1.10-en.md) |
| `0.2.10` vs `0.1.10` | `src/test` | [中文](diff/source/test-diff-0.2.10-vs-0.1.10.md) · [English](diff/source/test-diff-0.2.10-vs-0.1.10-en.md) |

## Other Documentation

- [Release notes](../release-notes.md)
- [JavaDoc for microsphere-hibernate-core](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-hibernate-core)
- [JavaDoc for microsphere-hibernate-test](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-hibernate-test)
