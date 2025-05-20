package org.cardanofoundation.metabus.integrationtest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.cardanofoundation.metabus.common.entities.JobJPA;
import org.cardanofoundation.metabus.common.entities.UnconfirmedTxJPA;
import org.cardanofoundation.metabus.common.enums.JobState;
import org.cardanofoundation.metabus.common.offchain.BusinessData;
import org.cardanofoundation.metabus.common.offchain.ConfirmingTransaction;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.integrationtest.kafka.KafkaConsumer;
import org.cardanofoundation.metabus.repos.JobRepository;
import org.cardanofoundation.metabus.repos.UnconfirmedTxRepository;
import org.cardanofoundation.metabus.service.QueueingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.context.EmbeddedKafka;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;

/**
 * This integration test can run independently with kafka embedded in server.
 * <p>
 * The IT Test for ConfirmingTransactionConsumer class.
 * </p>
 * <p>
 * The Confirming Transaction Consumer Integration Test
 * </p>
 * <p>
 * Pre-conditions:
 * Database is available.
 * </p>
 *
 * @author (Sotatek) joey.dao
 * @version 0.01
 * @category Integration Test
 * @since 2023/08
 */
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9093", "port=9093" })
public class ConfirmingTransactionConsumerIT extends BaseIntegrationTest {

    // Preparation Data Json File.
    public final static String METABUS_CONFIRMING_TRANSACTION_MESSAGE = BASE_JSON_FOLDER + REQUEST_FOLDER
            + "/create_confirming_transaction.json";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UnconfirmedTxRepository unConfirmedTxRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private QueueingService queueingService;

    @Autowired
    private KafkaConsumer kafkaConsumer;

    /**
     * <p>
     * Test the transaction is on-chain.
     * </p>
     */
    @Test
    @SneakyThrows
    public void test_The_transaction_is_on_chain() {
        // Prepare the data.
        final String transactionMessage = readJsonFromFile(METABUS_CONFIRMING_TRANSACTION_MESSAGE);
        final ConfirmingTransaction transaction = objectMapper.readValue(transactionMessage,
                ConfirmingTransaction.class);
        transaction.setSubmittedDate(Instant.now());

        final UnconfirmedTxJPA unconfirmedTxJPA = UnconfirmedTxJPA.builder().isDeleted(true)
                .txHash(transaction.getTxHash()).metadata(transaction.getJobBatch().toString())
                .createdDate(Instant.now()).lastUpdated(Instant.now()).build();
        final JobJPA jobJPA = ConfirmingTransactionConsumerIT.toJobJpa(transaction.getJobBatch().getJobs().get(0));
        final List<UnconfirmedTxJPA> unconfirmedList = unConfirmedTxRepository.findAllByTxHash(unconfirmedTxJPA.getTxHash());
        final List<Long> ids = unconfirmedList.stream().map(tx -> tx.getId()).toList();

        // Clear all data in the database
        jobRepository.deleteAll();
        unConfirmedTxRepository.deleteAllByIdInBatch(ids);

        // Insert the data to database.
        jobRepository.saveAndFlush(jobJPA);
        unConfirmedTxRepository.saveAndFlush(unconfirmedTxJPA);

        // Send the message to confirming transaction topic
        queueingService.sendConfirmingTransaction(transaction);

        Thread.sleep(4000);
        ConsumerRecord<String, Job> consumerRecord = kafkaConsumer.getConsumerRecord().get();
        assertNull(consumerRecord);
    }

    /**
     * <p>
     * Test the transaction is not on-chain.
     * </p>
     */
    @Test
    @SneakyThrows
    public void test_The_transaction_is_not_on_chain() {
        // Prepare the data.
        final String transactionMessage = readJsonFromFile(METABUS_CONFIRMING_TRANSACTION_MESSAGE);
        final ConfirmingTransaction transaction = objectMapper.readValue(transactionMessage,
                ConfirmingTransaction.class);
        transaction.setSubmittedDate(Instant.now());

        final UnconfirmedTxJPA unconfirmedTxJPA = UnconfirmedTxJPA.builder().isDeleted(false)
                .txHash(transaction.getTxHash()).metadata(transaction.getJobBatch().toString())
                .createdDate(Instant.now()).lastUpdated(Instant.now()).build();
        final JobJPA jobJPA = ConfirmingTransactionConsumerIT.toJobJpa(transaction.getJobBatch().getJobs().get(0));
        final List<UnconfirmedTxJPA> unconfirmedList = unConfirmedTxRepository.findAllByTxHash(unconfirmedTxJPA.getTxHash());
        final List<Long> ids = unconfirmedList.stream().map(tx -> tx.getId()).toList();

        // Clear all data in the database
        jobRepository.deleteAll();
        unConfirmedTxRepository.deleteAllByIdInBatch(ids);
        // Insert the data to database.
        jobRepository.saveAndFlush(jobJPA);
        unConfirmedTxRepository.saveAndFlush(unconfirmedTxJPA);

        // Send the message to confirming transaction topic
        queueingService.sendConfirmingTransaction(transaction);

        ConsumerRecord<String, Job> consumerRecord = null;
        while(Objects.isNull(consumerRecord)){
            consumerRecord = kafkaConsumer.getConsumerRecord().get();
        }

        // check the result
        assertNotNull(consumerRecord);
        Job jobPushedToKafka = consumerRecord.value();
        assertEquals(transaction.getJobBatch().getJobs().get(0).getId(), jobPushedToKafka.getId());
    }

    /**
     * <p>
     * Map The Job Object to JobJPA Object
     * </p>
     * 
     * @param job The source object
     * @return The mapped JobJPA Object
     */
    private static JobJPA toJobJpa(final Job job) {
        final BusinessData businessData = job.getBusinessData();
        return JobJPA.builder()
                .id(job.getId())
                .state(JobState.PENDING)
                .type(businessData.getType())
                .subType(businessData.getSubType())
                .data(businessData.getData())
                .pubKey(businessData.getPubKey())
                .signature(businessData.getSignature())
                .group(job.getGroup())
                .groupType(job.getGroupType())
                .isDeleted(false)
                .retryCount(job.getRetryCount())
                .build();
    }
}
