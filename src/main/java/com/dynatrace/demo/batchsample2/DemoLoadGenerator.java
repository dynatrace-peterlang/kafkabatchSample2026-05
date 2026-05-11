package com.dynatrace.demo.batchsample2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class DemoLoadGenerator {
    private static final Logger log = LoggerFactory.getLogger(DemoLoadGenerator.class);
    private static final String DESTINATION = "demo-batch-events";

    private final StreamBridge streamBridge;
    private final AtomicLong counter = new AtomicLong(0);
    private final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor();

    private volatile ScheduledFuture<?> future;
    private volatile long intervalMs;

    public DemoLoadGenerator(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public synchronized void start(long intervalMs) {
        stop();
        this.intervalMs = intervalMs;
        this.future = executor.scheduleWithFixedDelay(
                this::send, 0, intervalMs, TimeUnit.MILLISECONDS);
        log.info("Load generator started (intervalMs={})", intervalMs);
    }

    public synchronized void stop() {
        if (future != null) {
            future.cancel(false);
            future = null;
            log.info("Load generator stopped");
        }
    }

    public Map<String, Object> status() {
        return Map.of("running", future != null, "intervalMs", intervalMs);
    }

    private void send() {
        long n = counter.incrementAndGet();
        String payload = "msg-" + n + " at " + Instant.now();
        boolean sent = streamBridge.send(DESTINATION, payload);
        log.info("Sent #{}: {} (accepted={})", n, payload, sent);
    }
}
