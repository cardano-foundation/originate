package org.cardanofoundation.metabus.unittest.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.bloxbean.cardano.yaci.core.protocol.chainsync.messages.Point;
import com.bloxbean.cardano.yaci.core.protocol.chainsync.messages.Tip;
import com.bloxbean.cardano.yaci.helper.TipFinder;
import org.cardanofoundation.metabus.application.exceptions.TransactionHashNotFoundOnChainException;
import org.cardanofoundation.metabus.application.exceptions.UnreachableCheckingTimeException;
import org.cardanofoundation.metabus.common.entities.BlockJPA;
import org.cardanofoundation.metabus.common.entities.JobJPA;
import org.cardanofoundation.metabus.common.entities.UnconfirmedTxJPA;
import org.cardanofoundation.metabus.common.enums.JobState;
import org.cardanofoundation.metabus.common.offchain.BusinessData;
import org.cardanofoundation.metabus.common.offchain.ConfirmingTransaction;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.common.offchain.JobBatch;
import org.cardanofoundation.metabus.repos.BlockRepository;
import org.cardanofoundation.metabus.repos.JobRepository;
import org.cardanofoundation.metabus.repos.UnconfirmedTxRepository;
import org.cardanofoundation.metabus.repos.UtxoRepository;
import org.cardanofoundation.metabus.service.QueueingService;
import org.cardanofoundation.metabus.service.impl.JobServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

/**
 * <p>
 * The Job Service Implementation Class Unit Test
 * </p>
 *
 * @Modified (Sotatek) joey.dao
 * @category Unit Test
 * @since 2023/08
 */
@ExtendWith(MockitoExtension.class)
public class JobServiceImplTest {

    @Mock
    private QueueingService queueingService;

    @Mock
    private UnconfirmedTxRepository unconfirmedTxRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UtxoRepository utxoRepository;

    @Mock
    private BlockRepository blockRepository;

    @Mock
    TipFinder tipFinder;

    @InjectMocks
    @Spy
    private JobServiceImpl jobService;

    private final Integer reconfirmDelayMilliseconds = 900000;

    private final Integer retryConfirmingTransactionDelayTime = 5000;
    
    private final Integer crawlerMaxBlockLag = 5;

    /**
     * <p>
     * Inject the constant variable to the service implementation class before each
     * test is executed.
     * </p>
     * 
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    @BeforeEach
    void setUp() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        final Field reconfirmDelayMillisecondsField = JobServiceImpl.class
                .getDeclaredField("reconfirmDelayMilliseconds");
        reconfirmDelayMillisecondsField.setAccessible(true);
        reconfirmDelayMillisecondsField.set(jobService, reconfirmDelayMilliseconds);

        final Field retryConfirmingTransactionDelayTimeField = JobServiceImpl.class
                .getDeclaredField("retryConfirmingTransactionDelayTime");
        retryConfirmingTransactionDelayTimeField.setAccessible(true);
        retryConfirmingTransactionDelayTimeField.set(jobService, retryConfirmingTransactionDelayTime);
        
        final Field crawlerMaxBlockLagField = JobServiceImpl.class
                .getDeclaredField("crawlerMaxBlockLag");
        crawlerMaxBlockLagField.setAccessible(true);
        crawlerMaxBlockLagField.set(jobService, crawlerMaxBlockLag);
    }

    @Test
    void test_create_job_should_success() {
        // Prepare data
        final Job job = Job.builder().id(1L)
                .businessData(BusinessData.builder()
                        .data(new Object())
                        .pubKey(new byte[]{1, 2, 3})
                        .build())
                .build();

        // Assert
        assertDoesNotThrow(() -> {
            jobService.createJob(job);
        }, "create a job should not throw an exception");

        Mockito.verify(queueingService, Mockito.times(1)).sendJob(job);
    }

    /**
     * <p>
     * Test that the function throws an UnreachableCheckingTimeException when the
     * time to check the transaction on-chain has not been reached.
     * </p>
     */
    @Test
    public void testCheckingUnconfirmedTransaction_UnreachableCheckingTime() {
        // Set up mock objects and data
        final ConfirmingTransaction transaction = mock(ConfirmingTransaction.class);

        when(transaction.getSubmittedDate()).thenReturn(Instant.now());

        // Call the method under test
        assertThrows(UnreachableCheckingTimeException.class,
                () -> jobService.checkingUnconfirmedTransaction(transaction));
    }

