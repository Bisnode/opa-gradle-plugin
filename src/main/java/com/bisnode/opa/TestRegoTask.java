package com.bisnode.opa;

import org.gradle.api.DefaultTask;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

public class TestRegoTask extends DefaultTask {

    private final OpaPluginExtension pluginExtension;

    @Inject
    public TestRegoTask(ObjectFactory objectFactory) {
        pluginExtension = objectFactory.newInstance(OpaPluginExtension.class);
    }

    @TaskAction
    public void testRego() {
        String location = pluginExtension.getLocation();
        List<String> command =
                Arrays.asList(location, "test", pluginExtension.getSrcDir(), pluginExtension.getTestDir());

        getProject().exec(execSpec -> {
            getLogger().debug("Running command {}", String.join(" ", command));
            execSpec.commandLine(command);
        });
    }

    @Override
    public String getGroup() {
        return "opa";
    }

    @Override
    public String getDescription() {
        return "Run OPA tests in testDir of any policies provided in srcDir";
    }

}
