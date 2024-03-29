package com.bisnode.opa;

import com.bisnode.opa.configuration.OpaExtension;
import com.bisnode.opa.process.OpaTestProcess;
import com.bisnode.opa.process.ProcessConfiguration;
import com.bisnode.opa.process.ProcessExecutionResult;
import com.bisnode.opa.testformats.junit.JUnitXml;
import com.bisnode.opa.testformats.opa.OpaVerboseSummary;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.tooling.TestExecutionException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class TestRegoTask extends DefaultTask {

    public TestRegoTask() {
        setGroup("opa");
        setDescription("Run OPA tests in testDir of any policies provided in srcDir");
    }

    @Nullable
    private String srcDir;
    @Nullable
    private String testDir;

    @TaskAction
    public void testRego() {
        ProcessConfiguration processConfiguration = new ProcessConfiguration(getLocation(), getSrcDir(), getTestDir());
        getLogger().debug("Running command {}", processConfiguration);
        ProcessExecutionResult processExecutionResult = new OpaTestProcess(getProject().getRootDir(), processConfiguration).execute();

        writeToFile(testResultsPath(), asJUnitXml(processExecutionResult));

        getLogger().info(asOpaVerbose(processExecutionResult));

        if (processExecutionResult.getExitCode() != 0) {
            throw new TestExecutionException(processExecutionResult.getOutput());
        }
    }

    private Path testResultsPath() {
        return Paths.get(getProject().getBuildDir().getAbsolutePath() + "/test-results/opa/TEST-opa-tests.xml");
    }

    private void writeToFile(Path filePath, String output) {
        try {
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, output.getBytes());
        } catch (IOException e) {
            throw new TestExecutionException("Could not write to file " + filePath.toString(), e);
        }
    }

    private String asOpaVerbose(ProcessExecutionResult processExecutionResult) {
        try {
            OpaTestResults opaTestResults = OpaTestResults.fromJson(processExecutionResult.getOutput());
            return OpaVerboseSummary.of(opaTestResults).summary();
        } catch (JsonProcessingException e) {
            throw new TestExecutionException("Could not parse test command output", e);
        }
    }

    private String asJUnitXml(ProcessExecutionResult processExecutionResult) {
        try {
            OpaTestResults opaTestResults = OpaTestResults.fromJson(processExecutionResult.getOutput());
            return JUnitXml.from(opaTestResults).asXmlString();
        } catch (JsonProcessingException e) {
            throw new TestExecutionException("Could not parse test command output", e);
        }
    }

    @InputDirectory
    public String getSrcDir() {
        return Optional.ofNullable(srcDir)
                .orElse(getProject().getExtensions().getByType(OpaExtension.class).getSrcDir());
    }

    @InputDirectory
    public String getTestDir() {
        return Optional.ofNullable(testDir)
                .orElse(getProject().getExtensions().getByType(OpaExtension.class).getTestDir());
    }

    private String getLocation() {
        return getProject().getExtensions().getByType(OpaExtension.class).getLocation();
    }

    public void setSrcDir(String srcDir) {
        this.srcDir = srcDir;
    }

    public void setTestDir(String testDir) {
        this.testDir = testDir;
    }

}
