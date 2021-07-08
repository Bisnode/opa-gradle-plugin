package com.bisnode.opa.process;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.tooling.TestExecutionException;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

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

            OpaOutputConsumer opaOutputConsumer = new OpaOutputConsumer(process);
            String testResultFromOpa = compileOpaOutput(opaOutputConsumer);
            int exitCode = process.waitFor();
            // Should only retrieve results after OPA process exits
            return new ProcessExecutionResult(testResultFromOpa, exitCode);
        } catch (IOException | InterruptedException e) {
            throw new TestExecutionException("Failed to start OPA process for tests", e);
        }
    }

    private String compileOpaOutput(OpaOutputConsumer opaOutputConsumer) {
        BlockingQueue<String> outputFromOpa = opaOutputConsumer.spawn();
        StringBuilder compiledOutputStr = new StringBuilder();
        try {
            String line;
            while (!(line = outputFromOpa.take()).equals(OpaOutputConsumer.POISON_PILL)) {
                compiledOutputStr.append(line);
            }
        } catch (InterruptedException e) {
            log.error("Failed to read from consumed output from OPA");
        }
        return compiledOutputStr.toString();
    }
}
