package com.bisnode.opa.process;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static java.nio.charset.StandardCharsets.UTF_8;

public class OpaOutputConsumer {

    public static final String POISON_PILL = "DONE_CONSUMING_OUTPUT";
    private static final Logger log = Logging.getLogger(OpaOutputConsumer.class);

    private final Process opaProcess;

    public OpaOutputConsumer(Process opaProcess) {
        this.opaProcess = opaProcess;
    }

    /**
     * Consumes OPA output in another thread and enqueues each consumed line into a queue.
     * If there's nothing left to be consumed, enqueues a {@value #POISON_PILL}
     *
     * @return A queue that will be enqueued with each consumed line from OPA, and finalised by a {@value #POISON_PILL}
     */
    public BlockingQueue<String> spawn() {
        LinkedBlockingQueue<String> outputFromProcess = new LinkedBlockingQueue<>();
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
                e.printStackTrace();
            }
        }).start();
        return outputFromProcess;
    }

}
