package com.bisnode.opa.process;

import org.gradle.tooling.TestExecutionException;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

public class OpaTestProcess {

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

            OpaOutputConsumer opaOutputConsumer = new OpaOutputConsumer(process);
            opaOutputConsumer.spawn();
            String testResultFromOpa = opaOutputConsumer.readAll().stream().collect(Collectors.joining());
            int exitCode = process.waitFor();
            return new ProcessExecutionResult(testResultFromOpa, exitCode);
        } catch (IOException | InterruptedException e) {
            throw new TestExecutionException("Failed to start OPA process for tests", e);
        }
    }
}
