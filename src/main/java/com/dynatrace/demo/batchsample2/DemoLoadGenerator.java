package com.dynatrace.demo.batchsample2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class DemoLoadGenerator {
    private static final Logger log = LoggerFactory.getLogger(DemoLoadGenerator.class);
    private static final String DESTINATION = "demo-batch-events";
    private final StreamBridge streamBridge;
    private final AtomicLong counter = new AtomicLong(0);

    public DemoLoadGenerator(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Scheduled(fixedDelay = 2000)
    public void sendMessage() {
        long n = counter.incrementAndGet();
        String payload = "demo-event-" + n + " at " + Instant.now();
        boolean sent = streamBridge.send(DESTINATION, payload);
        log.info("Sent #{}: {} (accepted={})", n, payload, sent);
    }
}
