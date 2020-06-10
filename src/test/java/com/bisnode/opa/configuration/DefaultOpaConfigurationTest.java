package com.bisnode.opa.configuration;

import org.gradle.api.Project;
import org.gradle.api.reflect.TypeOf;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultOpaConfigurationTest {

    private Project project;

    @BeforeEach
    public void before() {
        project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("com.bisnode.opa");
    }

    @Test
    public void allConfigurationSettingsReturnExpectedValues() {
        DefaultOpaPluginConvention pluginConvention = new DefaultOpaPluginConvention(project);
        DefaultOpaConfiguration configuration = new DefaultOpaConfiguration(pluginConvention);

        configuration.setLocation("/tmp/location");
        configuration.setSrcDir("/tmp/src");
        configuration.setTestDir("/tmp/test");

        assertEquals("/tmp/location", configuration.getLocation());
        assertEquals("/tmp/src", configuration.getSrcDir());
        assertEquals("/tmp/test", configuration.getTestDir());

        assertEquals(TypeOf.typeOf(OpaPluginConvention.class), pluginConvention.getPublicType());
        assertTrue(pluginConvention.toString().startsWith("DefaultOpaPluginConvention{"));
    }

}
