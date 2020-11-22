package com.bisnode.opa;

import com.bisnode.opa.configuration.OpaPluginConvention;
import org.gradle.api.Project;
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

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
    public void opaPluginStartTaskSavesProcessInExtProperties() throws IOException {
        OpaPluginConvention convention = project.getConvention().getPlugin(OpaPluginConvention.class);
        Optional<String> opaBinaryPath = getOpaBinaryPath();
        assertTrue(opaBinaryPath.isPresent());
        convention.setLocation(opaBinaryPath.get());
        convention.setSrcDir(getPathToTmpFolder());

        StartOpaTask startOpaTask = (StartOpaTask) project.getTasks().getByName("startOpa");
        startOpaTask.startOpa();

        @Nullable Object object = project.getExtensions().getExtraProperties().get("opaProcess");
        assertTrue(object instanceof Process);
    }

    private Optional<String> getOpaBinaryPath() {
        return Optional.ofNullable(getClass().getClassLoader().getResource("opa")).map(URL::getPath);
    }

    private String getPathToTmpFolder() throws IOException {
        TemporaryFolder tmpdir = new TemporaryFolder();
        tmpdir.create();
        return tmpdir.getRoot().getAbsolutePath();
    }
}
