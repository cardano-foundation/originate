package org.cardanofoundation.metabus.integrationtest.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;

import org.cardanofoundation.metabus.common.entities.ScheduledBatchesJPA;
import org.cardanofoundation.metabus.repositories.JobRepository;
import org.cardanofoundation.metabus.repositories.ScheduledBatchesRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("it")
@DirtiesContext
public abstract class BaseIntegrationTest {
    // constants
    protected static int REST_ASSURED_METABUS_API_CALLING_PORT = 8082;
    protected static final int WIREMOCK_PORT = 8099;
    protected static final String WIREMOCK_HOST = "localhost";
    protected static String BASE_JSON_FOLDER = "json";
    protected static String BASE_WIREMOCK_JSON_FOLDER = "json/wiremock";
    protected static String REQUEST_FOLDER = "/requests";
    protected static String RESPONSE_FOLDER = "/response";

    // mock server
    protected WireMockServer wireMockServer;

    // services
    @Autowired
    protected JobRepository jobRepository;
    protected WebClient keycloakWebClient;

    @Autowired
    protected ScheduledBatchesRepository scheduledBatchesRepository;

    @Autowired(required = false)
    protected CacheManager cacheManager;

    @PostConstruct
    public void init(){
        keycloakWebClient = WebClient.builder()
                .baseUrl("http://localhost:8881/realms/Metabus")
                .build();
    }

    @BeforeEach
    public void setUp() {
        // set up port for rest assured call apis of metabus api
        RestAssured.port = REST_ASSURED_METABUS_API_CALLING_PORT;
        // set up a mock server for job producer
        wireMockServer = new WireMockServer(WIREMOCK_PORT);
        wireMockServer.start();
        WireMock.configureFor(WIREMOCK_HOST, WIREMOCK_PORT);
    }

    @AfterEach
    public void cleanUp(){
        clearDatabase();
        wireMockServer.stop();
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
        }
    }

    @SneakyThrows
    protected String readJsonFromFile(String jsonFilePath){
        URL resource = BaseIntegrationTest.class.getClassLoader().getResource(jsonFilePath);
        byte[] b = Files.readAllBytes(Paths.get(resource.toURI()));
        return new String(b);
    }

    @SneakyThrows
    protected Map readJsonMapFromFile(String jsonFilePath){
        URL resource = BaseIntegrationTest.class.getClassLoader().getResource(jsonFilePath);
        String absolutePath = Paths.get(resource.toURI()).toString();
        return new JsonPath(new File(absolutePath)).getMap("");
    }


    void clearDatabase(){
        jobRepository.deleteAll();
    }
}
