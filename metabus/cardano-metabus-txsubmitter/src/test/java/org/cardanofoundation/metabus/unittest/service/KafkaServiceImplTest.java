package org.cardanofoundation.metabus.unittest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import org.cardanofoundation.metabus.service.impl.KafkaServiceImpl;
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
 * KafkaServiceImpl Test Class
 * </p>
 *
 * @author (Sotatek) joey.dao
 * @category Unit-test
 * @since 2023/8
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class KafkaServiceImplTest {

    /**
     * Kafka Template mock object.
     */
    @Mock
    KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * <p>
     * Kafka service implementation mock
     * </p>
     */
    @InjectMocks
    @Spy
    KafkaServiceImpl kafkaService;

    /**
     * <p>
     * Test case 1: Verify that the message is sent successfully - This test case
     * should verify that when the sendMessage function is called with valid
     * arguments, the send method is called on the kafkaTemplate object with the
     * correct arguments and the message is sent successfully.
     * </p>
     */
    @Test
    void testSendMessage_VerifyMessageIsSentSuccessfully() {
        // Arrange
        final String message = "message";
        final String topic = "topic";
        final String key = "key";

        final SendResult<String, Object> result = mock(SendResult.class);
        // Mock
        final CompletableFuture<SendResult<String, Object>> future = mock(CompletableFuture.class);
        when(future.join()).thenReturn(result);
        when(kafkaTemplate.send(topic, key, message)).thenReturn(future);

        // Act
        kafkaService.sendMessage(message, topic, key);

        // Assert
        // Verify that the send method is called on the kafkaTemplate object with the
        // correct arguments and the message is sent successfully
        verify(kafkaTemplate, times(1)).send(topic, key, message);
    }

    /**
     * <p>
     * Test case 2: Verify that an exception is thrown when sending fails - This
     * test case should verify that when an exception occurs while calling the send
     * method on the kafkaTemplate object, a RuntimeException is thrown with an
     * appropriate error message.
     * </p>
     */
    @Test
    void testSendMessage_VerifyExceptionIsThrownWhenSendingFails() {
        // Arrange
        final String message = "message";
        final String topic = "topic";
        final String key = "key";

        when(kafkaTemplate.send(topic, key, message))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Error")));

        // Act and Assert
        // Verify that a RuntimeException is thrown with an appropriate error message
        final Exception exception = assertThrows(RuntimeException.class, () -> kafkaService.sendMessage(message, topic, key));
        assertEquals("Error to pushing message to Kafka", exception.getMessage());
    }
}
