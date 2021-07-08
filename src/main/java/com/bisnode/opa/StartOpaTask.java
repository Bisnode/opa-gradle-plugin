package com.bisnode.opa;

import com.bisnode.opa.configuration.OpaPluginConvention;
import com.bisnode.opa.process.OpaOutputConsumer;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
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
        OpaOutputConsumer outputConsumer = new OpaOutputConsumer(process);
        waitForOpaInit(outputConsumer);
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

    private void waitForOpaInit(OpaOutputConsumer outputConsumer) {
        BlockingQueue<String> outputFromOpa = outputConsumer.spawn();
        CountDownLatch serverInitializationLatch = new CountDownLatch(1);

        new Thread(() -> {
            try {
                String line;
                while (!(line = outputFromOpa.take()).equals(OpaOutputConsumer.POISON_PILL)) {
                    if (line.contains("Initializing server")) {
                        serverInitializationLatch.countDown();
                    }
                    getLogger().info("[OPA] {}", line);
                }
            } catch (InterruptedException e) {
                getLogger().error("Failed to read from consumed output from OPA");
            }
        }).start();

        try {
            if (!serverInitializationLatch.await(5, TimeUnit.SECONDS)) {
                throw new RuntimeException("OPA failed to initialize");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
