package com.dynatrace.demo.batchsample2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.binder.kafka.KafkaListenerContainerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.List;
import java.util.function.Consumer;

@Configuration
public class DemoBatchConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(DemoBatchConsumerConfig.class);

    @Bean
    public Consumer<Message<List<String>>> demoBatchConsumer() {
        return message -> {
            List<String> payloads = message.getPayload();
            log.info("Batch received: {} messages", payloads.size());
            payloads.forEach(payload -> log.info("  -> {}", payload));
        };
    }

    @Bean
    public KafkaListenerContainerCustomizer idleBetweenPollsCustomizer(
            @Value("${demo.consumer.idle-between-polls-ms:5000}") long idleBetweenPollsMs) {
        return (container, dest, group) -> {
            if ("demo-batch-events.consumer.batchsample2".equals(group)) {
                container.getContainerProperties().setIdleBetweenPolls(idleBetweenPollsMs);
            }
        };
    }
}
