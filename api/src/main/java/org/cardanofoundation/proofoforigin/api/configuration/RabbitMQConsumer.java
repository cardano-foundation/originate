package org.cardanofoundation.proofoforigin.api.configuration;

import java.io.IOException;

import org.cardanofoundation.proofoforigin.api.business.CertificateService;
import org.cardanofoundation.proofoforigin.api.business.ScanTrustService;
import org.cardanofoundation.proofoforigin.api.business.ScmService;
import org.cardanofoundation.proofoforigin.api.configuration.modal.Job;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.Unit;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust.ScmAsyncResponse;
import org.cardanofoundation.proofoforigin.api.repository.entities.CertificateEntity;
import org.cardanofoundation.proofoforigin.api.repository.entities.Lot;
import org.cardanofoundation.proofoforigin.api.utils.GsonUtil;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RabbitMQConsumer {

    private final ScanTrustService scanTrustService;
    private final ScmService scmService;
    private final CertificateService certificateService;

    @RabbitListener(queues = { "${spring.rabbitmq.bindings[0].queue}" })
    public void consume(final String jsonJob, final Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) final long tag)
            throws IOException {
        // Get job from message
        final Job job = GsonUtil.GSON.fromJson(jsonJob, Job.class);

        // If the job is not valid Nack the message to dead letter
        if (job == null ) {
            log.error(">>> RabbitMq job message is not valid {}", jsonJob);
            // Nack to dead-letter queue
            channel.basicNack(tag, false, false);
            return;
        }

        final Long jobId = job.getId();
        final String txId = job.getTxHash();
        final String jobIndex = job.getJobIndex();
        String businessType = job.getBusinessData().getType();
        if(Unit.MetabusConstants.METABUS_TYPE_CERT.equals(businessType)){
            final CertificateEntity certificate = certificateService.updateTxIdAndJobIndexForCertificate(jobId, txId, jobIndex);

            // If cannot find certificate with job id
            if (certificate == null) {
                log.error(">>> Can not find certificate with job id: {}", jobId);
                // Nack to dead-letter queue
                channel.basicNack(tag, false, false);
            }
            channel.basicAck(tag, false);
            return;
        }

        if(Unit.MetabusConstants.METABUS_TYPE_CERT_REVOCATION.equals(businessType)){
            final CertificateEntity certificate = certificateService.updateTxIdAndJobIndexForCertificateRevoke(jobId, txId, jobIndex);

            // If cannot find certificate with revoke job id
            if (certificate == null) {
                log.error(">>> Can not find certificate with job id: {}", jobId);
                // Nack to dead-letter queue
                channel.basicNack(tag, false, false);
            }
            channel.basicAck(tag, false);
            return;
        }

        final Lot lot = scmService.updateTxIdAndJobIndexForLot(jobId, txId, jobIndex);

        // If cannot find lot with job id
        if (lot == null) {
            log.error(">>> Can not find lot with job id: {}", jobId);
            // Nack to dead-letter queue
            channel.basicNack(tag, false, false);
            return;
        }

        // Submit scm data to ScanTrust.
        try {
            final ScmAsyncResponse scmAsyncResponse = scanTrustService.sendScmDataWhenTxConfirmed(txId, jobIndex, lot.getLotId());

            if (scmAsyncResponse != null) {
                scanTrustService.createJobCheckStatus(scmAsyncResponse.getTaskId(), jobId);
            }

            // Ack the current message.
            channel.basicAck(tag, false);

        } catch (final Exception e) {
            log.error("Cannot send data to Scan Trust because of: ", e);
            // Re-queue the message to retry submission.
            channel.basicNack(tag, false, isTheMessageNeedToBeRequeueOrNot(e));
        }
    }

    /**
     * <p>
     * Consider that the exception need to be requeued when sending data to ScanTrust throw an exception
     * </p>
     * 
     * @param exception The thrown exception
     * @return is need to be requeued or not
     */
    private static boolean isTheMessageNeedToBeRequeueOrNot(final Exception exception) {
        // If the ScanTrust server response an error.
        if (exception.getCause() instanceof WebClientResponseException) {
            final WebClientResponseException resException = (WebClientResponseException) exception.getCause();
            // If the ScanTrust server response the 4XX status.
            // Nack the message and push to Dlt Queue
            if (resException.getStatusCode().is4xxClientError()) {
                return false;
            }
        }

        // If the WebClient throw exception that is caused by client side.
        if (exception.getCause() instanceof WebClientRequestException) {
            log.error("Web client Request Exception has been occurred: ", exception.getCause());
        }

        return true;
    }
}