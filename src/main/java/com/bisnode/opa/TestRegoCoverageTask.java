package com.bisnode.opa;

import com.bisnode.opa.configuration.OpaPluginConvention;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import javax.annotation.Nullable;
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

    @Nullable
    private String srcDir;
    @Nullable
    private String testDir;

    @TaskAction
    public void testRegoCoverage() {
        OpaPluginConvention convention = getProject().getConvention().getPlugin(OpaPluginConvention.class);
        String location = convention.getLocation();

        String src = Optional.ofNullable(srcDir).orElse(convention.getSrcDir());
        String test = Optional.ofNullable(testDir).orElse(convention.getTestDir());

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

    @Nullable
    public String getSrcDir() {
        return srcDir;
    }

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
