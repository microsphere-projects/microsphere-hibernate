# Release Notes

## v0.1.0

# Release Notes for Version 0.1.0

## New Features
- Added `EntityCallback` integrator for Hibernate (`6b73e36`, `33807be`).
- Introduced composite `EntityCallback` and listener integration (`33807be`).
- Added `CompositeInterceptor` for Hibernate and updated related code (`a5352fe`).

## Bug Fixes
- Fixed duplicate `onPostUpdateCollection` override (`e591b83`).

## Documentation
- Added compatibility matrix to `README` (`701b7e8`).
- Updated Codecov badge to reflect `dev-1.x` branch (`1dba066`).
- Enhanced release notes and naming conventions (`e81006a`).
- Removed 'ORM' from project name in `README` (`ea2cf57`).

## Dependency Updates
- Adapted code for older Hibernate versions to maintain compatibility (`6e4254c`).

## Test Improvements
- Added tests for `EntityCallback`, `CompositeInterceptor`, and sample entities (`bbcaf7e`, `1687cd3`, `6b73e36`).

## Build and Workflow Enhancements
- Configured Maven publishing credentials (`ce6735a`, `5abd59c`).
- Added Maven profiles for CI (`144f0ef`).
- Removed stray bracket in Maven workflow (`971305e`).
- Updated Maven wrapper setup (`5abd59c`).
- Improved CI workflows, including a Hibernate matrix (`6e4254c`, `144f0ef`).

## Other Changes
- Removed unused `CompositeInterceptor.java` (`e34a34b`).

---

Thank you for using version 0.1.0! Stay tuned for future updates.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-hibernate/compare/...0.1.0## v0.1.1

# Release Notes - Version 0.1.1

## New Features
- Added Hibernate JUnit extension for testing.  
- Simplified static test setup using `javax.Entity`.  

## Documentation
- Updated README with branch names and version changes.  
- Added documentation for Hibernate JUnit extension.  

## Dependency Updates
- Upgraded `microsphere-spring-cloud` to version 0.1.12.  

## Build and Workflow Enhancements
- Merged `release-1.x` changes into `dev-1.x`.  
- Bumped version to next patch post 0.1.0 release.  

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-hibernate/compare/0.1.0...0.1.1## v0.1.2

# Release Notes - Version 0.1.2

