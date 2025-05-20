package org.cardanofoundation.metabus.integrationtest;


import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

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
    protected static int REST_ASSURED_METABUS_API_CALLING_PORT = 8098;
    protected static String BASE_JSON_FOLDER = "json";
    protected static String REQUEST_FOLDER = "/requests";
    protected static String RESPONSE_FOLDER = "/response";
    protected ObjectMapper objectMapper = new ObjectMapper();

    @Autowired(required = false)
    protected CacheManager cacheManager;


    @BeforeEach
    public void setUp() {
        // set up port for rest assured call apis of metabus api
        RestAssured.port = REST_ASSURED_METABUS_API_CALLING_PORT;
    }

    @AfterEach
    public void cleanUp(){
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
}
