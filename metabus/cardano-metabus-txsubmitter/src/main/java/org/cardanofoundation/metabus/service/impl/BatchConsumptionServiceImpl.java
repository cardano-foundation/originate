package org.cardanofoundation.metabus.service.impl;

import java.math.BigInteger;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.cardanofoundation.metabus.common.constants.CIDHashAlgo;
import org.cardanofoundation.metabus.common.entities.JobJPA;
import org.cardanofoundation.metabus.common.entities.ScheduledBatchesJPA;
import org.cardanofoundation.metabus.common.entities.UnconfirmedTxJPA;
import org.cardanofoundation.metabus.common.enums.BatchStatus;
import org.cardanofoundation.metabus.common.enums.GroupType;
import org.cardanofoundation.metabus.common.enums.JobState;
import org.cardanofoundation.metabus.common.offchain.ConfirmingTransaction;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.common.offchain.JobBatch;
import org.cardanofoundation.metabus.common.onchain.TxMetadata;
import org.cardanofoundation.metabus.configuration.TxSubmitterProperties;
import org.cardanofoundation.metabus.configuration.TxSubmitterProperties.Wallet;
import org.cardanofoundation.metabus.configuration.kafka.KafkaProperties;
import org.cardanofoundation.metabus.factory.MetadataServiceFactory;
import org.cardanofoundation.metabus.repos.JobRepository;
import org.cardanofoundation.metabus.repos.ScheduledBatchesRepository;
import org.cardanofoundation.metabus.service.BatchConsumptionService;
import org.cardanofoundation.metabus.service.OffchainStorageService;
import org.cardanofoundation.metabus.service.QueueingService;
import org.cardanofoundation.metabus.service.TransactionService;
import org.cardanofoundation.metabus.service.UtxoService;
import org.cardanofoundation.metabus.service.WalletService;
import org.cardanofoundation.metabus.util.JobMapperUtil;
import org.cardanofoundation.metabus.util.NetworkUtil;
import org.cardanofoundation.metabus.util.ProtocolParamsUtil;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.crypto.bip32.HdKeyPair;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadata;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.transaction.spec.TransactionOutput;
import com.bloxbean.cardano.client.transaction.spec.Value;
import com.bloxbean.cardano.yaci.helper.model.TxResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.nstant.in.cbor.CborException;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import reactor.util.retry.Retry;

/**
 * <p>
 * The Batch Consumption Service Implementation
 * </p>
 *
 * @author (Sotatek) joey.dao
 * @version 0.01
 * @category Service
 * @see BatchConsumptionService
 * @since 2023/06
 */
@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableConfigurationProperties(value = { TxSubmitterProperties.class, KafkaProperties.class })
@RequiredArgsConstructor
public class BatchConsumptionServiceImpl implements BatchConsumptionService {

    /**
     * Services
     */
    ObjectMapper objectMapper;

    JobRepository jobRepository;

    ScheduledBatchesRepository scheduledBatchRepository;

    TransactionService transactionService;

    WalletService walletService;

    UtxoService utxoService;

    OffchainStorageService offchainStorageService;

    MetadataServiceFactory metadataServiceFactory;

    TxSubmitterProperties txSubmitterProperties;

    KafkaProperties kafkaProperties;

    QueueingService queueingService;

    /**
     * Config properties
     */
    @NonFinal
    Network network;

    @NonFinal
    String mnemonic;

    @NonFinal
    BigInteger metadatumLabel;

    @NonFinal
    BigInteger batchConsumptionBoundaryTime;

    @NonFinal
    Integer numberOfDerivedAddresses;

    @NonFinal
    BigInteger waitingTimeToReConsume;

    Map<Address, HdKeyPair> addressAndKeyPairMap = new LinkedHashMap<>();

    @NonFinal
    Integer numberOfRetryPullingUtxo;

    @NonFinal
    String offchainBucket;

    @NonFinal
    Long txSubmissionRetryDelayDuration;

    @NonFinal
    String confirmingTransactionTopic;

    /**
     * <p>
     * Retry Count for Unexpected Error in Confirming Transaction
     * </p>
     */
    @NonFinal
    Long retryCountUEForConfirmingTransaction;

