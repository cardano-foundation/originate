package org.cardanofoundation.metabus.unittest.service;

import co.nstant.in.cbor.model.UnicodeString;
import co.nstant.in.cbor.model.UnsignedInteger;
import com.bloxbean.cardano.client.common.cbor.CborSerializationUtil;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.cardanofoundation.metabus.common.enums.GroupType;
import org.cardanofoundation.metabus.common.offchain.BusinessData;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.common.offchain.JobBatch;
import org.cardanofoundation.metabus.common.onchain.MultiGroupTxMetadata;
import org.cardanofoundation.metabus.configuration.TxSubmitterProperties;
import org.cardanofoundation.metabus.constants.TestConstants;
import org.cardanofoundation.metabus.service.MetadataService;
import org.cardanofoundation.metabus.service.impl.MultiGroupMetadataService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultiGroupMetadataServiceTest extends MetadataServiceTestHelper {

    static MultiGroupMetadataService multiGroupMetadataService;
    private static final byte[] jwsHeader = new byte[]{102, 1, -112, 56, 21, -26, 82, 13, 52, -16, -4, 66, -84, 2, -4, 102, 75, 62, 74, 116, -67, 110, 77, 93, 59, -71, -118, -85, -109, 44, 55, 90, -28, 25, 125, -21, -22, 54, -27, -55, 95, 6, 107, -91, -45, -28, 43, 76, -111, 24, -32, -70, -71, -11, 52, -98, 56, -87, 33, 21, -15, -36, 108, 10};

    public static byte[] signature1 = new byte[]{102, 1, -112, 56, 21, -26, 82, 13, 52, -16, -4, 66, -84, 2, -4, 102, 75, 62, 74, 116, -67, 110, 77, 93, 59, -71, -118, -85, -109, 44, 55, 90, -28, 25, 125, -21, -22, 54, -27, -55, 95, 6, 107, -91, -45, -28, 43, 76, -111, 24, -32, -70, -71, -11, 52, -98, 56, -87, 33, 21, -15, -36, 108, 10};
    public static byte[] signature2 = new byte[]{100, 1, -112, 56, 21, -26, 82, 13, 52, -16, -4, 66, -84, 2, -4, 102, 75, 62, 74, 116, -67, 110, 77, 93, 59, -71, -118, -85, -109, 44, 55, 90, -28, 25, 125, -21, -22, 54, -27, -55, 95, 6, 107, -91, -45, -28, 43, 76, -111, 24, -32, -70, -71, -11, 52, -98, 56, -87, 33, 21, -15, -36, 108, 10};
    public static byte[] signature5 = new byte[]{105, 1, -112, 56, 21, -26, 82, 13, 52, -16, -4, 66, -84, 2, -4, 102, 75, 62, 74, 116, -67, 110, 77, 93, 59, -71, -118, -85, -109, 44, 55, 90, -28, 25, 125, -21, -22, 54, -27, -55, 95, 6, 107, -91, -45, -28, 43, 76, -111, 24, -32, -70, -71, -11, 52, -98, 56, -87, 33, 21, -15, -36, 108, 10};
    public static byte[] signature6 = new byte[]{106, 1, -112, 56, 21, -26, 82, 13, 52, -16, -4, 66, -84, 2, -4, 102, 75, 62, 74, 116, -67, 110, 77, 93, 59, -71, -118, -85, -109, 44, 55, 90, -28, 25, 125, -21, -22, 54, -27, -55, 95, 6, 107, -91, -45, -28, 43, 76, -111, 24, -32, -70, -71, -11, 52, -98, 56, -87, 33, 21, -15, -36, 108, 10};
    private static final byte[] pubKey1 = new byte[]{101, 56, 72};
    private static final byte[] pubKey2 = new byte[]{-81, 99, 2};

    @BeforeAll
    static void beforeClass() {
        multiGroupMetadataService = new MultiGroupMetadataService();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        TxSubmitterProperties txSubmitterProperties = new TxSubmitterProperties();
        txSubmitterProperties.setMetadataVersion("1");
        setPrivateField(multiGroupMetadataService, "objectMapper", objectMapper);
        setPrivateField(multiGroupMetadataService, "txSubmitterProperties", txSubmitterProperties);
    }

    public static void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Field privateField = target.getClass().getDeclaredField(fieldName);
            privateField.setAccessible(true);
            privateField.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void buildMetadata_with_TxMetaData(@Mock MultiGroupTxMetadata txMetadata) {

        //Given
        Map<String, MultiGroupTxMetadata.VerificationInfo> map = new HashMap<>();
        map.put("key", MultiGroupTxMetadata.VerificationInfo.builder()
                .pubKey(pubKey1)
                .jwsHeader(jwsHeader)
                .signatures(List.of(signature1))
                .build());
        //When
        when(txMetadata.getVerification()).thenReturn(map);
        when(txMetadata.getType()).thenReturn("scm:georgianWine");

        CBORMetadata cborMetadata = multiGroupMetadataService.buildMetadata(txMetadata, new BigInteger("1904"));

        //Then
        assertEquals(1, cborMetadata.getData().getValues().size());
        co.nstant.in.cbor.model.Map metadata = (co.nstant.in.cbor.model.Map) cborMetadata.getData().get(new UnsignedInteger(1904));
        String jobType = CborSerializationUtil.toUnicodeString(
                metadata.get(new UnicodeString(MetadataService.TYPE)));
        assertEquals("scm", jobType);
    }

    @Test
    void buildMetadata_with_Job_Batch(@Mock JobBatch jobBatch, @Mock TxSubmitterProperties txSubmitterProperties) {
        //Given
        Job job1 = Job.builder().businessData(BusinessData.builder().pubKey(pubKey1).signature(signature1).build()).group("group").build();
        Job job2 = Job.builder().businessData(BusinessData.builder().pubKey(pubKey2).signature(signature2).build()).group("group").build();


        //When
        when(jobBatch.getJobs()).thenReturn(List.of(job1, job2));
        when(jobBatch.getCid()).thenReturn(TestConstants.CID_NORMAL);

        MultiGroupTxMetadata actual = (MultiGroupTxMetadata) multiGroupMetadataService.buildMetadata(jobBatch);

        //Then
        var verification = actual.getVerification().get("group");
        assertEquals("zCT5htkeCML2oQsCZsJzN9kzEjytQdnCpTMS9JqkjqPiA8Wducq9", actual.getCid());
        assertEquals(pubKey1, verification.getPubKey());
        assertArrayEquals(signature1, verification.getSignatures().get(0));
        assertArrayEquals(signature2, verification.getSignatures().get(1));
        assertEquals("1", actual.getVersion());
        assertNull(actual.getType());
    }

    @Test
    void buildOffchainJson(@Mock JobBatch jobBatch) throws JsonProcessingException {
        //Given
        Job job1 = Job.builder()
                .businessData(BusinessData.builder()
                        .data(prepareMultiGroupJobData1())
                        .pubKey(pubKey1)
                        .signature(signature1)
                        .build())
                .groupType(GroupType.MULTI_GROUP)
                .group("1234")
                .jobIndex("1234#0")
                .build();
        Job job2 = Job.builder()
                .businessData(BusinessData.builder()
                        .data(prepareMultiGroupJobData2())
                        .pubKey(pubKey2)
                        .signature(signature2)
                        .build())
                .groupType(GroupType.MULTI_GROUP)
                .group("1234")
                .jobIndex("1234#1")
                .build();

        Job job5 = Job.builder()
                .businessData(BusinessData.builder()
                        .data(prepareMultiGroupJobData5())
                        .pubKey(new byte[]{-81, 99, 22})
                        .signature(signature5)
                        .build())
                .groupType(GroupType.MULTI_GROUP)
                .group("5678")
                .jobIndex("5678#0")
                .build();
        Job job6 = Job.builder()
                .businessData(BusinessData.builder()
                        .data(prepareMultiGroupJobData6())
                        .pubKey(new byte[]{-81, 99, 21})
                        .signature(signature6)
                        .build())
                .groupType(GroupType.MULTI_GROUP)
                .group("5678")
                .jobIndex("5678#1")
                .build();

        //When
        when(jobBatch.getJobs()).thenReturn(List.of(job1, job2, job5, job6));

        String actual = multiGroupMetadataService.buildOffchainJson(jobBatch);

        //Then
        assertEquals(expectMultiGroupOffchainJson(), actual);
    }
}