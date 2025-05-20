package org.cardanofoundation.proofoforigin.api.integrationtest;

import com.rabbitmq.client.*;
import org.cardanofoundation.proofoforigin.api.configuration.RabbitMQConsumer;
import org.cardanofoundation.proofoforigin.api.repository.LotRepository;
import org.cardanofoundation.proofoforigin.api.repository.entities.Lot;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RabbitMQConsumerIT {


    private String QUEUE_NAME = "bolnisi";

    private int PORT = 5672;

    private String HOST = "10.4.10.184";

    private String USER_NAME = "guest";

    private String PASSWORD = "guest";

    private String EXCHANGE = "job";

    private String txId = "1233242314";

    @Autowired
    RabbitMQConsumer listener;

    @Autowired
    LotRepository lotRepository;

    private static final String MESSAGE = "{\n" +
            "    \"id\": 130,\n" +
            "    \"businessData\": {\n" +
            "        \"type\": \"bolnisi_lot\",\n" +
            "        \"data\": {\n" +
            "            \"id\": 92\n" +
            "        },\n" +
            "        \"signature\": \"string\",\n" +
            "        \"pubKey\": \"https://wine.gov.ge/.well-known/4527/publickey\"\n" +
            "    },\n" +
            "    \"txHash\": 1233242314,\n" +
            "    \"groupType\": \"SINGLE_GROUP\",\n" +
            "    \"group\": \"1234\"\n" +
            "}";

    private static final String ERROR_TYPE_MESSAGE = "{\n" +
            "    \"id\": 130,\n" +
            "    \"businessData\": {\n" +
            "        \"type\": \"bolnisi_error\",\n" +
            "        \"data\": {\n" +
            "            \"id\": 92\n" +
            "        },\n" +
            "        \"signature\": \"string\",\n" +
            "        \"pubKey\": \"https://wine.gov.ge/.well-known/4527/publickey\"\n" +
            "    },\n" +
            "    \"txHash\": 1233242314,\n" +
            "    \"groupType\": \"SINGLE_GROUP\",\n" +
            "    \"group\": \"1234\"\n" +
            "}";

    private static final String ERROR_MESSAGE = "{\n" +
            "error" +
            "}";

    public Channel setup() throws IOException, TimeoutException {
        // Create a connection factory and connect to RabbitMQ
        ConnectionFactory factory;
        Connection connection;
        Channel channel;
        factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(USER_NAME);
        factory.setPassword(PASSWORD);

        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-message-ttl", 10000);
        args.put("x-dead-letter-exchange", "bolnisi-dead-letter-exchange");

        connection = factory.newConnection();
        channel = connection.createChannel();

        // Declare a queue for the test messages
        channel.queueDeclare(QUEUE_NAME, true, false, false, args);
        return channel;
    }

    @Test
    @Order(1)
    public void testSendMessage() throws Exception {

        // Publish a test message to the queue
        Channel channel = this.setup();
        channel.basicPublish(EXCHANGE, QUEUE_NAME, null, MESSAGE.getBytes());

        // Assert that the message was received correctly
        assertDoesNotThrow(() -> listener.consume(MESSAGE,channel,1));
        Optional<Lot> lotOpt = lotRepository.findByTxId(txId);
        assertNotEquals(Optional.empty(), lotOpt);
        Lot lot = lotOpt.get();
        lot.setTxId(null);
        lotRepository.save(lot);
    }

    @Test
    @Order(2)
    public void testSendErrorTypeMessage() throws Exception {

        // Publish a test message to the queue
        Channel channel = this.setup();
        channel.basicPublish(EXCHANGE, QUEUE_NAME, null, ERROR_TYPE_MESSAGE.getBytes());

        // Assert that the message was received incorrectly
        assertDoesNotThrow(() -> listener.consume(ERROR_TYPE_MESSAGE,channel,1));
        Optional<Lot> value = lotRepository.findByTxId(txId);
        assertEquals(Optional.empty(), value);
    }

    @Test
    @Order(3)
    public void testSendErrorMessage() throws Exception {

        // Publish a test message to the queue
        Channel channel = this.setup();
        channel.basicPublish(EXCHANGE, QUEUE_NAME, null, ERROR_MESSAGE.getBytes());

        // Assert that the message was received incorrectly
        assertThrows(Exception.class, () -> listener.consume(ERROR_MESSAGE,channel,1));
    }
}