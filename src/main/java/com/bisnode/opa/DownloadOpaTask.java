package com.bisnode.opa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import com.bisnode.opa.configuration.OpaPlatform;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class DownloadOpaTask extends DefaultTask {

    public static final String TASK_BASE_NAME = "downloadOpa";

    @Input
    public abstract Property<String> getVersion();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    public DownloadOpaTask() {
        setGroup("opa");
        setDescription("Download OPA");
    }

    @TaskAction
    public void downloadOpa() throws IOException {
        final String downloadUrl = OpaPlatform.getPlatform().getDownloadUrl(getVersion().get());
        getLogger().info("Retrieving OPA executable from " + downloadUrl);
        final File targetFile = getOutputFile().getAsFile().get();
        getLogger().info("Saving OPA executable to " + targetFile.getAbsolutePath());
        try (FileOutputStream output = new FileOutputStream(targetFile);
             ReadableByteChannel input = Channels.newChannel(new URL(downloadUrl).openStream())) {
            output.getChannel().transferFrom(input, 0, Long.MAX_VALUE);
        }
        if (!targetFile.setReadable(true) || !targetFile.setExecutable(true)) {
            throw new IllegalStateException("Unable to set permissions on OPA executable");
        }
    }
}
