package org.cardanofoundation.metabus.configuration.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.function.BiFunction;

public class CustomDeadLetterPublishRecover extends DeadLetterPublishingRecoverer {

    private final String ORIGINAL_PARTITION = "original_partition";
    private final String ORIGINAL_OFFSET = "original_offset";

    private final String ORIGINAL_TIME_STAMP = "original_timestamp";

    private final String ORIGINAL_KEY = "original_key";


    public CustomDeadLetterPublishRecover(Map<Class<?>, KafkaOperations<? extends Object, ? extends Object>> templates, BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition> destinationResolver) {
        super(templates, destinationResolver);
    }

    @Override
    protected ProducerRecord<Object, Object> createProducerRecord(ConsumerRecord<?, ?> record, TopicPartition topicPartition, Headers headers, @Nullable byte[] key, @Nullable byte[] value) {
        String partition = Integer.toString(record.partition());
        String offset = Long.toString(record.offset());
        String timepstamp = Long.toString(record.timestamp());

        headers.add(ORIGINAL_PARTITION, partition.getBytes());
        headers.add(ORIGINAL_OFFSET,offset.getBytes());
        headers.add(ORIGINAL_TIME_STAMP, timepstamp.getBytes());
        return new ProducerRecord(topicPartition.topic(), topicPartition.partition() < 0 ? null : topicPartition.partition(), key != null ? key : record.key(), value != null ? value : record.value(), headers);
    }

}
