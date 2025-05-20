package org.cardanofoundation.metabus.service;

import java.time.Instant;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import org.cardanofoundation.metabus.application.exceptions.TransactionHashNotFoundOnChainException;
import org.cardanofoundation.metabus.application.exceptions.UnreachableCheckingTimeException;
import org.cardanofoundation.metabus.common.offchain.ConfirmingTransaction;
import org.cardanofoundation.metabus.common.offchain.Job;

/**
 * <p>
 * The Job Service Interface Class
 * </p>
 * 
 * @Modified @sotatek-joeydao, thaoho
 * @since 2023/08
 * @category Service
 * @version 0.01
 */
public interface JobService {
    /**
     * <p>
     * Create job for sending for txsubmitter.
     * </p>
     * 
     * @param job
     */
    void createJob(Job job);

    /**
     * <p>
     * After submitting the transaction to local node.
     * It may take a while to sync the tx to remote node.
     * </p>
     * 
     * <b>
     * This function will support the `ConfirmingTransactionConsumer` class to
     * checking that tx is on-chain.
     * </b>
     * 
     * @param transaction the unconfirmed transaction.
     * @throws UnreachableCheckingTimeException
     * @throws TransactionHashNotFoundOnChainException
     */
    void checkingUnconfirmedTransaction(final ConfirmingTransaction transaction)
            throws UnreachableCheckingTimeException, TransactionHashNotFoundOnChainException;

    /**
     * <p>
     * Detach the jobs in jobBatch and Re-send it.
     * </p>
     * 
     * @param transaction the not on-chain transaction.
     */
    void detachJobsAndResend(final ConfirmingTransaction transaction);

    /**
     * <p>
     * Republish the Unexpected Failed Transaction to main queue (confirming.transaction.topic)
     * </p>
     * 
     * @param transaction the target transaction.
     */
    void rePublishConfirmingTransaction(final ConfirmingTransaction transaction);

    /**
     * <p>
     * The BiPredicate to checking that is possible to check the tx is on-chain.
     * </p>
     * 
     * @param submittedTime the submitted time of the transaction.
     * @param threshold     the threshold time of the checking
     * 
     */
    static final BiPredicate<Instant, Integer> isTimeToCheckOnchain = (submittedTime, threshold) -> {
        final Instant checkPointTime = submittedTime.plusMillis(threshold);
        return Instant.now().equals(checkPointTime) || Instant.now().isAfter(checkPointTime);
    };

    /**
     * <p>
     * The BiFunction to calculate the time that is possible to check the tx is
     * on-chain.
     * </p>
     * 
     * @param submittedTime the submitted time of the transaction.
     * @param threshold     the threshold time of the checking
     * @return The remaining time
     */
    static final BiFunction<Instant, Integer, Long> calTimeRemaining = (submittedTime, threshold) -> {
        final Instant checkPointTime = submittedTime.plusMillis(threshold);
        return checkPointTime.toEpochMilli() - Instant.now().toEpochMilli();
    };
}
