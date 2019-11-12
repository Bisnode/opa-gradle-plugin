package com.bisnode.opa;

public class OpaPluginExtension {
    private String location = "opa";
    private String srcDir = "src/main/rego";
    private String testDir = "src/test/rego";

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSrcDir() {
        return srcDir;
    }

    public void setSrcDir(String srcDir) {
        this.srcDir = srcDir;
    }

    public String getTestDir() {
        return testDir;
    }

    public void setTestDir(String testDir) {
        this.testDir = testDir;
    }
}
