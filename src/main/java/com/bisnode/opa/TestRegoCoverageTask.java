package com.bisnode.opa;

import org.gradle.api.DefaultTask;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class TestRegoCoverageTask extends DefaultTask {

    private final OpaPluginExtension pluginExtension;

    @Nullable
    private String srcDir;
    @Nullable
    private String testDir;

    @Inject
    public TestRegoCoverageTask(ObjectFactory objectFactory) {
        pluginExtension = objectFactory.newInstance(OpaPluginExtension.class);
    }

    @TaskAction
    public void testRegoCoverage() {
        String location = pluginExtension.getLocation();

        String src = Optional.ofNullable(srcDir).orElse(pluginExtension.getSrcDir());
        String test = Optional.ofNullable(testDir).orElse(pluginExtension.getTestDir());

        List<String> command = Arrays.asList(location, "test", src, test, "--coverage");

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            getProject().exec(execSpec -> {
                getLogger().debug("Running command {}", String.join(" ", command));
                execSpec.commandLine(command);
                execSpec.setStandardOutput(outputStream);
            });

            String opaReportsPath = getProject().getRootDir() + "/build/reports/opa";
            String output = new String(outputStream.toByteArray(), UTF_8);
            if (new File(opaReportsPath).mkdirs()) {
                Files.write(Paths.get(opaReportsPath + "/opa-coverage.json"), output.getBytes(UTF_8));
            }
        } catch (IOException e) {
            getLogger().error("Failed writing coverage report", e);
            throw new RuntimeException(e);
        }
    }

    @Input
    @Nullable
    public String getSrcDir() {
        return srcDir;
    }

    @Input
    @Nullable
    public String getTestDir() {
        return testDir;
    }

    public void setSrcDir(String srcDir) {
        this.srcDir = srcDir;
    }

    public void setTestDir(String testDir) {
        this.testDir = testDir;
    }

    @Override
    public String getGroup() {
        return "opa";
    }

    @Override
    public String getDescription() {
        return "Run OPA tests in testDir of any policies provided in srcDir, saving the coverage report from the run.";
    }

}
