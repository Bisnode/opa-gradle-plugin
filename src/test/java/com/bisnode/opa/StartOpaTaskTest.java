package com.bisnode.opa;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class StartOpaTaskTest {

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
    public void taskIsInOpaGroup() {
        StartOpaTask task = (StartOpaTask) project.getTasks().getByName("startOpa");
        assertThat(task.getGroup(), is("opa"));
    }

    @Test
    public void opaPluginStartTaskSavesProcessInExtProperties() {
        StartOpaTask startOpaTask = (StartOpaTask) project.getTasks().getByName("startOpa");
        startOpaTask.startOpa();

        @Nullable Object object = project.getExtensions().getExtraProperties().get("opaProcess");
        assertThat(object instanceof Process, is(true));
    }

}
