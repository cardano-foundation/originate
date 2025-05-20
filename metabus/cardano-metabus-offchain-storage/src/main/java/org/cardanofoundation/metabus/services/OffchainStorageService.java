package org.cardanofoundation.metabus.services;

import io.minio.errors.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface OffchainStorageService {

    // Get file path from bucket
    String getObjectUrl(String bucketName, String objectName) throws ServerException, InsufficientDataException,
            IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException,
            InternalException, ErrorResponseException;

    // Store JSON object
    String storeObject(String bucketName, String jsonText);
}
