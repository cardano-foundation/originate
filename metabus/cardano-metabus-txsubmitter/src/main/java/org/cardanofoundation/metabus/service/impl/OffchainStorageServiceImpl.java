package org.cardanofoundation.metabus.service.impl;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.cardanofoundation.metabus.configuration.TxSubmitterProperties;
import org.cardanofoundation.metabus.constants.BaseUri.CARDANO_METABUS_TXSUBMITTER;
import org.cardanofoundation.metabus.exceptions.MetabusErrors;
import org.cardanofoundation.metabus.exceptions.MetabusException;
import org.cardanofoundation.metabus.service.OffchainStorageService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@EnableConfigurationProperties(value = {TxSubmitterProperties.class})
public class OffchainStorageServiceImpl implements OffchainStorageService {

    WebClient offchainStorageWebClient;

    public OffchainStorageServiceImpl(@Lazy WebClient offchainStorageWebClient) {
        this.offchainStorageWebClient = offchainStorageWebClient;
    }

    @Override
    public String storeObject(String bucketName, String jsonText) {
        try {
            String storeObjectEndpoint = "/" + CARDANO_METABUS_TXSUBMITTER.V1
                    + CARDANO_METABUS_TXSUBMITTER.STORAGE
                    + CARDANO_METABUS_TXSUBMITTER.STORE_OBJECT;

            return offchainStorageWebClient.post()
                    .uri(uriBuilder -> uriBuilder.path(storeObjectEndpoint + "/" + bucketName).build())
                    .body(Mono.just(jsonText), String.class)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<String>() {
                    })
                    .block();
        } catch (MetabusException metabusException) {
            throw metabusException;
        } catch (Exception e) {
            throw new MetabusException(MetabusErrors.ERROR_MINIO_STORING, ExceptionUtils.getStackTrace(e));
        }
    }
}
