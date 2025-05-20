package org.cardanofoundation.metabus.unittest.infrastructure.kafka;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import org.cardanofoundation.metabus.application.exceptions.JobProducerException;
import org.cardanofoundation.metabus.common.offchain.BusinessData;
import org.cardanofoundation.metabus.common.offchain.ConfirmingTransaction;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.common.offchain.JobBatch;
import org.cardanofoundation.metabus.infrastructure.kafka.KafkaServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

/**
 * <p>
 * The Kafka Service Implementation Class Unit Test
 * </p>
 *
 * @Modified (Sotatek) joey.dao
 * @category Unit Test
 * @since 2023/08
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class KafkaServiceImplTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    @Spy
    private KafkaServiceImpl kafkaService;

    @Test
    void test_send_job_success() throws NoSuchFieldException, IllegalAccessException {
        // Prepare data
        final Job job = Job.builder().id(1L)
                .businessData(BusinessData.builder()
                        .data(new Object())
                        .pubKey(new byte[]{1, 2, 3})
                        .build())
                .build();
        final Field scheduleTopic = KafkaServiceImpl.class.getDeclaredField("scheduleTopic");
        scheduleTopic.setAccessible(true);
        final String scheduleTopicValue = "schedule topic";
        scheduleTopic.set(kafkaService, scheduleTopicValue);
        final SendResult<String, Object> result = mock(SendResult.class);
        // Mock
        final CompletableFuture<SendResult<String, Object>> future = mock(CompletableFuture.class);
        when(future.whenComplete(any())).thenAnswer(invocationOnMock -> {
            final BiConsumer<SendResult<String, Object>, Throwable> consumer = invocationOnMock.getArgument(0);
            consumer.accept(result, null);
            return future;
        });
        when(kafkaTemplate.send(eq(scheduleTopicValue), eq(job.getId().toString()), eq(job))).thenReturn(future);

        // Assert
        assertDoesNotThrow(() -> {
            kafkaService.sendJob(job);
        }, "Sending a job should not throw an exception");
    }

    @Test
    void test_send_job_failed() throws NoSuchFieldException, IllegalAccessException {
        // Prepare data
        final Job job = Job.builder().id(1L)
                .businessData(BusinessData.builder()
                        .data(new Object())
                        .pubKey(new byte[]{1, 2, 3})
                        .build())
                .build();
        final Field scheduleTopic = KafkaServiceImpl.class.getDeclaredField("scheduleTopic");
        scheduleTopic.setAccessible(true);
        final String scheduleTopicValue = "schedule topic";
        scheduleTopic.set(kafkaService, scheduleTopicValue);

        // Mock
        final CompletableFuture<SendResult<String, Object>> future = mock(CompletableFuture.class);
        when(future.whenComplete(any())).thenAnswer(invocationOnMock -> {
            final BiConsumer<SendResult<String, Object>, Throwable> consumer = invocationOnMock.getArgument(0);
            consumer.accept(null, new RuntimeException("Kafka send method error"));
            return future;
        });
        when(kafkaTemplate.send(eq(scheduleTopicValue), eq(job.getId().toString()), eq(job))).thenReturn(future);

        // Assert
        assertThrows(JobProducerException.class, () -> kafkaService.sendJob(job));
    }

    /**
     * <p>
     * Send job to dead letter queue success
     * </p>
     * 
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @Test
    void test_send_job_to_dlq_success() throws NoSuchFieldException, IllegalAccessException {
        // Prepare data
        final Job job = Job.builder().id(1L)
                .businessData(BusinessData.builder()
                        .data(new Object())
                        .pubKey(new byte[]{1, 2, 3})
                        .build())
                .build();
        final Field deadLetterTopic = KafkaServiceImpl.class.getDeclaredField("deadLetterTopic");
        deadLetterTopic.setAccessible(true);
        final String deadLetterTopicValue = "dead letter topic";
        deadLetterTopic.set(kafkaService, deadLetterTopicValue);
        final SendResult<String, Object> result = mock(SendResult.class);
        final CompletableFuture<SendResult<String, Object>> future = mock(CompletableFuture.class);

        // Mock
        when(future.join()).thenReturn(result);
        when(kafkaTemplate.send(deadLetterTopicValue, job.getId().toString(), job)).thenReturn(future);

        // Assert
        assertDoesNotThrow(() -> {
            kafkaService.sendJobToDlq(job);
        }, "Sending a job should not throw an exception");
    }

    /**
     * <p>
     * Send job to dead letter queue failed.
     * </p>
     * 
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @Test
    void test_send_job_to_dlq_failed() throws NoSuchFieldException, IllegalAccessException {
        // Prepare data
        final Job job = Job.builder().id(1L)
                .businessData(BusinessData.builder()
                        .data(new Object())
                        .pubKey(new byte[]{1, 2, 3})
                        .build())
                .build();
        final Field deadLetterTopic = KafkaServiceImpl.class.getDeclaredField("deadLetterTopic");
        deadLetterTopic.setAccessible(true);
        final String deadLetterTopicValue = "dead letter topic";
        deadLetterTopic.set(kafkaService, deadLetterTopicValue);

        // Mock
        final CompletableFuture<SendResult<String, Object>> future = mock(CompletableFuture.class);
        when(future.join()).thenThrow(new RuntimeException("Kafka send method error"));
        when(kafkaTemplate.send(deadLetterTopicValue, job.getId().toString(), job)).thenReturn(future);

        // Assert
        assertThrows(JobProducerException.class, () -> kafkaService.sendJobToDlq(job));
    }

    /**
     * <p>
     * Send job to job schedule queue (sync) success
     * </p>
     * 
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @Test
    void test_send_job_sync_success() throws NoSuchFieldException, IllegalAccessException {
        // Prepare data
        final Job job = Job.builder().id(1L)
                .businessData(BusinessData.builder()
                        .data(new Object())
                        .pubKey(new byte[]{1, 2, 3})
                        .build())
                .build();
        final Field scheduleTopic = KafkaServiceImpl.class.getDeclaredField("scheduleTopic");
        scheduleTopic.setAccessible(true);
        final String scheduleTopicValue = "schedule topic";
        scheduleTopic.set(kafkaService, scheduleTopicValue);
        final SendResult<String, Object> result = mock(SendResult.class);
        final CompletableFuture<SendResult<String, Object>> future = mock(CompletableFuture.class);

        // Mock
        when(future.join()).thenReturn(result);
        when(kafkaTemplate.send(scheduleTopicValue, job.getId().toString(), job)).thenReturn(future);

        // Assert
        assertDoesNotThrow(() -> {
            kafkaService.sendJobSync(job);
        }, "Sending a job should not throw an exception");
    }

    /**
     * <p>
     * Send job to job schedule queue (sync) failed.
     * </p>
     * 
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @Test
    void test_send_job_sync_failed() throws NoSuchFieldException, IllegalAccessException {
        // Prepare data
        final Job job = Job.builder().id(1L)
                .businessData(BusinessData.builder()
                        .data(new Object())
                        .pubKey(new byte[]{1, 2, 3})
                        .build())
                .build();
        final Field scheduleTopic = KafkaServiceImpl.class.getDeclaredField("scheduleTopic");
        scheduleTopic.setAccessible(true);
        final String scheduleTopicValue = "schedule topic";
        scheduleTopic.set(kafkaService, scheduleTopicValue);

        // Mock
        final CompletableFuture<SendResult<String, Object>> future = mock(CompletableFuture.class);
        when(future.join()).thenThrow(new RuntimeException("Kafka send method error"));
        when(kafkaTemplate.send(scheduleTopicValue, job.getId().toString(), job)).thenReturn(future);

        // Assert
        assertThrows(JobProducerException.class, () -> kafkaService.sendJobSync(job));
    }

    /**
     * <p>
     * Send confirming transaction success
     * </p>
     * 
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @Test
    void test_send_confirming_transaction_success() throws NoSuchFieldException, IllegalAccessException {
        // Prepare data
        final ConfirmingTransaction transaction = ConfirmingTransaction.builder().txHash("txHash")
                .jobBatch(new JobBatch()).submittedDate(Instant.now()).build();
        final Field confirmingTransactionTopic = KafkaServiceImpl.class.getDeclaredField("confirmingTransactionTopic");
        confirmingTransactionTopic.setAccessible(true);
        final String confirmingTransactionTopicValue = "confirmingTransactionTopic";
        confirmingTransactionTopic.set(kafkaService, confirmingTransactionTopicValue);
        final SendResult<String, Object> result = mock(SendResult.class);
        final CompletableFuture<SendResult<String, Object>> future = mock(CompletableFuture.class);

        // Mock
        when(future.join()).thenReturn(result);
        when(kafkaTemplate.send(confirmingTransactionTopicValue, "txHash", transaction)).thenReturn(future);

        // Assert
        assertDoesNotThrow(() -> {
            kafkaService.sendConfirmingTransaction(transaction);
        }, "Sending a job should not throw an exception");
    }

    /**
     * <p>
     * Send confirming transaction failed.
     * </p>
     * 
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @Test
    void test_send_confirming_transaction_failed() throws NoSuchFieldException, IllegalAccessException {
        // Prepare data
        final ConfirmingTransaction transaction = ConfirmingTransaction.builder().txHash("txHash")
                .jobBatch(new JobBatch()).submittedDate(Instant.now()).build();
        final Field confirmingTransactionTopic = KafkaServiceImpl.class.getDeclaredField("confirmingTransactionTopic");
        confirmingTransactionTopic.setAccessible(true);
        final String confirmingTransactionTopicValue = "schedule topic";
        confirmingTransactionTopic.set(kafkaService, confirmingTransactionTopicValue);

        // Mock
        final CompletableFuture<SendResult<String, Object>> future = mock(CompletableFuture.class);
        when(future.join()).thenThrow(new RuntimeException("Kafka send method error"));
        when(kafkaTemplate.send(confirmingTransactionTopicValue, "txHash", transaction)).thenReturn(future);

        // Assert
        assertThrows(JobProducerException.class, () -> kafkaService.sendConfirmingTransaction(transaction));
    }
}
