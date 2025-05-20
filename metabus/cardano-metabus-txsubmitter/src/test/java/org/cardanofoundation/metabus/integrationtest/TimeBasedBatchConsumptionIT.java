package org.cardanofoundation.metabus.integrationtest;

import co.nstant.in.cbor.model.*;
import com.bloxbean.cardano.client.common.cbor.CborSerializationUtil;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadata;
import com.bloxbean.cardano.client.transaction.spec.AuxiliaryData;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import com.bloxbean.cardano.yaci.helper.LocalTxMonitorClient;
import com.bloxbean.cardano.yaci.helper.model.MempoolStatus;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.cardanofoundation.metabus.common.entities.JobJPA;
import org.cardanofoundation.metabus.common.entities.ScheduledBatchesJPA;
import org.cardanofoundation.metabus.common.enums.BatchStatus;
import org.cardanofoundation.metabus.common.enums.GroupType;
import org.cardanofoundation.metabus.common.enums.JobState;
import org.cardanofoundation.metabus.common.offchain.BusinessData;
import org.cardanofoundation.metabus.common.offchain.ConfirmingTransaction;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.common.offchain.JobBatch;
import org.cardanofoundation.metabus.common.onchain.TxMetadata;
import org.cardanofoundation.metabus.configuration.TxSubmitterProperties;
import org.cardanofoundation.metabus.factory.MetadataServiceFactory;
import org.cardanofoundation.metabus.integrationtest.kafka.ConfirmingTransactionConsumer;
import org.cardanofoundation.metabus.integrationtest.kafka.KafkaProducer;
import org.cardanofoundation.metabus.repos.JobRepository;
import org.cardanofoundation.metabus.repos.ScheduledBatchesRepository;
import org.cardanofoundation.metabus.service.MetadataService;
import org.cardanofoundation.metabus.service.impl.MultiGroupMetadataService;
import org.cardanofoundation.metabus.util.ProtocolParamsUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <p>
 * IT Test for submitting batch based on time.
 * </p>
 *
 * @author (sotatek) joey.dao
 */

@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9093", "port=9093" })
@Slf4j
@TestPropertySource(properties = {
        "cardano-metabus-txsubmitter.batchConsumptionBoundaryTime=5000"
})
public class TimeBasedBatchConsumptionIT extends BaseIntegrationTest{
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private LocalTxMonitorClient localTxMonitorClient;
    @Autowired
    private MetadataServiceFactory metadataServiceFactory;
    @Autowired
    private TxSubmitterProperties txSubmitterProperties;
    @Autowired
    private ScheduledBatchesRepository scheduledBatchesRepository;
    @Autowired
    private KafkaProducer kafkaProducer;

    public static byte[] signature = new byte[] {39, -32, 51, 64, -103, -104, -103, -86, 50, 113, 38, 57, -54, -119, 86,
            86, -61, -99, 80, 82, 67, 22, -40, -73, -88, -85, -46, 96, 15, -70, 39, 11, 88, 60, 49, -60, -94, 5, 39,
            -119, -57, -79, 27, 76, 30, 17, -1, -48, -43, -40, -93, 1, -27, 53, -112, -42, -126, -24, -11, -65, 84, 52,
            -101, 12};
    private static final byte[] jwsHeader = new byte[]{123, 34, 107, 105, 100, 34, 58, 34, 55, 50, 99, 99, 52, 102, 54,
            52, 45, 98, 50, 56, 49, 45, 52, 48, 56, 98, 45, 56, 52, 48, 52, 45, 57, 57, 102, 57, 98, 100, 100, 101, 56,
            57, 49, 57, 34, 44, 34, 97, 108, 103, 34, 58, 34, 69, 100, 68, 83, 65, 34, 125};
    private static final byte[] pubKey = new byte[]{123, 44, 5};

    /**
     * <p>
     * Confirming Transaction Test Consumer.
     * </p>
     */
    @Autowired
    private ConfirmingTransactionConsumer confirmingTxConsumer;

