package org.cardanofoundation.metabus.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.metabus.common.entities.JobJPA;
import org.cardanofoundation.metabus.common.entities.UnconfirmedTxJPA;
import org.cardanofoundation.metabus.common.enums.JobState;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.json.JsonParser;
import org.cardanofoundation.metabus.mapper.JobMapper;
import org.cardanofoundation.metabus.repos.JobRepository;
import org.cardanofoundation.metabus.repos.UnconfirmedTxRepository;
import org.cardanofoundation.metabus.service.JobService;
import org.cardanofoundation.metabus.service.TxService;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class TxServiceImpl implements TxService {

    final UnconfirmedTxRepository unconfirmedTxRepository;
    final JobService jobService;
    final JobMapper jobMapper;
    final JobRepository jobRepository;
    final JsonParser jsonParser;


    @Override
    public void updateTxStates(String onChainTransactions) {
        if (ObjectUtils.isEmpty(onChainTransactions)) return;
        List<String> onChainTxHashes = extractListOnChainHashes(onChainTransactions);
        log.debug(">>> Successfully get {} tx hash on chain", onChainTxHashes.size());
        updatePostgres(onChainTxHashes);
    }

    private List<String> extractListOnChainHashes(String onChainTransactions) {
        TypeReference<List<String>> typeReference = new TypeReference<>() {
        };
        return jsonParser.parseJsonStringToObject(onChainTransactions, typeReference);
    }

    public void updatePostgres(List<String> onChainTxHashes) {
        List<UnconfirmedTxJPA> unconfirmedTxJPAS = unconfirmedTxRepository.findAllByTxHashIn(onChainTxHashes);
        // For each on-chain tx, soft delete unconfirmed_tx from db, update job state to ON-CHAIN
        log.debug("Successfully retrieved {} unconfirmed transactions ready to be on-chain from state storage.", unconfirmedTxJPAS.size());
        List<JobJPA> listJobJPA = unconfirmedTxJPAS.stream().filter(Objects::nonNull)
                .map(unconfirmedTxJPA -> {
                    unconfirmedTxJPA.setIsDeleted(true);
                    List<JobJPA> jobJPAs = jobRepository.findAllByUnconfirmedTxId(unconfirmedTxJPA.getId());
                    jobJPAs.forEach(jobJPA -> {
                        jobJPA.setState(JobState.ON_CHAIN);
                        jobJPA.setUnconfirmedTx(unconfirmedTxJPA);
                    });
                    return jobJPAs;
                })
                .flatMap(Collection::stream)
                .toList();
        List<UnconfirmedTxJPA> savedUnconfirmedTxJPAs = unconfirmedTxRepository.saveAll(unconfirmedTxJPAS);
        List<JobJPA> savedJobJpas = jobRepository.saveAll(listJobJPA);

        // Push jobs to rabbit after successfully saved to database
        listJobJPA.forEach(jobJPA -> {
            Job job = jobMapper.toJob(jobJPA);
            jobService.pushJobToRabbit(job);
        });
        log.debug(">>> State storage Postgresql: update tx on-chain: successfully soft deleted {} unconfirmed txs and " +
                "successfully update {} jobs", savedUnconfirmedTxJPAs.size(), savedJobJpas.size());
    }
}