    /**
     * <p>
     * Test that the function throws a TransactionHashNotFoundOnChainException when
     * the transaction is not found on-chain. (Empty list on find by txHash)
     * </p>
     */
    @Test
    public void testCheckingUnconfirmedTransaction_TransactionNotFoundOnChain_EmptyTxList() {
        // Set up mock objects and data
        final ConfirmingTransaction transaction = mock(ConfirmingTransaction.class);

        when(transaction.getSubmittedDate())
                .thenReturn(Instant.now().minusSeconds(reconfirmDelayMilliseconds / 1000 + 1));
        when(transaction.getTxHash()).thenReturn("testTxHash");
        BlockJPA block = new BlockJPA();
        block.setBlockNo(10L);
        when(blockRepository.findTopByOrderByIdDesc()).thenReturn(Optional.of(block));

        Tip tipRes = new Tip(new Point(), 10L);
        when(tipFinder.find()).thenReturn(Mono.just(tipRes));

        // Call the method under test
        assertThrows(TransactionHashNotFoundOnChainException.class,
                () -> jobService.checkingUnconfirmedTransaction(transaction));
    }

    /**
     * <p>
     * Test that the function throws a TransactionHashNotFoundOnChainException when
     * the transaction is not found on-chain. (The unconfirmed record is deleted)
     * </p>
     */
    @Test
    public void testCheckingUnconfirmedTransaction_TransactionNotFoundOnChain_Not_Deleted() {
        // Set up mock objects and data
        final ConfirmingTransaction transaction = mock(ConfirmingTransaction.class);

        when(transaction.getSubmittedDate())
                .thenReturn(Instant.now().minusSeconds(reconfirmDelayMilliseconds / 1000 + 1));
        when(transaction.getTxHash()).thenReturn("testTxHash");

        final List<UnconfirmedTxJPA> unconfirmedTxList = new ArrayList<>();
        unconfirmedTxList.add(UnconfirmedTxJPA.builder().isDeleted(false).build());

        doReturn(unconfirmedTxList).when(unconfirmedTxRepository).findAllByTxHash("testTxHash");

        BlockJPA block = new BlockJPA();
        block.setBlockNo(10L);
        when(blockRepository.findTopByOrderByIdDesc()).thenReturn(Optional.of(block));

        Tip tipRes = new Tip(new Point(), 10L);
        when(tipFinder.find()).thenReturn(Mono.just(tipRes));

        // Call the method under test
        assertThrows(TransactionHashNotFoundOnChainException.class,
                () -> jobService.checkingUnconfirmedTransaction(transaction));
    }

    /**
     * <p>
     * Test that the function correctly retrieves an UnconfirmedTxJPA object from
     * the unconfirmedTxRepository when the transaction is found on-chain.
     * </p>
     */
    @Test
    public void testCheckingUnconfirmedTransaction_TransactionFoundOnChain() {
        // Set up mock objects and data
        final ConfirmingTransaction transaction = mock(ConfirmingTransaction.class);
        when(transaction.getSubmittedDate())
                .thenReturn(Instant.now().minusSeconds(reconfirmDelayMilliseconds / 1000 + 1));
        when(transaction.getTxHash()).thenReturn("testTxHash");

        final UnconfirmedTxJPA unconfirmedTx = mock(UnconfirmedTxJPA.class);
        when(unconfirmedTx.getIsDeleted()).thenReturn(true);
        when(unconfirmedTxRepository.findAllByTxHash("testTxHash"))
                .thenReturn(Collections.singletonList(unconfirmedTx));

        // Call the method under test
        jobService.checkingUnconfirmedTransaction(transaction);

        // Verify that the unconfirmedTxRepository was called with the correct arguments
        verify(unconfirmedTxRepository).findAllByTxHash("testTxHash");
    }

    /**
     * <p>
     * Test that the function correctly checks if the UnconfirmedTxJPA object is
     * deleted or not, and throws a TransactionHashNotFoundOnChainException if it is
     * not deleted. (is null)
     * </p>
     */
    @Test
    public void testCheckingUnconfirmedTransaction_TransactionNotDeleted() {
        // Set up mock objects and data
        final ConfirmingTransaction transaction = mock(ConfirmingTransaction.class);
        when(transaction.getSubmittedDate())
                .thenReturn(Instant.now().minusSeconds(reconfirmDelayMilliseconds / 1000 + 1));
        when(transaction.getTxHash()).thenReturn("testTxHash");

        final UnconfirmedTxJPA unconfirmedTx = mock(UnconfirmedTxJPA.class);
        when(unconfirmedTx.getIsDeleted()).thenReturn(null);

        when(unconfirmedTxRepository.findAllByTxHash("testTxHash"))
                .thenReturn(Collections.singletonList(unconfirmedTx));

        BlockJPA block = new BlockJPA();
        block.setBlockNo(9L);
        when(blockRepository.findTopByOrderByIdDesc()).thenReturn(Optional.of(block));

        Tip tipRes = new Tip(new Point(), 10L);
        when(tipFinder.find()).thenReturn(Mono.just(tipRes));

        // Call the method under test
        assertThrows(TransactionHashNotFoundOnChainException.class,
                () -> jobService.checkingUnconfirmedTransaction(transaction));
    }

