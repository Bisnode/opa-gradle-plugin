package com.bisnode.opa;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OpaPluginTest {

    private Project project;

    @Before
    public void before() {
        project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("com.bisnode.opa");
    }

    @After
    public void after() {
        OpaPluginUtils.stopOpaProcess(project);
    }

    @Test
    public void opaPluginAddsOpaStartTaskToProject() {
        assertThat(project.getTasks().getByName("startOpa") instanceof StartOpaTask, is(true));
    }

    @Test
    public void opaPluginAddsOpaStopTaskToProject() {
        assertThat(project.getTasks().getByName("stopOpa") instanceof StopOpaTask, is(true));
    }

}
