package com.bisnode.opa;

import com.bisnode.opa.configuration.OpaPluginConvention;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.util.Arrays;
import java.util.List;

public class TestRegoTask extends DefaultTask {

    @TaskAction
    public void testRego() {
        OpaPluginConvention convention = getProject().getConvention().getPlugin(OpaPluginConvention.class);

        String location = convention.getLocation();
        List<String> command = Arrays.asList(location, "test", convention.getSrcDir(), convention.getTestDir());

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
