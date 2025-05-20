package org.cardanofoundation.metabus.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.cardanofoundation.metabus.common.entities.JobJPA;
import org.cardanofoundation.metabus.common.entities.UnconfirmedTxJPA;
import org.cardanofoundation.metabus.common.enums.JobState;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.controllers.dtos.BusinessData;
import org.cardanofoundation.metabus.controllers.dtos.BusinessDataResp;
import org.cardanofoundation.metabus.controllers.dtos.JobReq;
import org.cardanofoundation.metabus.controllers.dtos.JobResp;
import org.cardanofoundation.metabus.exceptions.MetabusErrors;
import org.cardanofoundation.metabus.exceptions.MetabusException;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JobMapperImpl implements JobMapper{
    private final ObjectMapper objectMapper;

    /**
     * <p>
     *  Get the JwsHeader part from signature in format <jwsHeader>.<signature>
     * </p>
     * @param signature in format of <jwsHeader>.<signature>
     * @return jwsHeader
     */
    private byte[] getJwsHeader(String signature) throws JsonProcessingException {
        if(!signature.matches("^[^.]+\\.[^.]+$")){
            throw new MetabusException(MetabusErrors.INVALID_SIGNATURE);
        }
        String[] jwsHeaderAndSignature = signature.split("\\.");
        return Base64.getUrlDecoder().decode(jwsHeaderAndSignature[0]);
    }

    /**
     * <p>
     *  Get the signature part as byte from signature in format <jwsHeader>.<signature>
     * </p>
     * @param signature in format of <jwsHeader>.<signature>
     * @return signature
     */
    private byte[] getSignature(String signature){
        if(!signature.matches("^[^.]+\\.[^.]+$")){
            throw new MetabusException(MetabusErrors.INVALID_SIGNATURE);
        }
        String[] jwsHeaderAndSignature = signature.split("\\.");
        String jwsSignature = jwsHeaderAndSignature[1];
        return Base64.getUrlDecoder().decode(jwsSignature);
    }
    @Override
    public Job toJob(JobReq jobReq) throws JsonProcessingException {
        BusinessData businessData = jobReq.getBusinessData();
        String jwsHeaderAndSignature = businessData.getSignature();
        byte[] jwsHeader = getJwsHeader(jwsHeaderAndSignature);
        byte[] signature = getSignature(jwsHeaderAndSignature);
        return Job.builder()
                .businessData(org.cardanofoundation.metabus.common.offchain.BusinessData.builder()
                        .type(businessData.getType())
                        .data(businessData.getData())
                        .pubKey(Base64.getUrlDecoder().decode(businessData.getPubKey()))
                        .jwsHeader(jwsHeader)
                        .signature(signature)
                        .build())
                .groupType(jobReq.getGroupType())
                .group(jobReq.getGroup())
                .build();
    }

    @Override
    public Job toJob(JobJPA jobJPA){
        UnconfirmedTxJPA unconfirmedTxJPA = jobJPA.getUnconfirmedTx();
        String txHash = Objects.nonNull(unconfirmedTxJPA) ? unconfirmedTxJPA.getTxHash() : null;

        return Job.builder()
                .id(jobJPA.getId())
                .state(jobJPA.getState())
                .businessData(org.cardanofoundation.metabus.common.offchain.BusinessData.builder()
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

    @Override
    public JobJPA toJobJpa(Job job) {
        org.cardanofoundation.metabus.common.offchain.BusinessData businessData = job.getBusinessData();
        return JobJPA.builder()
                .state(JobState.PENDING)
                .type(businessData.getType())
                .subType(businessData.getSubType())
                .data(businessData.getData())
                .pubKey(businessData.getPubKey())
                .jwsHeader(businessData.getJwsHeader())
                .signature(businessData.getSignature())
                .group(job.getGroup())
                .groupType(job.getGroupType())
                .retryCount(job.getRetryCount())
                .isDeleted(false)
                .build();
    }

    @Override
    public JobResp toJobResp(Job job) {
        org.cardanofoundation.metabus.common.offchain.BusinessData businessData = job.getBusinessData();
        return JobResp.builder()
                .id(job.getId())
                .state(job.getState())
                .businessData(BusinessDataResp.builder()
                        .type(businessData.getType())
                        .subType(businessData.getSubType())
                        .data(businessData.getData())
                        .jwsHeader(Base64.getUrlEncoder().encodeToString(businessData.getJwsHeader()))
                        .signature(Base64.getUrlEncoder().encodeToString(businessData.getSignature()))
                        .pubKey(Base64.getUrlEncoder().encodeToString(businessData.getPubKey()))
                        .build())
                .txHash(job.getTxHash())
                .groupType(job.getGroupType())
                .group(job.getGroup())
                .build();
    }
}
