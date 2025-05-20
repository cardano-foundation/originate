package org.cardanofoundation.metabus.services.impl;

import lombok.SneakyThrows;
import org.cardanofoundation.metabus.constants.TestConstants;
import org.cardanofoundation.metabus.exceptions.MetabusErrors;
import org.cardanofoundation.metabus.exceptions.MetabusException;
import org.cardanofoundation.metabus.util.JsonUtil;
import org.cardanofoundation.metabus.util.MinioUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OffchainStorageServiceImplTest {

    @Mock
    private MinioUtil minioUtil;

    @Mock
    private JsonUtil jsonUtil;

    @InjectMocks
    private OffchainStorageServiceImpl offchainStorageService;

    @AfterEach
    @SneakyThrows
    void tearDown() {
        minioUtil.removeAllBuckets();
    }

    @SneakyThrows
    @Test
    void getObjectUrl_givenNormalJson_success() {

        // Setup pre-conditions
        when(minioUtil.bucketExists(any())).thenReturn(true);
        when(minioUtil.objectExists(any(), any())).thenReturn(true);
        when(minioUtil.getObjectUrl(TestConstants.BUCKET_NAME, TestConstants.CID_NORMAL))
                .thenReturn(TestConstants.OBJECT_URL_NORMAL);

        // Execute test method
        String url = offchainStorageService.getObjectUrl(TestConstants.BUCKET_NAME, TestConstants.CID_NORMAL);

        // Verify the result
        verify(minioUtil, times(1)).bucketExists(TestConstants.BUCKET_NAME);
        verify(minioUtil, times(1)).objectExists(
                TestConstants.BUCKET_NAME,
                TestConstants.CID_NORMAL);
        verify(minioUtil, times(1)).getObjectUrl(
                TestConstants.BUCKET_NAME,
                TestConstants.CID_NORMAL);
        assertEquals(TestConstants.OBJECT_URL_NORMAL, url);
    }

    @SneakyThrows
    @Test
    void getObjectUrl_givenBucketNotExisted_throwMetabusException() {

        // Setup pre-conditions
        when(minioUtil.bucketExists(any())).thenReturn(false);
        MetabusException expectedException = new MetabusException(
                MetabusErrors.ERROR_MINIO_GET_OBJECT,
                "Bucket does not exist");

        // Execute test method
        MetabusException exception = assertThrows(
                MetabusException.class,
                () -> {
                    offchainStorageService.getObjectUrl(TestConstants.BUCKET_NAME, TestConstants.CID_NORMAL);
                }
        );

        // Verify the result
        verify(minioUtil, times(1)).bucketExists(TestConstants.BUCKET_NAME);
        verify(minioUtil, never()).objectExists(any(), any());
        verify(minioUtil, never()).getObjectUrl(any(), any());
        assertThat(exception).usingRecursiveComparison().isEqualTo(expectedException);
    }

    @SneakyThrows
    @Test
    void getObjectUrl_givenObjectNotExistedInBucket_throwMetabusException() {

        // Setup pre-conditions
        when(minioUtil.bucketExists(any())).thenReturn(true);
        when(minioUtil.objectExists(any(), any())).thenReturn(false);
        MetabusException expectedException = new MetabusException(
                MetabusErrors.ERROR_MINIO_GET_OBJECT,
                "Object does not exist in the bucket");

        // Execute test method
        MetabusException exception = assertThrows(
                MetabusException.class,
                () -> {
                    offchainStorageService.getObjectUrl(TestConstants.BUCKET_NAME, TestConstants.CID_NORMAL);
                }
        );

        // Verify the result
        verify(minioUtil, times(1)).bucketExists(TestConstants.BUCKET_NAME);
        verify(minioUtil, times(1)).objectExists(
                TestConstants.BUCKET_NAME,
                TestConstants.CID_NORMAL);
        verify(minioUtil, never()).getObjectUrl(any(), any());
        assertThat(exception).usingRecursiveComparison().isEqualTo(expectedException);
    }

    @SneakyThrows
    @Test
    void storeObject_givenNormalJson_success() {

        // Setup pre-conditions
        when(jsonUtil.canonicalizeFromText(any())).thenReturn(TestConstants.CANONICALIZED_NORMAL);
        when(minioUtil.makeBucket(any())).thenReturn(true);
        when(minioUtil.storeObject(any(), any(), any())).thenReturn(true);

        // Execute test method
        String cid = offchainStorageService.storeObject(TestConstants.BUCKET_NAME, TestConstants.JSON_NORMAL);

        // Verify the result
        verify(jsonUtil, times(1)).canonicalizeFromText(TestConstants.JSON_NORMAL);
        verify(minioUtil, times(1)).makeBucket(TestConstants.BUCKET_NAME);
        verify(minioUtil, times(1)).storeObject(eq(TestConstants.BUCKET_NAME), eq(TestConstants.CID_NORMAL), any());
        assertEquals(TestConstants.CID_NORMAL, cid);
    }

    @SneakyThrows
    @Test
    void storeObject_givenCanonicalizeError_throwMetabusException() {

        // Setup pre-conditions
        when(jsonUtil.canonicalizeFromText(any()))
                .thenThrow(new IOException("Random error message when canonicalizing JSON"));
        MetabusException expectedException = new MetabusException(
                MetabusErrors.ERROR_CANONICALIZED,
                "Input JSON object is invalid");

        // Execute test method
        MetabusException exception = assertThrows(
                MetabusException.class,
                () -> {
                    offchainStorageService.storeObject(TestConstants.BUCKET_NAME, TestConstants.JSON_NORMAL);
                }
        );

        // Verify the result
        verify(jsonUtil, times(1)).canonicalizeFromText(TestConstants.JSON_NORMAL);
        verify(minioUtil, never()).makeBucket(any());
        verify(minioUtil, never()).storeObject(any(), any(), any());
        assertThat(exception).usingRecursiveComparison().isEqualTo(expectedException);
    }

    @SneakyThrows
    @Test
    void storeObject_givenStoreObjectError_throwMetabusException() {

        // Setup pre-conditions
        when(jsonUtil.canonicalizeFromText(any())).thenReturn(TestConstants.CANONICALIZED_NORMAL);
        when(minioUtil.makeBucket(any())).thenReturn(true);
        when(minioUtil.storeObject(any(), any(), any()))
                .thenThrow(new RuntimeException("Random error message when storing object into storage"));
        MetabusException expectedException = new MetabusException(
                MetabusErrors.ERROR_MINIO_STORING,
                "Random error message when storing object into storage");

        // Execute test method
        MetabusException exception = assertThrows(
                MetabusException.class,
                () -> {
                    offchainStorageService.storeObject(TestConstants.BUCKET_NAME, TestConstants.JSON_NORMAL);
                }
        );

        // Verify the result
        verify(jsonUtil, times(1)).canonicalizeFromText(TestConstants.JSON_NORMAL);
        verify(minioUtil, times(1)).makeBucket(TestConstants.BUCKET_NAME);
        verify(minioUtil, times(1)).storeObject(
                eq(TestConstants.BUCKET_NAME),
                eq(TestConstants.CID_NORMAL),
                any());
        assertThat(exception).usingRecursiveComparison().isEqualTo(expectedException);
    }

    @SneakyThrows
    @Test
    void storeObject_givenStoreObjectFails_throwMetabusException() {

        // Setup pre-conditions
        when(jsonUtil.canonicalizeFromText(any())).thenReturn(TestConstants.CANONICALIZED_NORMAL);
        when(minioUtil.makeBucket(any())).thenReturn(true);
        when(minioUtil.storeObject(any(), any(), any())).thenReturn(false);
        MetabusException expectedException = new MetabusException(
                MetabusErrors.ERROR_MINIO_STORING,
                "Random error message when storing object into storage");

        // Execute test method
        MetabusException exception = assertThrows(
                MetabusException.class,
                () -> {
                    offchainStorageService.storeObject(TestConstants.BUCKET_NAME, TestConstants.JSON_NORMAL);
                }
        );

        // Verify the result
        verify(jsonUtil, times(1)).canonicalizeFromText(TestConstants.JSON_NORMAL);
        verify(minioUtil, times(1)).makeBucket(TestConstants.BUCKET_NAME);
        verify(minioUtil, times(1)).storeObject(
                eq(TestConstants.BUCKET_NAME),
                eq(TestConstants.CID_NORMAL),
                any());
        assertThat(exception).usingRecursiveComparison().isEqualTo(expectedException);
    }
}