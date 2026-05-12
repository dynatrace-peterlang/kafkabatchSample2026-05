package com.dynatrace.demo.batchsample2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
public class DemoSingleConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(DemoSingleConsumerConfig.class);

    @Bean
    public Consumer<Message<String>> demoSingleConsumer() {
        return message -> log.info("Single received: {}", message.getPayload());
    }
}
