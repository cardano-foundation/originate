package org.cardanofoundation.proofoforigin.api.business;


import org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust.ScmAsyncResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust.ScmSyncResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust.ScmTaskResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust.ScmData;
import org.cardanofoundation.proofoforigin.api.repository.entities.Bottle;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.function.Predicate;

@Service
public interface ScanTrustService {
    ScmAsyncResponse sendScmDataWhenTxConfirmed(String transactionId, String jobIndex, String lotId) throws JsonProcessingException;

    ScmSyncResponse sendScmDataSync(String payloadScanTrust);

    ScmAsyncResponse sendScmData(String payloadScanTrust);

    ScmTaskResponse checkTaskState(String taskId, String lotId);

    void createJobCheckStatus(String taskId, Long batchId);

    /**
     * <p>
     * Send Scm Data by Synchronous API
     * </p>
     *
     * @param payload the Payload of the data
     * @return The Scm Sync Response
     */
    ScmSyncResponse sendScmDataBySyncApi(final String payload);

    void sendScmDataWhenApproved(ScmData scmData);

    void sendScmDataWhenMappingBottle(List<Bottle> listBottle);

    /**
     * <p>
     * The Predicate function to filter the exception when requesting to the
     * ScanTrust Server
     * </p>
     * 
     * @param throwable The throwable object
     * @return is the exception is worth for retry ?
     * @see WebClientResponseException
     * @see WebClientRequestException
     */
    public static Predicate<Throwable> filterTheScanTrustWebClientException = (throwable) -> {
        // We are having 2 exception when request to server is fail.
        // 1. WebClientResponseException
        // 2. WebClientRequestException
        if (throwable instanceof WebClientResponseException) {
            final WebClientResponseException exception = (WebClientResponseException) throwable;
            return exception.getStatusCode().is5xxServerError()
                    || exception.getStatusCode()
                            .isSameCodeAs(HttpStatusCode.valueOf(HttpStatus.TOO_MANY_REQUESTS.value()));
        }

        return true;
    };
}
