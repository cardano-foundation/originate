package org.cardanofoundation.proofoforigin.api.business.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.cardanofoundation.proofoforigin.api.business.JobService;
import org.cardanofoundation.proofoforigin.api.business.ScantrustTaskService;
import org.cardanofoundation.proofoforigin.api.configuration.properties.ScanTrustProperties;
import org.cardanofoundation.proofoforigin.api.constants.Constants;
import org.cardanofoundation.proofoforigin.api.constants.TaskState;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust.*;
import org.cardanofoundation.proofoforigin.api.job.CheckTaskStateJob;
import org.cardanofoundation.proofoforigin.api.job.JobDescriptor;
import org.cardanofoundation.proofoforigin.api.repository.BottleRepository;
import org.cardanofoundation.proofoforigin.api.repository.LotRepository;
import org.cardanofoundation.proofoforigin.api.repository.entities.Bottle;
import org.cardanofoundation.proofoforigin.api.repository.entities.Lot;
import org.cardanofoundation.proofoforigin.api.utils.GsonUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.quartz.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings({"rawtypes", "unchecked"})
class ScanTrustServiceImplTest {

    @Mock
    WebClient.Builder webClientBuilder;
    @Mock
    WebClient scanTrust;
    @Mock
    ScanTrustProperties scanTrustProperties;
    @Mock
    ScantrustTaskService scantrustTaskService;
    @Mock
    JobService jobService;
    @Mock
    LotRepository lotRepository;
    @Mock
    BottleRepository bottleRepository;
    ScanTrustServiceImpl scanTrustService;
    @Mock
    RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    RequestBodySpec requestBodySpec;
    @Mock
    RequestHeadersSpec requestHeadersSpec;
    @Mock
    RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    Mono mono;

    private final LocalDate date = LocalDate.of(2023, 1, 10);
    private final Lot lot = Lot.builder()
            .lotId("12345678903")
            .wineName("wine_name")
            .origin("origin")
            .countryOfOrigin("country")
            .producedBy("producer")
            .producerAddress("producer_address")
            .producerLatitude(10.0)
            .producerLongitude(100.0)
            .varietalName("varietal_name")
            .vintageYear(2022)
            .wineType("type")
            .wineColor("red")
            .harvestDate(date)
            .harvestLocation("harvest_location")
            .pressingDate(date)
            .processingLocation("processing_location")
            .fermentationVessel("fermentation_vessel")
            .fermentationDuration("fermentation duration")
            .agingRecipient("aging_recipient")
            .agingTime("aging time")
            .storageVessel("storage_vessel")
            .bottlingDate(date)
            .bottlingLocation("bottling_location")
            .numberOfBottles(10)
            .winerySignature("signature")
            .status(Constants.LOT_STATUS.APPROVED)
            .txId("txHash")
            .jobIndex("jobIndex")
            .build();

    @BeforeEach
    void setUp() {
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(scanTrust);
        scanTrustService = new ScanTrustServiceImpl(scanTrust, scanTrustProperties, scantrustTaskService, jobService,
                lotRepository, bottleRepository);
    }

    @Test
    void testSendScmDataWhenTxConfirmed() throws JsonProcessingException {
        final String transactionId = "transactionId";
        final String jobIndex = "jobIndex";
        final String lotId = "lotId";
        final Long jobId = 1L;

        final ScmAsyncResponse scmAsyncResponse = new ScmAsyncResponse();
        scmAsyncResponse.setTaskId("taskId");

        final Lot lot = new Lot();
        lot.setLotId(lotId);
        lot.setJobId(jobId);

        when(lotRepository.findByJobId(jobId)).thenReturn(Optional.of(lot));

        final ScmAsyncRequest scmAsyncRequest = ScmAsyncRequest.builder()
                .constraints(Constraints.initWithLotId(lotId))
                .scmData(ScmData.initWithTxIdAndBatchInfo(transactionId, jobIndex))
                .build();
        final String payloadScanTrust = GsonUtil.GSON_WITH_DATE.toJson(scmAsyncRequest);

        final Duration duration = Duration.ofSeconds(1);
        final RetryBackoffSpec backOffSpec = spy(Retry.fixedDelay(1, duration));

        try (MockedStatic<Duration> durationMocked = mockStatic(Duration.class)) {
            try (MockedStatic<Retry> retryMocked = mockStatic(Retry.class)) {
                when(scanTrustProperties.getRepeatTimes()).thenReturn(1);
                when(scanTrustProperties.getRepeatInterval()).thenReturn(1);

                durationMocked.when(() -> Duration.ofSeconds(1)).thenReturn(duration);
                retryMocked.when(() -> Retry.fixedDelay(1, duration)).thenReturn(backOffSpec);
                doReturn(backOffSpec).when(backOffSpec).filter(any());

                when(scanTrustProperties.getScmDataAsync()).thenReturn("testApi");
                when(scanTrust.post()).thenReturn(requestBodyUriSpec);
                when(requestBodyUriSpec.uri(scanTrustProperties.getScmDataAsync())).thenReturn(requestBodySpec);
                when(requestBodySpec.body(any(), eq(String.class))).thenReturn(requestHeadersSpec);
                when(requestHeadersSpec.exchangeToMono(any())).thenReturn(mono);
                when(mono.retryWhen(backOffSpec)).thenReturn(mono);
                when(mono.block()).thenReturn(scmAsyncResponse);
                when(scanTrustService.sendScmData(payloadScanTrust)).thenReturn(scmAsyncResponse);
                final ScmAsyncResponse scmAsyncResponseFromService = scanTrustService.sendScmDataWhenTxConfirmed(
                        transactionId,
                        jobIndex,
                        lotId);

                assertEquals("taskId", scmAsyncResponseFromService.getTaskId());
            }
        }
    }

