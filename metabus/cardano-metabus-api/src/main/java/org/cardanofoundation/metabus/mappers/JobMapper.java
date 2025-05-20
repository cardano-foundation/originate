package org.cardanofoundation.metabus.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.cardanofoundation.metabus.common.entities.JobJPA;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.controllers.dtos.JobReq;
import org.cardanofoundation.metabus.controllers.dtos.JobResp;

public interface JobMapper {
    Job toJob(JobReq jobReq) throws JsonProcessingException;
    Job toJob(JobJPA jobJPA);
    JobJPA toJobJpa(Job job);
    JobResp toJobResp(Job job);
}
