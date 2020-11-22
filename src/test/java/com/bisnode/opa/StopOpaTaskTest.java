package com.bisnode.opa;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    public void taskStopsOpaProcess() throws IOException, InterruptedException {
        Optional<String> opaBinaryPath = getOpaBinaryPath();
        assertTrue(opaBinaryPath.isPresent());
        Process process = new ProcessBuilder().directory(project.getRootDir()).command(opaBinaryPath.get(), "run", "-s").start();
        project.getExtensions().getExtraProperties().set("opaProcess", process);

        assertFalse(process.waitFor(3, TimeUnit.SECONDS));

        StopOpaTask task = (StopOpaTask) project.getTasks().getByName("stopOpa");
        task.stopOpa();

        @Nullable Object object = project.getExtensions().getExtraProperties().get("opaProcess");
        assertTrue(object instanceof Process);
        Process opaProcess = (Process) object;

        assertNotNull(opaProcess);
        assertFalse(opaProcess.isAlive());
    }

    private Optional<String> getOpaBinaryPath() {
        return Optional.ofNullable(getClass().getClassLoader().getResource("opa")).map(URL::getPath);
    }

}