    @Test
    void testSendScmData() {
        final ScmAsyncResponse scmAsyncResponse = new ScmAsyncResponse();
        scmAsyncResponse.setTaskId("taskId");
        final String payloadScanTrust = "payloadScanTrust";
        final Duration duration = Duration.ofSeconds(1);
        final RetryBackoffSpec backOffSpec = spy(Retry.fixedDelay(1, duration));

        try (MockedStatic<Duration> durationMocked = mockStatic(Duration.class)) {
            try (MockedStatic<Retry> retryMocked = mockStatic(Retry.class)) {

                when(scanTrustProperties.getRepeatTimes()).thenReturn(1);
                when(scanTrustProperties.getRepeatInterval()).thenReturn(1);

                durationMocked.when(() -> Duration.ofSeconds(1))
                        .thenReturn(duration);
                retryMocked.when(() -> Retry.fixedDelay(1, duration))
                        .thenReturn(backOffSpec);
                doReturn(backOffSpec).when(backOffSpec).filter(any());

                when(scanTrustProperties.getScmDataAsync()).thenReturn("testApi");
                when(scanTrust.post()).thenReturn(requestBodyUriSpec);
                when(requestBodyUriSpec.uri(scanTrustProperties.getScmDataAsync())).thenReturn(requestBodySpec);
                when(requestBodySpec.body(any(), eq(String.class))).thenReturn(requestHeadersSpec);
                when(requestHeadersSpec.exchangeToMono(any())).thenReturn(mono);
                when(mono.retryWhen(backOffSpec)).thenReturn(mono);
                when(mono.block()).thenReturn(scmAsyncResponse);

                final ScmAsyncResponse scmAsyncResponseFromService = scanTrustService.sendScmData(payloadScanTrust);
                assertEquals("taskId", scmAsyncResponseFromService.getTaskId());
            }
        }
    }

    @Test
    void testSendScmDataSync() {
        ScmSyncResponse scmSyncResponse = new ScmSyncResponse();
        scmSyncResponse.setCodesAffected(2);
        String payloadScanTrust = "payloadScanTrust";
        final Duration duration = Duration.ofSeconds(1);
        final RetryBackoffSpec backOffSpec = spy(Retry.backoff(1, duration));

        try (MockedStatic<Duration> durationMocked = mockStatic(Duration.class)) {
            try (MockedStatic<Retry> retryMocked = mockStatic(Retry.class)) {

                when(scanTrustProperties.getRepeatTimes()).thenReturn(1);
                when(scanTrustProperties.getMaxBackoff()).thenReturn(1);
                when(scanTrustProperties.getMinBackoff()).thenReturn(1);

                durationMocked.when(() -> Duration.ofSeconds(1))
                        .thenReturn(duration);
                retryMocked.when(() -> Retry.backoff(1, duration))
                        .thenReturn(backOffSpec);
                doReturn(backOffSpec).when(backOffSpec).maxBackoff(duration);
                doReturn(backOffSpec).when(backOffSpec).filter(any());

                when(scanTrustProperties.getScmDataSync()).thenReturn("testApi");
                when(scanTrust.post()).thenReturn(requestBodyUriSpec);
                when(requestBodyUriSpec.uri(scanTrustProperties.getScmDataSync())).thenReturn(requestBodySpec);
                when(requestBodySpec.body(any(), eq(String.class))).thenReturn(requestHeadersSpec);
                when(requestHeadersSpec.exchangeToMono(any())).thenReturn(mono);
                when(mono.retryWhen(backOffSpec)).thenReturn(mono);
                when(mono.block()).thenReturn(scmSyncResponse);

                ScmSyncResponse scmSyncResponseFromService = scanTrustService.sendScmDataSync(payloadScanTrust);
                assertEquals(2, scmSyncResponseFromService.getCodesAffected());
            }
        }
    }

