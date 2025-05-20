package org.cardanofoundation.metabus.integrationtest.kafka;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.cardanofoundation.metabus.common.offchain.ConfirmingTransaction;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * The Confirming Transaction Consumer For Integration Test
 * </p>
 *
 * @author (Sotatek) joey.dao
 * @version 0.01
 * @category Consumer
 * @since 2023/08
 */
@Component
@Getter
@Slf4j
public class ConfirmingTransactionConsumer {

    /**
     * The AtomicReference for thread-safe using
     */
    private AtomicReference<ConsumerRecord<String, ConfirmingTransaction>> consumerRecord = new AtomicReference<>();

    /**
     * <p>
     * The mocked consumer for confirming transaction
     * </p>
     * 
     * @param consumerRecord The record
     * @param acknowledgment The acknowledgement Object
     */
    @KafkaListener(topics = "${kafka.topics.confirmingTransaction.name}")
    public void receive(ConsumerRecord<String, ConfirmingTransaction> consumerRecord, Acknowledgment acknowledgment) {
        log.info("received payload='{}'", consumerRecord.toString());
        this.consumerRecord = new AtomicReference<>(consumerRecord);
        acknowledgment.acknowledge();
    }
}
