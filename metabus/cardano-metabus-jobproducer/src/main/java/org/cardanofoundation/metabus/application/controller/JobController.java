package org.cardanofoundation.metabus.application.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.cardanofoundation.metabus.application.BaseResponse;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.service.JobService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.cardanofoundation.metabus.application.BaseUri.CARDANO_METABUS_JOBPRODUCER.JOBS;
import static org.cardanofoundation.metabus.application.BaseUri.CARDANO_METABUS_JOBPRODUCER.V1;

@RestController
@RequestMapping(V1 + JOBS)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JobController {
    JobService jobService;

    @PostMapping
    public BaseResponse<String> createJob(@RequestBody Job job) {
        jobService.createJob(job);
        return BaseResponse.ofSucceeded("job is being proccessed");
    }
}
