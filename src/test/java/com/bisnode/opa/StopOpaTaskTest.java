package com.bisnode.opa;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StopOpaTaskTest {

    private Project project;

    @BeforeEach
    public void before() {
        project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("com.bisnode.opa");
    }

    @Test
    public void taskIsInOpaGroup() {
        StopOpaTask task = (StopOpaTask) project.getTasks().getByName("stopOpa");
        assertEquals("opa", task.getGroup());
    }

    @Test
    @Disabled("Unit test should not depend on OPA binary - move to functional test or mock")
    public void taskStopsOpaProcess() throws IOException {
        Process process = new ProcessBuilder().directory(project.getRootDir()).command("opa", "run", "-s").start();
        project.getExtensions().getExtraProperties().set("opaProcess", process);

        assertTrue(process.isAlive());

        StopOpaTask task = (StopOpaTask) project.getTasks().getByName("stopOpa");
        task.stopOpa();

        @Nullable Object object = project.getExtensions().getExtraProperties().get("opaProcess");
        assertTrue(object instanceof Process);
        Process opaProcess = (Process) object;

        assertNotNull(opaProcess);
        assertFalse(opaProcess.isAlive());
    }

}
