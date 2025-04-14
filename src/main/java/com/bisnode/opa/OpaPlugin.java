package com.bisnode.opa;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bisnode.opa.configuration.DefaultOpaExtension;
import com.bisnode.opa.configuration.ExecutableMode;
import com.bisnode.opa.configuration.OpaExtension;
import com.bisnode.opa.configuration.OpaPlatform;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

@SuppressWarnings("unused")
public class OpaPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        OpaExtension opaExtension = project.getExtensions().create(OpaExtension.class, "opa", DefaultOpaExtension.class);

        TaskContainer tasks = project.getTasks();
        List<TaskProvider<?>> addedTasks = new ArrayList<>();
        addedTasks.add(tasks.register("startOpa", StartOpaTask.class));
        addedTasks.add(tasks.register("stopOpa", StopOpaTask.class));
        addedTasks.add(tasks.register("testRego", TestRegoTask.class));
        addedTasks.add(tasks.register("testRegoCoverage", TestRegoCoverageTask.class));

        project.afterEvaluate(currentProject -> applyToRootProject(currentProject, opaExtension, addedTasks));
    }

    private void applyToRootProject(Project project, OpaExtension opaExtension, List<TaskProvider<?>> dependentTasks) {
        if (!ExecutableMode.DOWNLOAD.equals(opaExtension.getMode())) {
            return;
        }
        String version = opaExtension.getVersion();
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalStateException("You must specify OPA version in DOWNLOAD mode");
        }

        // When the current plugin is executed in a subproject,
        // the root project may (or may not) have been executed.
        // We need different strategies to apply depending on root project state
        Project rootProject = project.getRootProject();
        if (rootProject.getState().getExecuted()) {
            applyDownloadTask(rootProject, version, dependentTasks);
        } else {
            rootProject.afterEvaluate(root -> applyDownloadTask(root, version, dependentTasks));
        }
    }

    private synchronized void applyDownloadTask(Project project, String version, List<TaskProvider<?>> dependentTasks) {
        final String taskName = String.format("%s_%s", DownloadOpaTask.TASK_BASE_NAME, version);
        final File opaExecutable = OpaPlatform.getPlatform().getExecutablePath(project.getBuildDir().toPath(), version).toFile();
        Set<Task> downloadTasks = project.getTasksByName(taskName, false);
        if (downloadTasks.isEmpty()) {
            final TaskProvider<DownloadOpaTask> downloadTask = project.getTasks().register(taskName, DownloadOpaTask.class);
            downloadTask.configure(task -> {
                task.getVersion().set(version);
                task.getOutputFile().set(opaExecutable);
            });
            downloadTasks = new HashSet<>();
            downloadTasks.add(downloadTask.get());
        }
        downloadTasks.forEach(downloadTask -> dependentTasks.forEach(task -> {
            task.get().dependsOn(downloadTask);
            final OpaExtension opaExtension = task.get().getExtensions().findByType(OpaExtension.class);
            if (opaExtension != null) {
                opaExtension.setLocation(opaExecutable.getParent());
            }
        }));
    }

}
