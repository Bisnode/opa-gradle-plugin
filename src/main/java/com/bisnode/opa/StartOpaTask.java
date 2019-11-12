package com.bisnode.opa;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class StartOpaTask extends DefaultTask {

    private final OpaPluginExtension pluginExtension;

    @Inject
    public StartOpaTask(ObjectFactory objectFactory) {
        pluginExtension = objectFactory.newInstance(OpaPluginExtension.class);
    }

    @TaskAction
    public void startOpa() {
        String location = pluginExtension.getLocation();
        String srcDir = pluginExtension.getSrcDir();

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Starting OPA from {} with srcDir set to {}",
                    "opa".equals(location) ? "$PATH" : location, srcDir);
        }

        Project project = getProject();
        Path srcPath = Paths.get(srcDir);
        String srcAbsolutePath;
        srcAbsolutePath = srcPath.isAbsolute() ?
                srcPath.toString() :
                Paths.get(project.getRootDir().getPath(), srcDir).toString();
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
                return;
            }
            throw new RuntimeException("Failed to start OPA");
        }
    }

    @Override
    public String getGroup() {
        return "opa";
    }
}
