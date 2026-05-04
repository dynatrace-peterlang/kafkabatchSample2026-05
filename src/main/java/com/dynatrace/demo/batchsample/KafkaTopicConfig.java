package com.dynatrace.demo.batchsample;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.batch.poll-timeout-ms:10000}")
    private long batchPollTimeoutMs;

    @Value("${kafka.batch.idle-between-polls-ms:2000}")
    private long batchIdleBetweenPollsMs;

    @Bean
    public NewTopic singleMessagesTopic() {
        return TopicBuilder.name("single-messages")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic batchMessagesTopic() {
        return TopicBuilder.name("batch-messages")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> batchContainerFactory(
            ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(true);
        factory.getContainerProperties().setPollTimeout(batchPollTimeoutMs);
        factory.getContainerProperties().setIdleBetweenPolls(batchIdleBetweenPollsMs);
        return factory;
    }
}
