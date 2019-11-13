# Open Policy Agent plugin for Gradle

Plugin adding various tasks to help out integrating Open Policy Agent (OPA) in Gradle builds.

**Work in progress**

## Install

**Prerequisites**: OPA installed on same machine as the tasks are run, either on `$PATH` or pointed out by the `opaLocation`
configuration attribute (see Configuration below).

## Configuration

The following configuration properties are made available by the plugin:
```
opa {
    opaLocation = 'path/opa/executable'     // default: use opa on $PATH
    srcDir      = 'path/to/rego/src'        // default: src/main/rego
    testDir     = 'path/to/rego/tests/'     // default: src/test/rego
}
```

### Run Rego tests

To integrate policy tests into your regular Gradle pipeline you may use the `regoTest` task.

```
check.dependsOn regoTest
```

### Run OPA for integration tests

Just start/stop OPA before/after your test suite like this:
```
integrationTest.dependsOn startOpa
integrationTest.finalizedBy stopOpa
```
