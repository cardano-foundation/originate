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
import org.cardanofoundation.metabus.common.onchain.SingleGroupTxMetadata;
import org.cardanofoundation.metabus.configuration.TxSubmitterProperties;
import org.cardanofoundation.metabus.constants.TestConstants;
import org.cardanofoundation.metabus.service.MetadataService;
import org.cardanofoundation.metabus.service.impl.SingleGroupMetadataService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.List;

import static org.cardanofoundation.metabus.unittest.service.MultiGroupMetadataServiceTest.setPrivateField;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SingleGroupMetadataServiceTest extends MetadataServiceTestHelper {

    static SingleGroupMetadataService singleGroupMetadataService;
    public static byte[] signature1 = new byte[]{102, 1, -112, 56, 21, -26, 82, 13, 52, -16, -4, 66, -84, 2, -4, 102, 75, 62, 74, 116, -67, 110, 77, 93, 59, -71, -118, -85, -109, 44, 55, 90, -28, 25, 125, -21, -22, 54, -27, -55, 95, 6, 107, -91, -45, -28, 43, 76, -111, 24, -32, -70, -71, -11, 52, -98, 56, -87, 33, 21, -15, -36, 108, 10};
    public static byte[] signature2 = new byte[]{100, 1, -112, 56, 21, -26, 82, 13, 52, -16, -4, 66, -84, 2, -4, 102, 75, 62, 74, 116, -67, 110, 77, 93, 59, -71, -118, -85, -109, 44, 55, 90, -28, 25, 125, -21, -22, 54, -27, -55, 95, 6, 107, -91, -45, -28, 43, 76, -111, 24, -32, -70, -71, -11, 52, -98, 56, -87, 33, 21, -15, -36, 108, 10};
    private static final byte[] jwsHeader = new byte[]{102, 1, -112, 56, 21, -26, 82, 13, 52, -16, -4, 66, -84, 2, -4, 102, 75, 62, 74, 116, -67, 110, 77, 93, 59, -71, -118, -85, -109, 44, 55, 90, -28, 25, 125, -21, -22, 54, -27, -55, 95, 6, 107, -91, -45, -28, 43, 76, -111, 24, -32, -70, -71, -11, 52, -98, 56, -87, 33, 21, -15, -36, 108, 10};
    private static final byte[] pubKey1 = new byte[]{101, 56, 72};
    private static final byte[] pubKey2 = new byte[]{-81, 99, 2};

    @BeforeAll
    static void beforeClass() {
        singleGroupMetadataService = new SingleGroupMetadataService();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        TxSubmitterProperties txSubmitterProperties = new TxSubmitterProperties();
        txSubmitterProperties.setMetadataVersion("1");
        setPrivateField(singleGroupMetadataService, "objectMapper", objectMapper);
        setPrivateField(singleGroupMetadataService, "txSubmitterProperties", txSubmitterProperties);
    }

    @Test
    void buildMetadata_with_TxMetaData(@Mock SingleGroupTxMetadata txMetadata) {

        //When
        when(txMetadata.getSignatures()).thenReturn(List.of(signature1));
        when(txMetadata.getType()).thenReturn("scm:georgianWine");
        when(txMetadata.getJwsHeader()).thenReturn(jwsHeader);

        CBORMetadata cborMetadata = singleGroupMetadataService.buildMetadata(txMetadata, new BigInteger("1904"));

        //Then
        assertEquals(1, cborMetadata.getData().getValues().size());
        co.nstant.in.cbor.model.Map metadata = (co.nstant.in.cbor.model.Map) cborMetadata.getData().get(new UnsignedInteger(1904));
        String jobType = CborSerializationUtil.toUnicodeString(
                metadata.get(new UnicodeString(MetadataService.TYPE)));
        assertEquals("scm", jobType);
    }

    @Test
    void buildMetadata_with_Job_Batch(@Mock JobBatch jobBatch) {
        //Given
        Job job = Job.builder().businessData(BusinessData.builder().type("type:subType").subType("subType")
                .pubKey(pubKey1).signature(signature1).build()).group("group").build();

        //When
        when(jobBatch.getJobs()).thenReturn(List.of(job));
        when(jobBatch.getCid()).thenReturn(TestConstants.CID_NORMAL);
        when(jobBatch.getJobType()).thenReturn("type:subType");
        when(jobBatch.getJobSubType()).thenReturn("subType");

        SingleGroupTxMetadata actual = (SingleGroupTxMetadata) singleGroupMetadataService.buildMetadata(jobBatch);

        //Then
        assertEquals("zCT5htkeCML2oQsCZsJzN9kzEjytQdnCpTMS9JqkjqPiA8Wducq9", actual.getCid());
        assertEquals(pubKey1, actual.getPubKey());
        assertArrayEquals(signature1, actual.getSignatures().get(0));
        assertEquals("1", actual.getVersion());
        assertEquals("type:subType", actual.getType());
        assertEquals("subType", actual.getSubType());
    }

    @Test
    void buildOffchainJson(@Mock JobBatch jobBatch) throws JsonProcessingException {
        //Given
        Job job1 = Job.builder()
                .businessData(BusinessData.builder()
                        .data(prepareSingleGroupData1())
                        .pubKey(pubKey1)
                        .signature(signature1)
                        .build())
                .groupType(GroupType.SINGLE_GROUP)
                .jobIndex("0")
                .build();
        Job job2 = Job.builder()
                .businessData(BusinessData.builder()
                        .data(prepareSingleGroupData2())
                        .pubKey(pubKey2)
                        .signature(signature2)
                        .build())
                .groupType(GroupType.SINGLE_GROUP)
                .jobIndex("1")
                .build();

        //When
        when(jobBatch.getJobs()).thenReturn(List.of(job1, job2));

        String actual = singleGroupMetadataService.buildOffchainJson(jobBatch);

        //Then
        assertEquals(expectSingleGroupOffchainJson(), actual);
    }
}