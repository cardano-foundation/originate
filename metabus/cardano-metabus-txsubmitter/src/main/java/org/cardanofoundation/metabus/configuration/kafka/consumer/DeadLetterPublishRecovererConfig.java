package org.cardanofoundation.metabus.configuration.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static org.springframework.kafka.listener.DeadLetterPublishingRecoverer.HeaderNames.HeadersToAdd.*;

@Configuration
public class DeadLetterPublishRecovererConfig {
    @Value("${kafka.topics.deadLetter.name}")
    private String deadTopic;

    @Bean
    public DeadLetterPublishingRecoverer publisher(KafkaTemplate<?, ?> stringTemplate) {
        Map<Class<?>, KafkaOperations<?, ?>> templates = new LinkedHashMap<>();
        templates.put(String.class, stringTemplate);
        BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition> destinationResolver = (cr, e) -> new TopicPartition(deadTopic, cr.partition());
        CustomDeadLetterPublishRecover recoverer = new CustomDeadLetterPublishRecover(templates, destinationResolver);
        recoverer.excludeHeader(OFFSET,PARTITION, TS);
        return recoverer;
    }

}