    @Test
    void testCreateJobCheckStatus() {
        final String lotId = "lotId";
        final Long jobId = 1L;
        final String taskId = "taskId";

        final Lot lot = new Lot();
        lot.setLotId(lotId);
        lot.setJobId(jobId);

        when(lotRepository.findByJobId(jobId)).thenReturn(Optional.of(lot));

        final JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("taskId", taskId);
        jobDataMap.put("lotId", lotId);

        final JobDetail jobDetail = JobBuilder.newJob()
                .ofType(CheckTaskStateJob.class)
                .storeDurably()
                .usingJobData(jobDataMap)
                .withIdentity("Qrtz_Job_Detail_" + taskId)
                .withDescription("Job send check status send SCM data to Scan Trust")
                .build();

        final SimpleScheduleBuilder simpleSchedule = simpleSchedule()
                .withRepeatCount(scanTrustProperties.getRepeatTimes())
                .withIntervalInSeconds(scanTrustProperties.getRepeatInterval());

        final Trigger trigger = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity("Qrtz_Trigger_" + taskId)
                .withSchedule(simpleSchedule)
                .build();

        when(jobService.createJob(eq("task"), any()))
                .thenReturn(JobDescriptor.buildDescriptor(jobDetail, List.of(trigger)));

        final JobDescriptor jobDescriptor = jobService.createJob("task",
                JobDescriptor.buildDescriptor(jobDetail, List.of(trigger)));

        scanTrustService.createJobCheckStatus(taskId, jobId);
        assertEquals(jobDescriptor.getJobDataMap(), jobDataMap);
        assertEquals(jobDescriptor.getJobClass(), CheckTaskStateJob.class);
        assertEquals(jobDescriptor.getName(), jobDetail.getKey().getName());
        assertEquals(jobDescriptor.getGroup(), jobDetail.getKey().getGroup());
    }

