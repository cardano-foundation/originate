package org.cardanofoundation.proofoforigin.api.aspect;

import java.util.LinkedList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.cardanofoundation.proofoforigin.api.business.ScanTrustService;
import org.cardanofoundation.proofoforigin.api.configuration.UploadScmDataSync;
import org.cardanofoundation.proofoforigin.api.constants.UploadType;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust.ScmSyncResponse;
import org.cardanofoundation.proofoforigin.api.utils.ScanTrustDataHandlingUtil;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * <p>
 * An Aspect to handle the ScanTrust Data Synchronization
 * </p>
 * 
 * @author (Sotatek) joey.dao
 * @category Aspect
 * @since 2023/07
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class SynchronizingToScanTrustAspect {

    /**
     * The ScanTrust Service
     */
    private final ScanTrustService scanTrustService;

    /**
     * The object mapper class
     */
    private final ObjectMapper objectMapper;

    /**
     * A bean factory to get bean programmatically.
     */
    private final BeanFactory beanFactory;

    /**
     * <p>
     * Upload Scm Data To ScanTrust By Sync Api After the main task is done.
     * </p>
     * <p>
     * *NOTE: If we need build Payload data from return value of the function.
     * Add the `returning` variable of AfterReturning annotation
     * and add one more argument for `uploadScmDataToScanTrustBySyncApiAfter`. For
     * example:
     * </p>
     * 
     * <pre>
     * <code> 
     * \@AfterReturning(value = "@annotation(CustomAnnotation)", returning = "result")
     * uploadScmDataToScanTrustBySyncApiAfter(final JoinPoint joinPoint, Object result)`
     * </code>
     * </pre>
     * <p>
     * And after that we can add more abstract method to the abstract class
     * ScanTrustDataHandlingUtil
     * to handle more cases like:
     * <ul>
     * <li>
     * buildPayloadDataFromArgs
     * </li>
     * <li>
     * buildPayloadDataFromResult
     * </li>
     * <li>
     * buildPayloadDataFromArgsAndResult
     * </li>
     * </ul>
     * </p>
     * 
     * @param joinPoint         The join point of annotated function.
     * @param uploadScmDataSync the annotation object.
     */
    @AfterReturning("@annotation(org.cardanofoundation.proofoforigin.api.configuration.UploadScmDataSync)")
    public void uploadScmDataToScanTrustBySyncApiAfter(final JoinPoint joinPoint) {
        log.info(">>> After service method: {}", joinPoint.getSignature().getName());
        log.info(">>> Begin synchronize data to ScanTrust");

        /** Get the annotation information */
        final UploadScmDataSync uploadScmDataSync = AnnotationUtils
                .findAnnotation(((MethodSignature) joinPoint.getSignature()).getMethod(), UploadScmDataSync.class);

        if (uploadScmDataSync == null) {
            log.info(">>> Cannot find UploadScmDataSync annotation on method: {}", joinPoint.getSignature().getName());
            return;
        }

        final boolean isSyncProcess = uploadScmDataSync.doSync();
        final UploadType uploadType = uploadScmDataSync.uploadType();
        final Class<?>[] inputCustomClassTypes = uploadScmDataSync.inputCustomClassTypes();

        /** Get argument of the function */
        final Object[] argsValueList = joinPoint.getArgs();
        final ScanTrustDataHandlingUtil dataHandlingUtil = beanFactory.getBean(
                UploadType.getDataBuildUtilClass(uploadType),
                ScanTrustDataHandlingUtil.class);

        final List<String> scanTrustPayloadList = new LinkedList<>();

        try {
            scanTrustPayloadList.addAll(dataHandlingUtil.buildPayloadDataFromArgs(inputCustomClassTypes,
                    argsValueList));
            if (scanTrustPayloadList.isEmpty()) {
                log.info(">>> Cannot upload SCM approved bottle at this time please try again later.");
                return;
            }
        } catch (final Exception e) {
            log.error(" Something went wrong with the buildPayloadDataFromArgs", e);
            return;
        }

        log.info(">>> The upload task that using Sync Api is a async process: {}", isSyncProcess);
        log.info(">>> Upload type: {}", uploadType.getValue());

        /** Check if it is a sync process */
        if (isSyncProcess) {
            Mono.fromRunnable(() -> sendScmDataToScanTrust(scanTrustPayloadList, dataHandlingUtil)).block();
        } else {
            /** Execute it by task executor. */
            sendScmDataToScanTrust(scanTrustPayloadList, dataHandlingUtil);
        }
    }

    /**
     * <p>
     * Send Scm Data to ScanTrust and update the status of the bottle.
     * </p>
     * 
     * @param payload          The payload string
     * @param dataHandlingUtil The utility class
     */
    @Async
    public void sendScmDataToScanTrust(final List<String> payload, final ScanTrustDataHandlingUtil dataHandlingUtil) {
        try {
            for (final String scanTrustPayload : payload) {
                final ScmSyncResponse response = scanTrustService.sendScmDataBySyncApi(scanTrustPayload);
                log.info("API response detail: {}", objectMapper.writeValueAsString(response));

                if (response != null) {
                    dataHandlingUtil.executeProcedureAfterSubmitByPayload(scanTrustPayload);
                } else {
                    log.info("Cannot send SCM approved data to ScanTrust by Sync API. Data detail: {}",
                            scanTrustPayload);
                    dataHandlingUtil.executeProcedureAfterSubmitFailedByPayload(scanTrustPayload);
                }
            }
        } catch (final Exception e) {
            log.error(" Something went wrong with the sendScmDataToScanTrust", e);
        }
    }
}
