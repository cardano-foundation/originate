package org.cardanofoundation.metabus.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.metabus.application.exceptions.JobProducerErrors;
import org.cardanofoundation.metabus.application.exceptions.JobProducerException;
import org.cardanofoundation.metabus.common.offchain.ConfirmingTransaction;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.service.QueueingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaServiceImpl implements QueueingService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.jobSchedule.name}")
    private String scheduleTopic;

    @Value("${kafka.topics.deadLetter.name}")
    private String deadLetterTopic;

    @Value("${kafka.topics.confirmingTransaction.name}")
    private String confirmingTransactionTopic;

    private static final String LOG_MESSAGE_SEND_JOB_SUCCESS = "Send job {} to topic {} successfully";

    private static final String LOG_MESSAGE_SEND_JOB_FAILED = "Send job {} to topic {} failed";

    @Override
    public void sendJob(final Job job) {
        final String jobId = job.getId().toString();
        final CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(scheduleTopic, jobId, job);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error(LOG_MESSAGE_SEND_JOB_FAILED, job.getId(), scheduleTopic, ex);
                throw new JobProducerException(JobProducerErrors.ERROR_PUSHING_JOB_KAFKA);
            } else {
                log.info(LOG_MESSAGE_SEND_JOB_SUCCESS, job.getId(), scheduleTopic);
            }
        });
    }

    @Override
    public void sendJobToDlq(final Job job) {
        final String jobId = job.getId().toString();
        sendMessageToKafkaTopic(job, deadLetterTopic, jobId);
    }

    @Override
    public void sendJobSync(final Job job) {
        final String jobId = job.getId().toString();
        sendMessageToKafkaTopic(job, scheduleTopic, jobId);
    }

    @Override
    public void sendConfirmingTransaction(final ConfirmingTransaction transaction) {
        final String txHash = transaction.getTxHash();
        sendMessageToKafkaTopic(transaction, confirmingTransactionTopic, txHash);
    }


    /**
     * <p>
     * Send the message to the kafka topic
     * </p>
     * 
     * @param <T> The generic type of the message
     * @param message The message
     * @param topic The target topic
     * @param key The target key
     */
    private <T> void sendMessageToKafkaTopic(final T message, final String topic, final String key) {
        try {
            kafkaTemplate.send(topic, key, message).join();
            log.info(LOG_MESSAGE_SEND_JOB_SUCCESS, key, topic);
        } catch (final Exception ex) {
            log.error(LOG_MESSAGE_SEND_JOB_FAILED, key, topic, ex);
            throw new JobProducerException(JobProducerErrors.ERROR_PUSHING_JOB_KAFKA);
        }
    }
}
