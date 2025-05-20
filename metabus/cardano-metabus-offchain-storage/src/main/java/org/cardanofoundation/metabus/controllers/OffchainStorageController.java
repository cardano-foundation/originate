package org.cardanofoundation.metabus.controllers;

import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.metabus.constants.BaseUri;
import org.cardanofoundation.metabus.services.OffchainStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequiredArgsConstructor
@RequestMapping(BaseUri.CARDANO_METABUS_OFFCHAIN_STORAGE.V1 + BaseUri.CARDANO_METABUS_OFFCHAIN_STORAGE.STORAGE)
@Slf4j
public class OffchainStorageController {

    private final OffchainStorageService offchainStorageService;

    @GetMapping("/objectUrl/{bucketName}/{objectName}")
    public ResponseEntity<String> objectUrl(@PathVariable("bucketName") String bucketName,
                                            @PathVariable("objectName") String objectName)
            throws ServerException, InsufficientDataException, IOException, NoSuchAlgorithmException,
            InvalidKeyException, InvalidResponseException, XmlParserException, InternalException,
            ErrorResponseException {

        return ResponseEntity.ok(offchainStorageService.getObjectUrl(bucketName, objectName));
    }

    @PostMapping("/storeObject/{bucketName}")
    public ResponseEntity<String> storeObject(@PathVariable("bucketName") String bucketName, @RequestBody String jsonText) {
        try {
            return ResponseEntity.ok(offchainStorageService.storeObject(bucketName, jsonText));
        } catch (Exception e) {
            log.warn("Error while attempting to store s3 object", e);
            return ResponseEntity.internalServerError().body("error: " + e.getMessage());
        }

    }
}
