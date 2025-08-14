package org.cardanofoundation.metabus.integrationtest.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import lombok.SneakyThrows;
import org.cardanofoundation.metabus.common.entities.JobJPA;
import org.cardanofoundation.metabus.common.entities.ScheduledBatchesJPA;
import org.cardanofoundation.metabus.common.enums.BatchStatus;
import org.cardanofoundation.metabus.common.enums.JobState;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.LinkedHashMap;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Pre-condition for running these integration tests:
 * 1. Keycloak started
 * 2. PostgrestSQL state storage started
 */
public class JobControllerIT extends BaseIntegrationTest {
    // cardano-metabus-api requests/responses json file path
    public final static String METABUS_API_CREATE_SINGLE_GROUP_JOB_REQ =
            BASE_JSON_FOLDER + REQUEST_FOLDER + "/create_single_group_job_req.json";
    public final static String METABUS_API_CREATE_MULTI_GROUP_JOB_REQ =
            BASE_JSON_FOLDER + REQUEST_FOLDER + "/create_multi_group_job_req.json";

    // cardano-metabus-jobproducer requests/responses json file path
    public final static String METABUS_JOBPRODUCER_CREATE_JOB_SUCCESS_RESP =
            BASE_WIREMOCK_JSON_FOLDER + RESPONSE_FOLDER + "/request_job_prodcuer_service_create_job_success_resp.json";
    public final static String METABUS_JOBPRODUCER_CREATE_JOB_INTERNAL_SERVER_ERROR_RESP =
            BASE_WIREMOCK_JSON_FOLDER + RESPONSE_FOLDER + "/request_job_prodcuer_service_create_job_500_error.json";

    // other constants
    public final static String INVALID_ACCESS_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJZVE9PZVBVR2s3NFRkZE";

    public static final byte[] signature = new byte[] {39, -32, 51, 64, -103, -104, -103, -86, 50, 113, 38, 57, -54, -119, 86,
            86, -61, -99, 80, 82, 67, 22, -40, -73, -88, -85, -46, 96, 15, -70, 39, 11, 88, 60, 49, -60, -94, 5, 39,
            -119, -57, -79, 27, 76, 30, 17, -1, -48, -43, -40, -93, 1, -27, 53, -112, -42, -126, -24, -11, -65, 84, 52,
            -101, 12};
    private static final byte[] jwsHeader = new byte[]{123, 34, 107, 105, 100, 34, 58, 34, 55, 50, 99, 99, 52, 102, 54,
            52, 45, 98, 50, 56, 49, 45, 52, 48, 56, 98, 45, 56, 52, 48, 52, 45, 57, 57, 102, 57, 98, 100, 100, 101, 56,
            57, 49, 57, 34, 44, 34, 97, 108, 103, 34, 58, 34, 69, 100, 68, 83, 65, 34, 125};

