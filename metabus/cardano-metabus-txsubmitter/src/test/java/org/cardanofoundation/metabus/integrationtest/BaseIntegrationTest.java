package org.cardanofoundation.metabus.integrationtest;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.path.json.JsonPath;
import lombok.SneakyThrows;
import org.cardanofoundation.metabus.configuration.TxSubmitterProperties;
import org.cardanofoundation.metabus.repos.JobRepository;
import org.cardanofoundation.metabus.repos.ScheduledBatchesRepository;
import org.cardanofoundation.metabus.repos.UnconfirmedTxRepository;
import org.cardanofoundation.metabus.repos.UtxoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

@SpringBootTest
@ActiveProfiles("it")
public abstract class BaseIntegrationTest {

    protected static final int WIREMOCK_PORT = 8100;
    protected static final String WIREMOCK_HOST = "localhost";
    protected static String BASE_JSON_FOLDER = "json";
    protected static String BASE_WIREMOCK_JSON_FOLDER = "json/wiremock";
    protected static String REQUEST_FOLDER = "/requests";
    protected static String RESPONSE_FOLDER = "/response";

    protected final static String OFFCHAIN_STOREOBJECT_SUCCESS_RESP =
            BASE_WIREMOCK_JSON_FOLDER + RESPONSE_FOLDER + "/offchain_storeobject_success_resp.json";

    // mock server
    protected WireMockServer wireMockServer;

    @Autowired
    protected UnconfirmedTxRepository unconfirmedTxRepository;
    @Autowired
    protected JobRepository jobRepository;
    @Autowired
    protected ScheduledBatchesRepository scheduledBatchesRepository;
    @Autowired
    protected UtxoRepository utxoRepository;
    @Autowired
    private TxSubmitterProperties txSubmitterProperties;

    @BeforeEach
    public void cleanUp() {
        clearDatabase();

        // set up a mock server for Offchain Storage
        wireMockServer = new WireMockServer(WIREMOCK_PORT);
        wireMockServer.start();
        WireMock.configureFor(WIREMOCK_HOST, WIREMOCK_PORT);
    }

    @SneakyThrows
    protected String readJsonFromFile(String jsonFilePath) {
        URL resource = BaseIntegrationTest.class.getClassLoader().getResource(jsonFilePath);
        byte[] b = Files.readAllBytes(Paths.get(resource.toURI()));
        return new String(b);
    }

    @SneakyThrows
    protected Map readJsonMapFromFile(String jsonFilePath) {
        URL resource = BaseIntegrationTest.class.getClassLoader().getResource(jsonFilePath);
        String absolutePath = Paths.get(resource.toURI()).toString();
        return new JsonPath(new File(absolutePath)).getMap("");
    }

    private void clearDatabase() {
        jobRepository.deleteAll();
        utxoRepository.deleteAll();
        unconfirmedTxRepository.deleteAll();
        scheduledBatchesRepository.deleteAll();
    }

    protected void createStubForWireMock_offchainStorageServer_storeObject_success() {
        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching(
                        "/api/v1/storage/storeObject/" + txSubmitterProperties.getOffchainBucket()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain;charset=UTF-8")
                        .withBody(readJsonFromFile(OFFCHAIN_STOREOBJECT_SUCCESS_RESP))
                )
        );
    }
}