    @Override
    @Transactional
    public void consumeBasedOnTime() throws CborSerializationException, InterruptedException, JsonProcessingException, CborException, AddressExcepion {
        log.info("Begin consumeBasedOnTime function");
        log.info("Begin to consume the batch that is based on time");
        // Find all the "PENDING" BATCH on the schedule
        final List<ScheduledBatchesJPA> scheduledBatch = scheduledBatchRepository
                .findAllByBatchStatus(BatchStatus.PENDING);
        // If none of the batch is scheduled. Skip this turn
        if (scheduledBatch.isEmpty()) {
            log.info("There are no scheduled batches");
            log.info("End consumeBasedOnTime function");
            return;
        }

        // If having batches are scheduled. Check the boundary time and filtered to the
        final List<ScheduledBatchesJPA> filteredScheduledBatches = scheduledBatch.stream()
                .filter(batch -> {
                    log.debug("consumed time: {}", batch.getConsumedJobTime().toString());
                    log.debug("time now: {}", Instant.now(Clock.systemUTC()));
                    final boolean comparisionValue = batch.getConsumedJobTime()
                            .plusMillis(batchConsumptionBoundaryTime.longValue())
                            .isBefore(Instant.now(Clock.systemUTC()));
                    log.debug("is it batch (based on job type) submitted so far: {}", comparisionValue);

                    return comparisionValue;
                })
                .collect(Collectors.toList());

        // Loop the pending scheduled batch
        for (final ScheduledBatchesJPA batch : filteredScheduledBatches) {
            submitBatchToNode(null, batch.getJobType(), batch, true);
        }

        log.info("End consumeBasedOnTime function");
    }

    @Override
    @Transactional
    public void consumeBasedOnTxMaxSize(final ConsumerRecord<String, Job> consumerRecord)
            throws CborSerializationException, JsonProcessingException, InterruptedException, CborException,
            AddressExcepion {
        log.info("Begin consumeBasedOnTxMaxSize function");
        final var job = consumerRecord.value();
        log.debug("Consuming job: {}", objectMapper.writeValueAsString(job));

        final String jobType = job.getBusinessData().getType();

        // Get the target Scheduled Batch that is based on job type
        final List<ScheduledBatchesJPA> batchList = scheduledBatchRepository.findByJobType(jobType);
        final ScheduledBatchesJPA scheduledBatch = !batchList.isEmpty() ? batchList.get(0) : null;

        // Check the status of job type is not processing.
        if (!isBatchOfJobTypesProcessing(scheduledBatch)) {
            // Submit the batch to the node
            submitBatchToNode(job, jobType, scheduledBatch, false);
        }

        log.info("End consumeBasedOnTxMaxSize function");
    }

    /**
     * <p>
     * Check the batch that is based on job type is Processing in another thread.
     * If it is PENDING or NONE, set the state to processing
     * </p>
     *
     * @param batchInfo the batch info
     * @return is the batch is processing or not
     */
    public boolean isBatchOfJobTypesProcessing(final ScheduledBatchesJPA batchInfo) {
        if (batchInfo == null) {
            return false;
        }

        final BatchStatus status = batchInfo.getBatchStatus();

        return status.equals(BatchStatus.PROCESSING);
    }

    /**
     * <p>
     * Batching jobs with the same type before submitting
     * </p>
     *
     * @param jobType     The job type
     * @param currentJob  The consumed Jobs
     * @param pendingJobs The pending jobs from database
     * @return The batch
     */
    public JobBatch createJobBatch(final String jobType, final Job currentJob, final Job... pendingJobs) {
        // Find the same type pending jobs that are stored in the database.
        final List<Job> readyJobs = new ArrayList<>();

        if (currentJob == null && (pendingJobs == null || pendingJobs.length == 0)) {
            return JobBatch.builder().jobType(jobType).jobs(readyJobs).build();
        }

        if (pendingJobs != null && pendingJobs.length > 0) {
            readyJobs.addAll(Arrays.asList(pendingJobs));
        }

        if (currentJob != null && !Arrays.stream(pendingJobs).anyMatch(job -> job.getId().equals(currentJob.getId()))) {
            readyJobs.add(currentJob);
        }

        final GroupType groupType = currentJob != null ? currentJob.getGroupType()
                : readyJobs.get(0) != null ? readyJobs.get(0).getGroupType() : null;

        final String subType = currentJob != null ? currentJob.getBusinessData().getSubType()
                : readyJobs.get(0) != null ? readyJobs.get(0).getBusinessData().getSubType() : null;

        return JobBatch.builder()
                .jobType(jobType)
                .jobSubType(subType)
                .groupType(groupType)
                .jobs(readyJobs)
                .build();
    }

