package org.cardanofoundation.metabus.service;

import org.cardanofoundation.metabus.common.offchain.Job;

public interface JobService {
    void pushJobToRabbit(Job job);
}
