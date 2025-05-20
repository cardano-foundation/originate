package org.cardanofoundation.metabus.unittest.service;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.common.CardanoConstants;
import org.cardanofoundation.metabus.common.entities.UnconfirmedTxJPA;
import org.cardanofoundation.metabus.common.entities.UtxoJPA;
import org.cardanofoundation.metabus.repos.UtxoRepository;
import org.cardanofoundation.metabus.service.impl.LocalNodeServiceImpl;
import org.cardanofoundation.metabus.service.impl.UtxoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtxoServiceImplTest {
    @Mock
    LocalNodeServiceImpl localNodeService;

    @Mock
    UtxoRepository utxoRepository;

    @Captor
    ArgumentCaptor<List<UtxoJPA>> utxoArgumentCaptor;

    @InjectMocks
    UtxoServiceImpl utxoServiceImpl;

    @Test
    void getUnusedUtxosSortByAmount() {
        Address address = Mockito.mock(Address.class);
        List<Utxo> utxosFromNode = createDummyUtxoList();
        List<String> txHashes = utxosFromNode.stream().map(Utxo::getTxHash).collect(Collectors.toList());
        List<UtxoJPA> usedUtxosFromDb = createDummyUsedUtxoJPAList();

        when(localNodeService.queryUTXOs(address)).thenReturn(utxosFromNode);
        when(utxoRepository.findAllByTxHashIn(txHashes)).thenReturn(usedUtxosFromDb);

        List<Utxo> result = utxoServiceImpl.getUnusedUtxosSortByAmount(address);
        List<Utxo> expectedUnusedUtxos = new ArrayList<>();
        Utxo utxo = Utxo.builder()
                .txHash("txHash2")
                .outputIndex(1)
                .address("address2")
                .dataHash("dataHash2")
                .inlineDatum("inlineDatum2")
                .referenceScriptHash("referenceScriptHash2")
                .amount(List.of(Amount.builder()
                        .unit(CardanoConstants.LOVELACE)
                        .quantity(BigInteger.ONE)
                        .build()))
                .build();
        expectedUnusedUtxos.add(utxo);


        assertEquals(expectedUnusedUtxos, result);

    }

    private static List<Amount> createMockAmounts() {
        List<Amount> amounts = new ArrayList<>();
        Amount amount = new Amount();
        amount.setQuantity(BigInteger.ONE);
        amount.setUnit(CardanoConstants.LOVELACE);
        amounts.add(amount);
        return amounts;
    }

    private List<Utxo> createDummyUtxoList() {
        List<Utxo> utxos = new ArrayList<>();
        utxos.add(new Utxo("txHash1", 0, "address1"
                , createMockAmounts(), "dataHash1", "inlineDatum1", "referenceScriptHash1"));
        utxos.add(new Utxo("txHash2", 1, "address2"
                , createMockAmounts(), "dataHash2", "inlineDatum2", "referenceScriptHash"));
        return utxos;
    }

    private List<UtxoJPA> createDummyUsedUtxoJPAList() {
        List<UtxoJPA> usedUtxos = new ArrayList<>();

        UtxoJPA utxo1 = new UtxoJPA();
        utxo1.setAddress("address1");
        utxo1.setTxHash("txHash1");
        utxo1.setOutputIndex(0L);
        utxo1.setLovelace(BigInteger.ONE);
        utxo1.setUnconfirmedTx(new UnconfirmedTxJPA());
        usedUtxos.add(utxo1);

        return usedUtxos;
    }

    @Test
    void getGreatestUtxo() {
        List<Utxo> utxos = createDummyUtxoList();
        Optional<Utxo> result = utxoServiceImpl.getGreatestUtxo(utxos);
        Optional<Utxo> expected = Optional.of(new Utxo("txHash1", 0, "address1"
                , createMockAmounts(), "dataHash1", "inlineDatum1", "referenceScriptHash1"));

        assertEquals(expected, result);
    }

    @Test
    void saveUsedUtxo() {
        Utxo usedUtxo = new Utxo("txHash", 0, "address"
                , createMockAmounts(), "dataHash", "inlineDatum", "referenceScriptHash");
        Address address = mock(Address.class);
        when(address.getAddress()).thenReturn("address");

        UnconfirmedTxJPA unconfirmedTxJPA = new UnconfirmedTxJPA();
        utxoServiceImpl.saveUsedUtxo(List.of(usedUtxo), address, unconfirmedTxJPA);

        verify(utxoRepository, times(1)).saveAll(utxoArgumentCaptor.capture());
        List<UtxoJPA> savedUtxos = utxoArgumentCaptor.getValue();
        UtxoJPA savedUtxo = savedUtxos.get(0);

        assertEquals(usedUtxo.getTxHash(), savedUtxo.getTxHash());
        assertEquals(usedUtxo.getOutputIndex(), savedUtxo.getOutputIndex());
        assertEquals(BigInteger.valueOf(1L), savedUtxo.getLovelace());
        assertEquals(unconfirmedTxJPA, savedUtxo.getUnconfirmedTx());
        assertEquals(usedUtxo.getAddress(), savedUtxo.getAddress());
    }
}