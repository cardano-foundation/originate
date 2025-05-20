package org.cardanofoundation.metabus.integrationtest;

import io.restassured.http.ContentType;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.integrationtest.kafka.KafkaConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This integration test can run independently with kafka embedded in server
 */
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9093", "port=9093" })
public class JobControllerIT extends BaseIntegrationTest {
    // cardano-metabus-jobproducer requests/responses json file path
    public final static String METABUS_JOB_PRODUCER_CREATE_SINGLE_GROUP_JOB_REQ =
            BASE_JSON_FOLDER + REQUEST_FOLDER + "/create_multi_group_job_req.json";

    @Autowired
    private KafkaConsumer kafkaConsumer;

    @Test
    @SneakyThrows
    public void createJobShouldSuccess() {
        String requestBodyJson = readJsonFromFile(METABUS_JOB_PRODUCER_CREATE_SINGLE_GROUP_JOB_REQ);
        String message = given().headers(
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .body(requestBodyJson)
                .when()
                .post("/api/v1/jobs")
                .then()
                .assertThat()
                .statusCode(200).extract().jsonPath().get("data");

        ConsumerRecord<String, Job> consumerRecord = null;

        while(Objects.isNull(consumerRecord)){
            consumerRecord = kafkaConsumer.getConsumerRecord().get();
        }

        Job expectedJob = objectMapper.readValue(requestBodyJson, Job.class);
        String expectedJobJsonValue = objectMapper.writeValueAsString(expectedJob);
        Job jobPushedToKafka = consumerRecord.value();
        String jobPushedToKafkaJson = objectMapper.writeValueAsString(jobPushedToKafka);

        assertEquals(objectMapper.readValue(expectedJobJsonValue, JsonNode.class),
                objectMapper.readValue(jobPushedToKafkaJson, JsonNode.class));

        assertEquals("job is being proccessed", message);
    }
}
