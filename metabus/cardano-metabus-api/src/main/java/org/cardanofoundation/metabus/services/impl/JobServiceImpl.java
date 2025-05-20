package org.cardanofoundation.metabus.services.impl;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.cardanofoundation.metabus.common.entities.JobJPA;
import org.cardanofoundation.metabus.common.entities.ScheduledBatchesJPA;
import org.cardanofoundation.metabus.common.enums.BatchStatus;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.controllers.dtos.JobResp;
import org.cardanofoundation.metabus.exceptions.MetabusErrors;
import org.cardanofoundation.metabus.exceptions.MetabusException;
import org.cardanofoundation.metabus.mappers.JobMapper;
import org.cardanofoundation.metabus.repositories.JobRepository;
import org.cardanofoundation.metabus.repositories.ScheduledBatchesRepository;
import org.cardanofoundation.metabus.security.properties.MetabusSecurityProperties;
import org.cardanofoundation.metabus.services.JobProducerService;
import org.cardanofoundation.metabus.services.JobService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Modified (sotatek) joey.dao
 * @since 2023/08
 */
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    /** Services */
    JobRepository jobRepository;
    JobProducerService jobProducerService;
    MetabusSecurityProperties metabusSecurityProperties;
    JobMapper jobMapper;
    ScheduledBatchesRepository scheduledBatchesRepository;

    @NonFinal
    @Value("${metabus.jobDefaultRetryCount}")
    Integer retryCount;

    @Override
    @Transactional
    public Job createJob(Job job) {
        try {
            String jobType = job.getBusinessData().getType();
            if (!checkJobTypePermission(jobType)) {
                throw new MetabusException(MetabusErrors.FORBIDDEN,
                        String.format("You do not have permission to create job with type: %s", jobType));
            }
            JobJPA jobJPA = jobMapper.toJobJpa(job);
            jobJPA.setRetryCount(retryCount);
            JobJPA savedJobJPA = jobRepository.save(jobJPA);

            /** Create or update schedule batch */
            List<ScheduledBatchesJPA> scheduledBatches = scheduledBatchesRepository.findByJobType(jobType);
            // If the schedule by job type does not exist -> Create one.
            if (scheduledBatches.isEmpty()) {
                final ScheduledBatchesJPA scheduledBatchesJPA = ScheduledBatchesJPA.builder()
                        .batchStatus(BatchStatus.PENDING).consumedJobTime(Instant.now()).jobType(jobType).build();
                scheduledBatchesRepository.insertScheduledBatchesDoNothingOnConflict(scheduledBatchesJPA);
            } else {
                // Filter the schedule is not NONE.
                // The reason why i use findByJoType not findByJobTypeAndStatus
                // that we gonna have a case that having Status != NONE.
                // So we have to need one more query to execute that checks the jobType are
                // already existed.
                scheduledBatches = scheduledBatches.stream()
                        .filter(schedule -> BatchStatus.NONE.equals(schedule.getBatchStatus()))
                        .collect(Collectors.toList());
                // Update the status of the schedule
                // We need a custom query to execute the update The schedule batch.
                // Because of the JPA will generate the update query based on id of the entity
                // Furthermore, The jobType of the entity is configured unique
                // and we need to ensure only one update query is executed during multiple
                // transactions sessions
                for (ScheduledBatchesJPA scheduledBatch : scheduledBatches) {
                    scheduledBatch.setBatchStatus(BatchStatus.PENDING);
                    scheduledBatch.setConsumedJobTime(Instant.now());
                    scheduledBatchesRepository.updateScheduledBatchesByJobType(scheduledBatch);
                }
            }

            Job createdJob = jobMapper.toJob(savedJobJPA);
            jobProducerService.createJob(createdJob);
            return createdJob;
        }
        catch (MetabusException metabusException){
            throw metabusException;
        }
        catch (Exception e) {
            throw new MetabusException(MetabusErrors.ERROR_CREATING_JOB, e.getMessage());
        }
    }


    @SuppressWarnings("unchecked")
    private List<String> getCurrentGrantedRole(){
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) a.getCredentials();
        Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");
        return (List<String>) realmAccess.get("roles");
    }

    private boolean checkJobTypePermission(String jobType){
        List<String> grantedRoles = getCurrentGrantedRole();
        List<MetabusSecurityProperties.JobTypeAuthorization> jobTypeAuthorizations =
                metabusSecurityProperties.getJobTypeAuthorizations();
        for(String grantedRole: grantedRoles){
            for(MetabusSecurityProperties.JobTypeAuthorization jobTypeAuthorization: jobTypeAuthorizations){
                List<String> roles = jobTypeAuthorization.getRoles();
                List<String> allowedJobTypes= jobTypeAuthorization.getAllowedJobTypes();

                if(roles.stream().anyMatch(grantedRole::equals) && allowedJobTypes.stream().anyMatch(jobType::equals)){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Job getJob(Long jobId) {
        JobJPA jobJPA = jobRepository.findById(jobId)
                .orElseThrow(() -> new MetabusException(MetabusErrors.ERROR_GETTING_JOB,
                        String.format("Cannot find job with id: %s", jobId)));
        var jobType = jobJPA.getType();
        if (!checkJobTypePermission(jobType)) {
            throw new MetabusException(MetabusErrors.FORBIDDEN,
                    String.format("You do not have permission to get job with type: %s", jobType));
        }
        return jobMapper.toJob(jobJPA);
    }
}
