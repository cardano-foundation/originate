package org.cardanofoundation.metabus.service;

public interface OffchainStorageService {
    String storeObject(String bucketName, String jsonText);
}
