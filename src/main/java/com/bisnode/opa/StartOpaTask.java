package com.bisnode.opa;

import com.bisnode.opa.configuration.OpaPluginConvention;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class StartOpaTask extends DefaultTask {

    public StartOpaTask() {
        setGroup("opa");
        setDescription(
            "Starts OPA in background to allow for subsequent tasks to query it (for integration tests or such). " +
                "NOTE that you'll need to run the opaStop task to stop OPA after starting it with this task."
        );
    }

    @TaskAction
    public void startOpa() {
        OpaPluginConvention convention = getProject().getConvention().getPlugin(OpaPluginConvention.class);
        String location = convention.getLocation();
        String srcDir = convention.getSrcDir();

        String srcAbsolutePath = OpaPluginUtils.toAbsoluteProjectPath(getProject(), srcDir);
        getLogger().debug("Starting OPA from {} with srcDir set to {}", "opa".equals(location) ? "$PATH" : location, srcDir);
        Process process = runProcess(getProject().getRootDir(), buildCommand(location, srcAbsolutePath));

        storeOpaProcessInProject(process);
        CountDownLatch serverInitializationLatch = new CountDownLatch(1);
        spawnOutputConsumerThread(process, serverInitializationLatch);
        waitForOpaInit(serverInitializationLatch);
    }

    private List<String> buildCommand(String location, String srcAbsolutePath) {
        getLogger().debug("Absolute path of src directory determined to be {}", srcAbsolutePath);
        return Arrays.asList(location, "run", "-s", srcAbsolutePath);
    }

    private Process runProcess(File rootDir, List<String> command) {
        getLogger().debug("Running command {}", String.join(" ", command));
        try {
            return new ProcessBuilder()
                .directory(rootDir)
                .command(command)
                .redirectErrorStream(true)
                .start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void storeOpaProcessInProject(Process process) {
        if (process.isAlive()) {
            getLogger().debug("Storing running opa process in ext.opaProcess");
            getProject().getExtensions().getExtraProperties().set("opaProcess", process);
        } else {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream(), UTF_8))) {
                getLogger().error("{}", reader.lines().collect(Collectors.joining("\n")));
            } catch (IOException e) {
                getLogger().error("Failed to start OPA and failed to read error stream", e);
            }
            throw new RuntimeException("Failed to start OPA");
        }
    }

    private void spawnOutputConsumerThread(Process process, CountDownLatch latch) {
        new Thread(() -> {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), UTF_8))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains("Initializing server")) {
                        latch.countDown();
                    }
                    getLogger().info("[OPA] {}", line);
                }
            } catch (IOException e) {
                if (!"Stream closed".equals(e.getMessage())) {
                    getLogger().warn("IOException while reading OPA's stdout", e);
                }
            }
        }).start();
    }

    private void waitForOpaInit(CountDownLatch serverInitializationLatch) {
        try {
            if (!serverInitializationLatch.await(5, TimeUnit.SECONDS)) {
                throw new RuntimeException("OPA failed to initialize");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
