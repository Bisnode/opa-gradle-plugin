# Open Policy Agent plugin for Gradle
![](https://github.com/Bisnode/opa-gradle-plugin/workflows/build/badge.svg)
[![codecov](https://codecov.io/gh/Bisnode/opa-gradle-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/Bisnode/opa-gradle-plugin)

Plugin adding various tasks to help out integrating Open Policy Agent (OPA) in Gradle builds.

## Install

Simply add the plugin to your build.gradle `plugins` declaration:
```
plugins {
    id 'com.bisnode.opa' version '0.2.0'
}
```
For legacy versions of Gradle, see instructions in the
[Gradle plugin directory](https://plugins.gradle.org/plugin/com.bisnode.opa).

**Prerequisites**: OPA installed on same machine as the tasks are run, either on `$PATH` or pointed out by the 
`location` configuration attribute (see Configuration below).

## Configuration

The following configuration properties are made available by the plugin:
```
opa {
    location    = 'path/opa/executable'     // default: use opa on $PATH
    srcDir      = 'path/to/rego/src'        // default: src/main/rego
    testDir     = 'path/to/rego/tests/'     // default: src/test/rego
}
```

## Tasks

The plugin adds the following tasks:
* `testRego` - Runs `opa test {srcDir} {testDir}` (see below).
* `testRegoCoverage` - Runs `opa test {srcDir} {testDir} --coverage` saving report in `build/report/opa` directory. 
* `startOpa` - Start OPA in background for subsequent tasks like integration tests. 
* `stopOpa` - Stop OPA process started by `startOpa`.

### testRego

The `testRego` task runs the unit tests found in `testDir` with all policies provided in `srcDir`. If not provided, 
these directories default to `src/main/rego` (rego policies) and `src/test/rego` (rego tests) respectively. 

#### JUnit XML test results

The `testRego` task automatically converts the OPA test command output into JUnit XML and writes the output to the
`build/tests-results/opa` directory. This enables any tool or system (such as CI/CD servers) that knows how to parse
JUnit test results to include the OPA test results when handling test outcomes, like when compiling test reports.

Example test report output in Atlassian Bamboo:
![Example test report output](docs/resources/bamboo_test_results.png?raw=true)

### Run Rego tests

To integrate policy tests into your regular Gradle pipeline you may add the `testRego` and/or `testRegoCoverage` tasks 
as dependencies of the `check` task:
```
check.dependsOn(testRego, testRegoCoverage)
```

### Run OPA for integration tests

Just start/stop OPA before/after your test suite like this:
```
integrationTest.dependsOn startOpa
integrationTest.finalizedBy stopOpa
```