    @Test
    @SneakyThrows
    public void testSubmitJobByWaitingTime() {
        createStubForWireMock_offchainStorageServer_storeObject_success();

        // prepare max tx that fit for only 2 txs
        ProtocolParamsUtil.cachedProtocolParams.setMaxTxSize(16000);

        List<Job> preparedPendingJobs = new ArrayList<>();
        List<JobJPA> preparedSavedPendingJobJPAs = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            // simulate job saved by metabus-api
            JobJPA jobJPA = JobJPA.builder()
                    .type("scm:georgianWine")
                    .state(JobState.PENDING)
                    .signature(signature)
                    .jwsHeader(jwsHeader)
                    .subType("georgianWine")
                    .retryCount(5)
                    .pubKey(pubKey)
                    .data("data")
                    .groupType(GroupType.MULTI_GROUP)
                    .group("string")
                    .build();
            JobJPA savedJobJPA = jobRepository.saveAndFlush(jobJPA);

            // simulate job producer send job to kafka
            Job job = Job.builder()
                    .id(savedJobJPA.getId())
                    .businessData(BusinessData.builder()
                            .pubKey(jobJPA.getPubKey())
                            .data(jobJPA.getData())
                            .type(jobJPA.getType())
                            .subType("georgianWine")
                            .signature(jobJPA.getSignature())
                            .jwsHeader(jwsHeader)
                            .build())
                    .state(JobState.PENDING)
                    .groupType(jobJPA.getGroupType())
                    .group(jobJPA.getGroup())
                    .retryCount(5)
                    .build();
            preparedSavedPendingJobJPAs.add(savedJobJPA);
            preparedPendingJobs.add(job);

            // simulate job producer send job
            kafkaProducer.send("test.job.schedule", job);
        }

        ScheduledBatchesJPA scheduledBatchesJPA = ScheduledBatchesJPA.builder().jobType("scm:georgianWine")
                .batchStatus(BatchStatus.PENDING).consumedJobTime(Instant.now().minusMillis(1000000L)).build();
        scheduledBatchesRepository.saveAndFlush(scheduledBatchesJPA);

        // Sleep for 5 second
        Thread.sleep(5000);

        // query cardano-node's mempool and validate tx metadata in auxiliaryData
        List<byte[]> txBytesList = null;

