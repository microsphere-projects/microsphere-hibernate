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

**Full Changelog**: https://github.com/microsphere-projects/microsphere-hibernate/compare/...0.2.0