package org.cardanofoundation.metabus.integrationtest.kafka;

import org.cardanofoundation.metabus.common.offchain.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducer.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void send(String topic, Job job) {
        LOGGER.info("sending payload='{}' to topic='{}'", job, topic);
        kafkaTemplate.send(topic, job.getId().toString(), job);
    }

    public void send(String topic, Object object) {
        LOGGER.info("sending payload='{}' to topic='{}'", object, topic);
        kafkaTemplate.send(topic, object);
    }
}
