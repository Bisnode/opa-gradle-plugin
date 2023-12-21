package com.bisnode.opa.configuration;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public interface OpaExtension {

    ExecutableMode getMode();
    void setMode(ExecutableMode mode);

    String getLocation();
    void setLocation(String location);

    @Nullable String getVersion();
    void setVersion(String version);

    String getSrcDir();
    void setSrcDir(String srcDir);

    String getTestDir();
    void setTestDir(String testDir);

}
