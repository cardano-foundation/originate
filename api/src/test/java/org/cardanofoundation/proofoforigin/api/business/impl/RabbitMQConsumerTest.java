package org.cardanofoundation.proofoforigin.api.business.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.charset.Charset;

import org.cardanofoundation.proofoforigin.api.business.CertificateService;
import org.cardanofoundation.proofoforigin.api.business.ScanTrustService;
import org.cardanofoundation.proofoforigin.api.business.ScmService;
import org.cardanofoundation.proofoforigin.api.configuration.RabbitMQConsumer;
import org.cardanofoundation.proofoforigin.api.configuration.modal.BusinessData;
import org.cardanofoundation.proofoforigin.api.configuration.modal.Job;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.Unit;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust.ScmAsyncResponse;
import org.cardanofoundation.proofoforigin.api.repository.entities.CertificateEntity;
import org.cardanofoundation.proofoforigin.api.repository.entities.Lot;
import org.cardanofoundation.proofoforigin.api.utils.GsonUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.rabbitmq.client.Channel;

@ExtendWith(MockitoExtension.class)
public class RabbitMQConsumerTest {

    @Mock
    private ScanTrustService scanTrustService;

    @Mock
    private ScmService scmService;

    @Mock
    private Channel channel;

    @InjectMocks
    @Spy
    private RabbitMQConsumer consumer;

    @Mock
    private CertificateService certificateService;

