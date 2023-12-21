package com.bisnode.opa;

import com.bisnode.opa.configuration.OpaExtension;
import org.gradle.api.Project;
import org.gradle.internal.impldep.org.apache.http.client.methods.CloseableHttpResponse;
import org.gradle.internal.impldep.org.apache.http.client.methods.HttpGet;
import org.gradle.internal.impldep.org.apache.http.impl.client.CloseableHttpClient;
import org.gradle.internal.impldep.org.apache.http.impl.client.HttpClients;
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpaIOTest {
    private Project project;

    @BeforeEach
    public void before() {
        project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("com.bisnode.opa");
    }

    @AfterEach
    public void after() {
        OpaPluginUtils.stopOpaProcess(project);
    }

    //GH-28
    @Test
    void shouldNotHangOnOPAOutputBufferOverflow() throws IOException {
        //given
        OpaExtension extension = Objects.requireNonNull(project.getExtensions().findByType(
                OpaExtension.class), "opa extension");
        extension.setSrcDir(getPathToTmpFolder());

        StartOpaTask startOpaTask = (StartOpaTask) project.getTasks().getByName("startOpa");
        //when
        startOpaTask.startOpa();
        // Trial and error has shown that output buffer overflows after ~160 calls
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            for (int i = 0; i < 170; i++) executeCallToOPA(client);
        }
        StopOpaTask task = (StopOpaTask) project.getTasks().getByName("stopOpa");
        task.stopOpa();
        //then
        @Nullable Object object = project.getExtensions().getExtraProperties().get("opaProcess");
        assertTrue(object instanceof Process);
        Process opaProcess = (Process) object;
        assertNotNull(opaProcess);
        assertFalse(opaProcess.isAlive());
    }

    private String getPathToTmpFolder() throws IOException {
        TemporaryFolder tmpdir = new TemporaryFolder();
        tmpdir.create();
        return tmpdir.getRoot().getAbsolutePath();
    }

    private void executeCallToOPA(CloseableHttpClient client) throws IOException {
        CloseableHttpResponse execute = client.execute(new HttpGet("http://localhost:8181/api/v1/data/example"));
        // This output also has to be consumed to avoid hanging
        try (BufferedReader br = new BufferedReader(new InputStreamReader(execute.getEntity().getContent()))) {
            while (br.readLine() != null) {
                // noop
            }
        }
    }

}
