package com.bisnode.opa.process;

import java.util.Arrays;
import java.util.List;

public class ProcessConfiguration {
    private final String location;
    private final String srcLocation;
    private final String testSrcLocation;

    public ProcessConfiguration(String location, String srcLocation, String testSrcLocation) {
        this.location = location;
        this.srcLocation = srcLocation;
        this.testSrcLocation = testSrcLocation;
    }

    List<String> getCommandArgs() {
        return Arrays.asList(location, "test", "--format=json", srcLocation, testSrcLocation);
    }

    @Override
    public String toString() {
        return String.join(" ", getCommandArgs());
    }
}