    /**
     * <p>
     * Update unconfirmed transaction to the database and saved used Utxo to the
     * database
     * </p>
     *
     * @param jobs                 The list of jobs that have been submitted to the
     *                             database
     * @param senderPaymentAddress The address of the sender
     * @param txInList             The Utxo that is used as txIn for the transaction
     * @param metadata             The metadata of the transaction
     * @param transactionHash      txHash
     * @return The saved unconfirmed-transaction.
     */
    public UnconfirmedTxJPA updateUnconfirmedTxToDatabase(final List<Job> jobs, final Address senderPaymentAddress,
            final List<Utxo> txInList,
            final CBORMetadata metadata,
            final String transactionHash) {
        log.debug(">>> Begin the updateUnconfirmedTxToDatabase function");

        final UnconfirmedTxJPA unconfirmedTxJPA = transactionService.saveUnconfirmedTx(jobs, metadata, transactionHash);
        utxoService.saveUsedUtxo(txInList, senderPaymentAddress, unconfirmedTxJPA);

        // *NOTE: The next sprint will change to register to Redis Database.

        log.debug(">>> Saved unconfirmed tx, tx hash: {}", transactionHash);
        txInList.forEach(txIn -> {
            log.debug(">>> Saved utxo, tx hash: {}, index: {}", txIn.getTxHash(), txIn.getOutputIndex());
        });
        log.debug(">>> End the updateUnconfirmedTxToDatabase function");

        return unconfirmedTxJPA;
    }

    /**
     * <p>
     * Create the transaction
     * </p>
     *
     * @param senderPaymentAddress The sender address
     * @param firstChildKeyPair    The key pair
     * @param jobBatch             The batch info
     * @param txInList             The txIn
     * @param isBasedOnTime        Is it based on time or not
     * @return The signed transaction
     * @throws CborSerializationException
     */
    public Transaction createTransaction(final Address senderPaymentAddress, final HdKeyPair firstChildKeyPair,
            final JobBatch jobBatch, final List<Utxo> txInList, final boolean isBasedOnTime)
            throws CborSerializationException, JsonProcessingException, CborException, AddressExcepion {
        final List<Job> jobList = jobBatch.getJobs();

        // Check the job list is empty or not. If it is empty return null.
        if (jobList.isEmpty()) {
            return null;
        }

        // Get the fixed TxOut
        final long txOutAmount = ProtocolParamsUtil.getMinUtxo(senderPaymentAddress.getAddress());
        // Get the max tx size
        final Integer txMaxSize = ProtocolParamsUtil.cachedProtocolParams.getMaxTxSize();
        // Tx out for sending a fixed lovelace back to sender payment address
        final TransactionOutput txOut = TransactionOutput.builder()
                .address(senderPaymentAddress.getAddress())
                .value(Value.builder()
                        .coin(BigInteger.valueOf(txOutAmount))
                        .build())
                .build();
        final String senderAddress = senderPaymentAddress.getAddress();
        // Use a template CID string to calculate the tx size (every generated CID has the same format and size)
        jobBatch.setCid("zCT5htke4g3x5C3M7sArtXBALYXsePh9qo2CtrCxMMdFrr6kZ82Z");

        // Build transaction that is based on the maximum tx size to merge the jobs to
        // the batch.

        Transaction signedTxn = buildTheSignedTransaction(senderAddress, txInList, txOut, jobBatch, firstChildKeyPair);
        Integer sizeOfTx = Integer.valueOf(signedTxn.serialize().length);

        // Compare with the maximum size of a transaction constant
        if (!isBasedOnTime && txMaxSize.compareTo(sizeOfTx) > 0) {
            // If the size of the tx is not enough to submit to the node
            // DO NOT PROCESS THE BATCH
            return null;
        }

        // Pop-out the job until the size of the transaction is less than the txMaxSize
        while (txMaxSize.compareTo(sizeOfTx) < 0) {
            jobList.remove(jobList.size() - 1);
            // To prevent the ArrayIndex out bound
            if (CollectionUtils.isEmpty(jobList)) {
                return null;
            }
            jobBatch.setJobs(jobList);
            signedTxn = buildTheSignedTransaction(senderPaymentAddress.getAddress(), txInList, txOut, jobBatch,
                    firstChildKeyPair);
            sizeOfTx = signedTxn.serialize().length;
        }

        // Store the final job batch into storage, and get the genuine CID
        final String cid = storeBatchToOffchainStorage(jobBatch);
        jobBatch.setCid(cid);

        // Last time building Transaction (when the genuine CID has been generated
        // already)
        signedTxn = buildTheSignedTransaction(senderPaymentAddress.getAddress(), txInList, txOut, jobBatch,
                firstChildKeyPair);

        return signedTxn;
    }

