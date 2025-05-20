package org.cardanofoundation.metabus.unittest.mapper;

import org.cardanofoundation.metabus.common.entities.JobJPA;
import org.cardanofoundation.metabus.common.entities.UnconfirmedTxJPA;
import org.cardanofoundation.metabus.common.enums.GroupType;
import org.cardanofoundation.metabus.common.enums.JobState;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.mapper.JobMapper;
import org.cardanofoundation.metabus.mapper.JobMapperImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JobMapperTest {


    JobMapper jobMapper = new JobMapperImpl();

    @Test
    void test_map_job() {
        JobJPA jobJPA = JobJPA.builder()
                .id(0L)
                .state(JobState.ON_CHAIN)
                .type("Type")
                .subType("SubType")
                .data("Data")
                .signature("signature".getBytes())
                .pubKey("pubKey".getBytes())
                .unconfirmedTx(UnconfirmedTxJPA.builder().txHash("TxHash").build())
                .groupType(GroupType.MULTI_GROUP)
                .group("Group")
                .jobIndex("JobIndex")
                .build();
        Job job = jobMapper.toJob(jobJPA);

        assertEquals(jobJPA.getId(), job.getId());
        assertEquals(jobJPA.getState(), job.getState());
        assertEquals(jobJPA.getType(), job.getBusinessData().getType());
        assertEquals(jobJPA.getSubType(), job.getBusinessData().getSubType());
        assertEquals(jobJPA.getData(), job.getBusinessData().getData());
        assertEquals(jobJPA.getSignature(), job.getBusinessData().getSignature());
        assertEquals(jobJPA.getPubKey(), job.getBusinessData().getPubKey());
        assertEquals(jobJPA.getUnconfirmedTx().getTxHash(), job.getTxHash());
        assertEquals(jobJPA.getGroupType(), job.getGroupType());
        assertEquals(jobJPA.getGroup(), job.getGroup());
        assertEquals(jobJPA.getJobIndex(), job.getJobIndex());
    }
}
