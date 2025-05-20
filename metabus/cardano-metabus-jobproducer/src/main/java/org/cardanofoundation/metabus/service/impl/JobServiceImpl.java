package org.cardanofoundation.metabus.service.impl;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.bloxbean.cardano.yaci.core.protocol.chainsync.messages.Tip;
import com.bloxbean.cardano.yaci.helper.TipFinder;
import org.cardanofoundation.metabus.application.exceptions.TransactionHashNotFoundOnChainException;
import org.cardanofoundation.metabus.application.exceptions.UnreachableCheckingTimeException;
import org.cardanofoundation.metabus.common.entities.BaseEntity;
import org.cardanofoundation.metabus.common.entities.BlockJPA;
import org.cardanofoundation.metabus.common.entities.JobJPA;
import org.cardanofoundation.metabus.common.entities.UnconfirmedTxJPA;
import org.cardanofoundation.metabus.common.enums.JobState;
import org.cardanofoundation.metabus.common.offchain.ConfirmingTransaction;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.repos.BlockRepository;
import org.cardanofoundation.metabus.repos.JobRepository;
import org.cardanofoundation.metabus.repos.UnconfirmedTxRepository;
import org.cardanofoundation.metabus.repos.UtxoRepository;
import org.cardanofoundation.metabus.service.JobService;
import org.cardanofoundation.metabus.service.QueueingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * <p>
 * The Job Service Implemented Class
 * </p>
 * 
 * @Modified @sotatek-joeydao, thaoho
 * @since 2023/08
 * @category Service
 * @version 0.01
 */
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class JobServiceImpl implements JobService {

    @Autowired
    QueueingService queueingService;

    @Autowired
    UnconfirmedTxRepository unconfirmedTxRepository;

    @Autowired
    JobRepository jobRepository;

    @Autowired
    UtxoRepository utxoRepository;

    @Autowired
    BlockRepository blockRepository;

    @Autowired
    TipFinder tipFinder;

    @Value("${kafka.topics.confirmingTransaction.retryableTopic.reconfirmDelayMilliseconds}")
    private Integer reconfirmDelayMilliseconds;

    @Value("${cardano-metabus-txproducer.retryConfirmingTransactionDelayTime}")
    private Integer retryConfirmingTransactionDelayTime;

    // In case a burst of blocks appear
    @Value("${cardano-metabus-txproducer.crawlerMaxBlockLag}")
    private Integer crawlerMaxBlockLag;

    @Override
    public void createJob(final Job job) {
        queueingService.sendJob(job);
    }

    @Override
    public void checkingUnconfirmedTransaction(final ConfirmingTransaction transaction) {
        // check is that the right time to check the tx is submitted on-chain
        if (!isTimeToCheckOnchain.test(transaction.getSubmittedDate(), reconfirmDelayMilliseconds)) {
            log.info("Checking time remaining: "
                    + calTimeRemaining.apply(transaction.getSubmittedDate(), reconfirmDelayMilliseconds));
            throw new UnreachableCheckingTimeException("The time is not reached");
        }

        // if the time has come. Check the transaction is on-chain.
        final List<UnconfirmedTxJPA> unconfirmedTxList = unconfirmedTxRepository
                .findAllByTxHash(transaction.getTxHash());

        // Even this case is hard to occur but still need to cover that
        if (unconfirmedTxList.isEmpty()) {
            if (stateUpToTip()) {
                throw new TransactionHashNotFoundOnChainException("The transaction is not on-chain: " + transaction.getTxHash());
            } else {
                throw new UnreachableCheckingTimeException("Block database is not synced up to tip");
            }
        }

        final UnconfirmedTxJPA unconfirmedTx = unconfirmedTxList.get(0);

        /**
         * Check the tx is deleted or not? If it deleted, this means the tx is on-chain
         */
        if (unconfirmedTx.getIsDeleted() == null || (!unconfirmedTx.getIsDeleted())) {
            if (stateUpToTip()) {
                throw new TransactionHashNotFoundOnChainException("The transaction is not on-chain: " + transaction.getTxHash());
            } else {
                throw new UnreachableCheckingTimeException("Block database is not synced up to tip");
            }
        }
    }

    @Override
    @Transactional
    public void detachJobsAndResend(final ConfirmingTransaction transaction) {
        // Get the jobs from database. To change it status to Pending.
        final List<Job> jobList = transaction.getJobBatch().getJobs();
        // Update before retry first
        jobList.forEach(Job::updateBeforeRetry);
        // filter the job
        final List<Job> retryableListJobs = jobList.stream().filter(job -> job.getRetryCount() > 0).toList();
        final List<Job> deadJobs = jobList.stream().filter(job -> job.getRetryCount() == 0).toList();

        // Update retryable job's info.
        final List<Long> retryableIds = retryableListJobs.stream().map(Job::getId).collect(Collectors.toList());
        final List<JobJPA> retryJobJPAs = jobRepository.findAllById(retryableIds);
        retryJobJPAs.forEach(JobJPA::updateJobInfoBeforeRetry);

        // Update dead jobs
        final List<Long> deadJobIds = deadJobs.stream().map(Job::getId).collect(Collectors.toList());
        final List<JobJPA> deadJobsJPAs = jobRepository.findAllById(deadJobIds);
        deadJobsJPAs.forEach(JobJPA::updateJobInfoBeforeDLQ);

        // Save all the modified job to database.
        final List<JobJPA> savingJobList = new LinkedList<>();
        savingJobList.addAll(deadJobsJPAs);
        savingJobList.addAll(retryJobJPAs);
        jobRepository.saveAll(savingJobList);

        // Remove used UTxO that is being attached to target transaction
        final List<UnconfirmedTxJPA> unconfirmedTxJpas = unconfirmedTxRepository.findAllByTxHash(transaction.getTxHash());
        final List<Long> unconfirmedTxIds = unconfirmedTxJpas.stream().map(BaseEntity::getId).toList();
        utxoRepository.deleteByUnconfirmedTxIdIn(unconfirmedTxIds);

        // Re-send job to jobSchedule topic queue.
        retryableListJobs.forEach(job -> {
            job.setState(JobState.PENDING);
            queueingService.sendJobSync(job);
        });

        // Push un-retryable job to dlq
        deadJobs.forEach(job -> {
            job.setState(JobState.FAILED);
            queueingService.sendJobToDlq(job);
        });
    }

    @Override
    public void rePublishConfirmingTransaction(final ConfirmingTransaction transaction) {
        // If retry count is reach to 0 do not republish again to main queue
        if (transaction.getRetryCountsForUnexpectedError() == 0) {
            return;
        }

        // Unless for infinite definition (below 0). Must subtract the count util 0.
        if (transaction.getRetryCountsForUnexpectedError() > -1) {
            transaction.subtractTheRetryCountByOne();
        }

        // republish with delay
        final Executor delayExecutor = CompletableFuture.delayedExecutor(retryConfirmingTransactionDelayTime, TimeUnit.MILLISECONDS);
        CompletableFuture.runAsync(() -> queueingService.sendConfirmingTransaction(transaction), delayExecutor).join();
    }

    private boolean stateUpToTip() {
        BlockJPA block = blockRepository.findTopByOrderByIdDesc().orElseThrow(() -> new UnreachableCheckingTimeException("Block database is not synced up to tip (and is empty)"));

        Mono<Tip> tipMono = tipFinder.find();
        Tip tip = tipMono.block(Duration.ofSeconds(10));

        if (tip == null) {
            throw new UnreachableCheckingTimeException("Cannot check if block DB is up to sync, error reading block height from node");
        }
        return tip.getBlock() <= (block.getBlockNo() + crawlerMaxBlockLag);
    }
}
