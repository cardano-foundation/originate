package org.cardanofoundation.metabus.service;


import org.cardanofoundation.metabus.common.offchain.ConfirmingTransaction;
import org.cardanofoundation.metabus.common.offchain.Job;

/**
 * <p>
 * Queueing Service Interface Class.
 * <b>(For job scheduling)</b>
 * </p>
 * 
 * @Modified (sotatek) - joey.dao
 * @since 2023/08
 */
public interface QueueingService {
    /**
     * <p>
     * Send the target job to schedule topic.
     * </p>
     * 
     * @param job the target job
     */
    void sendJob(Job job);

    /**
     * <p>
     * Send job to Job's dead letter queue
     * </p>
     * 
     * @param job the target job.
     */
    void sendJobToDlq(final Job job);

    /**
     * <p>
     * Send the target job to schedule topic.
     * </p>
     * 
     * @param job the target job
     */
    void sendJobSync(final Job job);

    /**
     * <p>
     * Send the target unexpected failed transaction to confirming transaction queue
     * </p>
     * 
     * @param transaction the confirming transaction info
     */
    void sendConfirmingTransaction(final ConfirmingTransaction transaction);
}
