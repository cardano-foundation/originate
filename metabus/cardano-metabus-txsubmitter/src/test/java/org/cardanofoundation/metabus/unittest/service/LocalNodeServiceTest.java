package org.cardanofoundation.metabus.unittest.service;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.ProtocolParams;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.common.CardanoConstants;
import com.bloxbean.cardano.yaci.core.model.ProtocolParamUpdate;
import com.bloxbean.cardano.yaci.core.protocol.chainsync.messages.Point;
import com.bloxbean.cardano.yaci.core.protocol.localstate.api.QueryResult;
import com.bloxbean.cardano.yaci.core.protocol.localstate.queries.*;
import com.bloxbean.cardano.yaci.helper.LocalStateQueryClient;
import org.cardanofoundation.metabus.service.impl.LocalNodeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LocalNodeServiceTest {
    @Mock
    LocalStateQueryClient localStateQueryClient;
    @InjectMocks
    LocalNodeServiceImpl localNodeService;

    @Test
    void test_query_utxo() {
        Address address = new Address("addr_test1vqxnp3khzm7kcj9t23hskehat7428ghsenk0pfew4rqy5vq24rmml");
        List<Utxo> expectedUtxos = new ArrayList<>();
        expectedUtxos.add(Utxo.builder()
                .outputIndex(0)
                .txHash("db0d3b4a30c5a8027d9bf12717a156f9882e74103762a40a3343114e793eac63")
                .address("addr_test1vqxnp3khzm7kcj9t23hskehat7428ghsenk0pfew4rqy5vq24rmml")
                .amount(Collections.singletonList(Amount.builder()
                        .unit(CardanoConstants.LOVELACE)
                        .quantity(BigInteger.valueOf(1000000))
                        .build()))
                .build());
        UtxoByAddressQueryResult utxoByAddressQueryResult = new UtxoByAddressQueryResult(expectedUtxos);
        Mono<QueryResult> queryResultMono = Mono.just(utxoByAddressQueryResult);
        when(localStateQueryClient.executeQuery(any(UtxoByAddressQuery.class))).thenReturn(queryResultMono);
        when(localStateQueryClient.release()).thenReturn(Mono.empty());
        when(localStateQueryClient.acquire()).thenReturn(Mono.empty());

        List<Utxo> actualUtxos = localNodeService.queryUTXOs(address);
        assertEquals(expectedUtxos, actualUtxos);
    }

    @Test
    void test_query_utxo_with_utxo_list_is_empty() {
        Address address = new Address("addr_test1vqxnp3khzm7kcj9t23hskehat7428ghsenk0pfew4rqy5vq24rmml");

        Mono<QueryResult> queryResultMono = mock(Mono.class);
        UtxoByAddressQueryResult queryResult = mock(UtxoByAddressQueryResult.class);
        when(localStateQueryClient.executeQuery(any(UtxoByAddressQuery.class))).thenReturn(queryResultMono);
        when(queryResultMono.block(any(Duration.class))).thenReturn(queryResult);
        when(queryResult.getUtxoList()).thenReturn(Collections.emptyList());
        when(localStateQueryClient.release()).thenReturn(Mono.empty());
        when(localStateQueryClient.acquire()).thenReturn(Mono.empty());


        assertEquals(Collections.emptyList(), localNodeService.queryUTXOs(address));
    }

    @Test
    void test_protocol_params() {
        CurrentProtocolParamQueryResult currentProtocolParamQueryResult = new CurrentProtocolParamQueryResult(ProtocolParamUpdate.builder()
                .adaPerUtxoByte(BigInteger.ZERO)
                .build());
        Mono<QueryResult> queryResultMono = Mono.just(currentProtocolParamQueryResult);
        when(localStateQueryClient.release()).thenReturn(Mono.empty());
        when(localStateQueryClient.acquire()).thenReturn(Mono.empty());
        when(localStateQueryClient.executeQuery(any(CurrentProtocolParamsQuery.class))).thenReturn(queryResultMono);

        ProtocolParamUpdate protocolParamsUpdate = currentProtocolParamQueryResult.getProtocolParams();
        ProtocolParams expectedPm = new ProtocolParams();
        BeanUtils.copyProperties(protocolParamsUpdate, expectedPm);
        BigDecimal adaPerUtxoByte = new BigDecimal(protocolParamsUpdate.getAdaPerUtxoByte());
        expectedPm.setCoinsPerUtxoSize(adaPerUtxoByte.toString());

        ProtocolParams actualPm = localNodeService.queryProtocolParam();
        assertEquals(expectedPm, actualPm);
    }

    @Test
    void test_protocol_params_with_query_result_is_null() {
        Mono<QueryResult> mono = mock(Mono.class);

        when(localStateQueryClient.release()).thenReturn(Mono.empty());
        when(localStateQueryClient.acquire()).thenReturn(Mono.empty());
        when(localStateQueryClient.executeQuery(any(CurrentProtocolParamsQuery.class))).thenReturn(mono);
        when(mono.block(any(Duration.class))).thenReturn(null);

        assertThrows(RuntimeException.class, () -> localNodeService.queryProtocolParam());
    }

    @Test
    void test_query_chainpoint() {
        Point expectedPoint = Point.ORIGIN;
        Mono<QueryResult> queryResultMono = Mono.just(new ChainPointQueryResult(expectedPoint));
        when(localStateQueryClient.release()).thenReturn(Mono.empty());
        when(localStateQueryClient.acquire()).thenReturn(Mono.empty());
        Mockito.when(localStateQueryClient.executeQuery(any(ChainPointQuery.class))).thenReturn(queryResultMono);

        Point actualPoint = localNodeService.queryChainPoint();
        assertEquals(expectedPoint,actualPoint);
    }

    @Test
    void test_query_chainpoint_with_query_result_is_null() {
        Mono<QueryResult> queryResultMono = mock(Mono.class);

        when(localStateQueryClient.release()).thenReturn(Mono.empty());
        when(localStateQueryClient.acquire()).thenReturn(Mono.empty());
        when(localStateQueryClient.executeQuery(any(ChainPointQuery.class))).thenReturn(queryResultMono);
        when(queryResultMono.block()).thenReturn(null);

        assertThrows(RuntimeException.class, () -> localNodeService.queryChainPoint());

    }
}
