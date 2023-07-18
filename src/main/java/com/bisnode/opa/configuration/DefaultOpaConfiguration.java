package com.bisnode.opa.configuration;

import javax.annotation.Nullable;

public class DefaultOpaConfiguration implements OpaConfiguration {

    private final OpaPluginConvention opaPluginConvention;

    public DefaultOpaConfiguration(OpaPluginConvention opaPluginConvention) {
        this.opaPluginConvention = opaPluginConvention;
    }

    @Override
    public ExecutableMode getMode() {
        return opaPluginConvention.getMode();
    }

    @Override
    public void setMode(ExecutableMode mode) {
        opaPluginConvention.setMode(mode);
    }

    @Override
    public String getLocation() {
        return opaPluginConvention.getLocation();
    }

    @Override
    public void setLocation(String location) {
        opaPluginConvention.setLocation(location);
    }

    @Override
    public @Nullable String getVersion() {
        return opaPluginConvention.getVersion();
    }

    @Override
    public void setVersion(String version) {
        opaPluginConvention.setVersion(version);
    }

    @Override
    public String getSrcDir() {
        return opaPluginConvention.getSrcDir();
    }

    @Override
    public void setSrcDir(String srcDir) {
        opaPluginConvention.setSrcDir(srcDir);
    }

    @Override
    public String getTestDir() {
        return opaPluginConvention.getTestDir();
    }

    @Override
    public void setTestDir(String testDir) {
        opaPluginConvention.setTestDir(testDir);
    }
}