    @Test
    public void testCheckingUnconfirmedTransaction_NotUpToTip() {
        // Set up mock objects and data
        final ConfirmingTransaction transaction = mock(ConfirmingTransaction.class);
        when(transaction.getSubmittedDate())
                .thenReturn(Instant.now().minusSeconds(reconfirmDelayMilliseconds / 1000 + 1));
        when(transaction.getTxHash()).thenReturn("testTxHash");

        final UnconfirmedTxJPA unconfirmedTx = mock(UnconfirmedTxJPA.class);
        when(unconfirmedTx.getIsDeleted()).thenReturn(null);

        when(unconfirmedTxRepository.findAllByTxHash("testTxHash"))
                .thenReturn(Collections.singletonList(unconfirmedTx));

        BlockJPA block = new BlockJPA();
        block.setBlockNo(10L);
        when(blockRepository.findTopByOrderByIdDesc()).thenReturn(Optional.of(block));

        Tip tipRes = new Tip(new Point(), 1000L);
        when(tipFinder.find()).thenReturn(Mono.just(tipRes));

        // Call the method under test
        assertThrows(UnreachableCheckingTimeException.class,
                () -> jobService.checkingUnconfirmedTransaction(transaction));
    }

    @Test
    public void testCheckingUnconfirmedTransaction_NotUpToTipEmptyBlockDB() {
        // Set up mock objects and data
        final ConfirmingTransaction transaction = mock(ConfirmingTransaction.class);
        when(transaction.getSubmittedDate())
                .thenReturn(Instant.now().minusSeconds(reconfirmDelayMilliseconds / 1000 + 1));
        when(transaction.getTxHash()).thenReturn("testTxHash");

        final UnconfirmedTxJPA unconfirmedTx = mock(UnconfirmedTxJPA.class);
        when(unconfirmedTx.getIsDeleted()).thenReturn(null);

        when(unconfirmedTxRepository.findAllByTxHash("testTxHash"))
                .thenReturn(Collections.singletonList(unconfirmedTx));

        when(blockRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());

        // Call the method under test
        assertThrows(UnreachableCheckingTimeException.class,
                () -> jobService.checkingUnconfirmedTransaction(transaction));
    }

    @Test
    public void testCheckingUnconfirmedTransaction_NotUpToTipNullQuery() {
        // Set up mock objects and data
        final ConfirmingTransaction transaction = mock(ConfirmingTransaction.class);
        when(transaction.getSubmittedDate())
                .thenReturn(Instant.now().minusSeconds(reconfirmDelayMilliseconds / 1000 + 1));
        when(transaction.getTxHash()).thenReturn("testTxHash");

        final UnconfirmedTxJPA unconfirmedTx = mock(UnconfirmedTxJPA.class);
        when(unconfirmedTx.getIsDeleted()).thenReturn(null);

        when(unconfirmedTxRepository.findAllByTxHash("testTxHash"))
                .thenReturn(Collections.singletonList(unconfirmedTx));

        BlockJPA block = new BlockJPA();
        block.setBlockNo(10L);
        when(blockRepository.findTopByOrderByIdDesc()).thenReturn(Optional.of(block));

        when(tipFinder.find()).thenReturn(Mono.empty());

        // Call the method under test
        assertThrows(UnreachableCheckingTimeException.class,
                () -> jobService.checkingUnconfirmedTransaction(transaction));
    }

    @Test
    public void testCheckingUnconfirmedTransaction_NotUpToTipEmptyBlockDB_EmptyUnconfirmedTxDB() {
        // Set up mock objects and data
        final ConfirmingTransaction transaction = mock(ConfirmingTransaction.class);
        when(transaction.getSubmittedDate())
                .thenReturn(Instant.now().minusSeconds(reconfirmDelayMilliseconds / 1000 + 1));
        when(transaction.getTxHash()).thenReturn("testTxHash");

        when(unconfirmedTxRepository.findAllByTxHash("testTxHash"))
                .thenReturn(Collections.emptyList());

        when(blockRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());

        // Call the method under test
        assertThrows(UnreachableCheckingTimeException.class,
                () -> jobService.checkingUnconfirmedTransaction(transaction));
    }

