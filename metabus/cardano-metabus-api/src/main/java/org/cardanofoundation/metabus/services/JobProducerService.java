package org.cardanofoundation.metabus.services;

import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.controllers.BaseResponse;

public interface JobProducerService {
    BaseResponse<String> createJob(Job job);
}
