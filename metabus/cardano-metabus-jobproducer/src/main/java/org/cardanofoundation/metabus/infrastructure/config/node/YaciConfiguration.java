package org.cardanofoundation.metabus.infrastructure.config.node;

import com.bloxbean.cardano.yaci.core.common.Constants;
import com.bloxbean.cardano.yaci.core.protocol.chainsync.messages.Point;
import com.bloxbean.cardano.yaci.core.common.NetworkType;
import com.bloxbean.cardano.yaci.helper.TipFinder;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.metabus.configuration.TxProducerProperties;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@EnableConfigurationProperties(value = {TxProducerProperties.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class YaciConfiguration {
    final TxProducerProperties txProducerProperties;

    public YaciConfiguration(
            TxProducerProperties cardanoTransactionSubmitterProperties) {
        this.txProducerProperties = cardanoTransactionSubmitterProperties;
    }

    @Bean
    TipFinder tipFinder() {
        String network = txProducerProperties.getNetwork();
        return new TipFinder(txProducerProperties.getConnection().getAddress(), txProducerProperties.getConnection().getPort(), getWellKnownPoint(network), getNetwork(network).getProtocolMagic());
    }

    private static NetworkType getNetwork(String network) {
        return switch (network) {
            case "mainnet" -> NetworkType.MAINNET;
            case "preprod" -> NetworkType.PREPROD;
            case "preview" -> NetworkType.PREVIEW;
            case "testnet" -> NetworkType.LEGACY_TESTNET;
            default -> throw new RuntimeException("Invalid network string");
        };
    }

    private static Point getWellKnownPoint(String network) {
        return switch (network) {
            case "mainnet" -> Constants.WELL_KNOWN_MAINNET_POINT;
            case "preprod" -> Constants.WELL_KNOWN_PREPROD_POINT;
            case "preview" -> Constants.WELL_KNOWN_PREVIEW_POINT;
            case "testnet" -> Constants.WELL_KNOWN_TESTNET_POINT;
            default -> throw new RuntimeException("Invalid network string");
        };
    }
}
