package com.dynatrace.demo.batchsample;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BatchMessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(BatchMessageConsumer.class);

    @KafkaListener(topics = "batch-messages", groupId = "batch-group",
            containerFactory = "batchContainerFactory")
    public void receive(List<ConsumerRecord<String, String>> records) {
        if (records.isEmpty()) {
            return;
        }
        int firstPartition = records.getFirst().partition();
        int lastPartition = records.getLast().partition();
        log.info("Batch received {} messages (partitions {}-{})",
                records.size(), firstPartition, lastPartition);
        for (ConsumerRecord<String, String> record : records) {
            log.debug("  [partition={}, offset={}]: {}", record.partition(), record.offset(), record.value());
        }
    }
}
