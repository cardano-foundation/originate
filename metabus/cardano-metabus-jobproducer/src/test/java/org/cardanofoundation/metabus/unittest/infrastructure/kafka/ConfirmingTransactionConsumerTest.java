package org.cardanofoundation.metabus.unittest.infrastructure.kafka;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.cardanofoundation.metabus.application.exceptions.TransactionHashNotFoundOnChainException;
import org.cardanofoundation.metabus.application.exceptions.UnreachableCheckingTimeException;
import org.cardanofoundation.metabus.common.offchain.ConfirmingTransaction;
import org.cardanofoundation.metabus.infrastructure.kafka.ConfirmingTransactionConsumer;
import org.cardanofoundation.metabus.service.JobService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <p>
 * The Confirming Transaction Consumer Unit Test
 * </p>
 *
 * @author (Sotatek) joey.dao
 * @version 0.01
 * @category Unit Test
 * @since 2023/08
 */
@ExtendWith(MockitoExtension.class)
public class ConfirmingTransactionConsumerTest {

    @Mock
    private JobService jobService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    @Spy
    private ConfirmingTransactionConsumer consumer;

    /**
     * <p>
     * Test Case 1: Successful Transaction Confirmation
     * </p>
     * <p>
     * This test case verifies that the function correctly logs the received
     * transaction and checks if it is unconfirmed.
     * </p>
     * <p>
     * The expected outcome is that the function logs the received transaction and
     * its status as on-chain.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    void testListenConfirmingTransaction_SuccessfulTransactionConfirmation() throws Exception {
        // Arrange
        final ConfirmingTransaction transaction = new ConfirmingTransaction();
        transaction.setTxHash("txHash");
        final Acknowledgment acknowledgment = Mockito.mock(Acknowledgment.class);

        // Act
        consumer.listenConfirmingTransaction(transaction, acknowledgment);

        // Assert
        verify(jobService).checkingUnconfirmedTransaction(transaction);
        verify(acknowledgment).acknowledge();
    }

    /**
     * <p>
     * Test Case 2: Unreachable Checking Time Exception
     * </p>
     * <p>
     * This test case verifies that the function correctly handles an
     * UnreachableCheckingTimeException by logging that the transaction is pending
     * for checking and re-throwing the exception.
     * </p>
     * <p>
     * The expected outcome is that the function logs that the transaction is
     * pending for checking and rethrows the exception.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    void testListenConfirmingTransaction_UnreachableCheckingTimeException() throws Exception {
        // Arrange
        final ConfirmingTransaction transaction = new ConfirmingTransaction();
        transaction.setTxHash("txHash");
        final Acknowledgment acknowledgment = Mockito.mock(Acknowledgment.class);
        doThrow(new UnreachableCheckingTimeException("It's not the right time")).when(jobService)
                .checkingUnconfirmedTransaction(transaction);

        // Act
        try {
            consumer.listenConfirmingTransaction(transaction, acknowledgment);
        } catch (final UnreachableCheckingTimeException e) {
            // Assert
            assertEquals("It's not the right time", e.getMessage());
            verify(acknowledgment).acknowledge();
        } catch (final Exception e) {
            fail("The expected exception is not throwing");
        }
    }

    /**
     * <p>
     * Test Case 3: Transaction Hash Not Found On Chain Exception
     * </p>
     * <p>
     * - This test case verifies that the function correctly handles a
     * `TransactionHashNotFoundOnChainException` by logging that the
     * transaction is not on-chain, detaching jobs, and resending them to the main
     * queue (job.schedule)
     * </p>
     * <p>
     * - The expected outcome is that the function logs that the transaction is not
     * on-chain, detaches jobs, and re-sends them to the main queue.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    void testListenConfirmingTransaction_TransactionHashNotFoundOnChainException() throws Exception {
        // Arrange
        final ConfirmingTransaction transaction = new ConfirmingTransaction();
        transaction.setTxHash("txHash");
        final Acknowledgment acknowledgment = Mockito.mock(Acknowledgment.class);
        doThrow(new TransactionHashNotFoundOnChainException("not found")).when(jobService)
                .checkingUnconfirmedTransaction(transaction);

        // Act
        try {
            consumer.listenConfirmingTransaction(transaction, acknowledgment);
        } catch (final TransactionHashNotFoundOnChainException e) {
            // Assert
            verify(jobService).detachJobsAndResend(transaction);
            verify(acknowledgment).acknowledge();
        } catch (final Exception e) {
            fail("The expected exception is not throwing");
        }
    }

    /**
     * <p>
     * Test Case 4: Detach and Resend Failed
     * </p>
     * <p>
     * - This test case verifies that the function correctly handles a failure to
     * detach and resend jobs by logging an error message and
     * rethrowing the exception.
     * </p>
     * 
     * <p>
     * - The expected outcome is that the function logs an error message and
     * rethrows the exception.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    void testListenConfirmingTransaction_DetachAndResendFailed() throws Exception {
        // Arrange
        final ConfirmingTransaction transaction = new ConfirmingTransaction();
        transaction.setTxHash("txHash");
        final Acknowledgment acknowledgment = Mockito.mock(Acknowledgment.class);
        doThrow(new TransactionHashNotFoundOnChainException("not found")).when(jobService)
                .checkingUnconfirmedTransaction(transaction);
        doThrow(new RuntimeException("Detach and resend failed.")).when(jobService).detachJobsAndResend(transaction);

        // Act
        try {
            consumer.listenConfirmingTransaction(transaction, acknowledgment);
            fail("The expected exception is not throwing");
        } catch (final Exception e) {
            // Assert
            assertEquals("Detach and resend failed.", e.getMessage());
            verify(acknowledgment).acknowledge();
        }
    }

    /**
     * <p>
     * Test Case 5: Other Exceptions
     * </p>
     * <p>
     * - This test case verifies that the function correctly handles other
     * exceptions by logging an error message and re-throwing the exception.
     * </p>
     * <p>
     * - The expected outcome is that the function logs an error message and
     * rethrows the exception.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    void testListenConfirmingTransaction_OtherExceptions() throws Exception {
        // Arrange
        final ConfirmingTransaction transaction = new ConfirmingTransaction();
        transaction.setTxHash("txHash");
        final Acknowledgment acknowledgment = Mockito.mock(Acknowledgment.class);
        doThrow(new RuntimeException("Not found")).when(jobService)
                .checkingUnconfirmedTransaction(transaction);

        // Act
        try {
            consumer.listenConfirmingTransaction(transaction, acknowledgment);
            fail("The expected exception is not throwing");
        } catch (final Exception e) {
            // Assert
            assertEquals("Not found", e.getMessage());
            verify(acknowledgment).acknowledge();
        }
    }

    /**
     * <p>
     * Test Case 1: Successful Message Processing
     * </p>
     * <p>
     * - This test case verifies that the function correctly logs the received
     * transaction, republishes it, and acknowledges the message.
     * </p>
     * <p>
     * - The expected outcome is that the function logs the received transaction,
     * republishes it, and acknowledges the message.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    void testProcessMessage_SuccessfulMessageProcessing() throws Exception {
        // Arrange
        final ConfirmingTransaction transaction = new ConfirmingTransaction();
        transaction.setTxHash("txHash");
        final Acknowledgment acknowledgment = Mockito.mock(Acknowledgment.class);

        // Act
        consumer.processMessage(transaction, acknowledgment);

        // Assert
        verify(jobService).rePublishConfirmingTransaction(transaction);
        verify(acknowledgment).acknowledge();
    }
}
