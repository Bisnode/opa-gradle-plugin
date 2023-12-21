package com.bisnode.opa.configuration;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefaultOpaConfigurationTest {

    private Project project;

    @BeforeEach
    public void before() {
        project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("com.bisnode.opa");
    }

    @Test
    public void allConfigurationSettingsReturnExpectedValues() {
        DefaultOpaExtension configuration = new DefaultOpaExtension();

        configuration.setLocation("/tmp/location");
        configuration.setSrcDir("/tmp/src");
        configuration.setTestDir("/tmp/test");

        assertEquals("/tmp/location", configuration.getLocation());
        assertEquals("/tmp/src", configuration.getSrcDir());
        assertEquals("/tmp/test", configuration.getTestDir());
    }

}