    @Test
    public void testCheckingUnconfirmedTransaction_NotUpToTipNullQuery_EmptyUnconfirmedTxDB() {
        // Set up mock objects and data
        final ConfirmingTransaction transaction = mock(ConfirmingTransaction.class);
        when(transaction.getSubmittedDate())
                .thenReturn(Instant.now().minusSeconds(reconfirmDelayMilliseconds / 1000 + 1));
        when(transaction.getTxHash()).thenReturn("testTxHash");

        when(unconfirmedTxRepository.findAllByTxHash("testTxHash"))
                .thenReturn(Collections.emptyList());

        BlockJPA block = new BlockJPA();
        block.setBlockNo(10L);
        when(blockRepository.findTopByOrderByIdDesc()).thenReturn(Optional.of(block));

        when(tipFinder.find()).thenReturn(Mono.empty());

        // Call the method under test
        assertThrows(UnreachableCheckingTimeException.class,
                () -> jobService.checkingUnconfirmedTransaction(transaction));
    }

    /**
     * <p>
     * Test that the function correctly filters the list of jobs into
     * retryableListJobs and deadJobs based on their retryCount.
     * </p>
     */
    @Test
    public void testDetachJobsAndResend_RetrieveAndUpdateJobs() {
        // Set up mock objects and data
        final Job job1 = mock(Job.class);
        final Job job2 = mock(Job.class);
        final List<Job> jobs = List.of(job1, job2);

        final JobBatch jobBatch = mock(JobBatch.class);
        when(jobBatch.getJobs()).thenReturn(jobs);

        final ConfirmingTransaction transaction = mock(ConfirmingTransaction.class);
        when(transaction.getJobBatch()).thenReturn(jobBatch);
        when(transaction.getTxHash()).thenReturn("txHashTest");

        // Call the method under test
        jobService.detachJobsAndResend(transaction);

        // Verify that the getJobs method was called on the jobBatch object
        verify(jobBatch).getJobs();

        // Verify that the updateBeforeRetry method was called on each job object
        verify(job1).updateBeforeRetry();
        verify(job2).updateBeforeRetry();
    }

    /**
     * <p>
     * Test that the function correctly updates the information of retryable jobs
     * using the jobRepository.
     * </p>
     */
    @Test
    public void testDetachJobsAndResend_FilterJobs() {
        // Set up mock objects and data
        final Job job1 = mock(Job.class);
        when(job1.getRetryCount()).thenReturn(1);

        final Job job2 = mock(Job.class);
        when(job2.getRetryCount()).thenReturn(0);

        final List<Job> jobs = List.of(job1, job2);

        final JobBatch jobBatch = mock(JobBatch.class);
        when(jobBatch.getJobs()).thenReturn(jobs);

        final ConfirmingTransaction transaction = mock(ConfirmingTransaction.class);
        when(transaction.getJobBatch()).thenReturn(jobBatch);
        when(transaction.getTxHash()).thenReturn("txHashTest");

        // Call the method under test
        jobService.detachJobsAndResend(transaction);

        // Verify that the methods was called on each job object
        verify(queueingService).sendJobSync(job1);
        verify(queueingService).sendJobToDlq(job2);
    }

