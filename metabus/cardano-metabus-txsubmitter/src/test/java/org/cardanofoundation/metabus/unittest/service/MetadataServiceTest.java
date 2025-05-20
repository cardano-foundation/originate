package org.cardanofoundation.metabus.unittest.service;

import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadata;
import org.cardanofoundation.metabus.common.offchain.JobBatch;
import org.cardanofoundation.metabus.common.onchain.TxMetadata;
import org.cardanofoundation.metabus.service.MetadataService;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.List;

public class MetadataServiceTest extends MetadataService{
    public static byte[] signature68Bytes = new byte[]{102, 1, -112, 56, 21, -26, 82, 13, 52, -16, -4, 66, -84, 2, -4, 102, 75, 62, 74, 116, -67, 110, 77, 93, 59, -71, -118, -85, -109, 44, 55, 90, -28, 25, 125, -21, -22, 54, -27, -55, 95, 6, 107, -91, -45, -28, 43, 76, -111, 24, -32, -70, -71, -11, 52, -98, 56, -87, 33, 21, -15, -36, 108, 10, 10, 10, 102, 10};
    public static byte[] signature64Bytes = new byte[]{102, 1, -112, 56, 21, -26, 82, 13, 52, -16, -4, 66, -84, 2, -4, 102, 75, 62, 74, 116, -67, 110, 77, 93, 59, -71, -118, -85, -109, 44, 55, 90, -28, 25, 125, -21, -22, 54, -27, -55, 95, 6, 107, -91, -45, -28, 43, 76, -111, 24, -32, -70, -71, -11, 52, -98, 56, -87, 33, 21, -15, -36, 108, 10};
    public static byte[] signature128Bytes = new byte[]{102, 1, -112, 56, 21, -26, 82, 13, 52, -16, -4, 66, -84, 2, -4, 102, 75, 62, 74, 116, -67, 110, 77, 93, 59, -71, -118, -85, -109, 44, 55, 90, -28, 25, 125, -21, -22, 54, -27, -55, 95, 6, 107, -91, -45, -28, 43, 76, -111, 24, -32, -70, -71, -11, 52, -98, 56, -87, 33, 21, -15, -36, 108, 10, 102, 1, -112, 56, 21, -26, 82, 13, 52, -16, -4, 66, -84, 2, -4, 102, 75, 62, 74, 116, -67, 110, 77, 93, 59, -71, -118, -85, -109, 44, 55, 90, -28, 25, 125, -21, -22, 54, -27, -55, 95, 6, 107, -91, -45, -28, 43, 76, -111, 24, -32, -70, -71, -11, 52, -98, 56, -87, 33, 21, -15, -36, 108, 10};
    public static byte[] signature13Bytes = new byte[]{102, 1, -112, 56, 21, -26, 82, 13, 52, -16, -4, 66, -84};
    @Test
    public void testSplitSignatureBiggerThan64Bytes(){
        // Call the protected splitSignature method through the subclass
        List<DataItem> dataItems = ((Array) splitByteArray(signature68Bytes)).getDataItems();

        // Assert the expected result
        Assert.assertEquals(2, dataItems.size()); // The signature is split into two chunks

        // Assert the content of the chunks
        ByteString chunk1 = (ByteString) dataItems.get(0);
        byte[] chunk1ByteArray = chunk1.getBytes();
        Assert.assertArrayEquals(signature64Bytes, chunk1ByteArray);
        Assert.assertEquals(64, chunk1ByteArray.length);

        ByteString chunk2 = (ByteString) dataItems.get(1);
        byte[] chunk2ByteArray = chunk2.getBytes();
        Assert.assertArrayEquals(new byte[]{10, 10, 102, 10}, chunk2ByteArray);
    }

    @Test
    public void testSplitSignatureWithDataLessThan64Bytes(){
        // Call the protected splitSignature method through the subclass
        ByteString signature = (ByteString) splitByteArray(signature13Bytes);

        // Assert the content of the chunks
        byte[] chunkByteArray = signature.getBytes();
        Assert.assertArrayEquals(signature13Bytes, chunkByteArray);
    }


    @Test
    public void testSplitSignatureWithDataMultiples64Bytes(){
        // Call the protected splitSignature method through the subclass
        List<DataItem> dataItems = ((Array) splitByteArray(signature68Bytes)).getDataItems();

        // Assert the expected result
        Assert.assertEquals(2, dataItems.size()); // The signature is split into two chunks

        // Assert the content of the chunks
        ByteString chunk1 = (ByteString) dataItems.get(0);
        byte[] chunk1ByteArray = chunk1.getBytes();
        Assert.assertArrayEquals(signature64Bytes, chunk1ByteArray);
        Assert.assertEquals(64, chunk1ByteArray.length);

        ByteString chunk2 = (ByteString) dataItems.get(0);
        byte[] chunk2ByteArray = chunk2.getBytes();
        Assert.assertArrayEquals(signature64Bytes, chunk2ByteArray);
        Assert.assertEquals(64, chunk2ByteArray.length);

    }



    @Override
    public CBORMetadata buildMetadata(TxMetadata txMetadata, BigInteger metadatumLabel) {
        return null;
    }

    @Override
    public TxMetadata buildMetadata(JobBatch jobBatch) {
        return null;
    }

    @Override
    public String buildOffchainJson(JobBatch jobBatch) {
        return null;
    }
}
