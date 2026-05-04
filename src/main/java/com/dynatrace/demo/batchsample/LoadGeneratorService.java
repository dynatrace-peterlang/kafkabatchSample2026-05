package com.dynatrace.demo.batchsample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class LoadGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(LoadGeneratorService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final AtomicLong counter = new AtomicLong(0);

    private ScheduledFuture<?> currentTask;
    private volatile long intervalMs = 0;

    public LoadGeneratorService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public synchronized void start(long intervalMs) {
        if (currentTask != null && !currentTask.isCancelled()) {
            currentTask.cancel(false);
        }
        this.intervalMs = intervalMs;
        currentTask = scheduler.scheduleAtFixedRate(this::sendMessages, 0, intervalMs, TimeUnit.MILLISECONDS);
        log.info("Load generator started (intervalMs={})", intervalMs);
    }

    public synchronized void stop() {
        if (currentTask != null) {
            currentTask.cancel(false);
            currentTask = null;
            log.info("Load generator stopped");
        }
    }

    public synchronized Status getStatus() {
        boolean running = currentTask != null && !currentTask.isCancelled() && !currentTask.isDone();
        return new Status(running, intervalMs);
    }

    private void sendMessages() {
        long n = counter.incrementAndGet();
        String payload = "msg-" + n + " at " + Instant.now();
        //kafkaTemplate.send("single-messages", payload);
        kafkaTemplate.send("batch-messages", payload);
    }

    public record Status(boolean running, long intervalMs) {}
}
