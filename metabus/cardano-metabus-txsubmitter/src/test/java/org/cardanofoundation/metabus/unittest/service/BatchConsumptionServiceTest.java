package org.cardanofoundation.metabus.unittest.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.assertj.core.util.Arrays;
import org.cardanofoundation.metabus.common.entities.JobJPA;
import org.cardanofoundation.metabus.common.entities.ScheduledBatchesJPA;
import org.cardanofoundation.metabus.common.entities.UnconfirmedTxJPA;
import org.cardanofoundation.metabus.common.enums.BatchStatus;
import org.cardanofoundation.metabus.common.enums.GroupType;
import org.cardanofoundation.metabus.common.enums.JobState;
import org.cardanofoundation.metabus.common.offchain.BusinessData;
import org.cardanofoundation.metabus.common.offchain.ConfirmingTransaction;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.common.offchain.JobBatch;
import org.cardanofoundation.metabus.common.onchain.TxMetadata;
import org.cardanofoundation.metabus.configuration.TxSubmitterProperties;
import org.cardanofoundation.metabus.configuration.TxSubmitterProperties.Wallet;
import org.cardanofoundation.metabus.configuration.kafka.KafkaProperties;
import org.cardanofoundation.metabus.configuration.kafka.KafkaProperties.TopicConfig;
import org.cardanofoundation.metabus.constants.TestConstants;
import org.cardanofoundation.metabus.factory.MetadataServiceFactory;
import org.cardanofoundation.metabus.repos.JobRepository;
import org.cardanofoundation.metabus.repos.ScheduledBatchesRepository;
import org.cardanofoundation.metabus.service.OffchainStorageService;
import org.cardanofoundation.metabus.service.QueueingService;
import org.cardanofoundation.metabus.service.TransactionService;
import org.cardanofoundation.metabus.service.UtxoService;
import org.cardanofoundation.metabus.service.WalletService;
import org.cardanofoundation.metabus.service.impl.BatchConsumptionServiceImpl;
import org.cardanofoundation.metabus.service.impl.BatchConsumptionServiceImpl.ClientWalletInfo;
import org.cardanofoundation.metabus.util.JobMapperUtil;
import org.cardanofoundation.metabus.util.ProtocolParamsUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.crypto.bip32.HdKeyPair;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.metadata.Metadata;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadata;
import com.bloxbean.cardano.client.transaction.spec.AuxiliaryData;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.transaction.spec.TransactionOutput;
import com.bloxbean.cardano.client.transaction.spec.Value;
import com.bloxbean.cardano.yaci.helper.model.TxResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.nstant.in.cbor.CborException;
import reactor.core.publisher.Mono;

/**
 * <p>
 * BatchConsumptionService Test Class
 * </p>
 *
 * @author (Sotatek) joey.dao
 * @version 0.01
 * @category Unit-test
 * @since 2023/6
 */
@ExtendWith(MockitoExtension.class)
public class BatchConsumptionServiceTest {

    /**
     * TransactionService mock object
     */
    @Mock
    private TransactionService transactionService;

    /**
     * WalletService mock object
     */
    @Mock
    private WalletService walletService;

    /**
     * UtxoService mock object
     */
    @Mock
    private UtxoService utxoService;

    /**
     * MetadataServiceFactory mock object
     */
    @Mock
    private MetadataServiceFactory metadataServiceFactory;

    /**
     * OffchainStorageService mock object
     */
    @Mock
    private OffchainStorageService offchainStorageService;

    /**
     * ObjectMapper mock object
     */
    @Mock
    private ObjectMapper objectMapper;

    /**
     * The properties file
     */
    @Mock
    private TxSubmitterProperties txSubmitterProperties;

    /**
     * ScheduledBatchesRepository mock object
     */
    @Mock
    private ScheduledBatchesRepository scheduledBatchesRepository;

    /**
     * The wallet mock object
     */
    @Mock
    private Wallet wallet;

    /**
     * The Job Repository mock object
     */
    @Mock
    private JobRepository jobRepository;

    /**
     * Kafka Properties mock object
     */
    @Mock
    private KafkaProperties kafkaProperties;

    /**
     * Queue Service mock object
     */
    @Mock
    private QueueingService queueingService;

    /**
     * The target test class
     */
    @Spy
    @InjectMocks
    private BatchConsumptionServiceImpl serviceImplMock;

    private String mnemonic;

    /**
     * Tx Hash Constant
     */
    private final static String TX_HASH = "8507ce604eed3d65f5d0d3eaa3208b393bd39268242903c893b576e09ccccd9b";

    private static final byte[] jwsHeader = new byte[]{102, 1, -112, 56, 21, -26, 82, 13, 52, -16, -4, 66, -84, 2, -4, 102, 75, 62, 74, 116, -67, 110, 77, 93, 59, -71, -118, -85, -109, 44, 55, 90, -28, 25, 125, -21, -22, 54, -27, -55, 95, 6, 107, -91, -45, -28, 43, 76, -111, 24, -32, -70, -71, -11, 52, -98, 56, -87, 33, 21, -15, -36, 108, 10};


    @BeforeAll
    static void beforeClass() {
        ProtocolParamsUtil.cachedProtocolParams.setMaxTxSize(1000);
        ProtocolParamsUtil.cachedProtocolParams.setMinUtxo("100");
        ProtocolParamsUtil.cachedProtocolParams.setMinFeeA(1);
        ProtocolParamsUtil.cachedProtocolParams.setMinFeeB(1);
        ProtocolParamsUtil.cachedProtocolParams.setCoinsPerUtxoSize("4310");
    }

