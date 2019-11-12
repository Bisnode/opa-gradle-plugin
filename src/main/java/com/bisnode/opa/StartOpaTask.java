package com.bisnode.opa;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

        if (getLogger().isTraceEnabled()) {
            getLogger().trace("Starting OPA from {} with srcDir set to {}",
                    "opa".equals(location) ? "$PATH" : location, srcDir);
        }

        Project project = getProject();
        Path locationPath = Paths.get(location);
        String srcAbsolutePath;
        srcAbsolutePath = locationPath.isAbsolute() ?
                locationPath.toString() :
                Paths.get(project.getRootDir().getPath(), location).toString();

        getLogger().trace("Absolut path of src directory determined to be {}", srcAbsolutePath);

        ProcessBuilder processBuilder = new ProcessBuilder();
        Process process;
        try {
            process = processBuilder
                    .directory(project.getRootDir())
                    .command(location, "run", "-s", srcAbsolutePath)
                    .start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        project.getExtensions().getExtraProperties().set("opaProcess", process);
        getLogger().debug("Storing running opa process in ext.opaProcess");
    }

    @Override
    public String getGroup() {
        return "opa";
    }
}
