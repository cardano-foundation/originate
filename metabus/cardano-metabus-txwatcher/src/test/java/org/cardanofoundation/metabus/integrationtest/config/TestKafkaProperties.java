package org.cardanofoundation.metabus.integrationtest.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "kafka")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Profile("!test")
public class TestKafkaProperties {

    Admin admin;

    Boolean autoCreateTopics;

    Map<String, TopicConfig> topics = new HashMap<>();

    Map<String, ProducerConfig> producers = new HashMap<>();

    Map<String, ConsumerConfig> consumers = new HashMap<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TopicConfig {
        String name;
        Integer partitions;
        Short replicationFactor;
        Map<String, String> configs = new HashMap<>();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ProducerConfig {
        String clientId;
        String bootstrapServers;
        Integer maxInFlightRequestsPerConnection;
        Integer requestTimeoutMs;
        Integer batchSize;
        Integer lingerMs;
        Integer bufferMemory;
        String acks;
        String keySerializer;
        String valueSerializer;
        Integer retries;
        Boolean enableIdempotence;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ConsumerConfig {
        String bootstrapServers;
        String clientId;
        String keyDeserializer;
        String valueDeserializer;
        String autoOffsetReset;
        Boolean enableAutoCommit;
        Integer autoCommitIntervalMs;
        Integer sessionTimeoutMs;
        String trustedPackages;
        Boolean allowAutoCreateTopics;
        Integer concurrency;
        Integer pollTimeout;
        String groupId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Admin {
        String bootstrapServers;
    }
}