package com.bisnode.opa;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class StopOpaTask extends DefaultTask {

    public StopOpaTask() {
        setGroup("opa");
        setDescription("Stops the OPA server started by the startOpa task.");
    }

    @TaskAction
    public void stopOpa() {
        boolean result = OpaPluginUtils.stopOpaProcess(getProject());
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(result ? "OPA stopped" : "Did not find OPA process to stop");
        }
    }

}
