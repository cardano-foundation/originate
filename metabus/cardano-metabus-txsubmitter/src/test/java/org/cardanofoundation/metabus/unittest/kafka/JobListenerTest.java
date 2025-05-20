package org.cardanofoundation.metabus.unittest.kafka;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.kafka.JobListener;
import org.cardanofoundation.metabus.service.BatchConsumptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * #Modified By (Sotatek) joey.dao
 */
@ExtendWith(MockitoExtension.class)
class JobListenerTest {

    @Mock
    BatchConsumptionService batchConsumptionService;

    @InjectMocks
    @Spy
    JobListener jobListener;

    /**
     * <p>
     * Test that the consume method calls the consumeBasedOnTxMaxSize method of the BatchConsumptionService with the given ConsumerRecord.
     * </p>
     * 
     * @throws JsonProcessingException
     * @throws CborSerializationException
     * @throws InterruptedException
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testConsumeCallsConsumeBasedOnTxMaxSize() throws JsonProcessingException, CborSerializationException, InterruptedException, CborException, AddressExcepion {
        // Given
        final ConsumerRecord<String, Job> consumerRecord = mock(ConsumerRecord.class);
        final Acknowledgment acknowledgment = mock(Acknowledgment.class);

        // When
        jobListener.consume(consumerRecord, acknowledgment);

        // Then
        verify(batchConsumptionService).consumeBasedOnTxMaxSize(consumerRecord);
    }

    /**
     * <p>
     * Test that the consume method calls the acknowledge method of the given Acknowledgment.
     * </p>
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testConsumeCallsAcknowledge() {
        // Given
        final ConsumerRecord<String, Job> consumerRecord = mock(ConsumerRecord.class);
        final Acknowledgment acknowledgment = mock(Acknowledgment.class);

        // When
        jobListener.consume(consumerRecord, acknowledgment);

        // Then
        verify(acknowledgment).acknowledge();
    }

    /**
     * <p>
     * Test that the consume method logs an error when a CborSerializationException is thrown.
     * </p>
     * 
     * @throws JsonProcessingException
     * @throws CborSerializationException
     * @throws InterruptedException
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testConsumeLogsErrorWhenCborSerializationExceptionIsThrown()
            throws JsonProcessingException, CborSerializationException, InterruptedException, CborException, AddressExcepion {
        // Given
        final ConsumerRecord<String, Job> consumerRecord = mock(ConsumerRecord.class);
        final Acknowledgment acknowledgment = mock(Acknowledgment.class);
        final CborSerializationException exception = new CborSerializationException("Test");

        // When
        doThrow(exception).when(batchConsumptionService).consumeBasedOnTxMaxSize(consumerRecord);

        // Then
        assertDoesNotThrow(() -> jobListener.consume(consumerRecord, acknowledgment));
    }

    /**
     * <p>
     * Test that the consume method logs an error when any other exception is thrown.
     * </p>
     * 
     * @throws JsonProcessingException
     * @throws CborSerializationException
     * @throws InterruptedException
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testConsumeLogsErrorWhenOtherExceptionIsThrown()
            throws JsonProcessingException, CborSerializationException, InterruptedException, CborException, AddressExcepion {
        // Given
        final ConsumerRecord<String, Job> consumerRecord = mock(ConsumerRecord.class);
        final Acknowledgment acknowledgment = mock(Acknowledgment.class);
        final RuntimeException exception = new RuntimeException("Test");

        // When
        doThrow(exception).when(batchConsumptionService).consumeBasedOnTxMaxSize(consumerRecord);
        jobListener.consume(consumerRecord, acknowledgment);

        // Then
        assertDoesNotThrow(() -> jobListener.consume(consumerRecord, acknowledgment));
    }
}