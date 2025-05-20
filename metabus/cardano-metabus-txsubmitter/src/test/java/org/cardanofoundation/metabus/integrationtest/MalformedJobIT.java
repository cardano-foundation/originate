package org.cardanofoundation.metabus.integrationtest;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.cardanofoundation.metabus.integrationtest.kafka.KafkaConsumer;
import org.cardanofoundation.metabus.integrationtest.kafka.KafkaProducer;
import org.cardanofoundation.metabus.integrationtest.kafka.NotJob;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Objects;

@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9093", "port=9093"})
public class MalformedJobIT extends BaseIntegrationTest {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private KafkaConsumer kafkaConsumer;


    @Test
    void testIsMalformedMessageInDeadQueue() throws IOException {
        NotJob notJob = new NotJob("job_malformed");
        kafkaProducer.send("test.job.schedule", notJob);
        long startTime = System.currentTimeMillis();
        long timeout = 10000; // 10 seconds in milliseconds
        ConsumerRecord<String, Object> consumerRecordFromDeadLetter = null;

        while (Objects.isNull(consumerRecordFromDeadLetter)  && System.currentTimeMillis() - startTime < timeout) {
            consumerRecordFromDeadLetter = kafkaConsumer.getConsumerRecordFromDeadLetter().get();
        }

        Assertions.assertNotNull(consumerRecordFromDeadLetter);
    }
}
