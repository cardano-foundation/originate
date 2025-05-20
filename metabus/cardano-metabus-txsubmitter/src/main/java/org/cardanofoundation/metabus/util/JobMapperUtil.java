package org.cardanofoundation.metabus.util;

import java.util.Objects;

import org.cardanofoundation.metabus.common.entities.JobJPA;
import org.cardanofoundation.metabus.common.entities.UnconfirmedTxJPA;
import org.cardanofoundation.metabus.common.enums.JobState;
import org.cardanofoundation.metabus.common.offchain.BusinessData;
import org.cardanofoundation.metabus.common.offchain.Job;

/**
 * <p>
 * The utility interface to converting the JPA entity to MOJO entity
 * </p>
 * 
 * @version 0.01
 * @category Service
 * @since 2023/06
 */
public interface JobMapperUtil {

    /**
     * <p>
     * Map The JobJPA to Job Object
     * </p>
     * 
     * @param jobJPA The source object
     * @return The mapped Job Object
     */
    static Job toJob(JobJPA jobJPA) {
        UnconfirmedTxJPA unconfirmedTxJPA = jobJPA.getUnconfirmedTx();
        String txHash = Objects.nonNull(unconfirmedTxJPA) ? unconfirmedTxJPA.getTxHash() : null;

        return Job.builder()
                .id(jobJPA.getId())
                .state(jobJPA.getState())
                .businessData(BusinessData.builder()
                        .type(jobJPA.getType())
                        .subType(jobJPA.getSubType())
                        .data(jobJPA.getData())
                        .jwsHeader(jobJPA.getJwsHeader())
                        .signature(jobJPA.getSignature())
                        .pubKey(jobJPA.getPubKey())
                        .build())
                .txHash(txHash)
                .groupType(jobJPA.getGroupType())
                .group(jobJPA.getGroup())
                .retryCount(jobJPA.getRetryCount())
                .build();
    }

    /**
     * <p>
     * Map The Job Object to JobJPA Object
     * </p>
     * 
     * @param job The source object
     * @return The mapped JobJPA Object
     */
    static JobJPA toJobJpa(Job job) {
        BusinessData businessData = job.getBusinessData();
        return JobJPA.builder()
                .id(job.getId())
                .state(JobState.PENDING)
                .type(businessData.getType())
                .subType(businessData.getSubType())
                .data(businessData.getData())
                .pubKey(businessData.getPubKey())
                .jwsHeader(businessData.getJwsHeader())
                .signature(businessData.getSignature())
                .group(job.getGroup())
                .groupType(job.getGroupType())
                .isDeleted(false)
                .retryCount(job.getRetryCount())
                .build();
    }
}
