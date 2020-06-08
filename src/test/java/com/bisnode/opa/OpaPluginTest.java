package com.bisnode.opa;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpaPluginTest {

    private Project project;

    @BeforeEach
    public void before() {
        project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("com.bisnode.opa");
    }

    @AfterEach
    public void after() {
        OpaPluginUtils.stopOpaProcess(project);
    }

    @Test
    public void opaPluginAddsOpaStartTaskToProject() {
        assertTrue(project.getTasks().getByName("startOpa") instanceof StartOpaTask);
    }

    @Test
    public void opaPluginAddsOpaStopTaskToProject() {
        assertTrue(project.getTasks().getByName("stopOpa") instanceof StopOpaTask);
    }

    @Test
    public void opaPluginAddsTestRegoTaskToProject() {
        assertTrue(project.getTasks().getByName("testRego") instanceof TestRegoTask);
    }

}
