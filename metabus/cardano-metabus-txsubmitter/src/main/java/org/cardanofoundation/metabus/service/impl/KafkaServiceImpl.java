package org.cardanofoundation.metabus.service.impl;

import org.cardanofoundation.metabus.service.QueueingService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * The Queueing Service Implementation:
 * - Platform: Kafka
 * </p>
 *
 * @author (Sotatek) joey.dao
 * @version 0.01
 * @category Service
 * @see QueueingService
 * @since 2023/08
 */
@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class KafkaServiceImpl implements QueueingService {

    /**
     * Kafka Template.
     */
    KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public <T> void sendMessage(final T message, final String topic, final String key) {
        try {
            kafkaTemplate.send(topic, key, message).join();
            log.info("Send message {} to topic {} successfully", message, topic);
        } catch (final Exception ex) {
            log.error("Send message {} to topic {} failed", message, topic, ex);
            throw new RuntimeException("Error to pushing message to Kafka", ex);
        }
    }
}
