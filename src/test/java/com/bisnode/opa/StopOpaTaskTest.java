package com.bisnode.opa;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class StopOpaTaskTest {

    private Project project;

    @Before
    public void before() {
        project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("com.bisnode.opa");
    }

    @Test
    public void canAddTaskToProject() {
        project = ProjectBuilder.builder().build();
        Task task = project.task("stopOpa");
        assertThat(task instanceof StopOpaTask, is(true));
    }

    @Test
    public void taskIsInOpaGroup() {
        project = ProjectBuilder.builder().build();
        StartOpaTask task = (StartOpaTask) project.task("stopOpa");
        assertThat(task.getGroup(), is("opa"));
    }

}
