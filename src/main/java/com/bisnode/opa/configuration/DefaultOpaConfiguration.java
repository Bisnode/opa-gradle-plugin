package com.bisnode.opa.configuration;

public class DefaultOpaConfiguration implements OpaConfiguration {

    private final OpaPluginConvention opaPluginConvention;

    public DefaultOpaConfiguration(OpaPluginConvention opaPluginConvention) {
        this.opaPluginConvention = opaPluginConvention;
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
