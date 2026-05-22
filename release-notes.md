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

**Full Changelog**: https://github.com/microsphere-projects/microsphere-hibernate/compare/0.2.0...0.2.1