package org.cardanofoundation.proofoforigin.api.business.impl;

import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.Unit;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.request.JobRequest;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.response.AccessTokenResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.response.JobResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BaseResponse;
import org.cardanofoundation.proofoforigin.api.exceptions.OriginatePilotException;
import org.cardanofoundation.proofoforigin.api.security.properties.KeycloakTokenMetabusApiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
@ExtendWith(MockitoExtension.class)
public class MetabusCallerServiceImplTest {

    KeycloakTokenMetabusApiProperties keycloakTokenMetabusApiProperties;

    RestTemplate restTemplate;

    MetabusCallerServiceImpl metabusCallerService;

    private static final String signature = "string";

    @BeforeEach
    public void init() {
        keycloakTokenMetabusApiProperties  = mock(KeycloakTokenMetabusApiProperties.class);
        restTemplate = mock(RestTemplate.class);
        metabusCallerService = new MetabusCallerServiceImpl(keycloakTokenMetabusApiProperties, restTemplate);
    }

    @Test
    void test_get_access_token_success() {

        //Prepare data
        KeycloakTokenMetabusApiProperties.KeyCloakConfig keyCloakConfig = new KeycloakTokenMetabusApiProperties.KeyCloakConfig();
        keyCloakConfig.setUrl("Url");
        keyCloakConfig.setScope("Scope");
        keyCloakConfig.setGrantYype("Grant type");
        keyCloakConfig.setClientId("Client Id");
        keyCloakConfig.setClientSecret("Client Secret");

        AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
        accessTokenResponse.setAccessToken("access token");

        // Mock and assertion
        when(keycloakTokenMetabusApiProperties.getMetabusKeyCloakConfig()).thenReturn(keyCloakConfig);
        when(restTemplate.exchange(eq(keycloakTokenMetabusApiProperties.getMetabusKeyCloakConfig().getUrl()), eq(HttpMethod.POST), any(HttpEntity.class), eq(AccessTokenResponse.class)))
                .thenReturn(new ResponseEntity<>(accessTokenResponse, HttpStatus.OK));

        String accessToken = metabusCallerService.getAccessToken();
        assertEquals(accessTokenResponse.getAccessToken(), accessToken);
    }

    @Test
    void test_get_access_token_throw_Exception() {
        //Prepare data
        KeycloakTokenMetabusApiProperties.KeyCloakConfig keyCloakConfig = new KeycloakTokenMetabusApiProperties.KeyCloakConfig();
        keyCloakConfig.setUrl("Url");
        keyCloakConfig.setScope("Scope");
        keyCloakConfig.setGrantYype("Grant type");
        keyCloakConfig.setClientId("Client Id");
        keyCloakConfig.setClientSecret("Client Secret");

        AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
        accessTokenResponse.setAccessToken("access token");

        //Mock and assertion
        when(keycloakTokenMetabusApiProperties.getMetabusKeyCloakConfig()).thenReturn(keyCloakConfig);
        when(restTemplate.exchange(eq(keycloakTokenMetabusApiProperties.getMetabusKeyCloakConfig().getUrl()), eq(HttpMethod.POST), any(HttpEntity.class), eq(AccessTokenResponse.class)))
                .thenThrow(new RestClientException("Error calling Keycloak API"));

        String accessToken = metabusCallerService.getAccessToken();
        assertEquals(null, accessToken);
    }