    @Test
    public void testConsume_withValidJob_ORIGINATE_LOT() throws Exception {
        // Arrange
        final Job job = new Job();
        job.setId(1L);
        job.setTxHash("txHash");
        job.setJobIndex("jobIndex");

        final BusinessData businessData = new BusinessData();
        businessData.setType(Unit.MetabusConstants.METABUS_TYPE_SCM);
        job.setBusinessData(businessData);

        final String jsonJob = GsonUtil.GSON.toJson(job);
        final Long jobId = job.getId();
        final String txId = job.getTxHash();
        final String jobIndex = job.getJobIndex();
        final Lot lot = new Lot();
        lot.setLotId("123");

        final ScmAsyncResponse response = new ScmAsyncResponse();
        response.setTaskId("taskId");

        when(scmService.updateTxIdAndJobIndexForLot(jobId, txId, jobIndex)).thenReturn(lot);
        when(scanTrustService.sendScmDataWhenTxConfirmed(txId, jobIndex, lot.getLotId())).thenReturn(response);

        // Act
        consumer.consume(jsonJob, channel, 1L);

        // Assert
        verify(scmService, times(1)).updateTxIdAndJobIndexForLot(jobId, txId, jobIndex);
        verify(scanTrustService, times(1)).sendScmDataWhenTxConfirmed(txId, jobIndex, lot.getLotId());
        verify(scanTrustService, times(1)).createJobCheckStatus(response.getTaskId(), jobId);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    public void testConsume_withValidJob_ORIGINATE_CERT() throws Exception {
        // Arrange
        final Job job = new Job();
        job.setId(1L);
        job.setTxHash("txHash");
        job.setJobIndex("jobIndex");

        final BusinessData businessData = new BusinessData();
        businessData.setType(Unit.MetabusConstants.METABUS_TYPE_CERT);
        job.setBusinessData(businessData);

        final String jsonJob = GsonUtil.GSON.toJson(job);
        final Long jobId = job.getId();
        final String txId = job.getTxHash();
        final String jobIndex = job.getJobIndex();
        final CertificateEntity certificateEntity = new CertificateEntity();

        final ScmAsyncResponse response = new ScmAsyncResponse();
        response.setTaskId("taskId");

        when(certificateService.updateTxIdAndJobIndexForCertificate(jobId, txId, jobIndex))
                .thenReturn(certificateEntity);

        // Act
        consumer.consume(jsonJob, channel, 1L);

        // Assert
        verify(certificateService, times(1)).updateTxIdAndJobIndexForCertificate(jobId, txId, jobIndex);
    }

    @Test
    public void testConsume_withValidJob_ORIGINATE_CERT_REVOKE() throws Exception {
        // Arrange
        final Job job = new Job();
        job.setId(1L);
        job.setTxHash("txHash");
        job.setJobIndex("jobIndex");

        final BusinessData businessData = new BusinessData();
        businessData.setType(Unit.MetabusConstants.METABUS_TYPE_CERT_REVOCATION);
        job.setBusinessData(businessData);

        final String jsonJob = GsonUtil.GSON.toJson(job);
        final Long jobId = job.getId();
        final String txId = job.getTxHash();
        final String jobIndex = job.getJobIndex();
        final CertificateEntity certificateEntity = new CertificateEntity();

        final ScmAsyncResponse response = new ScmAsyncResponse();
        response.setTaskId("taskId");

        when(certificateService.updateTxIdAndJobIndexForCertificateRevoke(jobId, txId, jobIndex))
                .thenReturn(certificateEntity);

        // Act
        consumer.consume(jsonJob, channel, 1L);

        // Assert
        verify(certificateService, times(1)).updateTxIdAndJobIndexForCertificateRevoke(jobId, txId, jobIndex);
    }

    @Test
    public void testConsume_withInvalidJob() throws Exception {
        // Arrange
        final String jsonJob = null;
        final MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader(AmqpHeaders.DELIVERY_TAG, 1L);

        // Act
        consumer.consume(jsonJob, channel, 1L);

        // Assert
        verify(scmService, never()).updateTxIdAndJobIndexForLot(any(), any(), any());
        verify(scanTrustService, never()).sendScmDataWhenTxConfirmed(any(), any(), any());
        verify(scanTrustService, never()).createJobCheckStatus(any(), any());
        verify(channel, times(1)).basicNack(1L, false, false);
    }

    @Test
    public void testConsume_withScanTrustException() throws Exception {
        // Arrange
        final Job job = new Job();
        job.setId(1L);
        job.setTxHash("txHash");
        job.setJobIndex("jobIndex");

        final BusinessData businessData = new BusinessData();
        businessData.setType(Unit.MetabusConstants.METABUS_TYPE_SCM);
        job.setBusinessData(businessData);

        final String jsonJob = GsonUtil.GSON.toJson(job);
        final Long jobId = job.getId();
        final String txId = job.getTxHash();
        final String jobIndex = job.getJobIndex();
        final Lot lot = new Lot();
        lot.setLotId("123");

        when(scmService.updateTxIdAndJobIndexForLot(jobId, txId, jobIndex)).thenReturn(lot);
        when(scanTrustService.sendScmDataWhenTxConfirmed(txId, jobIndex, lot.getLotId()))
                .thenThrow(new RuntimeException("ScmServiceException"));
        // Act
        consumer.consume(jsonJob, channel, 1L);

        // Assert
        verify(scmService, times(1)).updateTxIdAndJobIndexForLot(jobId, txId, jobIndex);
        verify(scanTrustService, times(1)).sendScmDataWhenTxConfirmed(txId, jobIndex, lot.getLotId());
        verify(scanTrustService, never()).createJobCheckStatus(any(), any());
        verify(channel, times(1)).basicNack(1L, false, true);
    }

    /**
     * <p>
     * Test the consumer when the Web-client meet the
     * <b>WebClientRequestException</b>
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testConsume_withWebClientRequestException() throws Exception {
        // Arrange
        final Job job = new Job();
        job.setId(1L);
        job.setTxHash("txHash");
        job.setJobIndex("jobIndex");

        final BusinessData businessData = new BusinessData();
        businessData.setType(Unit.MetabusConstants.METABUS_TYPE_SCM);
        job.setBusinessData(businessData);

        final String jsonJob = GsonUtil.GSON.toJson(job);
        final Long jobId = job.getId();
        final String txId = job.getTxHash();
        final String jobIndex = job.getJobIndex();
        final Lot lot = new Lot();
        lot.setLotId("123");

        final WebClientRequestException requestException = new WebClientRequestException(new RuntimeException(),
                HttpMethod.POST, new URI("http://example.com"), HttpHeaders.EMPTY);

        when(scmService.updateTxIdAndJobIndexForLot(jobId, txId, jobIndex)).thenReturn(lot);
        when(scanTrustService.sendScmDataWhenTxConfirmed(txId, jobIndex, lot.getLotId()))
                .thenThrow(new RuntimeException(requestException));
        // Act
        consumer.consume(jsonJob, channel, 1L);

        // Assert
        verify(scmService, times(1)).updateTxIdAndJobIndexForLot(jobId, txId, jobIndex);
        verify(scanTrustService, times(1)).sendScmDataWhenTxConfirmed(txId, jobIndex, lot.getLotId());
        verify(scanTrustService, never()).createJobCheckStatus(any(), any());
        verify(channel, times(1)).basicNack(1L, false, true);
    }

    /**
     * <p>
     * Test the consumer when the Web-client meet the
     * <b>WebClientResponseException</b>
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testConsume_withWebClientResponseException() throws Exception {
        // Arrange
        final Job job = new Job();
        job.setId(1L);
        job.setTxHash("txHash");
        job.setJobIndex("jobIndex");

        final BusinessData businessData = new BusinessData();
        businessData.setType(Unit.MetabusConstants.METABUS_TYPE_SCM);
        job.setBusinessData(businessData);

        final String jsonJob = GsonUtil.GSON.toJson(job);
        final Long jobId = job.getId();
        final String txId = job.getTxHash();
        final String jobIndex = job.getJobIndex();
        final Lot lot = new Lot();
        lot.setLotId("123");

        final WebClientResponseException responseException = new WebClientResponseException("4xx error",
                HttpStatus.BAD_REQUEST.value(), "400", HttpHeaders.EMPTY, null, Charset.defaultCharset());

        when(scmService.updateTxIdAndJobIndexForLot(jobId, txId, jobIndex)).thenReturn(lot);
        when(scanTrustService.sendScmDataWhenTxConfirmed(txId, jobIndex, lot.getLotId()))
                .thenThrow(new RuntimeException(responseException));
        // Act
        consumer.consume(jsonJob, channel, 1L);

        // Assert
        verify(scmService, times(1)).updateTxIdAndJobIndexForLot(jobId, txId, jobIndex);
        verify(scanTrustService, times(1)).sendScmDataWhenTxConfirmed(txId, jobIndex, lot.getLotId());
        verify(scanTrustService, never()).createJobCheckStatus(any(), any());
        verify(channel, times(1)).basicNack(1L, false, false);
    }

    @Test
    public void testConsume_CannotFindJobForUpdate() throws Exception {
        // Arrange
        final Job job = new Job();
        job.setId(1L);
        job.setTxHash("txHash");
        job.setJobIndex("jobIndex");

        final BusinessData businessData = new BusinessData();
        businessData.setType(Unit.MetabusConstants.METABUS_TYPE_SCM);
        job.setBusinessData(businessData);

        final String jsonJob = GsonUtil.GSON.toJson(job);
        final Long jobId = job.getId();
        final String txId = job.getTxHash();
        final String jobIndex = job.getJobIndex();

        when(scmService.updateTxIdAndJobIndexForLot(jobId, txId, jobIndex)).thenReturn(null);

        // Act
        consumer.consume(jsonJob, channel, 1L);

        // Assert
        verify(scmService, times(1)).updateTxIdAndJobIndexForLot(jobId, txId, jobIndex);
        verify(channel, times(1)).basicNack(1L, false, false);
    }

    @Test
    public void testConsume_CannotFindCertificateForUpdate() throws Exception {
        // Arrange
        final Job job = new Job();
        job.setId(1L);
        job.setTxHash("txHash");
        job.setJobIndex("jobIndex");

        final BusinessData businessData = new BusinessData();
        businessData.setType(Unit.MetabusConstants.METABUS_TYPE_CERT);
        job.setBusinessData(businessData);

        final String jsonJob = GsonUtil.GSON.toJson(job);
        final Long jobId = job.getId();
        final String txId = job.getTxHash();
        final String jobIndex = job.getJobIndex();

        when(certificateService.updateTxIdAndJobIndexForCertificate(jobId, txId, jobIndex)).thenReturn(null);

        // Act
        consumer.consume(jsonJob, channel, 1L);

        // Assert
        verify(certificateService, times(1)).updateTxIdAndJobIndexForCertificate(jobId, txId, jobIndex);
        verify(channel, times(1)).basicNack(1L, false, false);
    }

    @Test
    public void testConsume_CannotFindCertificateRevokeForUpdate() throws Exception {
        // Arrange
        final Job job = new Job();
        job.setId(1L);
        job.setTxHash("txHash");
        job.setJobIndex("jobIndex");

        final BusinessData businessData = new BusinessData();
        businessData.setType(Unit.MetabusConstants.METABUS_TYPE_CERT_REVOCATION);
        job.setBusinessData(businessData);

        final String jsonJob = GsonUtil.GSON.toJson(job);
        final Long jobId = job.getId();
        final String txId = job.getTxHash();
        final String jobIndex = job.getJobIndex();

        when(certificateService.updateTxIdAndJobIndexForCertificateRevoke(jobId, txId, jobIndex)).thenReturn(null);

        // Act
        consumer.consume(jsonJob, channel, 1L);

        // Assert
        verify(certificateService, times(1)).updateTxIdAndJobIndexForCertificateRevoke(jobId, txId, jobIndex);
        verify(channel, times(1)).basicNack(1L, false, false);
    }
}