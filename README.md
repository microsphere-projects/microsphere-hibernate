# Microsphere Hibernate

> Microsphere Projects for [Hibernate](https://hibernate.org/)

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/microsphere-projects/microsphere-hibernate)
[![Maven Build](https://github.com/microsphere-projects/microsphere-hibernate/actions/workflows/maven-build.yml/badge.svg)](https://github.com/microsphere-projects/microsphere-hibernate/actions/workflows/maven-build.yml)
[![Codecov](https://codecov.io/gh/microsphere-projects/microsphere-hibernate/branch/main/graph/badge.svg)](https://app.codecov.io/gh/microsphere-projects/microsphere-hibernate)
![Maven](https://img.shields.io/maven-central/v/io.github.microsphere-projects/microsphere-hibernate.svg)
![License](https://img.shields.io/github/license/microsphere-projects/microsphere-hibernate.svg)

## Modules

| **Module**                             | **Purpose**                                                            |
|----------------------------------------|------------------------------------------------------------------------|
| **microsphere-hibernate-parent**       | Defines the parent POM with dependency management and version profiles |
| **microsphere-hibernate-dependencies** | Centralizes dependency management for all project modules              |
| **microsphere-hibernate-core**         | Core featurues of Hibernate ORM extension                              |
| **microsphere-hibernate-test**         | Testing framework for Hibernate ORM                                    |

## Getting Started

The easiest way to get started is by adding the Microsphere Hibernate BOM (Bill of Materials) to your project's
pom.xml:

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

`${microsphere-hibernate.version}` has two branches:

| **Branches** | **Purpose**                                      | **Latest Version** |
|--------------|--------------------------------------------------|--------------------|
| **main**     | Compatible with Spring Cloud 2022.0.x - 2025.0.x | `0.2.6`            |
| **1.x**      | Compatible with Spring Cloud Hoxton - 2021.0.x   | `0.1.6`            |

### Compatibility

| **Branch 0.2.x**   | **Min Version** | **Max Version** |
|--------------------|-----------------|-----------------|
| **JDK**            | 17              | 25              |
| **Spring Cloud**   | 2022.0.x        | 2025.0.x        |
| **Hibernate Core** | 6.2.x           | 7.3.x           |

| **Branch 0.1.x**   | **Min Version** | **Max Version** |
|--------------------|-----------------|-----------------|
| **JDK**            | 8               | 25              |
| **Spring Cloud**   | Hoxton          | 2021.0.x        |
| **Hibernate Core** | 5.0.x           | 5.6.x           |

## Building from Source

You don't need to build from source unless you want to try out the latest code or contribute to the project.

To build the project, follow these steps:

1. Clone the repository:

```bash
git clone https://github.com/microsphere-projects/microsphere-hibernate.git
```

2. Build the source:

- Linux/MacOS:

```bash
./mvnw package
```

- Windows:

```powershell
mvnw.cmd package
```

## Contributing

We welcome your contributions! Please read [Code of Conduct](./CODE_OF_CONDUCT.md) before submitting a pull request.

## Reporting Issues

* Before you log a bug, please search
  the [issues](https://github.com/microsphere-projects/microsphere-hibernate/issues)
  to see if someone has already reported the problem.
* If the issue doesn't already
  exist, [create a new issue](https://github.com/microsphere-projects/microsphere-hibernate/issues/new).
* Please provide as much information as possible with the issue report.

## Documentation

### User Guide

[DeepWiki Host](https://deepwiki.com/microsphere-projects/microsphere-hibernate)

### Wiki

[Github Host](https://github.com/microsphere-projects/microsphere-hibernate/wiki)

### JavaDoc

- [microsphere-hibernate-core](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-hibernate-core)
- [microsphere-hibernate-test](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-hibernate-test)

## License

The Microsphere Spring is released under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).