    /**
     * <p>
     * Test that the function correctly split the jobList and
     * sends them to the dead-letter queue or job schedule queue using the
     * queueingService.
     * </p>
     */
    @Test
    public void testDetachJobsAndResend_SetStateAndSendRetryableJobs() {
        // Set up mock objects and data
        final Job job1 = mock(Job.class);
        when(job1.getRetryCount()).thenReturn(1);
        when(job1.getId()).thenReturn(1L);

        final Job job2 = mock(Job.class);
        when(job2.getRetryCount()).thenReturn(0);
        when(job2.getId()).thenReturn(2L);

        final List<Job> jobs = List.of(job1, job2);

        final JobBatch jobBatch = mock(JobBatch.class);
        when(jobBatch.getJobs()).thenReturn(jobs);

        final ConfirmingTransaction transaction = mock(ConfirmingTransaction.class);
        when(transaction.getJobBatch()).thenReturn(jobBatch);
        when(transaction.getTxHash()).thenReturn("txHashTest");

        final JobJPA jobJPA = mock(JobJPA.class);
        final JobJPA jobJPA2 = mock(JobJPA.class);
        final UnconfirmedTxJPA unconfirmedTxJPA = mock(UnconfirmedTxJPA.class);
        final List<UnconfirmedTxJPA> unconfirmedTxJPAList = List.of(unconfirmedTxJPA);

        when(jobRepository.findAllById(List.of(1L))).thenReturn(List.of(jobJPA));
        when(jobRepository.findAllById(List.of(2L))).thenReturn(List.of(jobJPA2));
        when(unconfirmedTxRepository.findAllByTxHash("txHashTest")).thenReturn(unconfirmedTxJPAList);
        when(unconfirmedTxJPA.getId()).thenReturn(1L);

        // Call the method under test
        jobService.detachJobsAndResend(transaction);

        // Verify that the setState method was called on the retryable job object with
        // the correct arguments
        verify(job1).setState(JobState.PENDING);

        // Verify that the sendJobSync method was called on the queueingService object
        // with the correct arguments
        verify(queueingService).sendJobSync(job1);

        // Verify that the setState method was called on the dead job object with the
        // correct arguments
        verify(job2).setState(JobState.FAILED);

        // Verify that the sendJobToDlq method was called on the queueingService object
        // with the correct arguments
        verify(queueingService).sendJobToDlq(job2);
        // Verify that the updateJobInfoBeforeDLQ method was called on the jobJPA object
        verify(jobJPA2).updateJobInfoBeforeDLQ();

        // Verify that the updateJobInfoBeforeRetry method was called on the jobJPA
        // object
        verify(jobJPA).updateJobInfoBeforeRetry();

        // Verify that the used utxo that belongs to the tx is removed from the database
        verify(utxoRepository).deleteByUnconfirmedTxIdIn(List.of(1L));
    }

    /**
     * <p>
     * Test case 1: Retry count is 0 - This test case should verify that when the
     * retryCountsForUnexpectedError property of the transaction object is 0, the
     * function returns immediately without republishing the transaction to the main
     * queue.
     * </p>
     */
    @Test
    void testRePublishConfirmingTransaction_RetryCountIsZero() {
        // Arrange
        final ConfirmingTransaction confirmingTransaction = ConfirmingTransaction.builder().txHash("txHash")
                .jobBatch(null).submittedDate(Instant.now())
                .retryCountsForUnexpectedError(0L)
                .build();

        // Act
        jobService.rePublishConfirmingTransaction(confirmingTransaction);

        // Assert
        // Verify that the function returns immediately without republishing the
        // transaction to the main queue
        verify(queueingService, never()).sendConfirmingTransaction(any());
    }

    /**
     * <p>
     * Test case 2: Retry count is greater than 0 - This test case should verify
     * that when the retryCountsForUnexpectedError property of the transaction
     * object is greater than 0, the function decrements the retry count by one and
     * republishes the transaction to the main queue with a delay.
     * </p>
     */
    @Test
    void testRePublishConfirmingTransaction_RetryCountIsGreaterThanZero() {
        // Arrange
        final ConfirmingTransaction confirmingTransaction = ConfirmingTransaction.builder().txHash("txHash")
                .jobBatch(null).submittedDate(Instant.now())
                .retryCountsForUnexpectedError(1L)
                .build();

        // Act
        jobService.rePublishConfirmingTransaction(confirmingTransaction);

        // Assert
        // Verify that the function decrements the retry count by one and republishes
        // the transaction to the main queue with a delay
        verify(queueingService).sendConfirmingTransaction(confirmingTransaction);
    }

    /**
     * <p>
     * Test case 3: Retry count is less than 0 - This test case should verify that
     * when the retryCountsForUnexpectedError property of the transaction object is
     * less than 0, the function does not decrement the retry count and republishes
     * the transaction to the main queue with a delay.
     * </p>
     */
    @Test
    void testRePublishConfirmingTransaction_RetryCountIsLessThanZero() {
        // Arrange
        final ConfirmingTransaction confirmingTransaction = ConfirmingTransaction.builder().txHash("txHash")
                .jobBatch(null).submittedDate(Instant.now())
                .retryCountsForUnexpectedError(-1L)
                .build();

        // Act
        jobService.rePublishConfirmingTransaction(confirmingTransaction);

        // Assert
        // Verify that the function does not decrement the retry count and republishes
        // the transaction to the main queue with a delay
        verify(queueingService, times(1)).sendConfirmingTransaction(confirmingTransaction);
    }
}
