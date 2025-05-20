package org.cardanofoundation.metabus.configuration;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "cardano-metabus-txproducer")
public class TxProducerProperties {
    @NotNull
    Connection connection;

    @NotNull
    String network;

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Connection {
        @NotNull
        String address;

        @NotNull
        Integer port;
    }
}
