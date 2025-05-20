package org.cardanofoundation.metabus.service.impl;

import co.nstant.in.cbor.model.*;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadata;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadataMap;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import jakarta.annotation.PostConstruct;

import org.cardanofoundation.metabus.common.enums.GroupType;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.common.offchain.JobBatch;
import org.cardanofoundation.metabus.common.onchain.SingleGroupTxMetadata;
import org.cardanofoundation.metabus.common.onchain.TxMetadata;
import org.cardanofoundation.metabus.configuration.TxSubmitterProperties;
import org.cardanofoundation.metabus.service.MetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@EnableConfigurationProperties(TxSubmitterProperties.class)
public class SingleGroupMetadataService extends MetadataService<SingleGroupTxMetadata> {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TxSubmitterProperties txSubmitterProperties;

    @PostConstruct
    public void init() {
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }

    @Override
    public CBORMetadata buildMetadata(SingleGroupTxMetadata txMetadata, BigInteger metadatumLabel) {
        CBORMetadata cborMetadata = new CBORMetadata();
        co.nstant.in.cbor.model.Map cborMap = getBaseCborMetadata(txMetadata, GroupType.SINGLE_GROUP);
        cborMap.put(new UnicodeString(PUB_KEY), new ByteString(txMetadata.getPubKey()));

        byte[] jwsHeader = txMetadata.getJwsHeader();
        var jwsHeaderDataItem = splitByteArray(jwsHeader);
        cborMap.put(new UnicodeString(JWS_HEADER), jwsHeaderDataItem);

        Array signatureCborArray = new Array();
        txMetadata.getSignatures().forEach(
                signature -> {
                    // If the split list contains just 1 item, the signature on chain will be just a string instead
                    // of an array of chunks.
                    var signatureDataItem = splitByteArray(signature);
                    signatureCborArray.add(signatureDataItem);
                }
        );
        cborMap.put(new UnicodeString(SIGNATURES), signatureCborArray);
        CBORMetadataMap cborMetadataMap = new CBORMetadataMap(cborMap);
        cborMetadata.put(metadatumLabel, cborMetadataMap);
        return cborMetadata;
    }

    @Override
    public TxMetadata buildMetadata(JobBatch jobBatch) {
        List<Job> jobs = jobBatch.getJobs();

        // Build job-index
        int jobCount = jobs.size();
        IntStream.range(0, jobCount)
                .forEach(index -> jobs.get(index).setJobIndex(String.valueOf(index)));

        // Build verification info
        List<byte[]> signatures = jobs.stream()
                .map(job -> job.getBusinessData().getSignature())
                .collect(Collectors.toList());
        return SingleGroupTxMetadata
                .builder()
                .version(txSubmitterProperties.getMetadataVersion())
                .cid(jobBatch.getCid())
                .type(jobBatch.getJobType())
                .subType(jobBatch.getJobSubType())
                .pubKey(jobs.get(0).getBusinessData().getPubKey())
                .jwsHeader(jobs.get(0).getBusinessData().getJwsHeader())
                .signatures(signatures)
                .build();
    }

    @Override
    public String buildOffchainJson(JobBatch jobBatch) throws JsonProcessingException {
        List<Job> jobs = jobBatch.getJobs();
        List<Object> offChainDatas = jobs.stream()
                .map(job -> job.getBusinessData().getData())
                .collect(Collectors.toList());

        return objectMapper.writeValueAsString(offChainDatas);
    }
}
