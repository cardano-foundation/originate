package org.cardanofoundation.metabus.unittest.service;

import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.ProtocolParams;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.common.CardanoConstants;
import com.bloxbean.cardano.client.crypto.bip32.HdKeyPair;
import com.bloxbean.cardano.client.crypto.cip1852.CIP1852;
import com.bloxbean.cardano.client.crypto.cip1852.DerivationPath;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadata;
import com.bloxbean.cardano.client.transaction.spec.*;
import com.bloxbean.cardano.yaci.core.protocol.localtx.model.TxSubmissionRequest;
import com.bloxbean.cardano.yaci.helper.LocalTxSubmissionClient;
import com.bloxbean.cardano.yaci.helper.model.TxResult;
import org.cardanofoundation.metabus.common.entities.JobJPA;
import org.cardanofoundation.metabus.common.entities.UnconfirmedTxJPA;
import org.cardanofoundation.metabus.common.enums.JobState;
import org.cardanofoundation.metabus.common.offchain.BusinessData;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.repos.JobRepository;
import org.cardanofoundation.metabus.repos.UnconfirmedTxRepository;
import org.cardanofoundation.metabus.service.impl.TransactionServiceImpl;
import org.cardanofoundation.metabus.util.ProtocolParamsUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {
    @Mock
    LocalTxSubmissionClient localTxSubmissionClient;
    @Mock
    UnconfirmedTxRepository unconfirmedTxRepository;
    @Mock
    JobRepository jobRepository;

    @InjectMocks
    TransactionServiceImpl transactionService;
    public static byte[] signature1 = new byte[]{102, 1, -112, 56, 21, -26, 82, 13, 52, -16, -4, 66, -84, 2, -4, 102, 75, 62, 74, 116, -67, 110, 77, 93, 59, -71, -118, -85, -109, 44, 55, 90, -28, 25, 125, -21, -22, 54, -27, -55, 95, 6, 107, -91, -45, -28, 43, 76, -111, 24, -32, -70, -71, -11, 52, -98, 56, -87, 33, 21, -15, -36, 108, 10};
    public static byte[] signature2 = new byte[]{100, 1, -112, 56, 21, -26, 82, 13, 52, -16, -4, 66, -84, 2, -4, 102, 75, 62, 74, 116, -67, 110, 77, 93, 59, -71, -118, -85, -109, 44, 55, 90, -28, 25, 125, -21, -22, 54, -27, -55, 95, 6, 107, -91, -45, -28, 43, 76, -111, 24, -32, -70, -71, -11, 52, -98, 56, -87, 33, 21, -15, -36, 108, 10};
    private static byte[] pubKey1 = new byte[]{56, 99, 100};
    private static byte[] pubKey2 = new byte[]{22, 1, 55};

    @Test
    void buildTransaction() {
        //Given
        String senderPaymentAddress = "addr_test1vqxnp3khzm7kcj9t23hskehat7428ghsenk0pfew4rqy5vq24rmml";
        Utxo txIn = new Utxo("be883443857c761634e754f8f1580a74e3e6182f6d3e52c28d2255bbb58e4474", 1, "addr_test1vqxnp3khzm7kcj9t23hskehat7428ghsenk0pfew4rqy5vq24rmml",
                List.of(Amount.builder().quantity(new BigInteger("9910998209")).unit(CardanoConstants.LOVELACE).build()),
                null, null, null);

        TransactionOutput txOut = TransactionOutput.builder()
                .address("addr_test1vqxnp3khzm7kcj9t23hskehat7428ghsenk0pfew4rqy5vq24rmml")
                .value(Value.builder().coin(new BigInteger("1000000")).build())
                .build();

        CBORMetadata metadata = new CBORMetadata();
        ProtocolParams protocolParams = ProtocolParams.builder()
                .minFeeA(44)
                .minFeeB(155381)
                .maxBlockSize(90112)
                .maxTxSize(16384)
                .maxBlockHeaderSize(1100)
                .nOpt(500)
                .protocolMajorVer(8)
                .protocolMinorVer(0)
                .priceMem(new BigDecimal("0.0577"))
                .priceStep(new BigDecimal("0.0000721"))
                .maxCollateralInputs(3)
                .coinsPerUtxoSize("4310")
                .build();

        //When
        BeanUtils.copyProperties(protocolParams,ProtocolParamsUtil.cachedProtocolParams);
        Transaction actual = transactionService.buildTransaction(senderPaymentAddress, List.of(txIn), txOut, metadata);

        //Then
        assertTrue(actual.isValid());
        assertNotNull(actual.getWitnessSet());
        assertNotNull(actual.getAuxiliaryData());
        assertNotNull(actual.getBody());
        assertEquals(actual.getBody().getInputs().size(), 1);
        assertEquals(actual.getBody().getOutputs().size(), 1);
    }

    private Transaction getMockTransaction() {
        Transaction transaction = new Transaction();
        TransactionBody transactionBody = new TransactionBody();
        TransactionInput transactionInput = new TransactionInput();
        TransactionOutput transactionOutput = new TransactionOutput();
        Value transactionOutputValue = new Value();

        TransactionWitnessSet transactionWitnessSet = new TransactionWitnessSet();
        AuxiliaryData auxiliaryData = new AuxiliaryData();

        transactionInput.setTransactionId("02c5a42f7e92f77af2a9ba2d540f1f46e01c89993e7db089b4e1b0e077cea5ae");
        transactionInput.setIndex(0);

        transactionOutputValue.setCoin(new BigInteger("1000000"));
        transactionOutputValue.setMultiAssets(Collections.emptyList());

        transactionOutput.setAddress("addr_test1vqxnp3khzm7kcj9t23hskehat7428ghsenk0pfew4rqy5vq24rmml");
        transactionOutput.setValue(transactionOutputValue);

        transactionBody.setInputs(Collections.singletonList(transactionInput));
        transactionBody.setOutputs(List.of(transactionOutput, transactionOutput));
        transactionBody.setFee(new BigInteger("181837"));
        transactionBody.setTtl(30616587L);

        List<Integer> listInteger = List.of(87, -117, 77, -58, 9, 50, 59, 121, 119, 101, 104, 56, 11, -59, 9, 35, 75, -90, -93, 114, 24, -4, -7, 109, -47, -111, -25, -119, -80, -112, -25, 110);
        byte[] auxiliaryDataHash = new byte[listInteger.size()];
        for (int i = 0; i < auxiliaryDataHash.length; i++) {
            auxiliaryDataHash[i] = listInteger.get(i).byteValue();
        }
        transactionBody.setAuxiliaryDataHash(auxiliaryDataHash);

        transaction.setBody(transactionBody);
        transaction.setWitnessSet(transactionWitnessSet);
        transaction.setValid(false);
        transaction.setAuxiliaryData(auxiliaryData);
        return transaction;
    }

    @Test
    void signTransaction() {
        //Given
        String mnemonic = "kit color frog trick speak employ suit sort bomb goddess jewel primary spoil fade person useless measure manage warfare reduce few scrub beyond era";
        int childIndex = 0;
        HdKeyPair hdKeyPair = new CIP1852().getKeyPairFromMnemonic(mnemonic, DerivationPath.createExternalAddressDerivationPath(childIndex));
        Transaction actual = transactionService.signTransaction(hdKeyPair, getMockTransaction());
        assertEquals(1, actual.getWitnessSet().getVkeyWitnesses().size());
    }

    @Test
    void submitTransaction() throws CborSerializationException {
        Mono<TxResult> txResultMono = Mono.just(new TxResult());

        when(localTxSubmissionClient.submitTx(any(TxSubmissionRequest.class))).thenReturn(txResultMono);

        Mono<TxResult> actual = transactionService.submitTransaction(getMockTransaction());
        assertEquals(txResultMono, actual);

    }

    @Test
    void saveUnconfirmedTx(@Mock CBORMetadata metadata) {
        //Given
        Job job1 = Job.builder()
                .businessData(BusinessData.builder().pubKey(pubKey1).signature(signature1).build())
                .id(1L).group("group").jobIndex("0").build();
        Job job2 = Job.builder()
                .businessData(BusinessData.builder().pubKey(pubKey2).signature(signature2).build())
                .id(2L).group("group").jobIndex("1").build();
        String transactionHash = "transactionHash";

        UnconfirmedTxJPA unconfirmedTxJPA = UnconfirmedTxJPA.builder()
                .txHash(transactionHash)
                .metadata("meta data").build();
        List<JobJPA> jobJPAList = List.of(JobJPA.builder()
                        .id(1L)
                        .unconfirmedTx(unconfirmedTxJPA)
                        .state(JobState.SUBMITTED)
                        .jobIndex("0")
                        .build(),
                JobJPA.builder()
                        .id(2L)
                        .unconfirmedTx(unconfirmedTxJPA)
                        .state(JobState.SUBMITTED)
                        .jobIndex("1")
                        .build());

        //When
        when(metadata.toJson()).thenReturn("meta data");

        when(unconfirmedTxRepository.save(any(UnconfirmedTxJPA.class))).thenAnswer(invocationOnMock -> {
            UnconfirmedTxJPA jpa = invocationOnMock.getArgument(0);
            assertEquals(unconfirmedTxJPA.getTxHash(), jpa.getTxHash());
            return unconfirmedTxJPA;
        });
        when(jobRepository.findAllByIdIn(any(List.class))).thenReturn(jobJPAList);
        when(jobRepository.saveAll(any(List.class))).thenReturn(jobJPAList);

        UnconfirmedTxJPA actual = transactionService.saveUnconfirmedTx(List.of(job1, job2), metadata, transactionHash);

        //Then
        assertEquals("meta data", actual.getMetadata());
        assertEquals(actual.getTxHash(), unconfirmedTxJPA.getTxHash());
        verify(jobRepository, times(1)).saveAll(jobJPAList);
        List<JobJPA> savedJobs = jobRepository.findAllByIdIn(Arrays.asList(1L, 2L));
        assertEquals(JobState.SUBMITTED, savedJobs.get(0).getState());
        assertEquals("0", savedJobs.get(0).getJobIndex());
        assertEquals(JobState.SUBMITTED, savedJobs.get(1).getState());
        assertEquals("1", savedJobs.get(1).getJobIndex());
    }
}