package org.cardanofoundation.metabus.service.impl;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.api.model.ProtocolParams;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.yaci.core.model.ProtocolParamUpdate;
import com.bloxbean.cardano.yaci.core.protocol.chainsync.messages.Point;
import com.bloxbean.cardano.yaci.core.protocol.localstate.api.Era;
import com.bloxbean.cardano.yaci.core.protocol.localstate.queries.*;
import com.bloxbean.cardano.yaci.helper.LocalClientProvider;
import com.bloxbean.cardano.yaci.helper.LocalStateQueryClient;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.metabus.configuration.TxSubmitterProperties;
import org.cardanofoundation.metabus.service.LocalNodeService;
import org.cardanofoundation.metabus.util.ProtocolParamsUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@EnableConfigurationProperties(value = TxSubmitterProperties.class)
@Slf4j
public class LocalNodeServiceImpl implements LocalNodeService {
    LocalStateQueryClient localStateQueryClient;

    LocalClientProvider localClientProvider;

    TxSubmitterProperties txSubmitterProperties;

    //Try to release first before a new query to avoid stale data
    private void releaseAndAcquireSnapshot(){
        try {
            localStateQueryClient.release().block(Duration.ofSeconds(5));
        } catch (Exception e) {
            log.error("Fail to release snapshot");
        }
        try {
            localStateQueryClient.acquire().block(Duration.ofSeconds(5));
        } catch (Exception e) {
            // yaci for us keeps getting into a bad state (stuck in acquiring - see https://github.com/bloxbean/yaci/issues/19)
            // Quick fix for the pilot is to just restart the connection to yaci
            log.error("Fail to acquire snapshot");
            localClientProvider.reconnect();
        }
    }

    @Override
    public List<Utxo> queryUTXOs(Address address) {
        log.debug("Attempting to release and acquire snapshot before querying utxo with address {}",
                address.getAddress());
        releaseAndAcquireSnapshot();
        Mono<UtxoByAddressQueryResult> queryResultMono = localStateQueryClient.executeQuery(
                new UtxoByAddressQuery(address));
        UtxoByAddressQueryResult queryResult = queryResultMono.block(
                Duration.ofSeconds(20));
        List<Utxo> utxos = queryResult.getUtxoList();
        if (CollectionUtils.isEmpty(utxos)) {
            log.error(">>> There's no utxo of address {}", address.getAddress());
            return new ArrayList<>();
        }
        return utxos;
    }

    @Override
    public ProtocolParams queryProtocolParam() {
        log.debug("Attempting to release and acquire snapshot before querying protocol params");
        releaseAndAcquireSnapshot();
        Mono<CurrentProtocolParamQueryResult> mono = localStateQueryClient.executeQuery(
                new CurrentProtocolParamsQuery(Era.Conway));
        CurrentProtocolParamQueryResult queryResult = mono.block(Duration.ofSeconds(10));
        if (Objects.isNull(queryResult)) {
            String queryError = ">>> Error when query protocol param, cannot get protocol param";
            throw new RuntimeException(queryError);
        }
        ProtocolParamUpdate protocolParamsUpdate = queryResult.getProtocolParams();
        ProtocolParams pm = new ProtocolParams();
        BeanUtils.copyProperties(protocolParamsUpdate, pm);
        BigDecimal adaPerUtxoByte = new BigDecimal(protocolParamsUpdate.getAdaPerUtxoByte());
        pm.setCoinsPerUtxoSize(adaPerUtxoByte.toString());
        return pm;
    }

    @Override
    public Point queryChainPoint() {
        log.debug("Attempting to release and acquire snapshot before querying chain point");
        releaseAndAcquireSnapshot();
        Mono<ChainPointQueryResult> queryResultMono = localStateQueryClient.executeQuery(
                new ChainPointQuery());
        ChainPointQueryResult queryResult = queryResultMono.block();
        if (Objects.isNull(queryResult)) {
            String queryError = ">>> Error when query chain-point, cannot get chain-point";
            throw new RuntimeException(queryError);
        }
        return queryResult.getChainPoint();
    }

    /**
     * <p>
     * After Bean is created load the ProtocolParams into cached.
     * </p>
     */
    @PostConstruct
    private void afterInitialized() {
        ProtocolParamsUtil.loadCurrentProtocolParams(this.queryProtocolParam());
    }
}
