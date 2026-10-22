# Microsphere Hibernate 文档

本目录收录 [Microsphere Hibernate](../../README.md) 的中英双语文档，按**发布分支版本线**和**文档类型**组织。
英文为默认语言（位于各目录顶层），每篇指南文档的中文版本存放于平行的 `zh/` 子目录，文件名保持一致。
`diff/source/` 下的版本源码对比采用相反的布局：中文为主文件，英文版为同目录的 `-en.md` 同名兄弟文件。

## 文档结构

```
docs/
├── README.md                 # 本索引（英文）
├── zh/README.md              # 本索引（中文）
├── current/                  # release 分支     —— 0.2.x 线（Hibernate 6.2–7.3，JDK 17+）
│   ├── user-guide/           # 用户文档（英文）+ zh/（中文）
│   └── developer-guide/      # 开发者文档（英文）+ zh/（中文）
├── 1.x/                      # release-1.x 分支 —— 0.1.x 线（Hibernate 5.0–5.6，JDK 8+）
│   ├── user-guide/           # 用户文档（英文）+ zh/（中文）
│   └── developer-guide/      # 开发者文档（英文）+ zh/（中文）
└── diff/source/              # 相邻 tag 的源码逐模块对比（中文为主文件，英文为 `-en.md` 兄弟文件）
```

## 版本线

| 版本线   | 源码分支      | 最新发布版本   | JDK | Hibernate Core                | Spring Cloud          |
|----------|---------------|----------------|-----|-------------------------------|-----------------------|
| current  | `release`     | `0.2.10`       | 17  | 6.2.x – 7.3.x（`org.hibernate.orm`） | 2022.0.x – 2025.0.x |
| 1.x      | `release-1.x` | `0.1.10`       | 8   | 5.0.x – 5.6.x（`org.hibernate`）     | Hoxton – 2021.0.x   |

## 文档入口

### Current（0.2.x，`release` 分支）

- [用户文档（User Guide）](../current/user-guide/zh/README.md) — 各模块 API、Maven 坐标、版本兼容范围，
  代码示例取材于项目测试用例。英文版：[User Guide](../current/user-guide/README.md)
- [开发者文档（Developer Guide）](../current/developer-guide/zh/README.md) — 架构设计、源码分析、设计模式，
  复杂流程以 mermaid 流程图/时序图给出。英文版：[Developer Guide](../current/developer-guide/README.md)

### 1.x（0.1.x，`release-1.x` 分支）

- [用户文档（User Guide）](../1.x/user-guide/zh/README.md) — 覆盖兼容 Hibernate 5 的版本线，
  包含 1.x 独有的 `DelegatingInterceptor` API 说明。英文版：[User Guide](../1.x/user-guide/README.md)
- [开发者文档（Developer Guide）](../1.x/developer-guide/zh/README.md) — Hibernate 5 适配的架构与源码解析，
  附 mermaid 图。英文版：[Developer Guide](../1.x/developer-guide/README.md)

## 按 Maven 模块的文档

每套指南均按 Maven 模块独立成文：

| 模块 | 用途 |
|------|------|
| microsphere-hibernate-parent | 父 POM：依赖管理与 Hibernate 版本 profiles |
| microsphere-hibernate-dependencies | BOM：集中管理全部模块版本 |
| microsphere-hibernate-core | 核心扩展：基于 Hibernate `Integrator` SPI 的实体回调 |
| microsphere-hibernate-test | 测试框架：JUnit 5 扩展与 H2 测试基座 |

模块文档文件名即模块名（如 `microsphere-hibernate-core.md`），可在上述任一指南目录（及其 `zh/` 子目录）中查阅。

## 版本源码对比（Tag Source Diffs）

逐 Maven 模块对比最新两个 tag 的实现差异，`src/main` 与 `src/test` 独立成文；排除 Java 8/17 语法及 API 差异、
Java EE（`javax.*`）与 Jakarta EE（`jakarta.*`）差异，只保留实质性实现差异。

| 对比 | 范围 | 文档 |
|------|------|------|
| `0.2.10` vs `0.1.10` | `src/main` | [中文](../diff/source/main-diff-0.2.10-vs-0.1.10.md) · [English](../diff/source/main-diff-0.2.10-vs-0.1.10-en.md) |
| `0.2.10` vs `0.1.10` | `src/test` | [中文](../diff/source/test-diff-0.2.10-vs-0.1.10.md) · [English](../diff/source/test-diff-0.2.10-vs-0.1.10-en.md) |

## 其他文档

- [发布说明](../../release-notes.md)
- [microsphere-hibernate-core JavaDoc](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-hibernate-core)
- [microsphere-hibernate-test JavaDoc](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-hibernate-test)
