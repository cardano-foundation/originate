package org.cardanofoundation.proofoforigin.api.business.impl;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.proofoforigin.api.business.MetabusCallerService;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.BusinessDataDto;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.Unit;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.Unit.MetabusJobType;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.request.JobRequest;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.response.AccessTokenResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.response.JobResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BaseResponse;
import org.cardanofoundation.proofoforigin.api.exceptions.OriginatePilotErrors;
import org.cardanofoundation.proofoforigin.api.exceptions.OriginatePilotException;
import org.cardanofoundation.proofoforigin.api.security.properties.KeycloakTokenMetabusApiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@EnableConfigurationProperties(value = {KeycloakTokenMetabusApiProperties.class})
public class MetabusCallerServiceImpl implements MetabusCallerService {
    KeycloakTokenMetabusApiProperties keycloakTokenMetabusApiProperties;
    RestTemplate restTemplate;

    public MetabusCallerServiceImpl(KeycloakTokenMetabusApiProperties keycloakTokenMetabusApiProperties, RestTemplate restTemplate) {
        this.keycloakTokenMetabusApiProperties = keycloakTokenMetabusApiProperties;
        this.restTemplate = restTemplate;
    }

    private String token;

    private Long timeOut = 180L;

    @PostConstruct
    private void intiToken() {
        token = getAccessToken();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> token = getAccessToken(), 0, timeOut - 60, TimeUnit.SECONDS);
    }


    public String getAccessToken() {
        log.info("[MetabusCallerServiceImpl] Attempting to retrieve access token from key clock to get permission to metabus system");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add(Unit.ConfigKeycloak.GRANT_TYPE, keycloakTokenMetabusApiProperties.getMetabusKeyCloakConfig().getGrantYype());
        requestBody.add(Unit.ConfigKeycloak.CLIENT_ID, keycloakTokenMetabusApiProperties.getMetabusKeyCloakConfig().getClientId());
        requestBody.add(Unit.ConfigKeycloak.CLIENT_SECRET, keycloakTokenMetabusApiProperties.getMetabusKeyCloakConfig().getClientSecret());
        requestBody.add(Unit.ConfigKeycloak.SCOPE, keycloakTokenMetabusApiProperties.getMetabusKeyCloakConfig().getScope());
        ResponseEntity<AccessTokenResponse> response;
        try {
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);
            response = restTemplate.exchange(keycloakTokenMetabusApiProperties.getMetabusKeyCloakConfig().getUrl(), HttpMethod.POST, request, AccessTokenResponse.class);
            AccessTokenResponse accessTokenResponse = response.getBody();
            log.info("[MetabusCallerServiceImpl] Successfully retrieved access token from key clock to get permission to metabus system");
            timeOut = accessTokenResponse.getExpiresIn() != null ? accessTokenResponse.getExpiresIn() : 180;
            return accessTokenResponse.getAccessToken();
        } catch (Exception e) {
            log.error("[MetabusCallerServiceImpl] Failed to retrieve access token from key clock to get permission to metabus system, reason {}", e);
            return null;
        }
    }

    @Override
    public JobResponse createJob(Object job, Unit.MetabusJobType type, String signature, String pubKey, String group) {
        log.info("[MetabusCallerServiceImpl] Attempting to create job type {} with request {} metabus system", type, job);
        JobRequest jobRequest = new JobRequest();
        BusinessDataDto businessDataRequest = new BusinessDataDto();
        businessDataRequest.setData(job);
        businessDataRequest.setSignature(signature);
        businessDataRequest.setPubKey(pubKey);
        businessDataRequest.setType(getBusinessDataType(type));
        jobRequest.setBusinessData(businessDataRequest);
        jobRequest.setGroupType(getBusinessDataGroupType(type));
        jobRequest.setGroup(group);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);

        try {
            HttpEntity<JobRequest> request = new HttpEntity<>(jobRequest, headers);
            ResponseEntity<BaseResponse<JobResponse>> response = restTemplate.exchange(keycloakTokenMetabusApiProperties.getApiMetabus(), HttpMethod.POST, request, new ParameterizedTypeReference<>() {});
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null || response.getBody().getData() == null) {
                throw new OriginatePilotException(OriginatePilotErrors.METABUS_ERROR);
            }

            return response.getBody().getData();
        } catch (Exception e) {
            log.error("[MetabusCallerServiceImpl] Failed to post request to metabus system, reason {}", e);
            throw e;
        }
    }

    @Override
    public JobResponse createJob(Object job, Unit.MetabusJobType type, String signature) {
        return createJob(job, type, signature, "", "");
    }

    private String getBusinessDataType(Unit.MetabusJobType jobType) {
        switch (jobType) {
            case CERT -> {
                return Unit.MetabusConstants.METABUS_TYPE_CERT;
            }
            case LOT -> {
                return Unit.MetabusConstants.METABUS_TYPE_SCM;
            }
            case CERT_REVOCATION -> {
                return Unit.MetabusConstants.METABUS_TYPE_CERT_REVOCATION;
            }
            default -> {
                return null;
            }
        }
    }

    private Unit.GroupType getBusinessDataGroupType(Unit.MetabusJobType jobType) {
        switch (jobType) {
            case CERT, CERT_REVOCATION -> {
                return Unit.GroupType.SINGLE_GROUP;
            }
            case LOT -> {
                return Unit.GroupType.MULTI_GROUP;
            }
            default -> {
                return null;
            }
        }
    }


    @Override
    public JobResponse createJob(Object job, MetabusJobType type, String signature, String pubKey) {
        return createJob(job, type, signature, pubKey, "");
    }

}
