package org.cardanofoundation.metabus.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.cardanofoundation.metabus.common.entities.JobJPA;
import org.cardanofoundation.metabus.common.entities.UnconfirmedTxJPA;
import org.cardanofoundation.metabus.common.enums.GroupType;
import org.cardanofoundation.metabus.common.enums.JobState;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.controllers.dtos.BusinessData;
import org.cardanofoundation.metabus.controllers.dtos.JobReq;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JobMapperImplTest {
    @InjectMocks
    private JobMapperImpl jobMapper;

    final String fakeSignature = "eyJraWQiOiI3MmNjNGY2NC1iMjgxLTQwOGItODQwNC05OWY5YmRkZTg5MTkiLCJhbGciOiJFZERTQSJ9.J-AzQJmYmaoycSY5yolWVsOdUFJDFti3qKvSYA-6JwtYPDHEogUnicexG0weEf_Q1dijAeU1kNaC6PW_VDSbDA";

    final byte[] signaturePart = Base64.getUrlDecoder().decode("J-AzQJmYmaoycSY5yolWVsOdUFJDFti3qKvSYA-6JwtYPDHEogUnicexG0weEf_Q1dijAeU1kNaC6PW_VDSbDA");

    final byte[] pubKey = Base64.getUrlDecoder().decode("pubKey");

    @Test
    void testToJobFromJobReq() throws JsonProcessingException {
        String data = "data";
        BusinessData businessData = new BusinessData();
        businessData.setType("type");
        businessData.setData(data);
        businessData.setPubKey("pubKey");
        businessData.setSignature(fakeSignature);
        JobReq jobReq = new JobReq();
        jobReq.setBusinessData(businessData);
        jobReq.setGroupType(GroupType.SINGLE_GROUP);
        jobReq.setGroup("group");


        // Act
        Job actualJob = jobMapper.toJob(jobReq);

        // Assert
        assertEquals("type", actualJob.getBusinessData().getType());
        assertEquals("data", actualJob.getBusinessData().getData());
        assertArrayEquals(pubKey, actualJob.getBusinessData().getPubKey());
        assertArrayEquals(signaturePart, actualJob.getBusinessData().getSignature());
        assertEquals(GroupType.SINGLE_GROUP, actualJob.getGroupType());
        assertEquals("group", actualJob.getGroup());
    }


    @Test
    void testToJobFromJobJPA() {
        // Arrange
        JobJPA jobJPA = new JobJPA();
        jobJPA.setId(1L);
        jobJPA.setState(JobState.PENDING);
        jobJPA.setType("type");
        jobJPA.setSubType("subType");
        jobJPA.setData("data");
        jobJPA.setPubKey(pubKey);
        jobJPA.setSignature(signaturePart);
        jobJPA.setGroup("group");
        jobJPA.setGroupType(GroupType.SINGLE_GROUP);
        jobJPA.setRetryCount(5);
        jobJPA.setUnconfirmedTx(UnconfirmedTxJPA.builder().txHash("txHash").build());

        // Act
        Job actualJob = jobMapper.toJob(jobJPA);

        // Assert
        assertEquals(1L, actualJob.getId());
        assertEquals(JobState.PENDING, actualJob.getState());
        assertEquals("type", actualJob.getBusinessData().getType());
        assertEquals("subType", actualJob.getBusinessData().getSubType());
        assertEquals("data", actualJob.getBusinessData().getData());
        assertEquals(pubKey, actualJob.getBusinessData().getPubKey());
        assertArrayEquals(signaturePart, actualJob.getBusinessData().getSignature());
        assertEquals("txHash", actualJob.getTxHash());
        assertEquals("group", actualJob.getGroup());
        assertEquals(GroupType.SINGLE_GROUP, actualJob.getGroupType());
        assertEquals(5, actualJob.getRetryCount());
    }

    @Test
    void testToJobJpa() {
        // Arrange
        Job job = new Job();
        org.cardanofoundation.metabus.common.offchain.BusinessData businessData = new org.cardanofoundation.metabus.common.offchain.BusinessData();
        businessData.setType("type");
        businessData.setSubType("subType");
        businessData.setData("data");
        businessData.setPubKey(pubKey);
        businessData.setSignature(signaturePart);
        job.setBusinessData(businessData);
        job.setGroup("group");
        job.setGroupType(GroupType.SINGLE_GROUP);
        job.setRetryCount(5);

        // Act
        JobJPA actualJobJPA = jobMapper.toJobJpa(job);

        // Assert
        assertEquals(JobState.PENDING, actualJobJPA.getState());
        assertEquals("type", actualJobJPA.getType());
        assertEquals("subType", actualJobJPA.getSubType());
        assertEquals("data", actualJobJPA.getData());
        assertEquals(pubKey, actualJobJPA.getPubKey());
        assertArrayEquals(signaturePart, actualJobJPA.getSignature());
        assertEquals("group", actualJobJPA.getGroup());
        assertEquals(GroupType.SINGLE_GROUP, actualJobJPA.getGroupType());
        assertEquals(5, actualJobJPA.getRetryCount());
        assertFalse(actualJobJPA.getIsDeleted());
    }
}