    /**
     * <p>
     * Get the most appropriate UTxO to become next TxIn in the current Transaction
     * </p>
     *
     * @param walletAddresses The client addresses
     * @return The appropriate client wallet info
     */
    public ClientWalletInfo getAppropriateTxIn(final Map<Address, HdKeyPair> walletAddresses)
            throws JsonProcessingException, CborException, AddressExcepion, CborSerializationException {
        ClientWalletInfo clientWalletInfo = null;

        // Seeking the first appropriate UTxO for each address.
        for (final Entry<Address, HdKeyPair> walletEntry : walletAddresses.entrySet()) {
            // Get unused Utxo list sort by amount asc
            final List<Utxo> unusedUtxos = utxoService.getUnusedUtxosSortByAmount(walletEntry.getKey());
            final Address senderPaymentAddress = walletEntry.getKey();
            final long minUtxo = ProtocolParamsUtil.getMinUtxo(senderPaymentAddress.getAddress());

            final List<Utxo> listUtxoValid = getValidUtxos(unusedUtxos, minUtxo);
            if (!CollectionUtils.isEmpty(listUtxoValid)) {
                clientWalletInfo = new ClientWalletInfo(walletEntry.getValue(), walletEntry.getKey());
                clientWalletInfo.setUtxos(listUtxoValid);
                log.debug(">>> Client wallet info: {}", objectMapper.writeValueAsString(clientWalletInfo));
                break;
            }
        }

        return clientWalletInfo;
    }

    /**
     * <p>
     * Get list utxo is satisfy greater than or equal minUtxo + fee
     * </p>
     *
     * @param unusedUtxos The unused utxo
     * @param minUtxo     the least amount of the utxo.
     * @return List of Utxo
     */
    private List<Utxo> getValidUtxos(final List<Utxo> unusedUtxos, final long minUtxo) {
        final List<Utxo> validUtxos = new ArrayList<>();
        long totalAmount = 0;
        boolean isValid = false;

        for (final Utxo utxo : unusedUtxos) {
            validUtxos.add(utxo);
            totalAmount += utxo.getAmount().stream()
                    .map(Amount::getQuantity)
                    .reduce(BigInteger.ZERO, BigInteger::add).longValue();

            if (ProtocolParamsUtil.isTxInValid(totalAmount, minUtxo)) {
                isValid = true;
                break;
            }
        }

        return isValid ? validUtxos : Collections.emptyList();
    }

