package org.cardanofoundation.metabus.util;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.cardanofoundation.metabus.config.MinioConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class MinioUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinioUtil.class);

    @Qualifier("internalMinioClient")
    private final MinioClient minioClient;

    @Qualifier("publicMinioClient")
    private final MinioClient minioPublicClient;

    private final MinioConfig minioConfig;

    /**
     * Create bucket if it doesn't exist
     */
    public boolean makeBucket(String bucketName) throws ServerException, InsufficientDataException,
            ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException, InternalException {

        boolean existed = bucketExists(bucketName);
        if (!existed) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get file path from the specified bucket
     */
    public String getObjectUrl(String bucketName, String objectName) throws ServerException, InsufficientDataException,
            ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException, InternalException {

        String url = "";
        boolean existed = bucketExists(bucketName);
        if (existed) {
            url = minioPublicClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(minioConfig.getObjectUrlExpiry(), TimeUnit.MINUTES)
                            .build());
            LOGGER.debug("MinioUtil | getObjectUrl | url : " + url);
        }
        return url;
    }

    /**
     * Upload InputStream object to the specified bucket
     */
    public boolean storeObject(String bucketName, String objectName, InputStream inputStream) throws ServerException,
            InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException,
            InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(inputStream, -1, minioConfig.getFileSize())
                        .contentType("application/json")
                        .build()
        );

        return objectExists(bucketName, objectName);
    }

    /**
     * Check if bucket name exists
     */
    public boolean bucketExists(String bucketName) throws ServerException, InsufficientDataException,
            ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException, InternalException {

        // When bucket does not exist (e.g. input wrong bucket name), no exception occurs.
        return minioClient.bucketExists(
                BucketExistsArgs.builder().
                        bucket(bucketName).
                        build());
    }

    /**
     * Check if object exists in a specified bucket
     */
    public boolean objectExists(String bucketName, String objectName) throws ServerException,
            InsufficientDataException, IOException, NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException, InternalException, ErrorResponseException {

        boolean existed = bucketExists(bucketName);
        if (!existed) {
            return false;
        }

        try {
            StatObjectResponse sor = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
            existed = (sor != null && sor.size() > 0);

        } catch (ErrorResponseException e) {
            // When object does not exist in the bucket (e.g. input wrong object name), this exception occurs.
            LOGGER.debug("MinioUtil | objectExists | error : " + e.getMessage());
            existed = false;
        }

        return existed;
    }

    /**
     * Remove bucket
     */
    public boolean removeBucket(String bucketName) throws ServerException, InsufficientDataException,
            ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException, InternalException {

        if (!bucketExists(bucketName)) {
            return true;
        }

        // Check if bucket is empty
        Iterable<Result<Item>> objects = minioClient.listObjects(
                ListObjectsArgs
                        .builder()
                        .bucket(bucketName)
                        .build()
        );

        // Get all objects names
        List<String> objectNames = new ArrayList<>();
        for (Result<Item> object : objects) {
            Item item = object.get();
            objectNames.add(item.objectName());
        }

        // Delete all objects from the bucket
        List<DeleteObject> deleteObjects = new LinkedList<>();
        for (String objectName : objectNames) {
            deleteObjects.add(new DeleteObject(objectName));
        }
        Iterable<Result<DeleteError>> results =
                minioClient.removeObjects(
                        RemoveObjectsArgs
                                .builder()
                                .bucket(bucketName)
                                .objects(deleteObjects)
                                .build()
                );

        for (Result<DeleteError> result : results) {
            DeleteError error = result.get();
            LOGGER.debug("MinioUtil | removeBucket | error removing object : " + error.objectName() + " " + error.message());
            return false;
        }

        //  Delete the bucket
        minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());

        return !bucketExists(bucketName);
    }

    /**
     * List all bucket names
     */
    public boolean removeAllBuckets() throws ServerException, InsufficientDataException, ErrorResponseException,
            IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException,
            XmlParserException, InternalException {

        List<Bucket> bucketList = minioClient.listBuckets();

        for (Bucket bucket : bucketList) {
            if (!removeBucket(bucket.name())) {
                return false;
            }
        }

        return true;
    }
}
