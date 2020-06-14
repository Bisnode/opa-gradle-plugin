# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

[//]: # (## [Unreleased])
[//]: # (### Added)
[//]: # (### Changed)
[//]: # (### Removed)

## [0.2.1] - 2020-06-14
### Changed
- GH-14 Fix "Failed to read input stream" bug that was triggered on non-empty test-results directory.

## [0.2.0] - 2020-06-09
### Changed
- Complete rewrite of the `testRego` task, now not just failing the build on test failures or errors but actually translating the test results into formats recognizable both by Gradle and by CI/CD systems by exporing the test results into JUnit XML reports. This allows for OPA/Rego tests to be both tested and reported like any other Gradle tests.
- Task configuration may now override "base" configuration (like `srcDir` and `testDir`) on a per task basis. 
- Tons of internal improvements, tests and fixes.

## [0.1.1] - 2019-11-14
### Added
- Install/usage instructions to README now that plugin has been approved for 
[plugin.gradle.org](https://plugins.gradle.org/plugin/com.bisnode.opa).

### Changed
- Fixed `opa` configuration object which would always use default values regardless of provided config.

## [0.1.0] - 2019-11-13
### Added
- First release, including task `testRego`, `testRegoCoverage`, `startOpa`, `stopOpa`.
