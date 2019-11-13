package com.bisnode.test;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings("resource")
public class PluginFunctionalTest {

    @Rule
    public TemporaryFolder testProjectDir = new TemporaryFolder();
    private File buildFile;

    @Before
    public void setup() throws IOException {
        String buildFileContent = "plugins {\n" +
                "    id 'com.bisnode.opa'\n" +
                "}\n";
        buildFile = testProjectDir.newFile("build.gradle");
        Files.write(buildFile.toPath(), buildFileContent.getBytes(UTF_8));
    }

    @Test
    public void testRunningTestRegoCoverageTaskWithoutArgumentsWork() throws IOException {
        String directory = testProjectDir.getRoot().getAbsolutePath();
        String buildFileContent = "opa {\n" +
                "    srcDir '" + directory + "'\n" +
                "    testDir '" + directory + "'\n" +
                "}";

        Files.write(buildFile.toPath(), buildFileContent.getBytes(UTF_8), StandardOpenOption.APPEND);
        //System.out.println(String.join("\n", Files.readAllLines(buildFile.toPath())));

        StringWriter stringWriter = new StringWriter();
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("testRegoCoverage")
                .forwardStdOutput(stringWriter)
                .forwardStdError(stringWriter)
                .withPluginClasspath()
                .build();

        @Nullable BuildTask task = result.task(":testRegoCoverage");

        assertThat(task, is(notNullValue()));
        assertThat(Objects.requireNonNull(task).getOutcome(), is(TaskOutcome.SUCCESS));

    }

}