    /**
     * <p>
     * Submit the batch to the cardano node.
     * </p>
     *
     * @param job            The consumed job
     * @param jobType        The job type
     * @param scheduledBatch The scheduled batch
     * @param isBasedOnTime  Is it based on time or not
     * @throws CborSerializationException
     * @throws JsonProcessingException
     * @throws InterruptedException
     */
    public void submitBatchToNode(final Job job, final String jobType, final ScheduledBatchesJPA scheduledBatch,
            final boolean isBasedOnTime)
            throws CborSerializationException, JsonProcessingException, InterruptedException, CborException,
            AddressExcepion {
        // Update the scheduled batch status
        log.info("consuming scheduled batch: {}", objectMapper.writeValueAsString(scheduledBatch));
        final ScheduledBatchesJPA currentScheduledBatch = updateScheduledBatch(scheduledBatch, BatchStatus.PROCESSING,
                jobType);
        // Find the pending job and create jobs batch
        final List<JobJPA> pendingJobs = jobRepository.findTop20ByStateAndType(JobState.PENDING, jobType);

        final Job[] pendingJobList = pendingJobs.stream().map(element -> JobMapperUtil.toJob(element))
                .collect(Collectors.toList()).toArray(new Job[pendingJobs.size()]);
        final JobBatch jobBatch = createJobBatch(jobType, job, pendingJobList);

        // Update the scheduled batch to NONE when the pending jobs are empty.
        if (jobBatch.getJobs().isEmpty()) {
            log.debug("The jobs are empty, cancel the process");
            updateScheduledBatch(currentScheduledBatch, BatchStatus.NONE, jobType);
            return;
        }

        // Get the payment address that based on mnemonic and addressIndex
        // Get all the address of the wallet and find the max
        final ClientWalletInfo clientInfo = getTheClientWalletInfo(numberOfRetryPullingUtxo);

        // If cannot find the UTxO suit for the transaction, pending the jobs.
        if (clientInfo == null) {
            log.debug("Can not find the UTxO that can be processed right now");
            updateScheduledBatch(scheduledBatch, BatchStatus.PENDING, null);
            return;
        }

        final Transaction signedTxn = createTransaction(clientInfo.getAddress(), clientInfo.getHdKeyPair(),
                jobBatch,
                clientInfo.getUtxos(),
                isBasedOnTime);

        /**
         * If the createTransaction function is return null.
         * So that, Set the status of the batch to PENDING
         * and push the consumed job to the pending job lists.
         */
        if (signedTxn == null) {
            log.debug("Can not create the transaction, end the process now");
            updateScheduledBatch(currentScheduledBatch, BatchStatus.PENDING, null);
            return;
        }

        // Submit the transaction to the node
        try {
            log.info(">>> Submitting the transaction to the node. ");
            // `Mono.block` will block the code until the submission is completed
            // successfully
            final TxResult txResult = transactionService.submitTransaction(signedTxn).retryWhen(
                    Retry.fixedDelay(Retry.indefinitely().maxAttempts,
                            Duration.ofMillis(txSubmissionRetryDelayDuration)))
                    .block();

            final String transactionHash = txResult.getTxHash();

            if (txResult.isAccepted()) {
                log.info(">>> Transaction submitted successfully with txHash: {}", txResult.getTxHash());
                // Insert the Unconfirmed transaction info to the database
                final UnconfirmedTxJPA unconfirmedTxJPA = updateUnconfirmedTxToDatabase(jobBatch.getJobs(),
                        clientInfo.getAddress(), clientInfo.getUtxos(),
                        (CBORMetadata) signedTxn.getAuxiliaryData().getMetadata(), transactionHash);
                // Re-check the pending jobs and update the state of the scheduled batch.
                updateScheduledBatch(currentScheduledBatch, BatchStatus.NONE, jobType);
                // Send the transaction that have recently submitted to Node to Job-Producer
                // Module
                // to check that tx is on-chain or not.
                sendConfirmingJobToProducer(txResult.getTxHash(), jobBatch, unconfirmedTxJPA.getCreatedDate());
            } else {
                log.error(">>> Transaction submitted error with txHash: {}", txResult.getTxHash());
                // Because of the unhandled rejected submit.
                // I will leave it here for preventing cannot find batches.
                updateScheduledBatch(currentScheduledBatch, BatchStatus.PENDING, jobType);
                // TODO: Retry submitting transaction reject case ?
                /**
                 * *Note: I had faced the issue that the metadata was containing more than 1600
                 * characters
                 * I think it should investigate what will go wrong when we submit the
                 * insufficient validation transaction.
                 * After having all the overview about that we can implement the appropriate
                 * procedure to handle the error transaction.
                 */
            }
        } catch (final Exception e) {
            log.error(">>> Tx Submission is failed with unexpected errors", e);
            updateScheduledBatch(currentScheduledBatch, BatchStatus.PENDING, jobType);
            // TODO: Is it for alert implementation or something ?
            // Throw the exception so the consumer can not acknowledge the message
            throw e;
        }
    }

