package org.cardanofoundation.metabus.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.service.BatchConsumptionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.bloxbean.cardano.client.exception.CborSerializationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class JobListener {
    // Service
    private final BatchConsumptionService batchConsumptionService;

    /**
     * Consume jobs
     *
     * @param consumerRecord message topic
     */
    @KafkaListener(
            topics = "${kafka.topics.jobSchedule.name}"
    )
    public void consume(ConsumerRecord<String, Job> consumerRecord,
                        Acknowledgment acknowledgment) {
        try {
            // Make only one thread can use a instance of BatchConsumptionService
            // To ensure the consuming phase (based on time or based on tx max size) is concurrently processed
            synchronized (batchConsumptionService) {
                batchConsumptionService.consumeBasedOnTxMaxSize(consumerRecord);
                acknowledgment.acknowledge();
            }
        } catch (CborSerializationException e) {
            log.error(">>> Transaction serialization error: {}", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }
}