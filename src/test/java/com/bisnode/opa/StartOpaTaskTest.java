package com.bisnode.opa;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StartOpaTaskTest {

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
    public void taskIsInOpaGroup() {
        StartOpaTask task = (StartOpaTask) project.getTasks().getByName("startOpa");
        assertEquals("opa", task.getGroup());
    }

    @Test
    public void opaPluginStartTaskSavesProcessInExtProperties() {
        StartOpaTask startOpaTask = (StartOpaTask) project.getTasks().getByName("startOpa");
        startOpaTask.startOpa();

        @Nullable Object object = project.getExtensions().getExtraProperties().get("opaProcess");
        assertTrue(object instanceof Process);
    }

}
