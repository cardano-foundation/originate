package org.cardanofoundation.metabus.configuration.node;

import com.bloxbean.cardano.yaci.helper.LocalClientProvider;
import com.bloxbean.cardano.yaci.helper.LocalStateQueryClient;
import com.bloxbean.cardano.yaci.helper.LocalTxMonitorClient;
import com.bloxbean.cardano.yaci.helper.LocalTxSubmissionClient;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.metabus.configuration.TxSubmitterProperties;
import org.cardanofoundation.metabus.util.NetworkUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@EnableConfigurationProperties(value = {TxSubmitterProperties.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class YaciConfiguration {
    TxSubmitterProperties txSubmitterProperties;

    public YaciConfiguration(
            TxSubmitterProperties cardanoTransactionSubmitterProperties) {
        this.txSubmitterProperties = cardanoTransactionSubmitterProperties;
    }

    @Bean
    LocalClientProvider localClientProvider() throws InterruptedException {

        var socketPath = txSubmitterProperties.getConnection().getSocket().getPath();
        log.info("[cardano-metabus-txsubmitter] node socket file path: {}", socketPath);
        var networkMagic = NetworkUtil.getNetwork(txSubmitterProperties.getNetwork()).getProtocolMagic();
        log.info("[cardano-metabus-txsubmitter] networkMagic: {}", networkMagic);

        LocalClientProvider localClientProvider = new LocalClientProvider(socketPath, networkMagic);
        localClientProvider.start();
        return localClientProvider;
    }

    @Bean
    @DependsOn("localClientProvider")
    LocalStateQueryClient localStateQueryClient(@Autowired LocalClientProvider localClientProvider) {
        return localClientProvider.getLocalStateQueryClient();
    }

    @Bean
    @DependsOn("localClientProvider")
    LocalTxSubmissionClient localTxSubmissionClient(@Autowired LocalClientProvider localClientProvider) {
        return localClientProvider.getTxSubmissionClient();
    }

    @Bean
    @DependsOn("localClientProvider")
    LocalTxMonitorClient localTxMonitorClient(@Autowired LocalClientProvider localClientProvider) {
        return localClientProvider.getTxMonitorClient();
    }
}
