package org.cardanofoundation.metabus.configuration.kafka;

import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "kafka")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Profile("!test")
public class KafkaProperties {

    Admin admin;

    Boolean autoCreateTopics;

    Map<String, TopicConfig> topics = new HashMap<>();

    Map<String, ConsumerConfig> consumers = new HashMap<>();

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TopicConfig {
        String name;
        Integer partitions;
        Short replicationFactor;
        Map<String, String> configs = new HashMap<>();
    }

    @Getter
    @Setter
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

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Admin {
        String bootstrapServers;
    }
}