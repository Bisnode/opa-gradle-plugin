package com.bisnode.opa;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskContainer;

@SuppressWarnings("unused")
public class OpaPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        ExtensionContainer extensions = project.getExtensions();
        extensions.create("opa", OpaPluginExtension.class);

        TaskContainer tasks = project.getTasks();
        tasks.create("startOpa", StartOpaTask.class);
        tasks.create("stopOpa", StopOpaTask.class);
        tasks.create("testRego", TestRegoTask.class);
        tasks.create("testRegoCoverage", TestRegoCoverageTask.class);
    }

}
