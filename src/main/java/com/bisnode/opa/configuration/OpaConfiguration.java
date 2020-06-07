package com.bisnode.opa.configuration;

@SuppressWarnings("unused")
public interface OpaConfiguration {

    String getLocation();
    void setLocation(String location);

    String getSrcDir();
    void setSrcDir(String srcDir);

    String getTestDir();
    void setTestDir(String testDir);

}
