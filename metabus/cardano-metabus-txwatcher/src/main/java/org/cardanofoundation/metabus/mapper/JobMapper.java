package org.cardanofoundation.metabus.mapper;

import org.cardanofoundation.metabus.common.entities.JobJPA;
import org.cardanofoundation.metabus.common.offchain.BusinessData;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = BaseMapper.class)
public interface JobMapper {

    @Mapping(target = "businessData", source = "jobJPA", qualifiedByName = "getBusinessData")
    @Mapping(target = "txHash", source = "unconfirmedTx.txHash")
    Job toJob(JobJPA jobJPA);

    @Named("getBusinessData")
    default BusinessData getBusinessData(JobJPA jobJPA)  {
        return org.cardanofoundation.metabus.common.offchain.BusinessData.builder()
                .type(jobJPA.getType())
                .subType(jobJPA.getSubType())
                .data(jobJPA.getData())
                .signature(jobJPA.getSignature())
                .pubKey(jobJPA.getPubKey())
                .build();
    }


}