    @BeforeEach
    void setUp() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        final Field batchConsumptionBoundaryTime = BatchConsumptionServiceImpl.class
                .getDeclaredField("batchConsumptionBoundaryTime");
        batchConsumptionBoundaryTime.setAccessible(true);
        batchConsumptionBoundaryTime.set(serviceImplMock, BigInteger.valueOf(600000));
    }

    /**
     * <p>
     * Description:
     * Cannot find the scheduled batches.
     * </p>
     *
     * @throws Exception
     */
    @Test
    void test_consumeBasedOnTime_001() throws Exception {
        when(scheduledBatchesRepository.findAllByBatchStatus(BatchStatus.PENDING)).thenReturn(Collections.emptyList());
        serviceImplMock.consumeBasedOnTime();
        verify(serviceImplMock, times(0)).submitBatchToNode(any(), any(), any(), anyBoolean());
    }

    /**
     * <p>
     * Description:
     * Can find the scheduled batches.
     * </p>
     *
     * @throws Exception
     */
    @Test
    void test_consumeBasedOnTime_002() throws Exception {
        final List<ScheduledBatchesJPA> fakeSchedule = generateFakeScheduleBatches();

        when(scheduledBatchesRepository.findAllByBatchStatus(BatchStatus.PENDING)).thenReturn(fakeSchedule);
        doNothing().when(serviceImplMock).submitBatchToNode(any(), any(), any(), anyBoolean());

        serviceImplMock.consumeBasedOnTime();

        verify(serviceImplMock, times(1)).submitBatchToNode(null, "JOB_TYPE_A", fakeSchedule.get(0), true);
        verify(serviceImplMock, times(0)).submitBatchToNode(null, "JOB_TYPE_B", fakeSchedule.get(1), true);
        verify(serviceImplMock, times(1)).submitBatchToNode(null, "JOB_TYPE_C", fakeSchedule.get(2), true);
    }

    /**
     * <p>
     * Description:
     * Can find the scheduled batches.
     * </p>
     *
     * @throws Exception
     */
    @Test
    void test_consumeBasedOnTxMaxSize_001() throws Exception {
        final ConsumerRecord<String, Job> fakeConsumerRecord = generateFakeConsumerRecord();
        final Job fakeJob = fakeConsumerRecord.value();

        final List<ScheduledBatchesJPA> fakeSchedule = generateFakeScheduleBatches();

        doReturn(List.of(fakeSchedule.get(0))).when(scheduledBatchesRepository)
                .findByJobType(fakeJob.getBusinessData().getType());
        doNothing().when(serviceImplMock).submitBatchToNode(fakeJob, fakeJob.getBusinessData().getType(),
                fakeSchedule.get(0), false);

        serviceImplMock.consumeBasedOnTxMaxSize(fakeConsumerRecord);

        verify(serviceImplMock, times(1)).submitBatchToNode(fakeJob, fakeJob.getBusinessData().getType(),
                fakeSchedule.get(0), false);
    }

    /**
     * <p>
     * Description:
     * the scheduled batch is processing.
     * </p>
     *
     * @throws Exception
     */
    @Test
    void test_consumeBasedOnTxMaxSize_002() throws Exception {
        final ConsumerRecord<String, Job> fakeConsumerRecord = generateFakeConsumerRecord();
        final Job fakeJob = fakeConsumerRecord.value();

        final List<ScheduledBatchesJPA> fakeSchedule = generateFakeScheduleBatches();
        fakeSchedule.get(0).setBatchStatus(BatchStatus.PROCESSING);

        doReturn(List.of(fakeSchedule.get(0))).when(scheduledBatchesRepository)
                .findByJobType(fakeJob.getBusinessData().getType());
        doReturn(true).when(serviceImplMock).isBatchOfJobTypesProcessing(fakeSchedule.get(0));

        serviceImplMock.consumeBasedOnTxMaxSize(fakeConsumerRecord);

        verify(serviceImplMock, times(0)).submitBatchToNode(fakeJob, fakeJob.getBusinessData().getType(),
                fakeSchedule.get(0), false);
    }

    /**
     * <p>
     * Description:
     * the scheduled batch list is empty
     * </p>
     *
     * @throws Exception
     */
    @Test
    void test_consumeBasedOnTxMaxSize_003() throws Exception {
        final ConsumerRecord<String, Job> fakeConsumerRecord = generateFakeConsumerRecord();
        final List<ScheduledBatchesJPA> fakeSchedule = Collections.emptyList();
        final Job fakeJob = fakeConsumerRecord.value();

        doReturn(fakeSchedule).when(scheduledBatchesRepository).findByJobType(fakeJob.getBusinessData().getType());
        doReturn(true).when(serviceImplMock).isBatchOfJobTypesProcessing(null);
        serviceImplMock.consumeBasedOnTxMaxSize(fakeConsumerRecord);

        verify(serviceImplMock, times(0)).submitBatchToNode(fakeJob, fakeJob.getBusinessData().getType(),
                null, false);
    }

    /**
     * <p>
     * Description:
     * the batch submission phase throws exception.
     * </p>
     *
     * @throws Exception
     */
    @Test
    void test_consumeBasedOnTxMaxSize_004() throws Exception {
        final ConsumerRecord<String, Job> fakeConsumerRecord = generateFakeConsumerRecord();
        final Job fakeJob = fakeConsumerRecord.value();

        final List<ScheduledBatchesJPA> fakeSchedule = generateFakeScheduleBatches();

        doReturn(List.of(fakeSchedule.get(0))).when(scheduledBatchesRepository)
                .findByJobType(fakeJob.getBusinessData().getType());
        doThrow(RuntimeException.class).when(serviceImplMock).submitBatchToNode(fakeJob,
                fakeJob.getBusinessData().getType(),
                fakeSchedule.get(0), false);

        assertThrows(Exception.class, () -> serviceImplMock.consumeBasedOnTxMaxSize(fakeConsumerRecord));

        verify(serviceImplMock, times(1)).submitBatchToNode(fakeJob, fakeJob.getBusinessData().getType(),
                fakeSchedule.get(0), false);
    }

    /**
     * <p>
     * Description:
     * the scheduled batch is not processing.
     * </p>
     *
     * @throws Exception
     */
    @Test
    void test_isBatchOfJobTypesProcessing_001() throws Exception {
        final ScheduledBatchesJPA fakeConsumerRecord = new ScheduledBatchesJPA(1L, "JOB_TYPE_A",
                BatchStatus.PENDING,
                Instant.now().minusMillis(10000000));

        final boolean result = serviceImplMock.isBatchOfJobTypesProcessing(fakeConsumerRecord);

        assertFalse(result);
    }

    /**
     * <p>
     * Description:
     * the scheduled batch is processing.
     * </p>
     *
     * @throws Exception
     */
    @Test
    void test_isBatchOfJobTypesProcessing_002() throws Exception {
        final ScheduledBatchesJPA fakeConsumerRecord = new ScheduledBatchesJPA(1L, "JOB_TYPE_A",
                BatchStatus.PROCESSING,
                Instant.now().minusMillis(10000000));

        final boolean result = serviceImplMock.isBatchOfJobTypesProcessing(fakeConsumerRecord);

        assertTrue(result);
    }

    /**
     * <p>
     * Description:
     * the scheduled batch is null.
     * </p>
     *
     * @throws Exception
     */
    @Test
    void test_isBatchOfJobTypesProcessing_003() throws Exception {
        final ScheduledBatchesJPA fakeConsumerRecord = null;

        final boolean result = serviceImplMock.isBatchOfJobTypesProcessing(fakeConsumerRecord);

        assertFalse(result);
    }

    /**
     * <p>
     * Description:
     * the scheduled batch is null.
     * </p>
     *
     * @throws Exception
     */
    @Test
    void test_createJobBatch_001() throws Exception {
        final Job fakeCurrentJob = generateFakeJob(1);
        final Job[] fakePendingJobs = generateListJob(2);

        final JobBatch createdJobBatch = serviceImplMock.createJobBatch("JOB_TYPE_A", fakeCurrentJob,
                fakePendingJobs);
        assertNotNull(createdJobBatch);
        assertEquals(3, createdJobBatch.getJobs().size());
    }

    /**
     * <p>
     * Description:
     * the current job and pending jobs is null.
     * </p>
     *
     * @throws Exception
     */
    @Test
    void test_createJobBatch_002() throws Exception {
        final Job fakeCurrentJob = null;
        final Job[] fakePendingJobs = null;

        final JobBatch createdJobBatch = serviceImplMock.createJobBatch("JOB_TYPE_A", fakeCurrentJob,
                fakePendingJobs);
        assertNotNull(createdJobBatch);
        assertEquals(0, createdJobBatch.getJobs().size());
    }

    /**
     * <p>
     * Description:
     * the pending jobs is null and consuming job is not null
     * </p>
     *
     * @throws Exception
     */
    @Test
    void test_createJobBatch_003() throws Exception {
        final Job fakeCurrentJob = generateFakeJob(1);
        final Job[] fakePendingJobs = new Job[] {};

        final JobBatch createdJobBatch = serviceImplMock.createJobBatch("JOB_TYPE_A", fakeCurrentJob,
                fakePendingJobs);
        assertNotNull(createdJobBatch);
        assertEquals(1, createdJobBatch.getJobs().size());
    }

    /**
     * <p>
     * Description:
     * the pending jobs is not null and consuming job is null
     * </p>
     *
     * @throws Exception
     */
    @Test
    void test_createJobBatch_004() throws Exception {
        final Job fakeCurrentJob = null;
        final Job[] fakePendingJobs = generateListJob(1);

        final JobBatch createdJobBatch = serviceImplMock.createJobBatch("JOB_TYPE_A:sub_type", fakeCurrentJob,
                fakePendingJobs);
        assertNotNull(createdJobBatch);
        assertEquals(1, createdJobBatch.getJobs().size());
        assertEquals(GroupType.SINGLE_GROUP, createdJobBatch.getGroupType());
    }

    /**
     * <p>
     * Description:
     * Update the unconfirmedTx to database success.
     * </p>
     *
     * @throws Exception
     */
    @Test
    void test_updateUnconfirmedTxToDatabase_001() throws Exception {
        final List<Job> fakePendingJobs = Arrays.nonNullElementsIn(generateListJob(1));
        final Address address = mock(Address.class);
        final CBORMetadata metadata = new CBORMetadata();
        final UnconfirmedTxJPA unconfirmedTxJPA = new UnconfirmedTxJPA();
        final Utxo txIn = new Utxo(TX_HASH, 0, "fake Address", Collections.emptyList(), TX_HASH, mnemonic,
                TX_HASH);

        doReturn(unconfirmedTxJPA).when(transactionService).saveUnconfirmedTx(fakePendingJobs, metadata,
                TX_HASH);
        final List<Utxo> txInList = List.of(txIn);
        doNothing().when(utxoService).saveUsedUtxo(txInList, address, unconfirmedTxJPA);
        serviceImplMock.updateUnconfirmedTxToDatabase(fakePendingJobs, address, txInList, metadata, TX_HASH);

        verify(transactionService, times(1)).saveUnconfirmedTx(fakePendingJobs, metadata, TX_HASH);
        verify(utxoService, times(1)).saveUsedUtxo(txInList, address, unconfirmedTxJPA);
    }

    /**
     * <p>
     * Description:
     * Test with an empty walletAddresses map. Verify that the method returns null.
     * </p>
     *
     * @throws JsonProcessingException
     */
    @Test
    void testGetAppropriateTxIn_emptyWalletAddresses()
            throws JsonProcessingException, CborException, AddressExcepion, CborSerializationException {
        final Map<Address, HdKeyPair> walletAddresses = new LinkedHashMap<>();
        final ClientWalletInfo result = serviceImplMock.getAppropriateTxIn(walletAddresses);

        assertNull(result);
    }

    /**
     * <p>
     * Description:
     * Test with a non-empty walletAddresses map where the
     * utxoService.getUnusedUtxos method
     * returns an empty list for all addresses. Verify that the method returns null.
     * </p>
     *
     * @throws JsonProcessingException
     */
    @Test
    void testGetAppropriateTxIn_emptyUnusedUtxos()
            throws JsonProcessingException, CborException, AddressExcepion, CborSerializationException {
        final Address address = mock(Address.class);
        final HdKeyPair firstChildKeyPair = new HdKeyPair(null, null, "fake path");

        final Map<Address, HdKeyPair> walletAddresses = new LinkedHashMap<>();
        walletAddresses.put(address, firstChildKeyPair);

        when(utxoService.getUnusedUtxosSortByAmount(address)).thenReturn(new ArrayList<>());

        final ClientWalletInfo result = serviceImplMock.getAppropriateTxIn(walletAddresses);

        assertNull(result);
    }

    /**
     * <p>
     * Test with a non-empty walletAddresses map where
     * the utxoService.getUnusedUtxos method returns a non-empty list for at least
     * one address,
     * but the utxoService.getGreatestUtxo method returns an empty Optional for all
     * lists of UTXOs.
     * Verify that the method returns null.
     * </p>
     *
     * @throws JsonProcessingException
     */
    @Test
    void testGetAppropriateTxIn_emptyGreatestUtxo()
            throws JsonProcessingException, CborException, AddressExcepion, CborSerializationException {
        final Address address = mock(Address.class);
        final HdKeyPair firstChildKeyPair = new HdKeyPair(null, null, "fake path");
        final Map<Address, HdKeyPair> walletAddresses = new LinkedHashMap<>();
        walletAddresses.put(address, firstChildKeyPair);

        final Utxo utxo = new Utxo();
        utxo.setAmount(List.of(Amount.builder().quantity(new BigInteger("10")).build()));
        final List<Utxo> unusedUtxos = new ArrayList<>();
        unusedUtxos.add(utxo);

        when(utxoService.getUnusedUtxosSortByAmount(address)).thenReturn(unusedUtxos);

        final ClientWalletInfo result = serviceImplMock.getAppropriateTxIn(walletAddresses);

        assertNull(result);
    }

    /**
     * <p>
     * Description:
     * Test with a non-empty walletAddresses map where the
     * utxoService.getUnusedUtxos method returns a non-empty list for at least one
     * address,
     * and the utxoService.getGreatestUtxo method returns a non-empty Optional for
     * at least one list of UTXOs,
     * but the amount of the returned UTXO is not valid according to the
     * ProtocolParamsUtil.isTxInValid method.
     * Verify that the method returns null.
     * </p>
     *
     * @throws JsonProcessingException
     */
    @Test
    void testGetAppropriateTxIn_invalidUtxoAmount()
            throws JsonProcessingException, CborException, AddressExcepion, CborSerializationException {
        final Address address = mock(Address.class);
        final HdKeyPair firstChildKeyPair = new HdKeyPair(null, null, "fake path");
        final Map<Address, HdKeyPair> walletAddresses = new LinkedHashMap<>();
        walletAddresses.put(address, firstChildKeyPair);
        final List<Amount> amount = new ArrayList<>();
        amount.add(new Amount("lovelace", BigInteger.valueOf(0)));

        final Utxo utxo = new Utxo(TX_HASH, 0, "fake Address", amount, TX_HASH, mnemonic,
                TX_HASH);
        final List<Utxo> unusedUtxos = new ArrayList<>();
        unusedUtxos.add(utxo);

        when(utxoService.getUnusedUtxosSortByAmount(address)).thenReturn(unusedUtxos);

        final ClientWalletInfo result = serviceImplMock.getAppropriateTxIn(walletAddresses);

        assertNull(result);
    }

    /**
     * <p>
     * Description:
     * Test with a non-empty walletAddresses map where the
     * utxoService.getUnusedUtxos method returns a non-empty list for at least one
     * address,
     * and the utxoService.getGreatestUtxo method returns a non-empty Optional for
     * at least one list of UTXOs,
     * and the amount of the returned UTXO is valid according to the
     * ProtocolParamsUtil.isTxInValid method.
     * Verify that the method returns a non-null ClientWalletInfo object with the
     * expected values.
     * </p>
     *
     * @throws JsonProcessingException
     */
    @Test
    void testGetAppropriateTxIn_validUtxoAmount()
            throws JsonProcessingException, CborException, AddressExcepion, CborSerializationException {
        final Address address = mock(Address.class);
        final Address address2 = mock(Address.class);
        final HdKeyPair childKeyPair = new HdKeyPair(null, null, "fake path");
        final HdKeyPair childKeyPair2 = new HdKeyPair(null, null, "fake path2");

        final Map<Address, HdKeyPair> walletAddresses = new LinkedHashMap<>();
        walletAddresses.put(address, childKeyPair);
        walletAddresses.put(address2, childKeyPair2);

        final List<Amount> amount = new ArrayList<>();
        amount.add(new Amount("lovelace", BigInteger.valueOf(1L)));
        final List<Amount> amount2 = new ArrayList<>();
        amount2.add(new Amount("lovelace", BigInteger.valueOf(1000000000000000L)));

        final Utxo utxo = new Utxo(TX_HASH, 0, "fake Address", amount, TX_HASH, mnemonic, TX_HASH);
        final Utxo utxo2 = new Utxo(TX_HASH, 0, "fake Address2", amount2, TX_HASH, mnemonic, TX_HASH);

        final List<Utxo> unusedUtxos = new ArrayList<>();
        unusedUtxos.add(utxo);

        final List<Utxo> unusedUtxos2 = new ArrayList<>();
        unusedUtxos2.add(utxo);
        unusedUtxos2.add(utxo2);

        ProtocolParamsUtil.cachedProtocolParams.setMinFeeA(1);
        ProtocolParamsUtil.cachedProtocolParams.setMinFeeB(1);
        ProtocolParamsUtil.cachedProtocolParams.setMaxTxSize(10);
        ProtocolParamsUtil.cachedProtocolParams.setMinUtxo("10");

        when(utxoService.getUnusedUtxosSortByAmount(address)).thenReturn(unusedUtxos);
        when(utxoService.getUnusedUtxosSortByAmount(address2)).thenReturn(unusedUtxos2);

        final ClientWalletInfo result = serviceImplMock.getAppropriateTxIn(walletAddresses);

        assertNotNull(result);
        verify(utxoService, times(1)).getUnusedUtxosSortByAmount(address);
        verify(utxoService, times(1)).getUnusedUtxosSortByAmount(address);
        assertEquals(childKeyPair2, result.getHdKeyPair());
        assertEquals(address2, result.getAddress());
        assertEquals(utxo2, result.getUtxos().get(0));
    }

    /**
     * <p>
     * Description:
     * Normal case - The batch is submitted successfully
     * </p>
     *
     * @throws Exception
     */
    @Test
    void test_submitBatchToNode_001() throws Exception {
        final Job fakeCurrentJob = generateFakeJob(1);

        final String jobType = "JOB_TYPE_A";
        final ScheduledBatchesJPA scheduledBatchesJPA = new ScheduledBatchesJPA(1L, "JOB_TYPE_A",
                BatchStatus.PENDING,
                Instant.now().minusMillis(10000000));
        final List<JobJPA> pendingJobs = Arrays.nonNullElementsIn(generateListJob(1)).stream()
                .map(JobMapperUtil::toJobJpa).collect(Collectors.toList());

        final Address address = mock(Address.class);
        final Utxo txIn = new Utxo(TX_HASH, 0, "fake Address", Collections.emptyList(), TX_HASH, mnemonic,
                TX_HASH);
        final HdKeyPair firstChildKeyPair = new HdKeyPair(null, null, "fake path");
        final Map<Address, HdKeyPair> walletInfo = new LinkedHashMap<>();
        walletInfo.put(address, firstChildKeyPair);
        final ClientWalletInfo clientWalletInfo = generateFakeClientWalletInfo(address, firstChildKeyPair);
        final AuxiliaryData auxiliaryData = new AuxiliaryData();
        final Metadata metadata = new CBORMetadata(new co.nstant.in.cbor.model.Map());
        auxiliaryData.setMetadata(metadata);
        final List<Utxo> txInList = List.of(txIn);
        clientWalletInfo.setUtxos(txInList);

        final Transaction tx = new Transaction();
        tx.setAuxiliaryData((auxiliaryData));
        final Mono<TxResult> txResultMono = Mono
                .just(TxResult.builder().accepted(true).txHash(TX_HASH).build());
        final UnconfirmedTxJPA unconfirmedTxJPA = UnconfirmedTxJPA.builder().createdDate(Instant.now()).build();

        doReturn(scheduledBatchesJPA).when(serviceImplMock).updateScheduledBatch(scheduledBatchesJPA,
                BatchStatus.PROCESSING, jobType);
        doReturn(pendingJobs).when(jobRepository).findAllByStateAndType(JobState.PENDING, jobType);
        doReturn(tx).when(serviceImplMock).createTransaction(eq(address), eq(firstChildKeyPair),
                any(JobBatch.class),
                eq(txInList),
                eq(false));
        doReturn(txResultMono).when(transactionService).submitTransaction(tx);
        doReturn(clientWalletInfo).when(serviceImplMock).getTheClientWalletInfo(any());
        doReturn(unconfirmedTxJPA).when(serviceImplMock).updateUnconfirmedTxToDatabase(any(), eq(address), eq(txInList),
                any(), eq(TX_HASH));
        doNothing().when(serviceImplMock).sendConfirmingJobToProducer(eq(TX_HASH), any(),
                eq(unconfirmedTxJPA.getCreatedDate()));

        mockSetProperties();
        serviceImplMock.setProperties();
        serviceImplMock.submitBatchToNode(fakeCurrentJob, jobType, scheduledBatchesJPA, false);

        verify(transactionService).submitTransaction(tx);

        verify(serviceImplMock, times(1)).updateUnconfirmedTxToDatabase(any(), eq(address), eq(txInList),
                eq((CBORMetadata) metadata),
                eq(txResultMono.block().getTxHash()));
        verify(serviceImplMock, times(1)).updateScheduledBatch(scheduledBatchesJPA, BatchStatus.NONE,
                "JOB_TYPE_A");
    }

    /**
     * <p>
     * Description:
     * Abnormal case - The collected pending jobs is empty
     * </p>
     *
     * @throws Exception
     */
    @Test
    void test_submitBatchToNode_002() throws Exception {
        final Job fakeCurrentJob = generateFakeJob(1);

        final String jobType = "JOB_TYPE_A";
        final ScheduledBatchesJPA scheduledBatchesJPA = new ScheduledBatchesJPA(1L, "JOB_TYPE_A",
                BatchStatus.PENDING,
                Instant.now().minusMillis(10000000));
        final List<JobJPA> pendingJobs = Arrays.nonNullElementsIn(generateListJob(1)).stream()
                .map(JobMapperUtil::toJobJpa).collect(Collectors.toList());

        doReturn(scheduledBatchesJPA).when(serviceImplMock).updateScheduledBatch(scheduledBatchesJPA,
                BatchStatus.PROCESSING, jobType);
        doReturn(pendingJobs).when(jobRepository).findAllByStateAndType(JobState.PENDING, jobType);
        doReturn(JobBatch.builder().jobs(Collections.emptyList()).build()).when(serviceImplMock)
                .createJobBatch(eq("JOB_TYPE_A"), eq(fakeCurrentJob), any());

        serviceImplMock.submitBatchToNode(fakeCurrentJob, jobType, scheduledBatchesJPA, false);

        verify(serviceImplMock, times(1)).updateScheduledBatch(scheduledBatchesJPA, BatchStatus.PROCESSING,
                "JOB_TYPE_A");
        verify(serviceImplMock, times(1)).updateScheduledBatch(scheduledBatchesJPA, BatchStatus.NONE,
                "JOB_TYPE_A");
        verify(walletService, times(0)).getFirstNChildKeyPairs(any(), eq(0));
        verify(serviceImplMock, times(0)).createTransaction(any(), any(), any(), any(), anyBoolean());
    }

    /**
     * <p>
     * Description:
     * Abnormal case - Can not get the Appropriate TxIn
     * </p>
     *
     * @throws Exception
     */
    @Test
    void test_submitBatchToNode_003() throws Exception {
        final String jobType = "JOB_TYPE_A";
        final ScheduledBatchesJPA scheduledBatchesJPA = new ScheduledBatchesJPA(1L, "JOB_TYPE_A",
                BatchStatus.PENDING,
                Instant.now().minusMillis(10000000));
        final List<JobJPA> pendingJobs = Arrays.nonNullElementsIn(generateListJob(1)).stream()
                .map(JobMapperUtil::toJobJpa).collect(Collectors.toList());

        final Transaction tx = new Transaction();

        doReturn(scheduledBatchesJPA).when(serviceImplMock).updateScheduledBatch(scheduledBatchesJPA,
                BatchStatus.PROCESSING, jobType);
        doReturn(pendingJobs).when(jobRepository).findAllByStateAndType(JobState.PENDING, jobType);

        doReturn(null).when(serviceImplMock).getTheClientWalletInfo(any());

        serviceImplMock.submitBatchToNode(null, jobType, scheduledBatchesJPA, false);

        verify(transactionService, times(0)).submitTransaction(tx);
    }

    /**
     * <p>
     * Description:
     * Abnormal case - Can not create the transaction object.
     * </p>
     *
     * @throws Exception
     */
    @Test
    void test_submitBatchToNode_004() throws Exception {
        final String jobType = "JOB_TYPE_A";
        final ScheduledBatchesJPA scheduledBatchesJPA = new ScheduledBatchesJPA(1L, "JOB_TYPE_A",
                BatchStatus.PENDING,
                Instant.now().minusMillis(10000000));
        final List<JobJPA> pendingJobs = Arrays.nonNullElementsIn(generateListJob(1)).stream()
                .map(JobMapperUtil::toJobJpa).collect(Collectors.toList());

        final Address address = mock(Address.class);
        final Utxo txIn = new Utxo(TX_HASH, 0, "fake Address", Collections.emptyList(), TX_HASH, mnemonic,
                TX_HASH);
        final HdKeyPair firstChildKeyPair = new HdKeyPair(null, null, "fake path");

        final Map<Address, HdKeyPair> walletInfo = new LinkedHashMap<>();
        walletInfo.put(address, firstChildKeyPair);
        final ClientWalletInfo clientInfo = serviceImplMock.new ClientWalletInfo(firstChildKeyPair, address);
        final List<Utxo> txInList = List.of(txIn);
        clientInfo.setUtxos(txInList);
        final Transaction tx = new Transaction();

        doReturn(scheduledBatchesJPA).when(serviceImplMock).updateScheduledBatch(scheduledBatchesJPA,
                BatchStatus.PROCESSING, jobType);
        doReturn(pendingJobs).when(jobRepository).findAllByStateAndType(JobState.PENDING, jobType);
        doReturn(clientInfo).when(serviceImplMock).getTheClientWalletInfo(any());
        doReturn(null).when(serviceImplMock).createTransaction(eq(address), eq(firstChildKeyPair),
                any(JobBatch.class),
                eq(txInList),
                eq(false));

        serviceImplMock.submitBatchToNode(null, jobType, scheduledBatchesJPA, false);

        verify(transactionService, times(0)).submitTransaction(tx);
    }

    /**
     * <p>
     * Description:
     * Abnormal case - Can not submit the transaction object to the node.
     * </p>
     *
     * @throws Exception
     */
    @Test
    void test_submitBatchToNode_005() throws Exception {
        final String jobType = "JOB_TYPE_A";
        final ScheduledBatchesJPA scheduledBatchesJPA = new ScheduledBatchesJPA(1L, "JOB_TYPE_A",
                BatchStatus.PENDING,
                Instant.now().minusMillis(10000000));
        final List<JobJPA> pendingJobs = Arrays.nonNullElementsIn(generateListJob(1)).stream()
                .map(JobMapperUtil::toJobJpa).collect(Collectors.toList());

        final Address address = mock(Address.class);
        final Utxo txIn = new Utxo(TX_HASH, 0, "fake Address", Collections.emptyList(), TX_HASH, mnemonic,
                TX_HASH);
        final HdKeyPair firstChildKeyPair = new HdKeyPair(null, null, "fake path");
        final Map<Address, HdKeyPair> walletInfo = new LinkedHashMap<>();
        walletInfo.put(address, firstChildKeyPair);
        final ClientWalletInfo clientInfo = serviceImplMock.new ClientWalletInfo(firstChildKeyPair, address);
        final List<Utxo> txInList = List.of(txIn);
        clientInfo.setUtxos(txInList);

        final AuxiliaryData auxiliaryData = new AuxiliaryData();
        final Metadata metadata = new CBORMetadata(new co.nstant.in.cbor.model.Map());
        auxiliaryData.setMetadata(metadata);

        final Transaction tx = new Transaction();
        tx.setAuxiliaryData((auxiliaryData));
        final Mono<TxResult> txResultMono = Mono
                .just(TxResult.builder().accepted(false).txHash(TX_HASH).build());

        doReturn(scheduledBatchesJPA).when(serviceImplMock).updateScheduledBatch(scheduledBatchesJPA,
                BatchStatus.PROCESSING, jobType);
        doReturn(pendingJobs).when(jobRepository).findAllByStateAndType(JobState.PENDING, jobType);

        doReturn(clientInfo).when(serviceImplMock).getTheClientWalletInfo(any());
        doReturn(tx).when(serviceImplMock).createTransaction(eq(address), eq(firstChildKeyPair),
                any(JobBatch.class),
                eq(txInList),
                eq(false));
        doReturn(txResultMono).when(transactionService).submitTransaction(tx);

        mockSetProperties();
        serviceImplMock.setProperties();
        serviceImplMock.submitBatchToNode(null, jobType, scheduledBatchesJPA, false);

        verify(transactionService).submitTransaction(tx);
        verify(serviceImplMock, times(0)).updateUnconfirmedTxToDatabase(any(), eq(address), eq(txInList), any(),
                eq(TX_HASH));
        verify(serviceImplMock, times(1)).updateScheduledBatch(scheduledBatchesJPA, BatchStatus.PENDING,
                "JOB_TYPE_A");
    }

    /**
     * <p>
     * Description:
     * Abnormal case - JsonProcessingException occurs
     * </p>
     *
     * @throws Exception
     */
    @Test
    void test_submitBatchToNode_006() throws Exception {
        final String jobType = "JOB_TYPE_A";
        final ScheduledBatchesJPA scheduledBatchesJPA = new ScheduledBatchesJPA(1L, "JOB_TYPE_A",
                BatchStatus.PENDING,
                Instant.now().minusMillis(10000000));

        doThrow(JsonProcessingException.class).when(objectMapper).writeValueAsString(scheduledBatchesJPA);
        assertThrows(JsonProcessingException.class,
                () -> serviceImplMock.submitBatchToNode(null, jobType, scheduledBatchesJPA, false));
    }

    /**
     * <p>
     * Description:
     * Abnormal case - CborSerializationException occurs
     * </p>
     *
     * @throws Exception
     */
    @Test
    void test_submitBatchToNode_008() throws Exception {
        final Job fakeCurrentJob = generateFakeJob(1);

        final String jobType = "JOB_TYPE_A";
        final ScheduledBatchesJPA scheduledBatchesJPA = new ScheduledBatchesJPA(1L, "JOB_TYPE_A",
                BatchStatus.PENDING,
                Instant.now().minusMillis(10000000));
        final List<JobJPA> pendingJobs = Arrays.nonNullElementsIn(generateListJob(1)).stream()
                .map(JobMapperUtil::toJobJpa).collect(Collectors.toList());

        final Address address = mock(Address.class);
        final Utxo txIn = new Utxo(TX_HASH, 0, "fake Address", Collections.emptyList(), TX_HASH, mnemonic,
                TX_HASH);
        final HdKeyPair firstChildKeyPair = new HdKeyPair(null, null, "fake path");
        final Map<Address, HdKeyPair> walletInfo = new LinkedHashMap<>();
        walletInfo.put(address, firstChildKeyPair);

        final ClientWalletInfo clientWalletInfo = serviceImplMock.new ClientWalletInfo(firstChildKeyPair, address);
        final List<Utxo> txInList = List.of(txIn);
        clientWalletInfo.setUtxos(txInList);

        doReturn(scheduledBatchesJPA).when(serviceImplMock).updateScheduledBatch(scheduledBatchesJPA,
                BatchStatus.PROCESSING, jobType);
        doReturn(pendingJobs).when(jobRepository).findAllByStateAndType(JobState.PENDING, jobType);
        doReturn(clientWalletInfo).when(serviceImplMock).getTheClientWalletInfo(any());
        doThrow(CborSerializationException.class).when(serviceImplMock).createTransaction(eq(address),
                eq(firstChildKeyPair),
                any(JobBatch.class), eq(txInList),
                eq(false));

        assertThrows(CborSerializationException.class,
                () -> serviceImplMock.submitBatchToNode(fakeCurrentJob, jobType, scheduledBatchesJPA,
                        false));
    }

    /**
     * <p>
     * Description:
     * Abnormal case - An unexpected error occurred when Submitting batch to node
     * </p>
     *
     * @throws Exception
     */
    @Test
    void test_submitBatchToNode_009() throws Exception {
        final Job fakeCurrentJob = generateFakeJob(1);

        final String jobType = "JOB_TYPE_A";
        final ScheduledBatchesJPA scheduledBatchesJPA = new ScheduledBatchesJPA(1L, "JOB_TYPE_A",
                BatchStatus.PENDING,
                Instant.now().minusMillis(10000000));
        final List<JobJPA> pendingJobs = Arrays.nonNullElementsIn(generateListJob(1)).stream()
                .map(JobMapperUtil::toJobJpa).collect(Collectors.toList());

        final Address address = mock(Address.class);
        final Utxo txIn = new Utxo(TX_HASH, 0, "fake Address", Collections.emptyList(), TX_HASH, mnemonic,
                TX_HASH);
        final List<Utxo> txInList = List.of(txIn);
        final HdKeyPair firstChildKeyPair = new HdKeyPair(null, null, "fake path");
        final Map<Address, HdKeyPair> walletInfo = new LinkedHashMap<>();
        walletInfo.put(address, firstChildKeyPair);
        final ClientWalletInfo clientWalletInfo = generateFakeClientWalletInfo(address, firstChildKeyPair);
        clientWalletInfo.setUtxos(txInList);

        final Transaction tx = new Transaction();

        doReturn(scheduledBatchesJPA).when(serviceImplMock).updateScheduledBatch(scheduledBatchesJPA,
                BatchStatus.PROCESSING, jobType);
        doReturn(pendingJobs).when(jobRepository).findAllByStateAndType(JobState.PENDING, jobType);
        doReturn(tx).when(serviceImplMock).createTransaction(eq(address), eq(firstChildKeyPair),
                any(JobBatch.class),
                eq(txInList),
                eq(false));
        doReturn(clientWalletInfo).when(serviceImplMock).getTheClientWalletInfo(any());
        doThrow(RuntimeException.class).when(transactionService).submitTransaction(tx);

        mockSetProperties();
        serviceImplMock.setProperties();
        assertThrows(RuntimeException.class,
                () -> serviceImplMock.submitBatchToNode(fakeCurrentJob, jobType, scheduledBatchesJPA, false));

        verify(transactionService).submitTransaction(tx);

        verify(serviceImplMock, times(0)).updateUnconfirmedTxToDatabase(any(), eq(address), eq(txInList), any(),
                eq(TX_HASH));
        verify(serviceImplMock, times(1)).updateScheduledBatch(scheduledBatchesJPA, BatchStatus.PENDING,
                "JOB_TYPE_A");
    }

    /**
     * <p>
     * Description:
     * Create the schedule of the batch with the PROCESSING status
     * </p>
     *
     * @throws Exception
     */
    @Test
    void test_updateScheduledBatch_001() throws Exception {
        final String jobType = "JOB_TYPE_A";
        final ScheduledBatchesJPA scheduledBatchesJPA = new ScheduledBatchesJPA(1L, "JOB_TYPE_A",
                BatchStatus.PENDING,
                Instant.now().minusMillis(10000000));
        doReturn(scheduledBatchesJPA).when(scheduledBatchesRepository).save(any(ScheduledBatchesJPA.class));
        final ScheduledBatchesJPA sBatchesJPA = serviceImplMock.updateScheduledBatch(null, BatchStatus.PROCESSING,
                jobType);
        assertEquals(BatchStatus.PROCESSING, sBatchesJPA.getBatchStatus());
        assertEquals(jobType, sBatchesJPA.getJobType());
    }

    /**
     * <p>
     * Description:
     * Update the PENDING status to PROCESSING status
     * </p>
     *
     * @throws Exception
     */
    @Test
    void test_updateScheduledBatch_002() throws Exception {
        final String jobType = "JOB_TYPE_A";
        final ScheduledBatchesJPA scheduledBatchesJPA = new ScheduledBatchesJPA(1L, "JOB_TYPE_A",
                BatchStatus.PENDING,
                Instant.now().minusMillis(10000000));
        doReturn(scheduledBatchesJPA).when(scheduledBatchesRepository).save(scheduledBatchesJPA);

        final ScheduledBatchesJPA sBatchesJPA = serviceImplMock.updateScheduledBatch(scheduledBatchesJPA,
                BatchStatus.PROCESSING,
                jobType);
        assertEquals(BatchStatus.PROCESSING, sBatchesJPA.getBatchStatus());
        assertEquals(jobType, sBatchesJPA.getJobType());
    }

    /**
     * <p>
     * Description:
     * Update the NONE status to PENDING status
     * </p>
     *
     * @throws Exception
     */
    @Test
    void test_updateScheduledBatch_003() throws Exception {
        final String jobType = "JOB_TYPE_A";
        final ScheduledBatchesJPA scheduledBatchesJPA = new ScheduledBatchesJPA(1L, "JOB_TYPE_A",
                BatchStatus.NONE,
                Instant.now().minusMillis(10000000));
        doReturn(scheduledBatchesJPA).when(scheduledBatchesRepository).save(scheduledBatchesJPA);

        final ScheduledBatchesJPA sBatchesJPA = serviceImplMock.updateScheduledBatch(scheduledBatchesJPA,
                BatchStatus.PENDING,
                jobType);
        assertEquals(BatchStatus.PENDING, sBatchesJPA.getBatchStatus());
        assertEquals(jobType, sBatchesJPA.getJobType());
    }

    /**
     * <p>
     * Description:
     * Update the PROCESS status to NONE status but the list is still remain the
     * jobs.
     * </p>
     *
     * @throws Exception
     */
    @Test
    void test_updateScheduledBatch_004() throws Exception {
        final String jobType = "JOB_TYPE_A";
        final List<JobJPA> pendingJobs = Arrays.nonNullElementsIn(generateListJob(1)).stream()
                .map(JobMapperUtil::toJobJpa).collect(Collectors.toList());
        final ScheduledBatchesJPA scheduledBatchesJPA = new ScheduledBatchesJPA(1L, "JOB_TYPE_A",
                BatchStatus.PROCESSING,
                Instant.now().minusMillis(10000000));

        doReturn(scheduledBatchesJPA).when(scheduledBatchesRepository).save(scheduledBatchesJPA);
        doReturn(pendingJobs).when(jobRepository).findAllByStateAndType(JobState.PENDING,
                scheduledBatchesJPA.getJobType());

        final ScheduledBatchesJPA sBatchesJPA = serviceImplMock.updateScheduledBatch(scheduledBatchesJPA,
                BatchStatus.NONE,
                jobType);

        assertEquals(BatchStatus.PENDING, sBatchesJPA.getBatchStatus());
        assertEquals(jobType, sBatchesJPA.getJobType());
    }

    /**
     * <p>
     * Description:
     * Update the PROCESS status to NONE status
     * jobs.
     * </p>
     *
     * @throws Exception
     */
    @Test
    void test_updateScheduledBatch_005() throws Exception {
        final String jobType = "JOB_TYPE_A";
        final ScheduledBatchesJPA scheduledBatchesJPA = new ScheduledBatchesJPA(1L, "JOB_TYPE_A",
                BatchStatus.PROCESSING,
                Instant.now().minusMillis(10000000));

        doReturn(scheduledBatchesJPA).when(scheduledBatchesRepository).save(scheduledBatchesJPA);
        doReturn(Collections.emptyList()).when(jobRepository).findAllByStateAndType(JobState.PENDING,
                scheduledBatchesJPA.getJobType());

        final ScheduledBatchesJPA sBatchesJPA = serviceImplMock.updateScheduledBatch(scheduledBatchesJPA,
                BatchStatus.NONE,
                jobType);

        assertEquals(BatchStatus.NONE, sBatchesJPA.getBatchStatus());
        assertEquals(jobType, sBatchesJPA.getJobType());
    }

    /**
     * <p>
     * Description:
     * Build the transaction based on time but list job is empty.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void test_createTransaction_001()
            throws CborSerializationException, JsonProcessingException, CborException, AddressExcepion {
        // Given
        final boolean isBasedOnTime = true;
        ProtocolParamsUtil.cachedProtocolParams.setMaxTxSize(100);
        final List<Job> fakePendingJobs = Collections.emptyList();
        final Address address = mock(Address.class);
        final JobBatch jobBatch = JobBatch.builder().jobType("JOB_TYPE_A")
                .jobs(fakePendingJobs).build();
        final Utxo txIn = new Utxo(TX_HASH, 0, "fake Address", Collections.emptyList(), TX_HASH, mnemonic,
                TX_HASH);
        final List<Utxo> txInList = List.of(txIn);
        final HdKeyPair firstChildKeyPair = new HdKeyPair(null, null, "fake path");

        // When
        final Transaction result = serviceImplMock.createTransaction(address, firstChildKeyPair, jobBatch, txInList,
                isBasedOnTime);

        // Then
        assertNull(result);
    }

    /**
     * <p>
     * Description:
     * Build the transaction is not based on time but still not having enough size.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void test_createTransaction_002()
            throws CborSerializationException, JsonProcessingException, CborException, AddressExcepion {
        // Given
        final boolean isBasedOnTime = false;
        final List<Job> pendingJobs = Arrays.nonNullElementsIn(generateListJob(1));

        ProtocolParamsUtil.cachedProtocolParams.setMaxTxSize(100);
        final Address address = mock(Address.class);
        final JobBatch jobBatch = JobBatch.builder().jobType("JOB_TYPE_A")
                .groupType(GroupType.SINGLE_GROUP)
                .jobs(pendingJobs).build();
        final Utxo txIn = new Utxo(TX_HASH, 0, "fake Address", Collections.emptyList(), TX_HASH, mnemonic,
                TX_HASH);
        final List<Utxo> txInList = List.of(txIn);
        final HdKeyPair firstChildKeyPair = new HdKeyPair(null, null, "fake path");

        // When
        final Transaction signedTxn = mock(Transaction.class);
        when(signedTxn.serialize()).thenReturn(new byte[50]);
        when(serviceImplMock.buildTheSignedTransaction(eq(address.getAddress()), eq(txInList), any(), eq(jobBatch),
                eq(firstChildKeyPair)))
                .thenReturn(signedTxn);
        final Transaction result = serviceImplMock.createTransaction(address, firstChildKeyPair, jobBatch, txInList,
                isBasedOnTime);

        // Then
        assertNull(result);
    }

    /**
     * <p>
     * Description:
     * Build the transaction is not based on time and the tx size excess the
     * txMaxSize
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void test_createTransaction_003()
            throws CborSerializationException, JsonProcessingException, CborException, AddressExcepion {
        // Given
        final Utxo txIn = mock(Utxo.class);
        final List<Utxo> txInList = List.of(txIn);
        final JobBatch jobBatch = mock(JobBatch.class);
        ProtocolParamsUtil.cachedProtocolParams.setMaxTxSize(100);

        final HdKeyPair firstChildKeyPair = mock(HdKeyPair.class);
        final boolean isBasedOnTime = true;
        final List<Job> pendingJobs = Arrays.nonNullElementsIn(generateListJob(1));
        final Address address = mock(Address.class);

        // When
        when(jobBatch.getJobs()).thenReturn(pendingJobs);
        final Transaction signedTxn = mock(Transaction.class);
        when(signedTxn.serialize()).thenReturn(new byte[150]);
        when(serviceImplMock.buildTheSignedTransaction(eq(address.getAddress()), eq(txInList), any(), eq(jobBatch),
                eq(firstChildKeyPair)))
                .thenReturn(signedTxn);
        final Transaction result = serviceImplMock.createTransaction(address, firstChildKeyPair, jobBatch, txInList,
                isBasedOnTime);

        // Then
        assertNull(result);
    }

    /**
     * <p>
     * Build the transaction that based on time
     * </p>
     *
     * @throws CborSerializationException
     */
    @Test
    public void test_createTransaction_004()
            throws CborSerializationException, JsonProcessingException, CborException, AddressExcepion {
        // Given
        final Address address = mock(Address.class);
        final Utxo txIn = mock(Utxo.class);
        final List<Utxo> txInList = List.of(txIn);
        final JobBatch jobBatch = mock(JobBatch.class);
        ProtocolParamsUtil.cachedProtocolParams.setMaxTxSize(100);
        final HdKeyPair firstChildKeyPair = mock(HdKeyPair.class);
        final boolean isBasedOnTime = true;
        final List<Job> pendingJobs = Arrays.nonNullElementsIn(generateListJob(1));

        // When
        when(jobBatch.getJobs()).thenReturn(pendingJobs);
        final Transaction signedTxn = mock(Transaction.class);
        when(signedTxn.serialize()).thenReturn(new byte[50]);
        when(serviceImplMock.buildTheSignedTransaction(eq(address.getAddress()), eq(txInList), any(), eq(jobBatch),
                eq(firstChildKeyPair)))
                .thenReturn(signedTxn);

        final Transaction result = serviceImplMock.createTransaction(address, firstChildKeyPair, jobBatch, txInList,
                isBasedOnTime);

        // Then
        assertEquals(signedTxn, result);
    }

    /**
     * <p>
     * Build the transaction that based on time and the size excess the max size
     * </p>
     *
     * @throws CborSerializationException
     */
    @Test
    public void test_createTransaction_005()
            throws CborSerializationException, JsonProcessingException, CborException, AddressExcepion {
        // Given
        final Address address = mock(Address.class);
        final Utxo txIn = mock(Utxo.class);
        final List<Utxo> txInList = List.of(txIn);
        final JobBatch jobBatch = mock(JobBatch.class);
        ProtocolParamsUtil.cachedProtocolParams.setMaxTxSize(100);
        final HdKeyPair firstChildKeyPair = mock(HdKeyPair.class);
        final boolean isBasedOnTime = true;
        final List<Job> pendingJobs = Arrays.nonNullElementsIn(generateListJob(10));

        // When
        when(jobBatch.getJobs()).thenReturn(pendingJobs);
        final Transaction signedTxn = mock(Transaction.class);
        when(signedTxn.serialize()).thenReturn(new byte[150]).thenReturn(new byte[100]).thenReturn(new byte[90]);
        when(serviceImplMock.buildTheSignedTransaction(eq(address.getAddress()), eq(txInList), any(), eq(jobBatch),
                eq(firstChildKeyPair)))
                .thenReturn(signedTxn);
        when(metadataServiceFactory.buildOffchainJson(jobBatch)).thenReturn(TestConstants.JSON_NORMAL);
        when(offchainStorageService.storeObject(any(), eq(TestConstants.JSON_NORMAL)))
                .thenReturn(TestConstants.CID_NORMAL);

        final Transaction result = serviceImplMock.createTransaction(address, firstChildKeyPair, jobBatch, txInList,
                isBasedOnTime);

        // Then
        assertEquals(signedTxn, result);
        assertEquals(9, pendingJobs.size());
    }

    @Test
    public void test_setProperties_001() {
        mockSetProperties();

        serviceImplMock.setProperties();

        verify(txSubmitterProperties).getNetwork();
        verify(txSubmitterProperties).getWallet();
        verify(wallet).getMnemonic();
        verify(txSubmitterProperties).getMetadatumLabel();
        verify(txSubmitterProperties).getBatchConsumptionBoundaryTime();
        verify(txSubmitterProperties).getNumberOfDerivedAddresses();
        verify(txSubmitterProperties).getWaitingTimeToReConsume();
        verify(txSubmitterProperties).getTxSubmissionRetryDelayDuration();
    }

    @Test
    public void test_buildTheSignedTransaction_001() {
        final JobBatch jobBatch = mock(JobBatch.class);
        final Utxo txIn = mock(Utxo.class);
        final List<Utxo> txInList = List.of(txIn);
        final Address address = mock(Address.class);

        final TransactionOutput txOut = TransactionOutput.builder()
                .address(address.getAddress())
                .value(Value.builder()
                        .coin(BigInteger.valueOf(10000L))
                        .build())
                .build();
        final HdKeyPair firstChildKeyPair = mock(HdKeyPair.class);

        final TxMetadata txMetadata = mock(TxMetadata.class);
        final CBORMetadata metadata = new CBORMetadata();
        final Transaction transaction = new Transaction();

        when(metadataServiceFactory.buildTxMetadata(jobBatch)).thenReturn(txMetadata);
        when(metadataServiceFactory.buildCborTxMetadata(eq(txMetadata), any())).thenReturn(metadata);
        when(transactionService.buildTransaction(address.getAddress(), txInList, txOut, metadata))
                .thenReturn(transaction);
        when(transactionService.signTransaction(firstChildKeyPair, transaction)).thenReturn(transaction);

        serviceImplMock.buildTheSignedTransaction(address.getAddress(), txInList, txOut, jobBatch, firstChildKeyPair);

        verify(metadataServiceFactory).buildTxMetadata(jobBatch);
        verify(metadataServiceFactory).buildCborTxMetadata(eq(txMetadata), any());
        verify(transactionService).buildTransaction(address.getAddress(), txInList, txOut, metadata);
        verify(transactionService).signTransaction(firstChildKeyPair, transaction);
    }

    /**
     * <p>
     * Description:
     * checks if the getTheClientWalletInfo method returns the best client info when
     * it is found on the first try:
     * </p>
     *
     * @throws InterruptedException
     * @throws JsonProcessingException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
    @SuppressWarnings("unchecked")
    void testGetTheClientWalletInfoReturnsBestClientInfo() throws InterruptedException, JsonProcessingException,
            NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, CborException,
            AddressExcepion, CborSerializationException {
        final Integer retryCount = 1;
        final Address address = mock(Address.class);
        final Address address2 = mock(Address.class);
        final HdKeyPair childKeyPair = new HdKeyPair(null, null, "fake path");
        final HdKeyPair childKeyPair2 = new HdKeyPair(null, null, "fake path2");
        final Map<Address, HdKeyPair> walletAddresses = new LinkedHashMap<>();
        walletAddresses.put(address, childKeyPair);
        walletAddresses.put(address2, childKeyPair2);
        final Field addressAndKeyPairMapField = BatchConsumptionServiceImpl.class
                .getDeclaredField("addressAndKeyPairMap");
        addressAndKeyPairMapField.setAccessible(true);
        final Map<Address, HdKeyPair> addressAndKeyPairMap = (Map<Address, HdKeyPair>) addressAndKeyPairMapField
                .get(serviceImplMock);
        addressAndKeyPairMap.putAll(walletAddresses);

        final ClientWalletInfo expectedBestClientInfo = serviceImplMock.new ClientWalletInfo(childKeyPair2, address2);

        when(serviceImplMock.getAppropriateTxIn(walletAddresses)).thenReturn(expectedBestClientInfo);

        // Act
        final ClientWalletInfo actualBestClientInfo = serviceImplMock.getTheClientWalletInfo(retryCount);

        // Assert
        assertEquals(expectedBestClientInfo, actualBestClientInfo);
    }

    /**
     * <p>
     * Test case for null client wallet info:
     * This test case should verify
     * that the getTheClientWalletInfo method returns null when the
     * getAppropriateTxIn method
     * returns null and the retry count is less than or equal to 0.
     * </p>
     *
     * @throws InterruptedException
     * @throws JsonProcessingException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
    @SuppressWarnings("unchecked")
    void testGetTheClientWalletInfoReturnsNull() throws InterruptedException, JsonProcessingException,
            NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, CborException,
            AddressExcepion, CborSerializationException {
        // Create a mock instance of the class under test
        final Address address = mock(Address.class);
        final Address address2 = mock(Address.class);
        final HdKeyPair childKeyPair = new HdKeyPair(null, null, "fake path");
        final HdKeyPair childKeyPair2 = new HdKeyPair(null, null, "fake path2");
        final Map<Address, HdKeyPair> walletAddresses = new LinkedHashMap<>();
        walletAddresses.put(address, childKeyPair);
        walletAddresses.put(address2, childKeyPair2);
        final Field addressAndKeyPairMapField = BatchConsumptionServiceImpl.class
                .getDeclaredField("addressAndKeyPairMap");

        addressAndKeyPairMapField.setAccessible(true);
        final Map<Address, HdKeyPair> addressAndKeyPairMap = (Map<Address, HdKeyPair>) addressAndKeyPairMapField
                .get(serviceImplMock);
        addressAndKeyPairMap.putAll(walletAddresses);

        // Mock the behavior of the getAppropriateTxIn method to return null
        when(serviceImplMock.getAppropriateTxIn(Mockito.anyMap())).thenReturn(null);

        // Call the getTheClientWalletInfo method with a retry count of 0
        final ClientWalletInfo result = serviceImplMock.getTheClientWalletInfo(0);

        // Verify that the result is null
        assertNull(result);
    }

    /**
     * <p>
     * Test case for retrying retrieval of client wallet info:
     * This test case should verify that the getTheClientWalletInfo method
     * retries retrieving the client wallet info when the getAppropriateTxIn
     * method returns null and the retry count is greater than 0.
     * The test should also verify that the method waits
     * for the specified time before retrying and decrements the retry count.
     * </p>
     *
     * @throws InterruptedException
     * @throws JsonProcessingException
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    @Test
    @SuppressWarnings("unchecked")
    void testGetTheClientWalletInfoRetries() throws InterruptedException, JsonProcessingException, NoSuchFieldException,
            SecurityException, IllegalArgumentException, IllegalAccessException, CborException, AddressExcepion,
            CborSerializationException {
        final Integer retryCount = 1;
        final Address address = mock(Address.class);
        final Address address2 = mock(Address.class);
        final HdKeyPair childKeyPair = new HdKeyPair(null, null, "fake path");
        final HdKeyPair childKeyPair2 = new HdKeyPair(null, null, "fake path2");
        final Map<Address, HdKeyPair> walletAddresses = new LinkedHashMap<>();
        walletAddresses.put(address, childKeyPair);
        walletAddresses.put(address2, childKeyPair2);
        final Field addressAndKeyPairMapField = BatchConsumptionServiceImpl.class
                .getDeclaredField("addressAndKeyPairMap");
        addressAndKeyPairMapField.setAccessible(true);
        final Field waitingTimeToReConsumeField = BatchConsumptionServiceImpl.class
                .getDeclaredField("waitingTimeToReConsume");
        waitingTimeToReConsumeField.setAccessible(true);
        waitingTimeToReConsumeField.set(serviceImplMock, BigInteger.valueOf(1L));

        final Map<Address, HdKeyPair> addressAndKeyPairMap = (Map<Address, HdKeyPair>) addressAndKeyPairMapField
                .get(serviceImplMock);
        addressAndKeyPairMap.putAll(walletAddresses);

        // Mock the behavior of the getAppropriateTxIn method to return null on the
        // first call and a non-null value on the second call
        final ClientWalletInfo expectedClientWalletInfo = serviceImplMock.new ClientWalletInfo(childKeyPair2, address2);
        when(serviceImplMock.getAppropriateTxIn(walletAddresses))
                .thenReturn(null)
                .thenReturn(expectedClientWalletInfo);

        // Call the getTheClientWalletInfo method with a retry count of 1
        final ClientWalletInfo result = serviceImplMock.getTheClientWalletInfo(retryCount);

        // Verify that the result is the expected ClientWalletInfo object
        assertEquals(expectedClientWalletInfo, result);
    }

    /**
     * <p>
     * Test case for throwing InterruptedException:
     * This test case should verify that the getTheClientWalletInfo method throws an
     * InterruptedException
     * when the thread is interrupted while waiting to retry retrieving the client
     * wallet info.
     * </p>
     *
     * @throws JsonProcessingException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
    @SuppressWarnings("unchecked")
    void testGetTheClientWalletInfoThrowsInterruptedException() throws JsonProcessingException, NoSuchFieldException,
            SecurityException, IllegalArgumentException, IllegalAccessException, CborException, AddressExcepion,
            CborSerializationException {
        // Preparation of the class under test
        final Integer retryCount = 1;
        final Address address = mock(Address.class);
        final Address address2 = mock(Address.class);
        final HdKeyPair childKeyPair = new HdKeyPair(null, null, "fake path");
        final HdKeyPair childKeyPair2 = new HdKeyPair(null, null, "fake path2");
        final Map<Address, HdKeyPair> walletAddresses = new LinkedHashMap<>();
        walletAddresses.put(address, childKeyPair);
        walletAddresses.put(address2, childKeyPair2);
        final Field addressAndKeyPairMapField = BatchConsumptionServiceImpl.class
                .getDeclaredField("addressAndKeyPairMap");
        addressAndKeyPairMapField.setAccessible(true);
        final Map<Address, HdKeyPair> addressAndKeyPairMap = (Map<Address, HdKeyPair>) addressAndKeyPairMapField
                .get(serviceImplMock);
        addressAndKeyPairMap.putAll(walletAddresses);

        final Field waitingTimeToReConsumeField = BatchConsumptionServiceImpl.class
                .getDeclaredField("waitingTimeToReConsume");
        waitingTimeToReConsumeField.setAccessible(true);
        waitingTimeToReConsumeField.set(serviceImplMock, BigInteger.valueOf(30000L));

        // Mock the behavior of the getAppropriateTxIn method to return null
        when(serviceImplMock.getAppropriateTxIn(walletAddresses)).thenReturn(null);

        // Interrupt the current thread
        Thread.currentThread().interrupt();

        // Call the getTheClientWalletInfo method with a retry count of 1 and verify
        // that it throws an InterruptedException
        assertThrows(InterruptedException.class, () -> serviceImplMock.getTheClientWalletInfo(retryCount));
    }

    /**
     * <p>
     * Test case 1: Verify that the job state is updated - This test case should
     * verify that the setState method is called on each job in the jobBatch object
     * with the JobState.SUBMITTED argument.
     * </p>
     */
    @Test
    void testSendConfirmingJobToProducer_VerifyJobStateIsUpdated() {
        // Arrange
        final Job job1 = Mockito.mock(Job.class);
        final Job job2 = Mockito.mock(Job.class);
        final List<Job> jobs = List.of(job1, job2);
        final JobBatch jobBatch = new JobBatch();
        jobBatch.setJobs(jobs);
        final Instant submittedDate = Instant.now();

        // Act
        serviceImplMock.sendConfirmingJobToProducer(TX_HASH, jobBatch, submittedDate);

        // Assert
        // Verify that the setState method is called on each job in the jobBatch object
        // with the JobState.SUBMITTED argument
        verify(job1, times(1)).setState(JobState.SUBMITTED);
        verify(job2, times(1)).setState(JobState.SUBMITTED);
    }

    /**
     * <p>
     * Test case 2: Verify that a ConfirmingTransaction object is created - This
     * test case should verify that a ConfirmingTransaction object is created with
     * the correct properties, including txHash, jobBatch, submittedDate, and
     * retryCountsForUnexpectedError.
     * </p>
     */
    @Test
    void testSendConfirmingJobToProducer_VerifyConfirmingTransactionObjectIsCreated() {
        // Arrange
        final Job job1 = Mockito.mock(Job.class);
        final JobBatch jobBatch = JobBatch.builder().jobs(List.of(job1)).build();
        final Instant submittedDate = Instant.now();
        final ArgumentCaptor<ConfirmingTransaction> argumentCaptor = ArgumentCaptor
                .forClass(ConfirmingTransaction.class);

        // Act
        mockSetProperties();
        serviceImplMock.setProperties();
        serviceImplMock.sendConfirmingJobToProducer(TX_HASH, jobBatch, submittedDate);

        // Assert
        // Verify that a ConfirmingTransaction object is created with the correct
        // properties
        verify(queueingService, times(1)).sendMessage(argumentCaptor.capture(), eq("confirming.transaction.topic"), eq(TX_HASH));
        final ConfirmingTransaction confirmingTransaction = argumentCaptor.getValue();
        assertEquals(TX_HASH, confirmingTransaction.getTxHash());
        assertEquals(jobBatch, confirmingTransaction.getJobBatch());
        assertEquals(submittedDate, confirmingTransaction.getSubmittedDate());
    }

    /**
     * <p>
     * Test case 3: Verify that the message is sent to the confirming transaction
     * topic - This test case should verify that the sendMessage method is called on
     * the queueingService object with the correct arguments, including the created
     * ConfirmingTransaction object, the confirmingTransactionTopic, and the txHash.
     * </p>
     */
    @Test
    void testSendConfirmingJobToProducer_VerifyMessageIsSentToConfirmingTransactionTopic() {
        // Arrange
        final Job job1 = Mockito.mock(Job.class);
        final JobBatch jobBatch = JobBatch.builder().jobs(List.of(job1)).build();
        final Instant submittedDate = Instant.now();

        // Act
        mockSetProperties();
        serviceImplMock.setProperties();
        serviceImplMock.sendConfirmingJobToProducer(TX_HASH, jobBatch, submittedDate);

        // Assert
        // Verify that the sendMessage method is called on the queueingService object
        // with the correct arguments
        verify(queueingService, times(1)).sendMessage(any(ConfirmingTransaction.class),
                eq("confirming.transaction.topic"), eq(TX_HASH));
    }

    /**
     * <p>
     * Generate fake schedule
     * </p>
     *
     * @return The scheduled batch
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws NoSuchFieldException
     */
    private List<ScheduledBatchesJPA> generateFakeScheduleBatches()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        final List<ScheduledBatchesJPA> fakeSchedules = new ArrayList<>();
        fakeSchedules.add(
                new ScheduledBatchesJPA(1L, "JOB_TYPE_A", BatchStatus.PENDING, Instant.now().minusMillis(1000000)));
        fakeSchedules.add(
                new ScheduledBatchesJPA(1L, "JOB_TYPE_B", BatchStatus.PENDING, Instant.now().minusMillis(20000)));
        fakeSchedules.add(
                new ScheduledBatchesJPA(1L, "JOB_TYPE_C", BatchStatus.PENDING, Instant.now().minusMillis(1000000)));

        // Set the batchConsumptionBoundaryTime to the service
        final Field boundaryTimeField = BatchConsumptionServiceImpl.class
                .getDeclaredField("batchConsumptionBoundaryTime");
        boundaryTimeField.setAccessible(true);
        boundaryTimeField.set(serviceImplMock, BigInteger.valueOf(30000L));

        return fakeSchedules;
    }

    /**
     * <p>
     * Generate fake consumer record
     * </p>
     *
     * @return The consumer record
     */
    private ConsumerRecord<String, Job> generateFakeConsumerRecord() {
        final byte[] fakeSignature = new byte[]{102, 1, -112, 56, 21, -26, 82, 13, 52, -16, -4, 66, -84, 2, -4, 102, 75, 62, 74, 116, -67, 110, 77, 93, 59, -71, -118, -85, -109, 44, 55, 90, -28, 25, 125, -21, -22, 54, -27, -55, 95, 6, 107, -91, -45, -28, 43, 76, -111, 24, -32, -70, -71, -11, 52, -98, 56, -87, 33, 21, -15, -36, 108, 10};
        final byte[] fakePubKey = new byte[]{23, 6, 99};
        final BusinessData fakeBusinessData = new BusinessData("JOB_TYPE_A:sub_type", "sub_type", "fake_data",
                jwsHeader , fakeSignature,
                fakePubKey);
        final Job fakeJob = new Job(1L, JobState.PENDING, fakeBusinessData, TX_HASH, GroupType.SINGLE_GROUP,
                "FAKE_GROUP", "0", 5);
        final ConsumerRecord<String, Job> record = new ConsumerRecord<String, Job>("fake_topic", 1, 0, "key",
                fakeJob);

        return record;
    }

    /**
     * <p>
     * Generate fake job
     * </p>
     *
     * @param index The custom index
     * @return The fake job
     */
    private Job generateFakeJob(final long index) {
        BusinessData businessData = new BusinessData();
        businessData.setJwsHeader(jwsHeader);
        return new Job(index, JobState.PENDING, new BusinessData(), TX_HASH, GroupType.SINGLE_GROUP,
                "FAKE GROUP", "0", 5);
    }

    /**
     * <p>
     * Generate fake job list
     * </p>
     *
     * @param numberOfJobs The number of Jobs.
     * @return the generated Lists
     */
    private Job[] generateListJob(final int numberOfJobs) {
        final List<Job> fakeListJob = Collections.nCopies(numberOfJobs, generateFakeJob(numberOfJobs));
        return fakeListJob.toArray(new Job[fakeListJob.size()]);
    }

    /**
     * <p>
     * Generate client wallet info
     * </p>
     *
     * @param address
     * @param hdKeyPair
     * @return
     */
    private ClientWalletInfo generateFakeClientWalletInfo(final Address address, final HdKeyPair hdKeyPair) {
        final Utxo txIn = new Utxo();
        final List<Utxo> txInList = List.of(txIn);
        final List<Amount> amount = new ArrayList<>();
        amount.add(0, new Amount("lovelace", BigInteger.valueOf(1000000L)));
        txIn.setAmount(amount);

        final ClientWalletInfo clientWalletInfo = serviceImplMock.new ClientWalletInfo(hdKeyPair, address);
        clientWalletInfo.setUtxos(txInList);

        return clientWalletInfo;
    }

    private void mockSetProperties() {
        when(txSubmitterProperties.getNetwork()).thenReturn("testnet");
        when(txSubmitterProperties.getWallet()).thenReturn(wallet);
        when(wallet.getMnemonic()).thenReturn("mnemonic");
        when(txSubmitterProperties.getMetadatumLabel()).thenReturn(BigInteger.TWO);
        when(txSubmitterProperties.getBatchConsumptionBoundaryTime()).thenReturn(BigInteger.TEN);
        when(txSubmitterProperties.getNumberOfDerivedAddresses()).thenReturn(Integer.MAX_VALUE);
        when(txSubmitterProperties.getWaitingTimeToReConsume()).thenReturn(BigInteger.TEN);
        when(txSubmitterProperties.getTxSubmissionRetryDelayDuration()).thenReturn(Long.valueOf(10L));

        // Mock kafka config
        final Map<String, TopicConfig> mockMap = new HashMap<>();
        final TopicConfig config = new TopicConfig();
        config.setName("confirming.transaction.topic");

        final Map<String, String> topicConfig = new HashMap<>();
        topicConfig.put("retryCountForUnexpectedError", "1");
        config.setConfigs(topicConfig);
        mockMap.put("confirmingTransaction", config);

        when(kafkaProperties.getTopics()).thenReturn(mockMap);
    }
}
