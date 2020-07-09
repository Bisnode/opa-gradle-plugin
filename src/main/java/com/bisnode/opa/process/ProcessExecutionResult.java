package com.bisnode.opa.process;

public class ProcessExecutionResult {
    private final String output;
    private final int exitCode;

    ProcessExecutionResult(String output, int exitCode) {
        this.output = output;
        this.exitCode = exitCode;
    }

    public String getOutput() {
        return output;
    }

    public int getExitCode() {
        return exitCode;
    }
}
