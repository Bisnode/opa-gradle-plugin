# Open Policy Agent plugin for Gradle
![](https://github.com/Bisnode/opa-gradle-plugin/workflows/build/badge.svg)
[![codecov](https://codecov.io/gh/Bisnode/opa-gradle-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/Bisnode/opa-gradle-plugin)

Plugin adding various tasks to help out integrating Open Policy Agent (OPA) in Gradle builds.

## Install

Simply add the plugin to your build.gradle `plugins` declaration:
```
plugins {
    id 'com.bisnode.opa' version '0.1.1'
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

The following tasks are currently added by the plugin:
* `testRego` - Runs `opa test {srcDir} {testDir}` .
* `testRegoCoverage` - Runs `opa test {srcDir} {testDir} --coverage` saving report in `build/report/opa` directory. 
* `startOpa` - Start OPA in background for subsequent tasks like integration tests. 
* `stopOpa` - Stop OPA process started by `startOpa`.

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
