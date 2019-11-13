package com.bisnode.opa;

public class OpaPluginExtension {
    private String location = "opa";
    private String srcDir = "src/main/rego";
    private String testDir = "src/test/rego";

    String getLocation() {
        return location;
    }

    void setLocation(String location) {
        this.location = location;
    }

    String getSrcDir() {
        return srcDir;
    }

    void setSrcDir(String srcDir) {
        this.srcDir = srcDir;
    }

    String getTestDir() {
        return testDir;
    }

    void setTestDir(String testDir) {
        this.testDir = testDir;
    }
}
