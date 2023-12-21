package com.bisnode.opa.configuration;

import javax.annotation.Nullable;

public class DefaultOpaExtension implements OpaExtension {

    private ExecutableMode mode = ExecutableMode.LOCAL;
    private String location = "opa";
    @Nullable private String version;
    private String srcDir = "src/main/rego";
    private String testDir = "src/test/rego";

    @Override
    public ExecutableMode getMode() {
        return mode;
    }

    @Override
    public void setMode(ExecutableMode mode) {
        this.mode = mode;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public @Nullable String getVersion() {
        return version;
    }

    @Override
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String getSrcDir() {
        return srcDir;
    }

    @Override
    public void setSrcDir(String srcDir) {
        this.srcDir = srcDir;
    }

    @Override
    public String getTestDir() {
        return testDir;
    }

    @Override
    public void setTestDir(String testDir) {
        this.testDir = testDir;
    }
}