    @Test
    @SneakyThrows
    public void givenValidToken_whenCreateSingleGroupJob_shouldSuccess() {
        createStubForWireMockJobProducerServer_createJobSuccess();

        String accessToken = prepareValidAccessTokenForOriginateRole();

        Integer jobId = given().headers("Authorization", "Bearer " + accessToken,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .body(readJsonFromFile(METABUS_API_CREATE_SINGLE_GROUP_JOB_REQ))
                .when()
                .post("/api/v1/jobs")
                .then()
                .assertThat()
                .body("data.id", Matchers.notNullValue())
                .statusCode(200)
                .extract()
                .jsonPath()
                .get("data.id");

        assertNotNull(jobId);

        JobJPA jobFromDB = jobRepository.findById(Long.valueOf(jobId)).get();
        final List<ScheduledBatchesJPA> scheduledBatchesJPA = scheduledBatchesRepository.findByJobType("conformityCert:georgianWine");

        assertEquals(BatchStatus.PENDING, scheduledBatchesJPA.get(0).getBatchStatus());
        assertEquals(JobState.PENDING, jobFromDB.getState());
    }

    private void createStubForWireMockJobProducerServer_createJobSuccess() {
        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching("/api/v1/jobs"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readJsonFromFile(METABUS_JOBPRODUCER_CREATE_JOB_SUCCESS_RESP))
                )
        );
    }

    @Test
    @SneakyThrows
    public void givenValidToken_whenCreateMultiGroupJob_shouldSuccess() {
        createStubForWireMockJobProducerServer_createJobSuccess();

        String accessToken = prepareValidAccessTokenForOriginateRole();

        Integer jobId = given().headers("Authorization", "Bearer " + accessToken,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .body(readJsonFromFile(METABUS_API_CREATE_MULTI_GROUP_JOB_REQ))
                .when()
                .post("/api/v1/jobs")
                .then()
                .assertThat()
                .body("data.id", Matchers.notNullValue())
                .statusCode(200)
                .extract()
                .jsonPath()
                .get("data.id");

        assertNotNull(jobId);

        JobJPA jobFromDB = jobRepository.findById(Long.valueOf(jobId)).get();
        final List<ScheduledBatchesJPA> scheduledBatchesJPA = scheduledBatchesRepository.findByJobType("scm:georgianWine");

        assertEquals(BatchStatus.PENDING, scheduledBatchesJPA.get(0).getBatchStatus());
        assertEquals(JobState.PENDING, jobFromDB.getState());
    }


    @Test
    @SneakyThrows
    public void givenValidToken_whenCreateAJobWthTypeNotAllowed_thenVerifyPermissionDenied() {
        createStubForWireMockJobProducerServer_createJobSuccess();

        String accessToken = prepareValidAccessTokenForOtherRole();

        given().headers("Authorization", "Bearer " + accessToken,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .body(readJsonFromFile(METABUS_API_CREATE_SINGLE_GROUP_JOB_REQ))
                .when()
                .post("/api/v1/jobs")
                .then()
                .assertThat()
                .statusCode(403);
    }

    @Test
    @SneakyThrows
    public void givenInvalidToken_whenCreateAJob_thenVerifyUnauthorized() {
        createStubForWireMockJobProducerServer_createJobSuccess();
        String instructionToRetrieveToken = "Invalid or expired token. Please obtain a new token by the keycloak login api";

        String message = given().headers("Authorization", "Bearer " + INVALID_ACCESS_TOKEN,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .body(readJsonFromFile(METABUS_API_CREATE_SINGLE_GROUP_JOB_REQ))
                .when()
                .post("/api/v1/jobs")
                .then()
                .assertThat()
                .statusCode(401)
                .extract()
                .jsonPath()
                .get("meta.message");

        assertEquals(instructionToRetrieveToken, message);
    }

    @Test
    @SneakyThrows
    public void givenNoTokenPassIn_whenCreateAJob_thenVerifyForbidden() {
        createStubForWireMockJobProducerServer_createJobSuccess();
        String expectedMessage = "You do not have permission to access this resource.";

        String message = given().headers("Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .body(readJsonFromFile(METABUS_API_CREATE_SINGLE_GROUP_JOB_REQ))
                .when()
                .post("/api/v1/jobs")
                .then()
                .assertThat()
                .statusCode(403)
                .extract()
                .jsonPath()
                .get("meta.message");

        assertEquals(expectedMessage, message);
    }

    @Test
    @SneakyThrows
    public void givenValidToken_whenErrorCallingJobProducerServer_thenVerifyInternalServerError() {
        createStubForWireMockJobProducerServer_createJobFail();
        String accessToken = prepareValidAccessTokenForOriginateRole();

        JsonPath jsonPath = given().headers("Authorization", "Bearer " + accessToken,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .body(readJsonFromFile(METABUS_API_CREATE_SINGLE_GROUP_JOB_REQ))
                .when()
                .post("/api/v1/jobs")
                .then()
                .assertThat()
                .statusCode(500)
                .extract()
                .jsonPath();

        String message = jsonPath.get("meta.message");
        String internalMessage = jsonPath.get("meta.internal_message");

        assertEquals("Error when creating job", message );
        assertEquals("500 Internal Server Error from POST " +
                "http://localhost:8099/api/v1/jobs", internalMessage );
    }

    @Test
    @SneakyThrows
    public void givenValidToken_whenCreateJob_thenVerifyInternalServerError() {
        createStubForWireMockJobProducerServer_createJobFail();
        String accessToken = prepareValidAccessTokenForOriginateRole();

        JsonPath jsonPath = given().headers("Authorization", "Bearer " + accessToken,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .body(readJsonFromFile(METABUS_API_CREATE_SINGLE_GROUP_JOB_REQ))
                .when()
                .post("/api/v1/jobs")
                .then()
                .assertThat()
                .statusCode(500)
                .extract()
                .jsonPath();

        String message = jsonPath.get("meta.message");
        String internalMessage = jsonPath.get("meta.internal_message");

        assertEquals("Error when creating job", message );
        assertEquals("500 Internal Server Error from POST " +
                "http://localhost:8099/api/v1/jobs", internalMessage );
    }

    private void createStubForWireMockJobProducerServer_createJobFail() {
        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching("/api/v1/jobs"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readJsonFromFile(METABUS_JOBPRODUCER_CREATE_JOB_INTERNAL_SERVER_ERROR_RESP))
                )
        );
    }

    @Test
    @SneakyThrows
    public void givenValidToken_whenGetJobOfTypeNotAllowed_verifyForbidden() {
        createStubForWireMockJobProducerServer_createJobSuccess();

        String accessToken = prepareValidAccessTokenForOtherRole();

        var jobType = "scm:georgianWine";
        JobJPA savedJob = jobRepository.save(JobJPA.builder()
                .type(jobType)
                .subType("georgianWine")
                .signature(signature)
                .jwsHeader(jwsHeader)
                .retryCount(5)
                .build());
        JsonPath jsonPath = given().headers("Authorization", "Bearer " + accessToken,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .get("/api/v1/jobs/" + savedJob.getId())
                .then()
                .assertThat()
                .statusCode(403)
                .extract()
                .jsonPath();
        String message = jsonPath.get("meta.internal_message");
        assertEquals("You do not have permission to get job with type: " + jobType, message);
    }

    @Test
    @SneakyThrows
    public void givenValidToken_whenGetJob_shouldSucess() {
        createStubForWireMockJobProducerServer_createJobSuccess();

        String accessToken = prepareValidAccessTokenForOriginateRole();

        var jobType = "scm:georgianWine";
        JobJPA savedJob = jobRepository.save(JobJPA.builder()
                        .type(jobType)
                        .subType("georgianWine")
                        .signature(signature)
                        .jwsHeader(jwsHeader)
                        .retryCount(5)
                .build());
        JsonPath jsonPath = given().headers("Authorization", "Bearer " + accessToken,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .get("/api/v1/jobs/" + savedJob.getId())
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .jsonPath();
        Integer jobId = jsonPath.get("data.id");

        assertEquals(savedJob.getId(), jobId.longValue());
    }

    private String prepareValidAccessTokenForOriginateRole() {
        LinkedHashMap keycloakTokenResponse = keycloakWebClient.post()
                .uri(uriBuilder -> uriBuilder.path("/protocol/openid-connect/token").build())
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", "ORIGINATE_PILOT_APPLICATION")
                        .with("client_secret", "**********"))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<LinkedHashMap>() {
                })
                .block();
        return (String) keycloakTokenResponse.get("access_token");
    }

    private String prepareValidAccessTokenForOtherRole() {
        LinkedHashMap keycloakTokenResponse = keycloakWebClient.post()
                .uri(uriBuilder -> uriBuilder.path("/protocol/openid-connect/token").build())
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", "ADMIN")
                        .with("client_secret", "**********"))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<LinkedHashMap>() {
                })
                .block();
        return (String) keycloakTokenResponse.get("access_token");
    }
}
