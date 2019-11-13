package com.bisnode.opa;

import com.bisnode.opa.configuration.DefaultOpaConfiguration;
import com.bisnode.opa.configuration.DefaultOpaPluginConvention;
import com.bisnode.opa.configuration.OpaConfiguration;
import com.bisnode.opa.configuration.OpaPluginConvention;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;

@SuppressWarnings("unused")
public class OpaPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        OpaPluginConvention convention = new DefaultOpaPluginConvention(project);
        project.getConvention().getPlugins().put("opa", convention);
        project.getExtensions().create(OpaConfiguration.class, "opa", DefaultOpaConfiguration.class, convention);

        TaskContainer tasks = project.getTasks();
        tasks.create("startOpa", StartOpaTask.class);
        tasks.create("stopOpa", StopOpaTask.class);
        tasks.create("testRego", TestRegoTask.class);
        tasks.create("testRegoCoverage", TestRegoCoverageTask.class);
    }

}
