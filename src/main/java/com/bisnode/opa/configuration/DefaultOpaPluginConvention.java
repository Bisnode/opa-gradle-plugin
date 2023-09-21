package com.bisnode.opa.configuration;

import javax.annotation.Nullable;

import org.gradle.api.Project;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

public class DefaultOpaPluginConvention extends OpaPluginConvention implements HasPublicType {

    private final Project project;

    private ExecutableMode mode = ExecutableMode.LOCAL;
    private String location = "opa";
    @Nullable private String version;
    private String srcDir = "src/main/rego";
    private String testDir = "src/test/rego";

    public DefaultOpaPluginConvention(Project project) {
        this.project = project;
    }

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
    public void setVersion(@Nullable String version) {
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

    @Override
    public TypeOf<?> getPublicType() {
        return TypeOf.typeOf(OpaPluginConvention.class);
    }

    @Override
    public String toString() {
        return "DefaultOpaPluginConvention{" +
                "project=" + project +
                ", mode='" + mode + '\'' +
                ", location='" + location + '\'' +
                ", srcDir='" + srcDir + '\'' +
                ", testDir='" + testDir + '\'' +
                '}';
    }
}
