package com.bisnode.opa.process;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static java.nio.charset.StandardCharsets.UTF_8;

// GH-47
public class OpaOutputConsumer {

    private static final String POISON_PILL = "DONE_CONSUMING_OUTPUT";
    private static final Logger log = Logging.getLogger(OpaOutputConsumer.class);

    private final Process opaProcess;
    private final BlockingQueue<String> outputFromProcess;

    public OpaOutputConsumer(Process opaProcess) {
        this.opaProcess = opaProcess;
        this.outputFromProcess = new LinkedBlockingQueue<>();
    }

    /**
     * Consumes OPA output in another thread and enqueues each consumed line into a queue.
     */
    public void spawn() {
        new Thread(() -> {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.opaProcess.getInputStream(), UTF_8))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    outputFromProcess.put(line);
                }
                outputFromProcess.put(POISON_PILL);
            } catch (IOException e) {
                if (!"Stream closed".equals(e.getMessage())) {
                    log.warn("IOException while reading OPA's stdout", e);
                }
            } catch (InterruptedException e) {
                log.error("Unable to read from OPA's stdout", e);
            }
        }).start();
    }

    /**
     * Reads the next output line from OPA
     *
     * @return Next output line from OPA, or null if there's no more output
     */
    public String read() {
        try {
            String line = outputFromProcess.take();
            if (line.equals(POISON_PILL)) {
                return null;
            }
            return line;
        } catch (InterruptedException e) {
            log.error("Unable to read from OPA output buffer", e);
        }
        return null;
    }

    /**
     * Reads all output line from OPA, blocks until the OPA process ends the stream
     *
     * @return All outputs lines from OPA
     */
    public List<String> readAll() {
        List<String> allOutput = new ArrayList<>();
        String line;
        while ((line = read()) != null) {
            allOutput.add(line);
        }
        return allOutput;
    }

}
