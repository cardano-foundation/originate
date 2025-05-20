package org.cardanofoundation.proofoforigin.api.business;

import org.cardanofoundation.proofoforigin.api.job.JobDescriptor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface JobService {

    JobDescriptor createJob(String group, JobDescriptor descriptor);

    @Transactional(readOnly = true)
    Optional<JobDescriptor> findJob(String group, String name);

    void updateJob(String group, String name, JobDescriptor descriptor);

    void deleteJob(String group, String name);

    void pauseJob(String group, String name);

    void resumeJob(String group, String name);
}
