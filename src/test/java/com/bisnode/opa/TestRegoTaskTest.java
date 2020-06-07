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
import static org.hamcrest.MatcherAssert.assertThat;

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

    @Test
    public void opaPluginStartTestTaskSaves() throws IOException {
        TestRegoTask task = (TestRegoTask) project.getTasks().getByName("testRego");

        Path tmpDir = Files.createTempDirectory("rego");
        Files.copy(new ByteArrayInputStream(getRegoPolicy().getBytes(UTF_8)), tmpDir.resolve("policy.rego"));
        Files.copy(new ByteArrayInputStream(getRegoPolicyTest().getBytes(UTF_8)), tmpDir.resolve("policy_test.rego"));

        task.setSrcDir(toAbsoluteProjectPath(project, tmpDir.toAbsolutePath().toString()));
        task.setTestDir(toAbsoluteProjectPath(project, tmpDir.toAbsolutePath().toString()));

        task.testRego();

        String targetReport = project.getBuildDir().getAbsolutePath() + "/test-results/opa/TEST-opa-tests.xml";

        assertThat("OPA test result exists", new File(targetReport).exists());
    }

    @Test
    public void taskUsesDefaultSrcDirIfNoneProvided() {
        TestRegoTask task = (TestRegoTask) project.getTasks().getByName("testRego");

        assertThat(task.getSrcDir(), is("src/main/rego"));
    }

    @Test
    public void taskUsesDefaultTestDirIfNoneProvided() {
        TestRegoTask task = (TestRegoTask) project.getTasks().getByName("testRego");

        assertThat(task.getTestDir(), is("src/test/rego"));
    }

}