    /**
     * <p>
     * Update the state of the scheduled batch.
     * </p>
     * <ul>
     * <li>
     * If the batch had never been registered to the schedule. New one.<br/>
     * --> All the parameter must not be null other than scheduledBatch
     * </li>
     * <li>
     * If the batch is already registered. We have 2 cases:</br>
     * <ul>
     * <li>
     * Restart the time of consuming
     * --> All the parameter must not be null.
     * </li>
     * <li>
     * Just update the status
     * --> only 2 parameters (scheduledBatch, status) must not be null.
     * </li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param scheduledBatch The scheduled batch (can be nullable)
     * @param status         The status of the batch (required)
     * @param jobType        The job type (can be nullable)
     */
    public ScheduledBatchesJPA updateScheduledBatch(final ScheduledBatchesJPA scheduledBatch, final BatchStatus status,
            final String jobType) {
        if (scheduledBatch == null) {
            final ScheduledBatchesJPA newScheduledBatch = ScheduledBatchesJPA.builder()
                    .batchStatus(status).consumedJobTime(Instant.now(Clock.systemUTC()))
                    .jobType(jobType).build();
            scheduledBatchRepository.save(newScheduledBatch);

            return newScheduledBatch;
        } else if (scheduledBatch.getBatchStatus().equals(BatchStatus.NONE)) {
            scheduledBatch.setBatchStatus(status);
            scheduledBatch.setConsumedJobTime(Instant.now(Clock.systemUTC()));
        } else if (status.equals(BatchStatus.NONE)) {
            final List<JobJPA> pendingJobs = jobRepository.findTop20ByStateAndType(JobState.PENDING,
                    scheduledBatch.getJobType());
            if (pendingJobs.isEmpty()) {
                scheduledBatch.setBatchStatus(status);
            } else {
                scheduledBatch.setBatchStatus(BatchStatus.PENDING);
            }
        } else {
            scheduledBatch.setBatchStatus(status);
        }

        scheduledBatchRepository.save(scheduledBatch);
        return scheduledBatch;
    }

    /**
     * <p>
     * Store the batch information to off-chain storage.
     * </p>
     * 
     * @param jobBatch the job batch
     * @return returned cid
     * @throws JsonProcessingException
     */
    private String storeBatchToOffchainStorage(final JobBatch jobBatch) throws JsonProcessingException {
        final String jsonText = metadataServiceFactory.buildOffchainJson(jobBatch);

        return offchainStorageService.storeObject(this.offchainBucket, jsonText);
    }

    /**
     * <p>
     * Build the signed transaction.
     * </p>
     *
     * @param senderAddress     the senderAddress
     * @param txInList          The transaction In
     * @param txOut             The transaction Out
     * @param jobBatch          The job infos
     * @param firstChildKeyPair The key pair
     * @return the signed transaction
     */
    public Transaction buildTheSignedTransaction(final String senderAddress, final List<Utxo> txInList,
            final TransactionOutput txOut, final JobBatch jobBatch, final HdKeyPair firstChildKeyPair) {
        // Build the metadata (metadata: the list of jobs info)
        final TxMetadata txMetadata = metadataServiceFactory.buildTxMetadata(jobBatch);
        // Build the cardano transaction metadata
        final CBORMetadata metadata = metadataServiceFactory.buildCborTxMetadata(txMetadata, metadatumLabel);
        // Create the transaction and sign it
        final Transaction transaction = transactionService.buildTransaction(senderAddress, txInList, txOut, metadata);

        return transactionService.signTransaction(firstChildKeyPair, transaction);
    }