## Build and Workflow Enhancements
- Added Maven CI workflow for Java 11+. [(#27a7d54)](https://github.com/mercyblitz/dev-1.x/commit/27a7d54)
- Restricted CI for Java 8 and activated Hibernate profile in CI. [(#b5237a0, #7cf553b)](https://github.com/mercyblitz/dev-1.x/commits/dev-1.x)

## Dependency Updates
- Added `java9+` profile with `jaxb-api` to Maven POMs. [(#80a1278)](https://github.com/mercyblitz/dev-1.x/commit/80a1278)
- Bumped Hibernate versions in the build. [(#7cf553b)](https://github.com/mercyblitz/dev-1.x/commit/7cf553b)

## Test Improvements
- Removed redundant `persist` and `FROM User` tests. [(#6cba8b5)](https://github.com/mercyblitz/dev-1.x/commit/6cba8b5)
- Adjusted tests; removed `readOnly` from `onLoad` callback. [(#8b6a4f7)](https://github.com/mercyblitz/dev-1.x/commit/8b6a4f7)
- Added Hibernate H2 tests. [(#b5237a0)](https://github.com/mercyblitz/dev-1.x/commit/b5237a0)

## Documentation
- Updated Javadoc to reference public `EmptyInterceptor`. [(#f439210)](https://github.com/mercyblitz/dev-1.x/commit/f439210)

## Other Changes
- Performed routine merges to keep branches up to date with upstream. [(#0c7c434, #313453f, #1cfceda)]  
- Version bumped to `0.1.2` for patch release. [(#6b17122)](https://github.com/mercyblitz/dev-1.x/commit/6b17122)

---

**Full Changelog:** [0.1.1...0.1.2](https://github.com/mercyblitz/dev-1.x/compare/0.1.1...0.1.2)

**Full Changelog**: https://github.com/microsphere-projects/microsphere-hibernate/compare/0.1.1...0.1.2## v0.1.3

# Release Notes - Version 0.1.3

## Build and Workflow Enhancements
- Merged `release-1.x` into `dev-1.x` for branch alignment. ([#18](https://github.com/mercyblitz/dev-1.x))
- Bumped the version to the next patch after publishing 0.1.2.  

## Documentation
- Updated README with release information.  

## Dependency Updates
- Bumped parent project version for dependency alignment.  

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-hibernate/compare/0.1.2...0.1.3## v0.1.4

# Release Notes - Version 0.1.4

## New Features
- No new features in this release.

## Bug Fixes
- Removed duplicated line separators and trailing whitespace across the Java source codebase. ([#16](https://github.com/microsphere-projects/copilot/commit/d743607))

## Documentation
- Aligned Javadoc `@param` indentation for improved readability. ([21ee37d](https://github.com/microsphere-projects/copilot/commit/21ee37d))
- Updated `README` to bump branch latest versions. ([9420a73](https://github.com/microsphere-projects/copilot/commit/9420a73))

## Dependency Updates
- Upgraded `microsphere-spring-cloud` to version `0.1.15`. ([b9f92d7](https://github.com/microsphere-projects/copilot/commit/b9f92d7))

## Build and Workflow Enhancements
- Merged `release-1.x` into `dev-1.x` for consistency. ([405eb00](https://github.com/microsphere-projects/copilot/commit/405eb00))
- Bumped the patch version post-release for future development. ([1deaa12](https://github.com/microsphere-projects/copilot/commit/1deaa12))

## Other Changes
- Merged updates from the upstream branch `dev-1.x`. ([21795f0](https://github.com/microsphere-projects/copilot/commit/21795f0))

**Full Changelog**: https://github.com/microsphere-projects/microsphere-hibernate/compare/0.1.3...0.1.4## v0.1.5

# Release Notes - Version 0.1.5

## Dependency Updates
- Upgraded Microsphere Spring Cloud parent to `0.1.16`. [7b7a092](#)
  
## Documentation
- Updated README to reflect branch versions `0.2.5` and `0.1.5`. [ad5dd6a](#)

## Other Changes
- Bumped version to `0.1.5` post-release of `0.1.4`. [14cef43](#)

---

*Thank you for using our project!*

**Full Changelog**: https://github.com/microsphere-projects/microsphere-hibernate/compare/0.1.4...0.1.5## v0.1.6

# Release Notes - Version 0.1.6

## 📚 Documentation
- Updated **README** with branch version matrix for clarity. ([#234cfee](https://github.com/mercyblitz/microsphere-spring-cloud/commit/234cfee))

## 📦 Dependency Updates
- Bumped `microsphere-spring-cloud` to **v0.1.21** for improved compatibility and features. ([#af2b56a](https://github.com/mercyblitz/microsphere-spring-cloud/commit/af2b56a))

## ⚙️ Build and Workflow Enhancements
- Merged changes from `release-1.x` into `dev-1.x`. ([#d95aaae](https://github.com/mercyblitz/microsphere-spring-cloud/commit/d95aaae), [#95dff63](https://github.com/mercyblitz/microsphere-spring-cloud/commit/95dff63))
- Updated project version to prepare for the next patch release. ([#0104700](https://github.com/mercyblitz/microsphere-spring-cloud/commit/0104700))

## 🔗 Other Changes
- Synced with upstream branch `dev-1.x`. ([#de42f68](https://github.com/mercyblitz/microsphere-spring-cloud/commit/de42f68))
- Merged pull request `#34`. ([#c719b0b](https://github.com/mercyblitz/microsphere-spring-cloud/commit/c719b0b)) 

---

**Full Changelog**: [v0.1.5...v0.1.6](https://github.com/mercyblitz/microsphere-spring-cloud/compare/v0.1.5...v0.1.6)

**Full Changelog**: https://github.com/microsphere-projects/microsphere-hibernate/compare/0.1.5...0.1.6## v0.1.7

# Release Notes - Version 0.1.7

## Dependency Updates
- Bumped Microsphere Spring Cloud to `0.1.23`. ([a676aed](#))
- Fixed `hibernate-core` `groupId` in test `pom.xml`. ([27db810](#))
- Fixed Maven dependency metadata in modules. ([438c880](#))

## Documentation
- Updated branch version numbers in `README`. ([f7ce605](#))

## Build and Workflow Enhancements
- Merged `release-1.x` into `dev-1.x`. ([0c4e765](#))
- Bumped version to next patch after publishing `0.1.6`. ([0e70eab](#))

---

**Note:** For details on all changes, refer to the source control history.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-hibernate/compare/0.1.6...0.1.7## v0.1.8

# Release Notes - Version 0.1.8

## Dependency Updates
- Bumped parent project to version `0.1.24` and made core dependency optional. ([a543a36](#))

## Documentation
- Updated README to reflect branch version updates. ([d8eacdb](#))

## Build and Workflow Enhancements
- Merged `release-1.x` into `dev-1.x` for branch alignment and maintenance. ([125f07d](#))
- Bumped version to the next patch after publishing `0.1.7`. ([68f743a](#))

---

For detailed changes, refer to the [Full Changelog](#).

**Full Changelog**: https://github.com/microsphere-projects/microsphere-hibernate/compare/0.1.7...0.1.8## v0.1.9

_Release notes generation failed. Raw commits since 0.1.8:_

```
c6ea3de Update version numbers in README.md
a3fcce9 Merge pull request #43 from mercyblitz/dev-1.x
8db1f89 Bump microsphere-spring-cloud to 0.1.25
afe938e chore: merge release-1.x into dev-1.x [skip ci]
e7fc85a chore: bump version to next patch after publishing 0.1.8
```

**Full Changelog**: https://github.com/microsphere-projects/microsphere-hibernate/compare/0.1.8...0.1.9