    @Test
    void test_create_job_success() throws NoSuchFieldException, IllegalAccessException {

        // Prepare data
        Field tokenField = metabusCallerService.getClass().getDeclaredField("token");
        tokenField.setAccessible(true);
        tokenField.set(metabusCallerService, "access_token");
        Unit.MetabusJobType type = Unit.MetabusJobType.CERT;
        JobResponse job = new JobResponse();
        BaseResponse<JobResponse> baseResponse = new BaseResponse<>();
        baseResponse.setData(job);

        // Mock and assertion
        ResponseEntity<BaseResponse<JobResponse>> responseEntityMock = mock(ResponseEntity.class);
        when(responseEntityMock.getBody()).thenReturn(baseResponse);
        when(responseEntityMock.getStatusCode()).thenReturn(HttpStatus.OK);
        when(keycloakTokenMetabusApiProperties.getApiMetabus()).thenReturn("api-meta-bus");
        when(restTemplate.exchange(eq(keycloakTokenMetabusApiProperties.getApiMetabus()), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenAnswer(invocationOnMock -> {
                    HttpEntity<JobRequest> http = invocationOnMock.getArgument(2);
                    assertEquals(http.getBody().getBusinessData().getData(), job);
                    return responseEntityMock;
                });
        JobResponse object = metabusCallerService.createJob(job, type, signature);
        assertEquals(job, object);
    }

    @Test
    void test_create_job_failed_by_http_status_is_not_ok() throws NoSuchFieldException, IllegalAccessException {

        // Prepare data
        Field tokenField = metabusCallerService.getClass().getDeclaredField("token");
        tokenField.setAccessible(true);
        tokenField.set(metabusCallerService, "access_token");
        Unit.MetabusJobType type = Unit.MetabusJobType.CERT;
        JobResponse job = new JobResponse();
        BaseResponse<JobResponse> baseResponse = new BaseResponse<>();
        baseResponse.setData(job);

        // Mock and assertion
        ResponseEntity<BaseResponse<JobResponse>> responseEntityMock = mock(ResponseEntity.class);
        when(responseEntityMock.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        when(keycloakTokenMetabusApiProperties.getApiMetabus()).thenReturn("api-meta-bus");
        when(restTemplate.exchange(eq(keycloakTokenMetabusApiProperties.getApiMetabus()), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenAnswer(invocationOnMock -> {
                    HttpEntity<JobRequest> http = invocationOnMock.getArgument(2);
                    assertEquals(http.getBody().getBusinessData().getData(), job);
                    return responseEntityMock;
                });

        assertThrows(OriginatePilotException.class, () -> metabusCallerService.createJob(job, type, signature));

    }

    @Test
    void test_create_job_failed_by_base_response_is_null() throws NoSuchFieldException, IllegalAccessException {

        // Prepare data
        Field tokenField = metabusCallerService.getClass().getDeclaredField("token");
        tokenField.setAccessible(true);
        tokenField.set(metabusCallerService, "access_token");
        Unit.MetabusJobType type = Unit.MetabusJobType.CERT;
        JobResponse job = new JobResponse();
        BaseResponse<JobResponse> baseResponse = new BaseResponse<>();
        baseResponse.setData(job);

        // Mock and assertion
        ResponseEntity<BaseResponse<JobResponse>> responseEntityMock = mock(ResponseEntity.class);

        when(responseEntityMock.getBody()).thenReturn(null);
        when(responseEntityMock.getStatusCode()).thenReturn(HttpStatus.OK);
        when(keycloakTokenMetabusApiProperties.getApiMetabus()).thenReturn("api-meta-bus");
        when(restTemplate.exchange(eq(keycloakTokenMetabusApiProperties.getApiMetabus()), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenAnswer(invocationOnMock -> {
                    HttpEntity<JobRequest> http = invocationOnMock.getArgument(2);
                    assertEquals(http.getBody().getBusinessData().getData(), job);
                    return responseEntityMock;
                });

        assertThrows(OriginatePilotException.class, () -> metabusCallerService.createJob(job, type, signature));
    }

    @Test
    void test_create_job_failed_by_data_response_is_null() throws NoSuchFieldException, IllegalAccessException {

        // Prepare data
        Field tokenField = metabusCallerService.getClass().getDeclaredField("token");
        tokenField.setAccessible(true);
        tokenField.set(metabusCallerService, "access_token");
        Unit.MetabusJobType type = Unit.MetabusJobType.CERT;
        JobResponse job = new JobResponse();
        BaseResponse<JobResponse> baseResponse = new BaseResponse<>();
        baseResponse.setData(null);

        // Mock and assertion
        ResponseEntity<BaseResponse<JobResponse>> responseEntityMock = mock(ResponseEntity.class);

        when(responseEntityMock.getBody()).thenReturn(baseResponse);
        when(responseEntityMock.getStatusCode()).thenReturn(HttpStatus.OK);
        when(keycloakTokenMetabusApiProperties.getApiMetabus()).thenReturn("api-meta-bus");
        when(restTemplate.exchange(eq(keycloakTokenMetabusApiProperties.getApiMetabus()), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenAnswer(invocationOnMock -> {
                    HttpEntity<JobRequest> http = invocationOnMock.getArgument(2);
                    assertEquals(http.getBody().getBusinessData().getData(), job);
                    return responseEntityMock;
                });

        assertThrows(OriginatePilotException.class, () -> metabusCallerService.createJob(job, type, signature));
    }
}