    @Test
    void testCheckTaskState() {
        final String taskId = "taskId";
        final String lotId = "lotId";

        final ScmTaskResponse scmTaskResponse = new ScmTaskResponse();
        scmTaskResponse.setId("id");
        scmTaskResponse.setState(TaskState.COMPLETED);
        scmTaskResponse.setReference("reference");
        scmTaskResponse.setCodesAffected(1L);
        scmTaskResponse.setCreatedBy(1L);

        when(scanTrustProperties.getScmTaskState()).thenReturn("taskStateApi");
        when(scanTrust.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(scanTrustProperties.getScmTaskState() + "/" + taskId))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any())).thenReturn(mono);
        when(mono.block()).thenReturn(scmTaskResponse);

        final ScmTaskResponse response = scanTrustService.checkTaskState(taskId, lotId);
        assertEquals(scmTaskResponse.getId(), response.getId());
        assertEquals(scmTaskResponse.getState(), response.getState());
        assertEquals(scmTaskResponse.getReference(), response.getReference());
        assertEquals(scmTaskResponse.getCodesAffected(), response.getCodesAffected());
        assertEquals(scmTaskResponse.getCreatedBy(), response.getCreatedBy());

    }

    @Test
    void checkTaskStateFail() {
        final String taskId = "taskId";
        final String lotId = "lotId";
        final ScmTaskResponse scmTaskResponse = new ScmTaskResponse();
        scmTaskResponse.setId("id");
        scmTaskResponse.setState(TaskState.COMPLETED);
        scmTaskResponse.setReference("reference");
        scmTaskResponse.setCodesAffected(1L);
        scmTaskResponse.setCreatedBy(1L);

        when(scanTrustProperties.getScmTaskState()).thenReturn("taskStateApi");
        when(scanTrust.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(scanTrustProperties.getScmTaskState() + "/" + taskId))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any())).thenReturn(mono);
        when(mono.block()).thenReturn(null);
        Assertions.assertNull(scanTrustService.checkTaskState(taskId, lotId));

    }

    /**
     * <p>
     * Test Send Scm Data By Sync Api Successfully.
     * </p>
     */
    @Test
    void testSendScmDataBySyncApi() {
        final ScmSyncResponse scmSyncResponse = new ScmSyncResponse();
        scmSyncResponse.setTotalUpdates(10);
        final String payloadScanTrust = "payloadScanTrust";
        final Duration duration = Duration.ofSeconds(1);
        final RetryBackoffSpec backOffSpec = spy(Retry.backoff(1, duration));

        try (MockedStatic<Duration> durationMocked = mockStatic(Duration.class)) {
            try (MockedStatic<Retry> retryMocked = mockStatic(Retry.class)) {

                when(scanTrustProperties.getMinBackoff()).thenReturn(1);
                when(scanTrustProperties.getMaxBackoff()).thenReturn(1);
                when(scanTrustProperties.getRepeatTimes()).thenReturn(1);

                durationMocked.when(() -> Duration.ofSeconds(1))
                        .thenReturn(duration);
                retryMocked.when(() -> Retry.backoff(1, duration))
                        .thenReturn(backOffSpec);
                doReturn(backOffSpec).when(backOffSpec).maxBackoff(duration);
                doReturn(backOffSpec).when(backOffSpec).filter(any());

                when(scanTrustProperties.getScmDataSync()).thenReturn("testApi");
                when(scanTrust.post()).thenReturn(requestBodyUriSpec);
                when(requestBodyUriSpec.uri(scanTrustProperties.getScmDataSync())).thenReturn(requestBodySpec);
                when(requestBodySpec.body(any(), eq(String.class))).thenReturn(requestHeadersSpec);
                when(requestHeadersSpec.exchangeToMono(any())).thenReturn(mono);
                when(mono.retryWhen(backOffSpec)).thenReturn(mono);
                when(mono.block()).thenReturn(scmSyncResponse);

                final ScmSyncResponse scmSyncResponseFromService = scanTrustService
                        .sendScmDataBySyncApi(payloadScanTrust);
                assertEquals(10, scmSyncResponseFromService.getTotalUpdates());
            }
        }
    }

    /**
     * <p>
     * Test Send Scm Data By Sync Api Fail.
     * </p>
     */
    @Test
    void testSendScmDataBySyncApiFail() {
        final String payloadScanTrust = "payloadScanTrust";
        final Duration duration = Duration.ofSeconds(1);
        final RetryBackoffSpec backOffSpec = spy(Retry.fixedDelay(1, duration));

        try (MockedStatic<Duration> durationMocked = mockStatic(Duration.class)) {
            try (MockedStatic<Retry> retryMocked = mockStatic(Retry.class)) {

                when(scanTrustProperties.getRepeatTimes()).thenReturn(1);
                when(scanTrustProperties.getMinBackoff()).thenReturn(1);
                when(scanTrustProperties.getMaxBackoff()).thenReturn(1);

                durationMocked.when(() -> Duration.ofSeconds(1))
                        .thenReturn(duration);
                retryMocked.when(() -> Retry.fixedDelay(1, duration))
                        .thenReturn(backOffSpec);
                doReturn(backOffSpec).when(backOffSpec).filter(any());

                when(scanTrustProperties.getScmDataSync()).thenReturn("testApi");
                when(scanTrust.post()).thenReturn(requestBodyUriSpec);
                when(requestBodyUriSpec.uri(scanTrustProperties.getScmDataSync())).thenReturn(requestBodySpec);
                when(requestBodySpec.body(any(), eq(String.class))).thenReturn(requestHeadersSpec);
                when(requestHeadersSpec.exchangeToMono(any())).thenReturn(mono);
                when(mono.retryWhen(backOffSpec)).thenReturn(mono);
                when(mono.block()).thenThrow(new RuntimeException());

                final ScmSyncResponse scmSyncResponseFromService = scanTrustService
                        .sendScmDataBySyncApi(payloadScanTrust);
                assertNull(scmSyncResponseFromService);
            }
        }
    }

    @Test
    public void testSendScmDataWhenApproved_Success() throws Exception {
        ScmData scmData = new ScmData();
        String preparedScmData = "";

        ScanTrustServiceImpl spyScanTrustService = spy(scanTrustService);
        when(spyScanTrustService.prepareScanTrustParamsWhenApproved(scmData)).thenReturn(preparedScmData);

        spyScanTrustService.sendScmDataWhenApproved(scmData);

        verify(spyScanTrustService).sendScmData(preparedScmData);
    }

    @Test
    public void testSendScmDataWhenApproved_Exception() throws Exception {
        ScmData scmData = new ScmData();

        ScanTrustServiceImpl spyScanTrustService = spy(scanTrustService);
        when(spyScanTrustService.prepareScanTrustParamsWhenApproved(scmData)).thenThrow(new RuntimeException("Test exception"));

        assertDoesNotThrow(() -> spyScanTrustService.sendScmDataWhenApproved(scmData));

        verify(spyScanTrustService, never()).sendScmData(any());
    }

    @Test
    public void testSendScmDataWhenMappingBottle_Success() {
        ScmSyncResponse scmSyncResponse = new ScmSyncResponse();
        scmSyncResponse.setCodesAffected(3);
        final Duration duration = Duration.ofSeconds(1);
        final RetryBackoffSpec backOffSpec = spy(Retry.backoff(1, duration));

        try (MockedStatic<Duration> durationMocked = mockStatic(Duration.class)) {
            try (MockedStatic<Retry> retryMocked = mockStatic(Retry.class)) {

                when(scanTrustProperties.getRepeatTimes()).thenReturn(1);
                when(scanTrustProperties.getMaxBackoff()).thenReturn(1);
                when(scanTrustProperties.getMinBackoff()).thenReturn(1);

                durationMocked.when(() -> Duration.ofSeconds(1))
                        .thenReturn(duration);
                retryMocked.when(() -> Retry.backoff(1, duration))
                        .thenReturn(backOffSpec);
                doReturn(backOffSpec).when(backOffSpec).maxBackoff(duration);
                doReturn(backOffSpec).when(backOffSpec).filter(any());

                when(scanTrustProperties.getScmDataSync()).thenReturn("testApi");
                when(scanTrust.post()).thenReturn(requestBodyUriSpec);
                when(requestBodyUriSpec.uri(scanTrustProperties.getScmDataSync())).thenReturn(requestBodySpec);
                when(requestBodySpec.body(any(), eq(String.class))).thenReturn(requestHeadersSpec);
                when(requestHeadersSpec.exchangeToMono(any())).thenReturn(mono);
                when(mono.retryWhen(backOffSpec)).thenReturn(mono);
                when(mono.block()).thenReturn(scmSyncResponse);

                List<Bottle> listBottle = mockListBottle();
                when(lotRepository.findByLotIdInAndStatus(anyList(), eq(Constants.LOT_STATUS.APPROVED))).thenReturn(List.of(lot));

                doNothing().when(bottleRepository).updateLotUpdateStatusById(anyString(), eq(Constants.SCANTRUST.STATUS.NOT_UPDATED));

                ScanTrustServiceImpl spyScanTrustService = spy(scanTrustService);
                when(spyScanTrustService.sendScmDataSync(anyString())).thenReturn(scmSyncResponse);

                spyScanTrustService.sendScmDataWhenMappingBottle(listBottle);

                ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
                verify(spyScanTrustService).sendScmDataSync(argumentCaptor.capture());
                assertEquals("{\"items\":[{\"extended_id\":\"b1\",\"sequential_number_in_lot\":\"1\",\"lot_number\":\"12345678901\",\"wine_name\":\"\",\"origin\":\"\",\"country_of_origin\":\"\",\"produced_by\":\"\",\"producer_address\":\"\",\"producer_latitude\":\"\",\"producer_longitude\":\"\",\"varietal_name\":\"\",\"vintage_year\":\"\",\"wine_type\":\"\",\"wine_color\":\"\",\"harvest_date\":\"\",\"harvest_location\":\"\",\"pressing_date\":\"\",\"processing_location\":\"\",\"fermentation_vessel\":\"\",\"fermentation_duration\":\"\",\"aging_recipient\":\"\",\"aging_time\":\"\",\"storage_vessel\":\"\",\"bottling_date\":\"\",\"bottling_location\":\"\",\"number_of_bottles\":\"\",\"supply_chain_data_txid\":\"\",\"supply_chain_data_batch_info\":\"\",\"product\":\"Dummy Product\"},{\"extended_id\":\"b2\",\"sequential_number_in_lot\":\"2\",\"lot_number\":\"12345678901\",\"wine_name\":\"\",\"origin\":\"\",\"country_of_origin\":\"\",\"produced_by\":\"\",\"producer_address\":\"\",\"producer_latitude\":\"\",\"producer_longitude\":\"\",\"varietal_name\":\"\",\"vintage_year\":\"\",\"wine_type\":\"\",\"wine_color\":\"\",\"harvest_date\":\"\",\"harvest_location\":\"\",\"pressing_date\":\"\",\"processing_location\":\"\",\"fermentation_vessel\":\"\",\"fermentation_duration\":\"\",\"aging_recipient\":\"\",\"aging_time\":\"\",\"storage_vessel\":\"\",\"bottling_date\":\"\",\"bottling_location\":\"\",\"number_of_bottles\":\"\",\"supply_chain_data_txid\":\"\",\"supply_chain_data_batch_info\":\"\",\"product\":\"Dummy Product\"},{\"extended_id\":\"b3\",\"sequential_number_in_lot\":\"3\",\"lot_number\":\"12345678903\",\"wine_name\":\"wine_name\",\"origin\":\"origin\",\"country_of_origin\":\"country\",\"produced_by\":\"producer\",\"producer_address\":\"producer_address\",\"producer_latitude\":\"10.0\",\"producer_longitude\":\"100.0\",\"varietal_name\":\"varietal_name\",\"vintage_year\":\"2022\",\"wine_type\":\"type\",\"wine_color\":\"red\",\"harvest_date\":\"2023-01-10\",\"harvest_location\":\"harvest_location\",\"pressing_date\":\"2023-01-10\",\"processing_location\":\"processing_location\",\"fermentation_vessel\":\"fermentation_vessel\",\"fermentation_duration\":\"fermentation duration\",\"aging_recipient\":\"aging_recipient\",\"aging_time\":\"aging time\",\"storage_vessel\":\"storage_vessel\",\"bottling_date\":\"2023-01-10\",\"bottling_location\":\"bottling_location\",\"number_of_bottles\":\"10\",\"supply_chain_data_txid\":\"txHash\",\"supply_chain_data_batch_info\":\"jobIndex\",\"product\":\"wine_name - producer\"}]}", argumentCaptor.getValue());

                InOrder inOrder = inOrder(bottleRepository);
                inOrder.verify(bottleRepository).updateLotUpdateStatusById(eq("b1"), eq(Constants.SCANTRUST.STATUS.UPDATED));
                inOrder.verify(bottleRepository).updateLotUpdateStatusById(eq("b2"), eq(Constants.SCANTRUST.STATUS.UPDATED));
                inOrder.verify(bottleRepository).updateLotUpdateStatusById(eq("b3"), eq(Constants.SCANTRUST.STATUS.UPDATED));
            }
        }
    }

    @Test
    public void testSendScmDataWhenMappingBottle_Response_Not_Map() {
        ScmSyncResponse scmSyncResponse = new ScmSyncResponse();
        scmSyncResponse.setCodesAffected(1);
        Map<String, List<String>> map = new HashMap<>();
        map.put("b1", new ArrayList<>());
        scmSyncResponse.setItems(map);
        final Duration duration = Duration.ofSeconds(1);
        final RetryBackoffSpec backOffSpec = spy(Retry.backoff(1, duration));

        try (MockedStatic<Duration> durationMocked = mockStatic(Duration.class)) {
            try (MockedStatic<Retry> retryMocked = mockStatic(Retry.class)) {

                when(scanTrustProperties.getRepeatTimes()).thenReturn(1);
                when(scanTrustProperties.getMaxBackoff()).thenReturn(1);
                when(scanTrustProperties.getMinBackoff()).thenReturn(1);

                durationMocked.when(() -> Duration.ofSeconds(1))
                        .thenReturn(duration);
                retryMocked.when(() -> Retry.backoff(1, duration))
                        .thenReturn(backOffSpec);
                doReturn(backOffSpec).when(backOffSpec).maxBackoff(duration);
                doReturn(backOffSpec).when(backOffSpec).filter(any());

                when(scanTrustProperties.getScmDataSync()).thenReturn("testApi");
                when(scanTrust.post()).thenReturn(requestBodyUriSpec);
                when(requestBodyUriSpec.uri(scanTrustProperties.getScmDataSync())).thenReturn(requestBodySpec);
                when(requestBodySpec.body(any(), eq(String.class))).thenReturn(requestHeadersSpec);
                when(requestHeadersSpec.exchangeToMono(any())).thenReturn(mono);
                when(mono.retryWhen(backOffSpec)).thenReturn(mono);
                when(mono.block()).thenReturn(scmSyncResponse);

                List<Bottle> listBottle = mockListBottle();
                when(lotRepository.findByLotIdInAndStatus(anyList(), eq(Constants.LOT_STATUS.APPROVED))).thenReturn(List.of(lot));
                doNothing().when(bottleRepository).updateLotUpdateStatusById(anyString(), eq(Constants.SCANTRUST.STATUS.NOT_UPDATED));

                ScanTrustServiceImpl spyScanTrustService = spy(scanTrustService);
                when(spyScanTrustService.sendScmDataSync(anyString())).thenReturn(scmSyncResponse);

                spyScanTrustService.sendScmDataWhenMappingBottle(listBottle);

                ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
                verify(spyScanTrustService).sendScmDataSync(argumentCaptor.capture());
                assertEquals("{\"items\":[{\"extended_id\":\"b1\",\"sequential_number_in_lot\":\"1\",\"lot_number\":\"12345678901\",\"wine_name\":\"\",\"origin\":\"\",\"country_of_origin\":\"\",\"produced_by\":\"\",\"producer_address\":\"\",\"producer_latitude\":\"\",\"producer_longitude\":\"\",\"varietal_name\":\"\",\"vintage_year\":\"\",\"wine_type\":\"\",\"wine_color\":\"\",\"harvest_date\":\"\",\"harvest_location\":\"\",\"pressing_date\":\"\",\"processing_location\":\"\",\"fermentation_vessel\":\"\",\"fermentation_duration\":\"\",\"aging_recipient\":\"\",\"aging_time\":\"\",\"storage_vessel\":\"\",\"bottling_date\":\"\",\"bottling_location\":\"\",\"number_of_bottles\":\"\",\"supply_chain_data_txid\":\"\",\"supply_chain_data_batch_info\":\"\",\"product\":\"Dummy Product\"},{\"extended_id\":\"b2\",\"sequential_number_in_lot\":\"2\",\"lot_number\":\"12345678901\",\"wine_name\":\"\",\"origin\":\"\",\"country_of_origin\":\"\",\"produced_by\":\"\",\"producer_address\":\"\",\"producer_latitude\":\"\",\"producer_longitude\":\"\",\"varietal_name\":\"\",\"vintage_year\":\"\",\"wine_type\":\"\",\"wine_color\":\"\",\"harvest_date\":\"\",\"harvest_location\":\"\",\"pressing_date\":\"\",\"processing_location\":\"\",\"fermentation_vessel\":\"\",\"fermentation_duration\":\"\",\"aging_recipient\":\"\",\"aging_time\":\"\",\"storage_vessel\":\"\",\"bottling_date\":\"\",\"bottling_location\":\"\",\"number_of_bottles\":\"\",\"supply_chain_data_txid\":\"\",\"supply_chain_data_batch_info\":\"\",\"product\":\"Dummy Product\"},{\"extended_id\":\"b3\",\"sequential_number_in_lot\":\"3\",\"lot_number\":\"12345678903\",\"wine_name\":\"wine_name\",\"origin\":\"origin\",\"country_of_origin\":\"country\",\"produced_by\":\"producer\",\"producer_address\":\"producer_address\",\"producer_latitude\":\"10.0\",\"producer_longitude\":\"100.0\",\"varietal_name\":\"varietal_name\",\"vintage_year\":\"2022\",\"wine_type\":\"type\",\"wine_color\":\"red\",\"harvest_date\":\"2023-01-10\",\"harvest_location\":\"harvest_location\",\"pressing_date\":\"2023-01-10\",\"processing_location\":\"processing_location\",\"fermentation_vessel\":\"fermentation_vessel\",\"fermentation_duration\":\"fermentation duration\",\"aging_recipient\":\"aging_recipient\",\"aging_time\":\"aging time\",\"storage_vessel\":\"storage_vessel\",\"bottling_date\":\"2023-01-10\",\"bottling_location\":\"bottling_location\",\"number_of_bottles\":\"10\",\"supply_chain_data_txid\":\"txHash\",\"supply_chain_data_batch_info\":\"jobIndex\",\"product\":\"wine_name - producer\"}]}", argumentCaptor.getValue());

                InOrder inOrder = inOrder(bottleRepository);
                inOrder.verify(bottleRepository).updateLotUpdateStatusById(eq("b2"), eq(Constants.SCANTRUST.STATUS.FAILED));
                inOrder.verify(bottleRepository).updateLotUpdateStatusById(eq("b1"), eq(Constants.SCANTRUST.STATUS.UPDATED));
            }
        }
    }

    @Test
    public void testSendScmDataWhenMappingBottle_Exception() {

        ScmSyncResponse scmSyncResponse = new ScmSyncResponse();
        scmSyncResponse.setCodesAffected(10);
        final Duration duration = Duration.ofSeconds(1);
        final RetryBackoffSpec backOffSpec = spy(Retry.backoff(1, duration));

        try (MockedStatic<Duration> durationMocked = mockStatic(Duration.class)) {
            try (MockedStatic<Retry> retryMocked = mockStatic(Retry.class)) {

                when(scanTrustProperties.getRepeatTimes()).thenReturn(1);
                when(scanTrustProperties.getMaxBackoff()).thenReturn(1);
                when(scanTrustProperties.getMinBackoff()).thenReturn(1);

                durationMocked.when(() -> Duration.ofSeconds(1))
                        .thenReturn(duration);
                retryMocked.when(() -> Retry.backoff(1, duration))
                        .thenReturn(backOffSpec);
                doReturn(backOffSpec).when(backOffSpec).maxBackoff(duration);
                doReturn(backOffSpec).when(backOffSpec).filter(any());

                when(scanTrustProperties.getScmDataSync()).thenReturn("testApi");
                when(scanTrust.post()).thenReturn(requestBodyUriSpec);
                when(requestBodyUriSpec.uri(scanTrustProperties.getScmDataSync())).thenReturn(requestBodySpec);
                when(requestBodySpec.body(any(), eq(String.class))).thenReturn(requestHeadersSpec);
                when(requestHeadersSpec.exchangeToMono(any())).thenReturn(mono);
                when(mono.retryWhen(backOffSpec)).thenReturn(mono);
                when(mono.block()).thenReturn(scmSyncResponse);

                List<Bottle> listBottle = mockListBottle();
                when(lotRepository.findByLotIdInAndStatus(anyList(), eq(Constants.LOT_STATUS.APPROVED))).thenReturn(List.of(lot));
                doNothing().when(bottleRepository).updateLotUpdateStatusById(anyString(), eq(Constants.SCANTRUST.STATUS.NOT_UPDATED));

                ScanTrustServiceImpl spyScanTrustService = spy(scanTrustService);
                when(spyScanTrustService.sendScmDataSync(anyString())).thenThrow(new RuntimeException("Test exception"));

                spyScanTrustService.sendScmDataWhenMappingBottle(listBottle);

                ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
                verify(spyScanTrustService).sendScmDataSync(argumentCaptor.capture());
                assertEquals("{\"items\":[{\"extended_id\":\"b1\",\"sequential_number_in_lot\":\"1\",\"lot_number\":\"12345678901\",\"wine_name\":\"\",\"origin\":\"\",\"country_of_origin\":\"\",\"produced_by\":\"\",\"producer_address\":\"\",\"producer_latitude\":\"\",\"producer_longitude\":\"\",\"varietal_name\":\"\",\"vintage_year\":\"\",\"wine_type\":\"\",\"wine_color\":\"\",\"harvest_date\":\"\",\"harvest_location\":\"\",\"pressing_date\":\"\",\"processing_location\":\"\",\"fermentation_vessel\":\"\",\"fermentation_duration\":\"\",\"aging_recipient\":\"\",\"aging_time\":\"\",\"storage_vessel\":\"\",\"bottling_date\":\"\",\"bottling_location\":\"\",\"number_of_bottles\":\"\",\"supply_chain_data_txid\":\"\",\"supply_chain_data_batch_info\":\"\",\"product\":\"Dummy Product\"},{\"extended_id\":\"b2\",\"sequential_number_in_lot\":\"2\",\"lot_number\":\"12345678901\",\"wine_name\":\"\",\"origin\":\"\",\"country_of_origin\":\"\",\"produced_by\":\"\",\"producer_address\":\"\",\"producer_latitude\":\"\",\"producer_longitude\":\"\",\"varietal_name\":\"\",\"vintage_year\":\"\",\"wine_type\":\"\",\"wine_color\":\"\",\"harvest_date\":\"\",\"harvest_location\":\"\",\"pressing_date\":\"\",\"processing_location\":\"\",\"fermentation_vessel\":\"\",\"fermentation_duration\":\"\",\"aging_recipient\":\"\",\"aging_time\":\"\",\"storage_vessel\":\"\",\"bottling_date\":\"\",\"bottling_location\":\"\",\"number_of_bottles\":\"\",\"supply_chain_data_txid\":\"\",\"supply_chain_data_batch_info\":\"\",\"product\":\"Dummy Product\"},{\"extended_id\":\"b3\",\"sequential_number_in_lot\":\"3\",\"lot_number\":\"12345678903\",\"wine_name\":\"wine_name\",\"origin\":\"origin\",\"country_of_origin\":\"country\",\"produced_by\":\"producer\",\"producer_address\":\"producer_address\",\"producer_latitude\":\"10.0\",\"producer_longitude\":\"100.0\",\"varietal_name\":\"varietal_name\",\"vintage_year\":\"2022\",\"wine_type\":\"type\",\"wine_color\":\"red\",\"harvest_date\":\"2023-01-10\",\"harvest_location\":\"harvest_location\",\"pressing_date\":\"2023-01-10\",\"processing_location\":\"processing_location\",\"fermentation_vessel\":\"fermentation_vessel\",\"fermentation_duration\":\"fermentation duration\",\"aging_recipient\":\"aging_recipient\",\"aging_time\":\"aging time\",\"storage_vessel\":\"storage_vessel\",\"bottling_date\":\"2023-01-10\",\"bottling_location\":\"bottling_location\",\"number_of_bottles\":\"10\",\"supply_chain_data_txid\":\"txHash\",\"supply_chain_data_batch_info\":\"jobIndex\",\"product\":\"wine_name - producer\"}]}", argumentCaptor.getValue());

                verify(bottleRepository, times(listBottle.size())).updateLotUpdateStatusById(anyString(), eq(Constants.SCANTRUST.STATUS.FAILED));
            }
        }
    }

    private List<Bottle> mockListBottle() {
        List<Bottle> listBottle = new ArrayList<>();
        Bottle bottle = new Bottle();
        bottle.setId("b1");
        bottle.setLotId("12345678901");
        bottle.setSequentialNumber(1);
        bottle.setSequentialNumberInLot(1);
        bottle.setReelNumber(2);

        listBottle.add(bottle);
        Bottle bottle1 = new Bottle();
        bottle1.setId("b2");
        bottle1.setLotId("12345678901");
        bottle1.setSequentialNumber(3);
        bottle1.setSequentialNumberInLot(2);
        bottle1.setReelNumber(4);

        listBottle.add(bottle1);

        Bottle bottle3 = new Bottle();
        bottle3.setId("b3");
        bottle3.setLotId("12345678903");
        bottle3.setSequentialNumber(3);
        bottle3.setSequentialNumberInLot(3);
        bottle3.setReelNumber(4);

        listBottle.add(bottle3);
        return listBottle;
    }
}