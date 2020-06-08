package com.bisnode.opa;

import com.bisnode.opa.configuration.OpaPluginConvention;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
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

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Starting OPA from {} with srcDir set to {}",
                    "opa".equals(location) ? "$PATH" : location, srcDir);
        }

        Project project = getProject();
        String srcAbsolutePath = OpaPluginUtils.toAbsoluteProjectPath(project, srcDir);
        getLogger().debug("Absolute path of src directory determined to be {}", srcAbsolutePath);

        List<String> command = Arrays.asList(location, "run", "-s", srcAbsolutePath);
        getLogger().debug("Running command {}", String.join(" ", command));
        Process process;
        try {
            process = new ProcessBuilder()
                    .directory(project.getRootDir())
                    .command(command)
                    .start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (process.isAlive()) {
            getLogger().debug("Storing running opa process in ext.opaProcess");
            project.getExtensions().getExtraProperties().set("opaProcess", process);
        } else {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream(), UTF_8))) {
                getLogger().error("{}", reader.lines().collect(Collectors.joining("\n")));
            } catch (IOException e) {
                getLogger().error("Failed to start OPA and failed to read error stream", e);
            }
            throw new RuntimeException("Failed to start OPA");
        }
    }

}