        while (CollectionUtils.isEmpty(txBytesList)) {
            log.info("Waiting to acquire next snapshot ...");
            txBytesList = localTxMonitorClient.acquireAndGetMempoolTransactionsAsMono()
                    .block();

            if (!CollectionUtils.isEmpty(txBytesList)) {
                byte[] txBytes = txBytesList.get(0);

                String txHash = TransactionUtil.getTxHash(txBytes);
                log.info("Tx Hash >> " + txHash);

                Transaction transaction = null;
                try {
                    transaction = Transaction.deserialize(txBytes);
                    byte[] actualAuxiliaryDataHashFromMemPool = transaction.getBody().getAuxiliaryDataHash();

                    CBORMetadata expectedCBORMetadata = createExpectedCBORMetadata(preparedPendingJobs);
                    AuxiliaryData auxiliaryData = new AuxiliaryData();
                    auxiliaryData.setMetadata(expectedCBORMetadata);
                    byte[] expectedAuxiliaryDataHash = auxiliaryData.getAuxiliaryDataHash();

                    // assert expected auxiliary data hash equal to auxiliary data hash from mempool
                    assertArrayEquals(expectedAuxiliaryDataHash, actualAuxiliaryDataHashFromMemPool);

                    Map metadata = (Map) expectedCBORMetadata.getData().get(new UnsignedInteger(1904));
                    String jobType = CborSerializationUtil.toUnicodeString(
                            metadata.get(new UnicodeString(MetadataService.TYPE)));
                    String cid = CborSerializationUtil.toUnicodeString(
                            metadata.get(new UnicodeString(MetadataService.CID)));
                    Map verification = (Map) metadata.get(new UnicodeString(MultiGroupMetadataService.VERIFICATION));
                    DataItem verificationKey = ((List<DataItem>) verification.getKeys()).get(0);
                    Map verificationInfo = (Map) ((Map) verification).get(new UnicodeString("string"));
                    String pubKey = CborSerializationUtil.toUnicodeString(
                            verificationInfo.get(new UnicodeString(MetadataService.PUB_KEY)));

                    Array signatures = (Array) verificationInfo.get(new UnicodeString(MetadataService.SIGNATURES));
                    List<DataItem> signaturesList = signatures.getDataItems();
                    List<byte[]> signatureByteArrays = signaturesList.stream()
                            .map(CborSerializationUtil::toBytes)
                            .collect(Collectors.toList());

                    assertEquals("scm", jobType);
                    assertEquals("CT5htke5VQLLnT55vp9B62yNaMgjwyYPzayfon89LXgRy2LXeVo", cid);
                    assertEquals(preparedPendingJobs.get(0).getGroup(),
                            CborSerializationUtil.toUnicodeString(verificationKey));
                    assertEquals("string", pubKey);
                    for (int i = 0; i < 3; i++) {
                        assertArrayEquals(signature, signatureByteArrays.get(i));
                    }
                    assertEquals(1, transaction.getBody().getOutputs().size());
                } catch (CborDeserializationException e) {
                    throw new RuntimeException(e);
                }
                log.info("Tx Body >> " + transaction);

                MempoolStatus mempoolStatus = localTxMonitorClient.getMempoolSizeAndCapacity().block();
                log.info("Mem Pool >> " + mempoolStatus);

                // Assert the status of submitted job in database
                List<Long> pendingJobIds = preparedPendingJobs.stream().map(Job::getId)
                        .collect(Collectors.toList());

                List<JobJPA> submittedJobJPAs = new ArrayList<>();
                while (submittedJobJPAs.isEmpty() || submittedJobJPAs.get(2).getState().equals(JobState.PENDING)) {
                    submittedJobJPAs = jobRepository.findAllByIdIn(pendingJobIds);
                }

                submittedJobJPAs.forEach(submittedJobJPA -> {
                    assertEquals(JobState.SUBMITTED, submittedJobJPA.getState());
                });

                /**
                 * Check the confirming transaction consumer receive the message from
                 * tx-submitter
                 */
                ConsumerRecord<String, ConfirmingTransaction> consumerRecord = null;
                while (Objects.isNull(consumerRecord)) {
                    consumerRecord = confirmingTxConsumer.getConsumerRecord().get();
                }

                assertNotNull(consumerRecord);
                final ConfirmingTransaction confirmingTransactionPushedToKafka = consumerRecord.value();
                final JobBatch submittedJobBatch = confirmingTransactionPushedToKafka.getJobBatch();
                final List<Job> listSubmittedJobs = submittedJobBatch.getJobs();
                final boolean isAllMatch = submittedJobJPAs.stream().allMatch(jobJPAs -> {
                    return listSubmittedJobs.stream()
                            .anyMatch(jobElement -> jobJPAs.getId().equals(jobElement.getId()));
                });

                assertTrue(isAllMatch);
                assertEquals(confirmingTransactionPushedToKafka.getTxHash(), txHash);

            }
        }

    }

    private CBORMetadata createExpectedCBORMetadata(final List<Job> jobs) {
        final JobBatch jobBatch = JobBatch.builder()
                .cid("CT5htke5VQLLnT55vp9B62yNaMgjwyYPzayfon89LXgRy2LXeVo")
                .jobType(jobs.get(0).getBusinessData().getType())
                .groupType(jobs.get(0).getGroupType())
                .jobs(jobs)
                .jobSubType("georgianWine")
                .build();

        final TxMetadata txMetadata = metadataServiceFactory.buildTxMetadata(jobBatch);

        final CBORMetadata metadata = metadataServiceFactory.buildCborTxMetadata(txMetadata,
                txSubmitterProperties.getMetadatumLabel());

        return metadata;
    }
}
