package com.bisnode.opa.process;

import org.gradle.tooling.TestExecutionException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.UTF_8;

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
            int exitCode = process.waitFor();
            return new ProcessExecutionResult(asString(process.getInputStream()), exitCode);
        } catch (IOException | InterruptedException e) {
            throw new TestExecutionException("Failed to start OPA process for tests", e);
        }
    }

    private String asString(InputStream inputStream) {
        // "Stupid Scanner trick" https://community.oracle.com/blogs/pat/2004/10/23/stupid-scanner-tricks
        return new Scanner(inputStream, UTF_8.name()).useDelimiter("\\A").next();
    }

}
