package org.cardanofoundation.proofoforigin.api.business.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.proofoforigin.api.business.JobService;
import org.cardanofoundation.proofoforigin.api.business.ScanTrustService;
import org.cardanofoundation.proofoforigin.api.business.ScantrustTaskService;
import org.cardanofoundation.proofoforigin.api.configuration.properties.ScanTrustProperties;
import org.cardanofoundation.proofoforigin.api.constants.Constants;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust.*;
import org.cardanofoundation.proofoforigin.api.job.CheckTaskStateJob;
import org.cardanofoundation.proofoforigin.api.job.JobDescriptor;
import org.cardanofoundation.proofoforigin.api.repository.LotRepository;
import org.cardanofoundation.proofoforigin.api.repository.entities.Lot;
import org.quartz.*;
import org.cardanofoundation.proofoforigin.api.repository.BottleRepository;
import org.cardanofoundation.proofoforigin.api.repository.entities.Bottle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.Optional;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScanTrustServiceImpl implements ScanTrustService {

    private final WebClient scanTrust;
    private final ScanTrustProperties scanTrustProperties;
    private final ScantrustTaskService scantrustTaskService;
    private final JobService jobService;
    private final LotRepository lotRepository;
    private final BottleRepository bottleRepository;

    @Override
    public ScmSyncResponse sendScmDataSync(String payloadScanTrust) {
        final long start = System.currentTimeMillis();
        log.info("Start send scm data to Scan Trust with body: {}", payloadScanTrust);
        ScmSyncResponse scmSyncResponse = scanTrust.post()
                .uri(scanTrustProperties.getScmDataSync())
                .body(Mono.just(payloadScanTrust), String.class)
                .exchangeToMono(ScmSyncResponse::fromClientRequest)
                .retryWhen(
                        Retry.backoff(scanTrustProperties.getRepeatTimes(),
                                Duration.ofSeconds(scanTrustProperties.getMinBackoff()))
                                .maxBackoff(Duration.ofSeconds(scanTrustProperties.getMaxBackoff()))
                                .filter(ScanTrustService.filterTheScanTrustWebClientException))
                .block();
        log.info("Send data to Scan Trust completely in {} ms", System.currentTimeMillis() - start);
        return scmSyncResponse;
    }

    @Override
    public ScmAsyncResponse sendScmDataWhenTxConfirmed(String transactionId, String jobIndex, String lotId) throws JsonProcessingException {
        final String payloadScanTrust = prepareScanTrustParams(transactionId, lotId, jobIndex);
        return sendScmData(payloadScanTrust);
    }

    @Override
    public ScmAsyncResponse sendScmData(String payloadScanTrust) {
        final long start = System.currentTimeMillis();
        log.info("Start send scm data to Scan Trust with body: {}", payloadScanTrust);

        final ScmAsyncResponse scmAsyncResponse = scanTrust.post()
                .uri(scanTrustProperties.getScmDataAsync())
                .body(Mono.just(payloadScanTrust), String.class)
                .exchangeToMono(ScmAsyncResponse::fromClientRequest)
                .retryWhen(Retry.fixedDelay(scanTrustProperties.getRepeatTimes(),
                        Duration.ofSeconds(scanTrustProperties.getRepeatInterval()))
                        .filter(ScanTrustService.filterTheScanTrustWebClientException))
                .block();

        log.info("Send data to Scan Trust completely in {} ms", System.currentTimeMillis() - start);

        return scmAsyncResponse;
    }


    @Override
    public void createJobCheckStatus(String taskId, Long jobId) {
        Optional<Lot> lotOpt = lotRepository.findByJobId(jobId);
        if (lotOpt.isEmpty()) {
            return;
        }
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("taskId", taskId);
        jobDataMap.put("lotId", lotOpt.get().getLotId());

        JobDetail jobDetail = JobBuilder.newJob()
                .ofType(CheckTaskStateJob.class)
                .storeDurably()
                .usingJobData(jobDataMap)
                .withIdentity("Qrtz_Job_Detail_" + taskId)
                .withDescription("Job send check status send SCM data to Scan Trust")
                .build();

        SimpleScheduleBuilder simpleSchedule = simpleSchedule()
                .withRepeatCount(scanTrustProperties.getRepeatTimes())
                .withIntervalInSeconds(scanTrustProperties.getRepeatInterval());

        Trigger trigger = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity("Qrtz_Trigger_" + taskId)
                .withSchedule(simpleSchedule)
                .build();

        jobService.createJob("task", JobDescriptor.buildDescriptor(jobDetail, List.of(trigger)));
    }

    @Override
    public ScmTaskResponse checkTaskState(String taskId, String lotId) {
        long start = System.currentTimeMillis();
        ScmTaskResponse scmTaskResponse = scanTrust.get()
                .uri(scanTrustProperties.getScmTaskState() + "/" + taskId)
                .exchangeToMono(ScmTaskResponse::fromClientRequest)
                .block();
        log.debug("Check task state completely in {} ms", System.currentTimeMillis() - start);
        if (scmTaskResponse == null) {
            log.error("Cannot check status with task id {}", taskId);
            return null;
        }
        scantrustTaskService.saveScantrustTask(taskId, scmTaskResponse.getState(), lotId, 2);
        return scmTaskResponse;
    }

    private String prepareScanTrustParams(String txId, String lotId, String jobIndex) throws JsonProcessingException {
        ScmAsyncRequest scmAsyncRequest = ScmAsyncRequest.builder()
                .constraints(Constraints.initWithLotId(lotId))
                .scmData(ScmData.initWithTxIdAndBatchInfo(txId, jobIndex))
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper.writeValueAsString(scmAsyncRequest);
    }

    @Override
    public ScmSyncResponse sendScmDataBySyncApi(final String payload) {
        try {
            return sendScmDataSync(payload) ;
        } catch (final Exception e) {
            log.error("Cannot send SCM data to Scan Trust by Sync Api. Because of: {}", e);
            return null;
        }
    }

    @Transactional
    public void sendScmDataWhenApproved(ScmData scmData) {
        try {
            sendScmData(prepareScanTrustParamsWhenApproved(scmData));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    String prepareScanTrustParamsWhenApproved(ScmData scmData) throws JsonProcessingException {
        Constraints constraints = new Constraints();
        constraints.setLotId(scmData.getLotId());

        ScmAsyncRequest scmAsyncRequest = ScmAsyncRequest.builder()
                .constraints(constraints)
                .scmData(scmData)
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper.writeValueAsString(scmAsyncRequest);
    }

    @Override
    @Transactional
    public void sendScmDataWhenMappingBottle(List<Bottle> listBottle) {
        List<String> ids = listBottle.stream().map(Bottle::getId).toList();
        try {

            List<Lot> listLotApproved = lotRepository.findByLotIdInAndStatus(listBottle.stream().map(Bottle::getLotId).toList(), Constants.LOT_STATUS.APPROVED);
            List<ScmUploadItem> scmUploadItems = listBottle.stream().map(bottle -> {
                String currentLotId = bottle.getLotId();
                Optional<Lot> lotOptional = listLotApproved.stream().filter(lot -> lot.getLotId().equals(currentLotId)).findFirst();
                if (lotOptional.isPresent()) { // If lot is approved -> send lot info to scan trust
                    return ScmUploadItem.init(bottle.getId(), bottle.getSequentialNumberInLot(), lotOptional.get());
                }
                return ScmUploadItem.init(bottle.getId(), bottle.getSequentialNumberInLot(), Lot.builder().lotId(bottle.getLotId()).build());
            }).toList();

            ScmSyncRequest scmSyncRequest = ScmSyncRequest.builder().items(scmUploadItems).build();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            ScmSyncResponse response = sendScmDataSync(objectMapper.writeValueAsString(scmSyncRequest));
            if (response.getCodesAffected() != listBottle.size()) {
                Set<String> updatedId = response.getItems().keySet();

                if (updatedId != null && !updatedId.isEmpty()) {
                    ids = ids.stream().filter(id -> !updatedId.contains(id)).toList();
                }

                for (String id: ids) {
                    bottleRepository.updateLotUpdateStatusById(id, Constants.SCANTRUST.STATUS.FAILED);
                }

                for (String id: List.copyOf(updatedId)) {
                    bottleRepository.updateLotUpdateStatusById(id, Constants.SCANTRUST.STATUS.UPDATED);
                }
            } else {
                for (String id: ids) {
                    bottleRepository.updateLotUpdateStatusById(id, Constants.SCANTRUST.STATUS.UPDATED);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);

            // Log error to db so that we can handle that later
            for (String id: ids) {
                bottleRepository.updateLotUpdateStatusById(id, Constants.SCANTRUST.STATUS.FAILED);
            }
        }
    }
}
