package org.cardanofoundation.metabus.factory;

import com.bloxbean.cardano.client.metadata.cbor.CBORMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.cardanofoundation.metabus.common.enums.GroupType;
import org.cardanofoundation.metabus.common.offchain.JobBatch;
import org.cardanofoundation.metabus.common.onchain.TxMetadata;
import org.cardanofoundation.metabus.service.MetadataService;
import org.cardanofoundation.metabus.service.impl.MultiGroupMetadataService;
import org.cardanofoundation.metabus.service.impl.SingleGroupMetadataService;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class MetadataServiceFactory extends AbstractServiceFactory<MetadataService<? extends TxMetadata>, MetadataService> {
    public MetadataServiceFactory(
            List<MetadataService<? extends TxMetadata>> metadataServices) {
        super(metadataServices);
    }

    @Override
    void init() {
        serviceMap = services.stream()
                .collect(
                        Collectors.toMap(
                                MetadataService::supports,
                                Function.identity()));
    }

    public CBORMetadata buildCborTxMetadata(TxMetadata txMetadata, BigInteger metadatumLabel){
        return serviceMap.get(txMetadata.getClass()).buildMetadata(txMetadata, metadatumLabel);
    }

    public TxMetadata buildTxMetadata(JobBatch jobBatch) {
        final GroupType groupType = jobBatch.getGroupType();

        switch (groupType) {
            case MULTI_GROUP -> {
                return serviceMap.get(new MultiGroupMetadataService().supports()).buildMetadata(jobBatch);
            }
            case SINGLE_GROUP -> {
                return serviceMap.get(new SingleGroupMetadataService().supports()).buildMetadata(jobBatch);
            }
            default -> throw new IllegalStateException("Unexpected value: " + groupType);
        }
    }

    public String buildOffchainJson(JobBatch jobBatch) throws JsonProcessingException {
        GroupType groupType = jobBatch.getGroupType();
        switch (groupType) {
            case MULTI_GROUP -> {
                return serviceMap.get(new MultiGroupMetadataService().supports()).buildOffchainJson(jobBatch);
            }
            case SINGLE_GROUP -> {
                return serviceMap.get(new SingleGroupMetadataService().supports()).buildOffchainJson(jobBatch);
            }
            default -> throw new IllegalStateException("Unexpected value: " + groupType);
        }
    }
}