    @PostConstruct
    public void setProperties() {
        this.network = NetworkUtil.getNetwork(txSubmitterProperties.getNetwork());
        final Wallet wallet = txSubmitterProperties.getWallet();
        this.mnemonic = wallet.getMnemonic();
        this.metadatumLabel = txSubmitterProperties.getMetadatumLabel();
        this.batchConsumptionBoundaryTime = txSubmitterProperties.getBatchConsumptionBoundaryTime();
        this.numberOfDerivedAddresses = txSubmitterProperties.getNumberOfDerivedAddresses();
        this.waitingTimeToReConsume = txSubmitterProperties.getWaitingTimeToReConsume();
        this.numberOfRetryPullingUtxo = txSubmitterProperties.getNumberOfRetryPullingUtxo();
        this.txSubmissionRetryDelayDuration = txSubmitterProperties.getTxSubmissionRetryDelayDuration();
        this.confirmingTransactionTopic = kafkaProperties.getTopics().get("confirmingTransaction").getName();
        this.retryCountUEForConfirmingTransaction = Long.parseLong(kafkaProperties.getTopics().get("confirmingTransaction").getConfigs().get("retryCountForUnexpectedError"));

        // Get the address and key pair to after the beans are created and
        // then cache that to memory
        // Get the children key pair
        final List<HdKeyPair> listOfChildKeyPair = walletService.getFirstNChildKeyPairs(mnemonic,
                numberOfDerivedAddresses);
        // Get the list of the addresses in following key pair
        addressAndKeyPairMap.putAll(walletService.getAddressesWithKeyPair(listOfChildKeyPair, network));

        this.offchainBucket = txSubmitterProperties.getOffchainBucket();
    }

    /**
     * <p>
     * Get the most client wallet info to make a transaction.
     * If we can not find the UTxO.
     * The consumer should wait for 30 second intervals and re-pull the client
     * wallet info.
     * </p>
     *
     * @param retryCount The retry counter
     * @return The best ClientWalletInfo
     * @throws InterruptedException
     */
    public ClientWalletInfo getTheClientWalletInfo(final Integer retryCount)
            throws InterruptedException, JsonProcessingException, CborException, AddressExcepion,
            CborSerializationException {
        // Get client wallet info
        final ClientWalletInfo bestClientInfo = getAppropriateTxIn(addressAndKeyPairMap);

        // If it cannot be found, wait for indicated seconds and retry 1 time.
        if (bestClientInfo == null) {
            // Just to prevent that the retry count could be set by a negative number
            if (retryCount <= 0) {
                return null;
            }

            Thread.sleep(waitingTimeToReConsume.longValue());
            return getTheClientWalletInfo(retryCount - 1);
        }

        return bestClientInfo;
    }

    /**
     * <p>
     * Send the recent transaction info to job-producer to check that is on-chain or
     * not.
     * </p>
     * 
     * @param txHash The tx hash
     * @param jobBatch The job batch
     * @param submittedDate The submitted date of the transaction.
     */
    public void sendConfirmingJobToProducer(final String txHash, final JobBatch jobBatch, final Instant submittedDate) {
        // Re-state the job.
        jobBatch.getJobs().forEach(job -> job.setState(JobState.SUBMITTED));

        /** Create confirming Transaction. */
        final ConfirmingTransaction confirmingTransaction = ConfirmingTransaction.builder().txHash(txHash)
                .jobBatch(jobBatch).submittedDate(submittedDate)
                .retryCountsForUnexpectedError(this.retryCountUEForConfirmingTransaction)
                .build();

        /** Send the message to the confirming transaction topics */
        queueingService.sendMessage(confirmingTransaction, this.confirmingTransactionTopic, txHash);
    }

    /**
     * <p>
     * ClientWalletInfo class
     * </p>
     *
     * <p>
     * The client wallet info that store 3 info:
     * <li>- HdKeyPair</li>
     * <li>- Address</li>
     * <li>- Utxo</li>
     * </p>
     */
    @Getter
    @Setter
    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = false)
    public final class ClientWalletInfo {
        /**
         * The Hd KeyPair
         */
        final HdKeyPair hdKeyPair;

        /**
         * The wallet of the
         */
        final Address address;

        /**
         * The available UTXO
         */
        List<Utxo> utxos;
    }
}
