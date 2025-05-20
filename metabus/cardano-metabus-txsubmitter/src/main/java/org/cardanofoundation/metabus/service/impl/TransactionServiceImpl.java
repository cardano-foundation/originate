package org.cardanofoundation.metabus.service.impl;

import com.bloxbean.cardano.client.api.UtxoSupplier;
import com.bloxbean.cardano.client.api.model.ProtocolParams;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.common.CardanoConstants;
import com.bloxbean.cardano.client.crypto.bip32.HdKeyPair;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.function.TxBuilder;
import com.bloxbean.cardano.client.function.TxBuilderContext;
import com.bloxbean.cardano.client.function.TxSigner;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.metadata.Metadata;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadata;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.transaction.spec.TransactionOutput;
import com.bloxbean.cardano.yaci.core.common.TxBodyType;
import com.bloxbean.cardano.yaci.core.protocol.localtx.model.TxSubmissionRequest;
import com.bloxbean.cardano.yaci.helper.LocalTxSubmissionClient;
import com.bloxbean.cardano.yaci.helper.model.TxResult;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.metabus.TxSubmitterUtxoSupplier;
import org.cardanofoundation.metabus.common.entities.JobJPA;
import org.cardanofoundation.metabus.common.entities.UnconfirmedTxJPA;
import org.cardanofoundation.metabus.common.enums.JobState;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.configuration.TxSubmitterProperties;
import org.cardanofoundation.metabus.repos.JobRepository;
import org.cardanofoundation.metabus.repos.UnconfirmedTxRepository;
import org.cardanofoundation.metabus.service.LocalNodeService;
import org.cardanofoundation.metabus.service.TransactionService;
import org.cardanofoundation.metabus.util.ProtocolParamsUtil;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.bloxbean.cardano.client.function.helper.AuxDataProviders.metadataProvider;
import static com.bloxbean.cardano.client.function.helper.FeeCalculators.feeCalculator;
import static com.bloxbean.cardano.client.function.helper.InputBuilders.createFromSender;
import static com.bloxbean.cardano.client.function.helper.OutputBuilders.createFromOutput;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@EnableConfigurationProperties(value = {TxSubmitterProperties.class})
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    LocalTxSubmissionClient localTxSubmissionClient;
    UnconfirmedTxRepository unconfirmedTxRepository;
    JobRepository jobRepository;

    @Override
    public Transaction buildTransaction(String senderPaymentAddress, List<Utxo> txInList, TransactionOutput txOut,
                                        Metadata metadata) {
        ProtocolParams protocolParams = ProtocolParamsUtil.cachedProtocolParams;

        boolean mergeChangeOutput = true;

        TxBuilder txBuilder = createFromOutput(txOut)
                .buildInputs(createFromSender(senderPaymentAddress, senderPaymentAddress), mergeChangeOutput)
                .andThen(metadataProvider(() -> metadata))
                .andThen(feeCalculator(senderPaymentAddress, 1));

        txInList.forEach(utxo -> {
            utxo.getAmount().stream().forEach(amount -> {
                String unit = amount.getUnit();
                // If amount item is token, replace the "." character in the assetId as yaci and cardano client lib is not sync
                // (utxo return from yaci return format <policyId>.<assetName> while cardano client lib TxBuilder use
                // <policyId><assetName>)
                if(!amount.getUnit().equals(CardanoConstants.LOVELACE)){
                    amount.setUnit(unit.replace(".",""));
                }
            });
        });

        UtxoSupplier utxoSupplier = new TxSubmitterUtxoSupplier(txInList);

        return TxBuilderContext
                .init(utxoSupplier, protocolParams)
                .build(txBuilder);
    }

    @Override
    public Transaction signTransaction(HdKeyPair hdKeyPair, Transaction transaction) {
        TxSigner signer = SignerProviders.signerFrom(hdKeyPair);
        return signer.sign(transaction);
    }

    @Override
    public Mono<TxResult> submitTransaction(Transaction transaction) throws CborSerializationException {
        byte[] serializedTransaction = transaction.serialize();
        TxSubmissionRequest txnRequest = new TxSubmissionRequest(TxBodyType.CONWAY, serializedTransaction);
        return localTxSubmissionClient.submitTx(txnRequest);
    }

    @Override
    @Transactional
    public UnconfirmedTxJPA saveUnconfirmedTx(List<Job> jobs, CBORMetadata metadata,
                                               String transactionHash) {
        UnconfirmedTxJPA unconfirmedTxJPA = UnconfirmedTxJPA.builder()
                .metadata(metadata.toJson())
                .txHash(transactionHash)
                .build();
        UnconfirmedTxJPA savedUnconfirmedTxJPA = unconfirmedTxRepository.save(
                unconfirmedTxJPA);

        updateJobIndexAndState(jobs, savedUnconfirmedTxJPA, JobState.SUBMITTED);
        return savedUnconfirmedTxJPA;
    }

    private void updateJobIndexAndState(List<Job> jobs, UnconfirmedTxJPA savedUnconfirmedTxJPA,
                                        JobState jobState) {
        List<Long> jobIds = jobs.stream().map(Job::getId).collect(Collectors.toList());
        List<JobJPA> jobJPAs = jobRepository.findAllByIdIn(jobIds);

        jobJPAs.forEach(jobJPA -> {
            jobJPA.setUnconfirmedTx(savedUnconfirmedTxJPA);
            jobJPA.setState(jobState);

            Optional<Job> job = jobs.stream()
                    .filter(j -> jobJPA.getId().equals(j.getId()))
                    .findFirst();
            job.ifPresent(value -> jobJPA.setJobIndex(value.getJobIndex()));
        });

        jobRepository.saveAll(jobJPAs);
    }
}
