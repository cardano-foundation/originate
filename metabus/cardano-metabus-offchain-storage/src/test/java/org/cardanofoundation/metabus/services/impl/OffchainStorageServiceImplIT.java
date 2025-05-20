package org.cardanofoundation.metabus.services.impl;

import lombok.SneakyThrows;
import org.cardanofoundation.metabus.constants.TestConstants;
import org.cardanofoundation.metabus.testhelper.MinioHelper;
import org.cardanofoundation.metabus.util.MinioUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles("ut")
class OffchainStorageServiceImplIT {

    @Value("${minio.accessKey}")
    private String accessKey;

    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    private OffchainStorageServiceImpl offchainStorageService;

    @AfterEach
    @SneakyThrows
    void tearDown() {
        minioUtil.removeAllBuckets();
    }

    @SneakyThrows
    @Test
    void getObjectUrl_givenNormalJson_success() {

        // Setup existed storage data and other pre-conditions
        MinioHelper.putNormalJsonOnStorage(minioUtil);

        // Execute test method
        String url = offchainStorageService.getObjectUrl(TestConstants.BUCKET_NAME, TestConstants.CID_NORMAL);

        // Verify the result
        assertTrue(isValidMinioObjectUrl(
                TestConstants.BUCKET_NAME,
                TestConstants.CID_NORMAL,
                url,
                accessKey));
    }

    @SneakyThrows
    @Test
    void getObjectUrl_givenCrazyJson_success() {

        // Setup existed storage data and other pre-conditions
        MinioHelper.putCrazyJsonOnStorage(minioUtil);

        // Execute test method
        String url = offchainStorageService.getObjectUrl(TestConstants.BUCKET_NAME, TestConstants.CID_CRAZY);

        // Verify the result
        assertTrue(isValidMinioObjectUrl(
                TestConstants.BUCKET_NAME,
                TestConstants.CID_CRAZY,
                url,
                accessKey));
    }

    @SneakyThrows
    @Test
    void storeObject_givenNormalJson_success() {

        // Setup existed storage data and other pre-conditions
        MinioHelper.putNormalJsonOnStorage(minioUtil);

        // Execute test method
        String cid = offchainStorageService.storeObject(TestConstants.BUCKET_NAME, TestConstants.JSON_NORMAL);

        // Verify the result
        assertEquals(TestConstants.CID_NORMAL, cid);
        assertTrue(minioUtil.bucketExists(TestConstants.BUCKET_NAME));
        assertTrue(minioUtil.objectExists(TestConstants.BUCKET_NAME, TestConstants.CID_NORMAL));
    }

    /**
     * The only difference between this method and storeObject_givenNormalJson_success() is,
     * format of the input JSON is re-arranged. That mean the input JSONs are still canonically equal.
     * <p>
     * Then we verify that the result output is still the same as storeObject_givenNormalJson_success().
     */
    @SneakyThrows
    @Test
    void storeObject_givenNormalJson_rearrangeOrder_success() {

        // Setup existed storage data and other pre-conditions
        MinioHelper.putNormalJsonOnStorage(minioUtil);

        // Execute test method
        String cid = offchainStorageService.storeObject(
                TestConstants.BUCKET_NAME,
                TestConstants.JSON_NORMAL_REARRANGED);

        // Verify the result
        assertEquals(TestConstants.CID_NORMAL, cid);
        assertTrue(minioUtil.bucketExists(TestConstants.BUCKET_NAME));
        assertTrue(minioUtil.objectExists(TestConstants.BUCKET_NAME, TestConstants.CID_NORMAL));
    }

    @SneakyThrows
    @Test
    void storeObject_givenCrazyJson_success() {

        // Setup existed storage data and other pre-conditions
        MinioHelper.putCrazyJsonOnStorage(minioUtil);

        // Execute test method
        String cid = offchainStorageService.storeObject(TestConstants.BUCKET_NAME, TestConstants.JSON_CRAZY);

        // Verify the result
        assertEquals(TestConstants.CID_CRAZY, cid);
        assertTrue(minioUtil.bucketExists(TestConstants.BUCKET_NAME));
        assertTrue(minioUtil.objectExists(TestConstants.BUCKET_NAME, TestConstants.CID_CRAZY));
    }

    private static boolean isValidMinioObjectUrl(String bucketName, String cid, String url, String accessKey) {

        // substring1 sample: "/test1/zCT5htkeCML2oQsCZsJzN9kzEjytQdnCpTMS9JqkjqPiA8Wducq9"
        String substring1 = "/" + bucketName + "/" + cid;

        // substring2 sample: "X-Amz-Credential=cardano-admin"
        String substring2 = "X-Amz-Credential=" + accessKey;

        return url.contains(substring1) && url.contains(substring2);
    }
}