package org.cardanofoundation.metabus.integrationtest.kafka;

import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
@Getter
public class KafkaConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumer.class);

    private AtomicReference<ConsumerRecord<String, Object>> consumerRecordFromDeadLetter = new AtomicReference<>();
    @KafkaListener(topics = "test.job.dead.letter")
    public void receiveFromDeadLetter(ConsumerRecord<String, Object> consumerRecord, Acknowledgment acknowledgment) {
        LOGGER.info("received payload='{}'", consumerRecord.toString());
        this.consumerRecordFromDeadLetter = new AtomicReference<>(consumerRecord);
        acknowledgment.acknowledge();
    }
}
