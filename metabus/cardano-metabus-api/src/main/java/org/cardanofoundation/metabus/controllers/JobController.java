package org.cardanofoundation.metabus.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.constants.BaseUri;
import org.cardanofoundation.metabus.controllers.dtos.JobReq;
import org.cardanofoundation.metabus.controllers.dtos.JobResp;
import org.cardanofoundation.metabus.exceptions.MetabusErrors;
import org.cardanofoundation.metabus.exceptions.MetabusException;
import org.cardanofoundation.metabus.mappers.JobMapper;
import org.cardanofoundation.metabus.services.JobService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(BaseUri.CARDANO_METABUS_API.V1 + BaseUri.CARDANO_METABUS_API.JOBS)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JobController {
    JobService jobService;
    JobMapper jobMapper;

    @PostMapping
    public BaseResponse<JobResp> createJob(@Valid @RequestBody JobReq jobReq) throws JsonProcessingException {
        String jobType = jobReq.getBusinessData().getType();
        if(!jobType.matches("^[^:]+:[^:]+$")){
            throw new MetabusException(MetabusErrors.INVALID_JOB_TYPE);
        }
        String subType = jobType.split(":")[1];

        Job job = jobMapper.toJob(jobReq);
        job.getBusinessData().setSubType(subType);
        Job createdJob = jobService.createJob(job);

        BaseResponse<JobResp> response = BaseResponse.ofSucceeded(jobMapper.toJobResp(createdJob));
        response.getMeta().setMessage("Your job is being processed");
        return response;
    }

    @GetMapping("/{jobId}")
    public BaseResponse<JobResp> getJob(@PathVariable(value = "jobId") Long jobId) {
        return BaseResponse.ofSucceeded(jobMapper.toJobResp(jobService.getJob(jobId)));
    }
}
