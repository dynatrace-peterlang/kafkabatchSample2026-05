package com.dynatrace.demo.batchsample;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SingleMessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(SingleMessageConsumer.class);

    @KafkaListener(topics = "single-messages", groupId = "single-group")
    public void receive(ConsumerRecord<String, String> record) {
        log.info("Single received [partition={}, offset={}]: {}",
                record.partition(), record.offset(), record.value());
    }
}
