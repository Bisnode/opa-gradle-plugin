package com.bisnode.opa.configuration;

import org.gradle.api.Project;
import org.gradle.api.reflect.TypeOf;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultOpaConfigurationTest {

    private Project project;

    @TempDir
    static Path opaPath;
    static Path opaFile;

    @BeforeAll
    public static void init() throws IOException {
        opaFile = Files.createFile(opaPath.resolve("opa"));
    }

    @BeforeEach
    public void before() {
        project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("com.bisnode.opa");
    }

    @Test
    public void allConfigurationSettingsReturnExpectedValues() {
        DefaultOpaPluginConvention pluginConvention = new DefaultOpaPluginConvention(project);
        DefaultOpaConfiguration configuration = new DefaultOpaConfiguration(pluginConvention);

        configuration.setLocation("/tmp/location/opa");
        configuration.setSrcDir("/tmp/src");
        configuration.setTestDir("/tmp/test");

        assertEquals("/tmp/location/opa", configuration.getLocation());
        assertEquals("/tmp/src", configuration.getSrcDir());
        assertEquals("/tmp/test", configuration.getTestDir());

        assertEquals(TypeOf.typeOf(OpaPluginConvention.class), pluginConvention.getPublicType());
        assertTrue(pluginConvention.toString().startsWith("DefaultOpaPluginConvention{"));
    }

    @Test
    public void notProvidingLocationSearchesPathForBinary() {
        Environment.put("PATH", opaPath.toString());
        DefaultOpaPluginConvention pluginConvention = new DefaultOpaPluginConvention(project);
        DefaultOpaConfiguration configuration = new DefaultOpaConfiguration(pluginConvention);

        assertEquals(opaFile.toString(), configuration.getLocation());
    }

}
