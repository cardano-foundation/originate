package org.cardanofoundation.metabus.services;


import org.cardanofoundation.metabus.common.offchain.Job;

public interface JobService {
    Job createJob(Job job);

    Job getJob(Long jobId);
}
