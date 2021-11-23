package com.bisnode.opa.configuration;

import org.gradle.api.Project;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

public class DefaultOpaPluginConvention extends OpaPluginConvention implements HasPublicType {

    private final Project project;

    @Nullable
    private String location = null;
    private String srcDir = "src/main/rego";
    private String testDir = "src/test/rego";

    public DefaultOpaPluginConvention(Project project) {
        this.project = project;
    }

    @Override
    public String getLocation() {
        return Optional.ofNullable(location)
                .orElse(getOpaPathLocation().orElse(Paths.get("opa")).toString());
    }

    @Override
    public void setLocation(String location) {
        this.location = location;
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
                ", location='" + location + '\'' +
                ", srcDir='" + srcDir + '\'' +
                ", testDir='" + testDir + '\'' +
                '}';
    }

    private static Optional<Path> getOpaPathLocation() {
        return Environment.get("PATH").flatMap(path -> Arrays.stream(path.split(":"))
                .map(DefaultOpaPluginConvention::findOpaExecutable)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst());
    }

    private static Optional<Path> findOpaExecutable(String path) {
        try {
            return Files.find(Paths.get(path), 1, (p, basicFileAttributes) -> "opa".equals(p.getFileName().toString()))
                    .findFirst();
        } catch (IOException ignored) {
        }
        return Optional.empty();
    }
}
