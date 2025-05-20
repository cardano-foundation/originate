package org.cardanofoundation.proofoforigin.api.business.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.proofoforigin.api.business.JobService;
import org.cardanofoundation.proofoforigin.api.job.JobDescriptor;
import org.quartz.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.quartz.JobKey.jobKey;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final Scheduler scheduler;

    @Override
    public JobDescriptor createJob(String group, JobDescriptor descriptor) {
        descriptor.setGroup(group);
        JobDetail jobDetail = descriptor.buildJobDetail(descriptor.getJobDataMap());
        Set<Trigger> triggersForJob = descriptor.buildTriggers();
        log.info("About to save job with key - {}", jobDetail.getKey());
        try {
            scheduler.scheduleJob(jobDetail, triggersForJob, false);
            log.info("Job with key - {} saved successfully", jobDetail.getKey());
        } catch (SchedulerException e) {
            log.error("Could not save job with key - {} due to error - {}", jobDetail.getKey(), e.getLocalizedMessage());
            throw new IllegalArgumentException(e.getLocalizedMessage());
        }
        return descriptor;
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<JobDescriptor> findJob(String group, String name) {
        try {
            JobDetail jobDetail = scheduler.getJobDetail(jobKey(name, group));
            if (Objects.nonNull(jobDetail))
                return Optional.of(
                        JobDescriptor.buildDescriptor(jobDetail, scheduler.getTriggersOfJob(jobKey(name, group))));
        } catch (SchedulerException e) {
            log.error("Could not find job with key - {}.{} due to error - {}", group, name, e.getLocalizedMessage());
        }
        log.warn("Could not find job with key - {}.{}", group, name);
        return Optional.empty();
    }

    @Override
    public void updateJob(String group, String name, JobDescriptor descriptor) {
        try {
            JobDetail oldJobDetail = scheduler.getJobDetail(jobKey(name, group));
            if (Objects.nonNull(oldJobDetail)) {
                JobDataMap jobDataMap = oldJobDetail.getJobDataMap();
                JobBuilder jb = oldJobDetail.getJobBuilder();
                JobDetail newJobDetail = jb.usingJobData(jobDataMap).storeDurably().build();
                scheduler.addJob(newJobDetail, true);
                log.info("Updated job with key - {}", newJobDetail.getKey());
                return;
            }
            log.warn("Could not find job with key - {}.{} to update", group, name);
        } catch (SchedulerException e) {
            log.error("Could not find job with key - {}.{} to update due to error - {}", group, name, e.getLocalizedMessage());
        }
    }

    @Override
    public void deleteJob(String group, String name) {
        try {
            scheduler.deleteJob(jobKey(name, group));
            log.info("Deleted job with key - {}.{}", group, name);
        } catch (SchedulerException e) {
            log.error("Could not delete job with key - {}.{} due to error - {}", group, name, e.getLocalizedMessage());
        }
    }

    @Override
    public void pauseJob(String group, String name) {
        try {
            scheduler.pauseJob(jobKey(name, group));
            log.info("Paused job with key - {}.{}", group, name);
        } catch (SchedulerException e) {
            log.error("Could not pause job with key - {}.{} due to error - {}", group, name, e.getLocalizedMessage());
        }
    }

    @Override
    public void resumeJob(String group, String name) {
        try {
            scheduler.resumeJob(jobKey(name, group));
            log.info("Resumed job with key - {}.{}", group, name);
        } catch (SchedulerException e) {
            log.error("Could not resume job with key - {}.{} due to error - {}", group, name, e.getLocalizedMessage());
        }
    }

}
