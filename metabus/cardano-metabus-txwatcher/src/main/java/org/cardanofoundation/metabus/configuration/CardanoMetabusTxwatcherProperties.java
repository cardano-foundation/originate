package org.cardanofoundation.metabus.configuration;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "cardano-metabus-txwatcher")
public class CardanoMetabusTxwatcherProperties {
    RabbitMQ rabbitmq;

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class RabbitMQ {
        String exchange;
        List<Binding> bindings;
        Map<String, String> subTypeRoutingKeyMapping;
        String deadLetterQueue;
        String deadLetterRoutingKey;
        String deadLetterExchange;
    }

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Binding {
        String routingKey;
        String queue;
        Long messageTTL;
        Boolean hasDLQ;
    }
}
