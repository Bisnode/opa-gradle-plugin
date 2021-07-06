package com.bisnode.opa.process;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.tooling.TestExecutionException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.nio.charset.StandardCharsets.UTF_8;

public class OpaTestProcess {

    private static final Logger log = Logging.getLogger(OpaTestProcess.class);

    private final File rootDir;
    private final ProcessConfiguration command;

    public OpaTestProcess(File rootDir, ProcessConfiguration command) {
        this.rootDir = rootDir;
        this.command = command;
    }

    public ProcessExecutionResult execute() {
        try {
            Process process = new ProcessBuilder()
                    .directory(rootDir)
                    .command(command.getCommandArgs())
                    .start();

            Future<String> resultFromOpa = spawnOutputConsumerThread(process);
            int exitCode = process.waitFor();
            // Should only retrieve results after OPA process exits
            String result = resultFromOpa.get();
            return new ProcessExecutionResult(result, exitCode);
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new TestExecutionException("Failed to start OPA process for tests", e);
        }
    }

    private Future<String> spawnOutputConsumerThread(Process process) {
        CompletableFuture<String> outputFromProcess = new CompletableFuture<>();
        new Thread(() -> {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), UTF_8))) {
                StringBuilder resultStr = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    resultStr.append(line);
                }
                log.debug("[OPA] {}", resultStr);
                outputFromProcess.complete(resultStr.toString());

            } catch (IOException e) {
                log.error("IOException while reading OPA's test results", e);
            }
        }).start();
        return outputFromProcess;
    }

}
