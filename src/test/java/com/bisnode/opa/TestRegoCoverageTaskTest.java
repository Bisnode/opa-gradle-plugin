package com.bisnode.opa;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.bisnode.opa.OpaPluginTestUtils.getRegoPolicy;
import static com.bisnode.opa.OpaPluginTestUtils.getRegoPolicyTest;
import static com.bisnode.opa.OpaPluginUtils.toAbsoluteProjectPath;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestRegoCoverageTaskTest {

    private Project project;

    @Before
    public void before() {
        project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("com.bisnode.opa");
    }

    @Test
    public void canAddTaskToProject() {
        Task task = project.getTasks().getByName("testRegoCoverage");
        assertThat(task instanceof TestRegoCoverageTask, is(true));
    }

    @Test
    public void taskIsInOpaGroup() {
        TestRegoCoverageTask task = (TestRegoCoverageTask) project.getTasks().getByName("testRegoCoverage");
        assertThat(task.getGroup(), is("opa"));
    }

    @Test
    public void taskGeneratesTestReport() throws IOException {
        TestRegoCoverageTask task = (TestRegoCoverageTask) project.getTasks().getByName("testRegoCoverage");

        Path tmpDir = Files.createTempDirectory("rego");
        Files.copy(new ByteArrayInputStream(getRegoPolicy().getBytes(UTF_8)), tmpDir.resolve("policy.rego"));
        Files.copy(new ByteArrayInputStream(getRegoPolicyTest().getBytes(UTF_8)), tmpDir.resolve("policy_test.rego"));

        task.setSrcDir(toAbsoluteProjectPath(project, tmpDir.toAbsolutePath().toString()));
        task.setTestDir(toAbsoluteProjectPath(project, tmpDir.toAbsolutePath().toString()));
        task.testRegoCoverage();

        String reportFile = project.getRootDir() + "/build/reports/opa/opa-coverage.json";
        assertThat(new File(reportFile).exists(), is(true));
    }

}
