package org.cardanofoundation.metabus.infrastructure.kafka;

import org.cardanofoundation.metabus.application.exceptions.TransactionHashNotFoundOnChainException;
import org.cardanofoundation.metabus.application.exceptions.UnreachableCheckingTimeException;
import org.cardanofoundation.metabus.common.offchain.ConfirmingTransaction;
import org.cardanofoundation.metabus.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.SameIntervalTopicReuseStrategy;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * The Confirming Transaction Consumer
 * </p>
 *
 * @author (Sotatek) joey.dao
 * @version 0.01
 * @category Consumer
 * @since 2023/08
 */
@Slf4j
@Component
public class ConfirmingTransactionConsumer {

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${kafka.topics.confirmingTransaction.retryableTopic.reconfirmDelayMilliseconds}")
    private Integer reconfirmDelayMilliseconds;

    @Autowired
    private JobService jobService;

    /**
     * <p>
     * - Listen to confirming transaction topic.
     * </p>
     * <p>
     * - Looking for the transaction is On-chain.
     * </p>
     * <p>
     * - If the transaction is not on-chain push the transaction info to the dlt queue
     * and re-push the transaction info to confirming transaction topic.
     * </p>
     * 
     * @param transaction the target transaction
     * @throws Exception
     */
    @RetryableTopic(
        attempts = "${kafka.topics.confirmingTransaction.retryableTopic.attemptCounts}", 
        autoCreateTopics = "${kafka.topics.confirmingTransaction.retryableTopic.autoCreateRetryTopics}", 
        backoff = @Backoff(delayExpression = "${kafka.topics.confirmingTransaction.retryableTopic.reconfirmDelayMilliseconds}"), 
        sameIntervalTopicReuseStrategy = SameIntervalTopicReuseStrategy.SINGLE_TOPIC, 
        include = { UnreachableCheckingTimeException.class }, 
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE, 
        dltStrategy = DltStrategy.FAIL_ON_ERROR,
        replicationFactor = "1")
    @KafkaListener(topics = "${kafka.topics.confirmingTransaction.name}", containerFactory = "kafkaListenerContainerFactory")
    public void listenConfirmingTransaction(final ConfirmingTransaction transaction,
            final Acknowledgment acknowledgment) throws Exception {
        try {
            log.info("Confirming Transaction Consumer: Received message with payload: {}",
                    objectMapper.writeValueAsString(transaction));
            // Checking confirming transaction
            jobService.checkingUnconfirmedTransaction(transaction);
            log.info("Transaction with txhash is on-chain: {}", transaction.getTxHash());
        } catch (final UnreachableCheckingTimeException e) {
            log.info("The transaction with txHash {} is pending for checking (reason: {})", transaction.getTxHash(), e.getMessage());
            // Throw exception for push message to retryable topic.
            throw e;
        } catch (final TransactionHashNotFoundOnChainException e) {
            log.error("The transaction with txHash {} is currently not on-chain. Prepare for retry",
                    transaction.getTxHash());
            try {
                // When the tx is not on-chain. detach jobs into each job and resend to main
                // queue.
                log.info("Transaction with txhash is not on-chain: {}", transaction.getTxHash());
                jobService.detachJobsAndResend(transaction);
            } catch (final Exception ex) {
                log.error("Detach and resend failed.", ex);
                // The message will be sent to configured: DLQ
                throw ex;
            }
        } catch (final Exception e) {
            log.error("listenConfirmingTransaction - error processing message", e);
            // The message will be sent to configured: DLQ
            throw e;
        } finally {
            // Always ack the message.
            // The nack (not-acknowledge) is not effect in retryable-topic. 
            // Because nack mechanism will block other message to consume. 
            // So that it will conflict the idea of the non-blocking retry
            acknowledgment.acknowledge();
        }
    }

    /**
     * <p>
     * The Unexpected failed transaction will be retry publish to main queue.
     * The `@DtlHandler` is similar to `@KafkaListener` The method will act as the message listener.
     * </p>
     * 
     * <p>
     * **Note: If you are worrying about the redundant message is written over again
     * in DLQ.
     * Kafka have a configuration that can config cleanup policies to sweep out the
     * redundant log message. (base on key)
     * 
     * @see Kafka Topic Configuration: Log Compaction, 
     * </p>
     * 
     * @param transaction The target transaction.
     * @throws JsonProcessingException
     */
    @DltHandler
    public void processMessage(final ConfirmingTransaction transaction, final Acknowledgment acknowledgment)
            throws JsonProcessingException {
        // Re-publish the message.
        log.info("Move message to dlq and prepare to re-publish to recent topic: {}",
                objectMapper.writeValueAsString(transaction));
        jobService.rePublishConfirmingTransaction(transaction);
        acknowledgment.acknowledge();
    }
}
