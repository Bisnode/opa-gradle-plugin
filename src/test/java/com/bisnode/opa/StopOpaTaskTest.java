package com.bisnode.opa;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Nullable;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class StopOpaTaskTest {

    private Project project;

    @Before
    public void before() {
        project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("com.bisnode.opa");
    }

    @Test
    public void taskIsInOpaGroup() {
        StopOpaTask task = (StopOpaTask) project.getTasks().getByName("stopOpa");
        assertThat(task.getGroup(), is("opa"));
    }

    @Test
    @Ignore("Unit test should not depend on OPA binary - move to functional test or mock")
    public void taskStopsOpaProcess() throws IOException {
        Process process = new ProcessBuilder().directory(project.getRootDir()).command("opa", "run", "-s").start();
        project.getExtensions().getExtraProperties().set("opaProcess", process);

        assertThat(process.isAlive(), is(true));

        StopOpaTask task = (StopOpaTask) project.getTasks().getByName("stopOpa");
        task.stopOpa();

        @Nullable Object object = project.getExtensions().getExtraProperties().get("opaProcess");
        assertThat(object, instanceOf(Process.class));
        Process opaProcess = (Process) object;

        assertThat(opaProcess, notNullValue());
        assertThat(opaProcess.isAlive(), is(false));
    }

}
