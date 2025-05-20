package org.cardanofoundation.metabus.service.impl;

import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.UnicodeString;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadata;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadataMap;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import jakarta.annotation.PostConstruct;

import org.cardanofoundation.metabus.common.enums.GroupType;
import org.cardanofoundation.metabus.common.offchain.BusinessData;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.common.offchain.JobBatch;
import org.cardanofoundation.metabus.common.onchain.MultiGroupTxMetadata;
import org.cardanofoundation.metabus.common.onchain.TxMetadata;
import org.cardanofoundation.metabus.configuration.TxSubmitterProperties;
import org.cardanofoundation.metabus.service.MetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigInteger;
import java.util.*;

@Service
@EnableConfigurationProperties(TxSubmitterProperties.class)
public class MultiGroupMetadataService extends MetadataService<MultiGroupTxMetadata> {
    public static String VERIFICATION = "d";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TxSubmitterProperties txSubmitterProperties;

    @PostConstruct
    public void init() {
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }

    @Override
    public CBORMetadata buildMetadata(MultiGroupTxMetadata txMetadata, BigInteger metadatumLabel) {
        CBORMetadata cborMetadata = new CBORMetadata();

        co.nstant.in.cbor.model.Map cborMap = getBaseCborMetadata(txMetadata, GroupType.MULTI_GROUP);
        co.nstant.in.cbor.model.Map verificationCborMap = new co.nstant.in.cbor.model.Map();
        Map<String, MultiGroupTxMetadata.VerificationInfo> verification =
                txMetadata.getVerification();

        if (Objects.nonNull(verification)) {
            verification.forEach((jobGroup, verificationInfo) -> {
                co.nstant.in.cbor.model.Map verificationInfoCborMap = new co.nstant.in.cbor.model.Map();
                verificationInfoCborMap.put(new UnicodeString(PUB_KEY),
                        new ByteString(verificationInfo.getPubKey()));

                byte[] jwsHeader = verificationInfo.getJwsHeader();
                var jwsHeaderDataItem = splitByteArray(jwsHeader);
                verificationInfoCborMap.put(new UnicodeString(JWS_HEADER), jwsHeaderDataItem);

                Array signatureCborArray = new Array();
                List<byte[]> signatures = verificationInfo.getSignatures();
                if (!CollectionUtils.isEmpty(signatures)) {
                    signatures.forEach(signature -> {
                        // If the split list contains just 1 item, the signature on chain will be just a string instead
                        // of an array of chunks.
                        var signatureDataItem = splitByteArray(signature);
                        signatureCborArray.add(signatureDataItem);
                    });
                }

                verificationInfoCborMap.put(new UnicodeString(SIGNATURES), signatureCborArray);

                verificationCborMap.put(new UnicodeString(jobGroup), verificationInfoCborMap);
            });
        }

        cborMap.put(new UnicodeString(VERIFICATION), verificationCborMap);

        CBORMetadataMap cborMetadataMap = new CBORMetadataMap(cborMap);
        cborMetadata.put(metadatumLabel, cborMetadataMap);
        return cborMetadata;
    }

    @Override
    public TxMetadata buildMetadata(JobBatch jobBatch) {
        List<Job> jobs = jobBatch.getJobs();
        Map<String, MultiGroupTxMetadata.VerificationInfo> verificationInfoMap = new HashMap<>();
        Map<String, Integer> jobIndexMap = new HashMap<>(); // <key: jobGroup, value: highest (numeric) index in group>

        jobs.forEach(job -> {
            BusinessData businessData = job.getBusinessData();
            byte[] pubKey = businessData.getPubKey();
            byte[] signature = businessData.getSignature();
            String jobGroup = job.getGroup();

            // Build verification info
            if (!verificationInfoMap.containsKey(jobGroup)) {
                List<byte[]> signatures = new ArrayList<>();
                signatures.add(signature);
                MultiGroupTxMetadata.VerificationInfo verificationInfo =
                        MultiGroupTxMetadata.VerificationInfo.builder()
                                .pubKey(pubKey)
                                .jwsHeader(job.getBusinessData().getJwsHeader())
                                .signatures(signatures)
                                .build();
                verificationInfoMap.putIfAbsent(jobGroup, verificationInfo);
            } else {
                MultiGroupTxMetadata.VerificationInfo verificationInfo = verificationInfoMap.get(jobGroup);
                List<byte[]> signatures = verificationInfo.getSignatures();
                if (Objects.nonNull(signatures)) {
                    signatures.add(signature);
                }
            }

            // Build job-index
            Integer highestNumericIndexInGroup = !jobIndexMap.containsKey(jobGroup) ? 0 : (jobIndexMap.get(jobGroup) + 1);
            jobIndexMap.put(jobGroup, highestNumericIndexInGroup);
            String jobIndex = jobGroup + "#" + highestNumericIndexInGroup;
            job.setJobIndex(jobIndex);
        });
        return MultiGroupTxMetadata
                .builder()
                .version(txSubmitterProperties.getMetadataVersion())
                .cid(jobBatch.getCid())
                .type(jobBatch.getJobType())
                .subType(jobBatch.getJobSubType())
                .verification(
                        verificationInfoMap
                )
                .build();
    }

    @Override
    public String buildOffchainJson(JobBatch jobBatch) throws JsonProcessingException {
        List<Job> jobs = jobBatch.getJobs();
        Map<String, List<Object>> offchainDataMap = new HashMap<>();

        jobs.forEach(job -> {
            String group = job.getGroup();
            List<Object> offchainDatas = null;

            if (!offchainDataMap.containsKey(group)) {
                offchainDatas = new ArrayList<>();
                offchainDataMap.put(group, offchainDatas);
            } else {
                offchainDatas = offchainDataMap.get(group);
            }

            offchainDatas.add(job.getBusinessData().getData());
        });

        return objectMapper.writeValueAsString(offchainDataMap);
    }
}
