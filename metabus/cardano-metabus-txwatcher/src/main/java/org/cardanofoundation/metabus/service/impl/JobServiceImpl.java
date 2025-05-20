package org.cardanofoundation.metabus.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.configuration.CardanoMetabusTxwatcherProperties;
import org.cardanofoundation.metabus.service.JobService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableConfigurationProperties(value = {CardanoMetabusTxwatcherProperties.class})
@Slf4j
public class JobServiceImpl implements JobService {
    RabbitTemplate rabbitTemplate;
    CardanoMetabusTxwatcherProperties cardanoMetabusTxwatcherProperties;

    @Override
    public void pushJobToRabbit(Job job) {
        try {
            rabbitTemplate.convertAndSend("job",
                    getRoutingKeyFromJobSubType(job.getBusinessData().getSubType()), job);
            log.info("[cardano-metabus-txwatcher] Successfully push job with id: {}", job.getId());
        } catch (RuntimeException e) {
            log.error("[cardano-metabus-txwatcher] failed to push job with id: {}", job.getId());
            throw e;
        }
    }

    /**
     * Get routing key to push the job to the right queue.
     *
     * @param subType
     * @return
     */
    private String getRoutingKeyFromJobSubType(String subType) {
        try {
            Map<String, String> subTypeRoutingKeyMapping =
                    cardanoMetabusTxwatcherProperties.getRabbitmq().getSubTypeRoutingKeyMapping();
            String routingKey = subTypeRoutingKeyMapping.get(subType);
            List<CardanoMetabusTxwatcherProperties.Binding> bindings =
                    cardanoMetabusTxwatcherProperties.getRabbitmq().getBindings();

            return bindings.stream().map(CardanoMetabusTxwatcherProperties.Binding::getRoutingKey)
                    .filter(routingKey::equals)
                    .findFirst()
                    .get();
        } catch (RuntimeException e) {
            log.error("[cardano-metabus-txwatcher] failed get to get the routing key from job type: {}" +
                    ", check the prefix of job type match the routing key that rabbitmq specify in config", subType);
            throw e;
        }
    }
}
