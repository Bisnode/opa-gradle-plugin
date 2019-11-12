package com.bisnode.opa;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestRegoTaskTest {

    private Project project;

    @Before
    public void before() {
        project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("com.bisnode.opa");
    }

    @Test
    public void canAddTaskToProject() {
        Task task = project.getTasks().getByName("testRego");
        assertThat(task instanceof TestRegoTask, is(true));
    }

    @Test
    public void taskIsInOpaGroup() {
        TestRegoTask task = (TestRegoTask) project.getTasks().getByName("testRego");
        assertThat(task.getGroup(), is("opa"));
    }

}
