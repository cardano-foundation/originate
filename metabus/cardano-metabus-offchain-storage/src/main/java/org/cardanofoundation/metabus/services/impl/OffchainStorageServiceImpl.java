package org.cardanofoundation.metabus.services.impl;

import com.bloxbean.cardano.client.crypto.Blake2bUtil;
import io.ipfs.cid.Cid;
import io.ipfs.multibase.Multibase;
import io.ipfs.multihash.Multihash;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.cardanofoundation.metabus.exceptions.MetabusErrors;
import org.cardanofoundation.metabus.exceptions.MetabusException;
import org.cardanofoundation.metabus.services.OffchainStorageService;
import org.cardanofoundation.metabus.util.JsonUtil;
import org.cardanofoundation.metabus.util.MinioUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class OffchainStorageServiceImpl implements OffchainStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OffchainStorageServiceImpl.class);

    private final MinioUtil minioUtil;
    private final JsonUtil jsonUtil;

    @Override
    public String getObjectUrl(String bucketName, String objectName) throws ServerException, InsufficientDataException,
            IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException,
            XmlParserException, InternalException, ErrorResponseException {

        LOGGER.debug("OffchainStorageServiceImpl | getObjectUrl is called");
        LOGGER.debug("OffchainStorageServiceImpl | getObjectUrl | bucketName : " + bucketName);
        LOGGER.debug("OffchainStorageServiceImpl | getObjectUrl | objectName : " + objectName);

        if (!minioUtil.bucketExists(bucketName)) {
            throw new MetabusException(MetabusErrors.ERROR_MINIO_GET_OBJECT, "Bucket does not exist");
        }
        if (!minioUtil.objectExists(bucketName, objectName)) {
            throw new MetabusException(MetabusErrors.ERROR_MINIO_GET_OBJECT, "Object does not exist in the bucket");
        }

        return minioUtil.getObjectUrl(bucketName, objectName);
    }

    @Override
    public String storeObject(String bucketName, String jsonText) {
        LOGGER.debug("OffchainStorageServiceImpl | storeObject is called");
        LOGGER.debug("OffchainStorageServiceImpl | storeObject | bucketName : " + bucketName);
        LOGGER.debug("OffchainStorageServiceImpl | storeObject | jsonText length: " + jsonText.length());

        // Canonicalize the input JSON object.
        // This also validate the JSON object.
        String canonicalized = null;
        try {
            canonicalized = jsonUtil.canonicalizeFromText(jsonText);
        } catch (IOException e) {
            throw new MetabusException(MetabusErrors.ERROR_CANONICALIZED, "Input JSON object is invalid");
        }

        // Get CID from canonicalized content.
        String cid = getCidFromJson(canonicalized);

        byte[] jsonBytes = canonicalized.getBytes();
        InputStream inputStream = new ByteArrayInputStream(jsonBytes);

        boolean stored = false;

        try {
            // Create the bucket if it doesn't exist.
            minioUtil.makeBucket(bucketName);

            // Put the object into storage, with CID as object name.
            stored = minioUtil.storeObject(bucketName, cid, inputStream);

        } catch (Exception e) {
            throw new MetabusException(MetabusErrors.ERROR_MINIO_STORING, ExceptionUtils.getStackTrace(e));
        }

        if (!stored) {
            throw new MetabusException(MetabusErrors.ERROR_MINIO_STORING);
        }

        return cid;
    }

    /**
     * Generate CID from a canonicalized JSON.
     */
    private String getCidFromJson(String canonicalized) {
        // Get bytes to hash.
        byte[] canonicalizedBytes = canonicalized.getBytes();

        // Blake2b-256 as the hashing algorithm.
        var hashed = Blake2bUtil.blake2bHash256(canonicalizedBytes);

        // Content Identifier (CID) as defined by Multiformats (Protocol Labs).
        Cid cid = new Cid(1, Cid.Codec.Raw, Multihash.Type.blake2b_256, hashed);

        // CID be represented in Multibase format with base58btc encoding.
        return Multibase.encode(Multibase.Base.Base58BTC, cid.toBytes());
    }
}
