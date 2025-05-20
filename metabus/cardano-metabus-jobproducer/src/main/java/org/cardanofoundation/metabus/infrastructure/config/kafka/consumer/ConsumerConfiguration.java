package org.cardanofoundation.metabus.infrastructure.config.kafka.consumer;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.cardanofoundation.metabus.infrastructure.config.KafkaProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConsumerAwareListenerErrorHandler;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class ConsumerConfiguration {
    private static final String JSON_CONSUMER = "json-consumer";
    @Value("${kafka.consumers.json-consumer.concurrency}")
    private int CONCURRENCY_NUMBER;
    @Value("${kafka.consumers.json-consumer.pollTimeout}")
    private int POLL_TIMEOUT;
    KafkaProperties kafkaProperties;

    @Autowired
    public ConsumerConfiguration(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public Map<String, Object> consumerConfigs() throws ClassNotFoundException {
        var configs = kafkaProperties.getConsumers().get(JSON_CONSUMER);
        var props = new HashMap<String, Object>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, configs.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, configs.getGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, Class.forName(configs.getKeyDeserializer()));
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, Class.forName(configs.getValueDeserializer()));
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, configs.getAutoOffsetReset());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, configs.getEnableAutoCommit());
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, configs.getPollTimeout());
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, configs.getSessionTimeoutMs());
        props.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, configs.getAllowAutoCreateTopics());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, configs.getTrustedPackages());

        return props;
    }

    @Bean
    @Primary
    public ConsumerFactory<String, String> consumerFactory() throws ClassNotFoundException {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() throws ClassNotFoundException {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();

        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(CONCURRENCY_NUMBER);
        factory.getContainerProperties().setPollTimeout(POLL_TIMEOUT);
        factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);

        return factory;
    }

    @Bean
    public ConsumerAwareListenerErrorHandler orderListenerErrorHandler() {
        return (message, exception, consumer) -> {

            var headers = message.getHeaders();

            var currentReceivedPartitionId = headers.get(KafkaHeaders.RECEIVED_PARTITION,
                    Integer.class);

            var currentOffset = headers.get(KafkaHeaders.RECEIVED_PARTITION, Long.class);

            var receivedTopic = headers.get(KafkaHeaders.RECEIVED_TOPIC, String.class);

            var exceptionMessageFormat = MessageFormat.format(
                    "Kafka Listener error occurred : error message = {}",
                    exception.getMessage());

            log.error(exceptionMessageFormat);

            if (Objects.isNull(currentReceivedPartitionId)) {
                return Optional.empty();
            }

            if (Objects.isNull(currentOffset)) {
                return Optional.empty();
            }

            var topicPartition = new TopicPartition(receivedTopic, currentReceivedPartitionId);

            consumer.seek(topicPartition, ++currentOffset);

            return Optional.empty();
        };
    }
}
