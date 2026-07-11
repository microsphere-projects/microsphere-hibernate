# Release Notes

## v0.2.0

# Release Notes for Version 0.2.0  

## New Features  
- Added `CompositeInterceptor` and integrated it with entity callbacks.  
- Introduced `EntityCallback` integrator for streamlined entity listener integration.  
- Added compatibility matrix in README for better environment tracking.  
- Added test entities and entity tests for `User`, `Order`, `Product`, and `Profile`.  
- Enabled Hibernate core with CI integration and Maven profiles.  

## Bug Fixes  
- Fixed Java matrix variable and YAML formatting issues.  
- Removed stray bracket in Maven workflow, ensuring error-free builds.  

## Documentation  
- Updated README to include a compatibility matrix.  
- Removed 'ORM' from the project name in README for alignment.  

## Test Improvements  
- Enhanced test coverage for user-defined constants and enabled features.  
- Strengthened entity tests and callback integrations functionality.  

## Build and Workflow Enhancements  
- Configured Maven publication with server credentials.  
- Reconfigured Maven wrapper to support CI credentials efficiently.  
- Added comprehensive Hibernate CI matrix for streamlined builds.  

## Other Changes  
- Removed unnecessary final modifiers in code.  
- Deleted unused file `CompositeInterceptor.java` to reduce clutter.  

**Full Changelog**: https://github.com/microsphere-projects/microsphere-hibernate/compare/...0.2.0## v0.2.1

# Release Notes for Version 0.2.1

## New Features
- Added `@HibernateTest` and `@HibernateRuntime` annotations with `HibernateExtension` for enhanced Hibernate integration.  
- Provided integration tests for Hibernate features.

## Bug Fixes
- Addressed robust JAR path extraction during runtime.  
- Improved handling of malformed properties and null directory listings with warnings.

## Documentation
- Updated JavaDoc to production and test-module source files with example usage.  
- Fixed README table alignment issues.  
- Renamed branches referenced in the README for clarity.

## Dependency Updates
- Upgraded `microsphere-spring-cloud` dependency to version `0.2.12`.

## Test Improvements
- Enhanced Hibernate test annotations for improved testing capabilities with `SimpleClassScanner` integration.

## Build and Workflow Enhancements
- Merged main into release branch for workflow consistency.  
- Performed version bump to the next patch after publishing `0.2.0`.

## Other Changes
- Integrated code review feedback for improved runtime logic robustness.  

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-hibernate/compare/0.2.0...0.2.1## v0.2.2

# Release Notes - Version 0.2.2

## Test Improvements
- **Unit Test Additions**: Introduced `EntityCallbackListener` unit tests for improved test coverage. ([ba3ebbc](https://example.com))

## Other Changes
- **Callback Refactor**: Removed upsert callbacks and `DelegatingInterceptor` for cleaner code. ([66c64ef](https://example.com))
- **Hibernate Updates**: Activated Hibernate profile and pinned the Hibernate version for better stability. ([1e0a99c](https://example.com))
- **Documentation Update**: Revised README to reflect the latest branch versions. ([1384c38](https://example.com))

---

Full Changelog: [v0.2.1...v0.2.2](https://example.com)

**Full Changelog**: https://github.com/microsphere-projects/microsphere-hibernate/compare/0.2.1...0.2.2## v0.2.3

# Release Notes: Version 0.2.3

## Bug Fixes
- **Code Cleanup**: Removed duplicated line separators and trailing whitespace. ([#17](https://github.com/microsphere-projects/copilot/pull/17))

## Dependency Updates
- Bumped `microsphere-spring-cloud` to version `0.2.14`.

## Documentation
- Updated README with latest branch versions. ([95c95e3](https://github.com/microsphere-projects/copilot/commit/95c95e3))

## Other Changes
- Version bumped post-release of `0.2.2`. ([4545ac6](https://github.com/microsphere-projects/copilot/commit/4545ac6))
- Merged `main` into `release`. [skip ci] 

**Full Changelog**: https://github.com/microsphere-projects/microsphere-hibernate/compare/0.2.2...0.2.3## v0.2.4

# Release Notes for Version 0.2.4

## Dependency Updates
- Updated `microsphere-spring-cloud` to `0.2.15`. ([e29c458](#))

## Documentation
- Updated branch version details in `README`. ([f3b5096](#))

## Test Improvements
- Replaced implicit assertions with explicit JUnit assertions.
- Fixed Javadoc parameter inconsistencies. ([921b38d](#))

## Other Changes
- Routine version bump and branch merges. ([3ccdd87](#), [f9fa7ab](#), [2267985](#), [0939a98](#)) 

**Full Changelog**: https://github.com/microsphere-projects/microsphere-hibernate/compare/0.2.3...0.2.4## v0.2.5

# Release Notes - Version 0.2.5

## Documentation
- **README Update:** Reflect parent version bump to `0.2.16`.  
  ([2091a44](https://example.com/commit/2091a44))

## Build and Workflow Enhancements
- **Version Management:** Bumped patch version post publishing `0.2.4` for improved release tracking.  
  ([bb579e0](https://example.com/commit/bb579e0))
- **Branch Merges:** Merged `main` into `release` and vice versa for branch alignment.  
  ([b5ea517](https://example.com/commit/b5ea517), [f8155df](https://example.com/commit/f8155df))

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-hibernate/compare/0.2.4...0.2.5## v0.2.6

# Release Notes - Version 0.2.6

## Dependency Updates
- Bumped **Microsphere Spring Cloud** to `0.2.21`. (#76079e5)

## Documentation
- Updated README to reflect current branch version numbers. (#6bdcc82)

## Build and Workflow Enhancements
- Merged `main` into `release`. [skip ci] (#735de0e, #63b35bf)
- Merged `release` into `main`. [skip ci] (#857e249)
- Bumped version to next patch after publishing `0.2.5`. (#d1c5d2b)

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-hibernate/compare/0.2.5...0.2.6