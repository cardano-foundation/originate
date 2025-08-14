package org.cardanofoundation.metabus.unittest.service;

import org.cardanofoundation.metabus.common.offchain.BusinessData;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.configuration.CardanoMetabusTxwatcherProperties;
import org.cardanofoundation.metabus.service.impl.JobServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    RabbitTemplate rabbitTemplate;

    CardanoMetabusTxwatcherProperties cardanoMetabusTxwatcherProperties;

    JobServiceImpl jobService;
    @BeforeEach
    public void init() {
        new JobServiceImpl(rabbitTemplate, cardanoMetabusTxwatcherProperties);
        rabbitTemplate = mock(RabbitTemplate.class);
        cardanoMetabusTxwatcherProperties = mock(CardanoMetabusTxwatcherProperties.class);
        jobService = new JobServiceImpl(rabbitTemplate, cardanoMetabusTxwatcherProperties);
    }

    @Test
    void test_push_job_to_rabbit_success() {
        String routingKey = "originate";
        String typeJob = "conformityCert:georgianWine";
        String subType = "georgianWine";
        String queue = "originate";
        String exchange = "job";
        // prepare data
        Job job = new Job();
        BusinessData businessData = new BusinessData();
        businessData.setType(typeJob);
        businessData.setSubType(subType);
        job.setBusinessData(businessData);

        CardanoMetabusTxwatcherProperties.RabbitMQ rabbitMQConfig = new CardanoMetabusTxwatcherProperties.RabbitMQ();
        CardanoMetabusTxwatcherProperties.Binding bindingConfig = new CardanoMetabusTxwatcherProperties.Binding();
        bindingConfig.setRoutingKey(routingKey);
        bindingConfig.setQueue(queue);
        rabbitMQConfig.setSubTypeRoutingKeyMapping(Map.of("georgianWine","originate"));
        List<CardanoMetabusTxwatcherProperties.Binding> bindingList = List.of(bindingConfig);
        rabbitMQConfig.setBindings(bindingList);

        //mock
        doAnswer(invocation -> {
            String type = invocation.getArgument(1);
            Assertions.assertEquals(type, routingKey);
            return null;
        }).when(rabbitTemplate).convertAndSend(anyString(), anyString(), eq(job));
        when(cardanoMetabusTxwatcherProperties.getRabbitmq()).thenReturn(rabbitMQConfig);

        jobService.pushJobToRabbit(job);
        verify(rabbitTemplate, times(1)).convertAndSend(exchange, routingKey, job);
    }


    @Test
    void test_push_job_to_rabbit_failed() {
        String routingKey = "originate";
        String typeJob = "conformityCert:georgianWine";
        String subType = "georgianWine";
        String queue = "originate";
        String exchange = "job";
        // prepare data
        Job job = new Job();
        BusinessData businessData = new BusinessData();
        businessData.setType(typeJob);
        businessData.setSubType(subType);
        job.setBusinessData(businessData);

        CardanoMetabusTxwatcherProperties.RabbitMQ rabbitMQConfig = new CardanoMetabusTxwatcherProperties.RabbitMQ();
        CardanoMetabusTxwatcherProperties.Binding bindingConfig = new CardanoMetabusTxwatcherProperties.Binding();
        bindingConfig.setRoutingKey(routingKey);
        bindingConfig.setQueue(queue);
        rabbitMQConfig.setSubTypeRoutingKeyMapping(Map.of("georgianWine","originate"));
        List<CardanoMetabusTxwatcherProperties.Binding> bindingList = List.of(bindingConfig);
        rabbitMQConfig.setBindings(bindingList);

        //mock
        doThrow(RuntimeException.class)
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), eq(job));
        when(cardanoMetabusTxwatcherProperties.getRabbitmq()).thenReturn(rabbitMQConfig);

        assertThrows(RuntimeException.class, () -> jobService.pushJobToRabbit(job));
        verify(rabbitTemplate, times(1)).convertAndSend(exchange, routingKey, job);
    }

    @Test
    void test_get_routing_key_failed() {
        String routingKey = "type";
        String typeJob = "type_foo";
        // prepare data
        Job job = new Job();
        BusinessData businessData = new BusinessData();
        businessData.setType(typeJob);
        job.setBusinessData(businessData);

        CardanoMetabusTxwatcherProperties.RabbitMQ rabbitMQConfig = new CardanoMetabusTxwatcherProperties.RabbitMQ();
        CardanoMetabusTxwatcherProperties.Binding bindingConfig = new CardanoMetabusTxwatcherProperties.Binding();
        bindingConfig.setRoutingKey(routingKey);
        List<CardanoMetabusTxwatcherProperties.Binding> bindingList = List.of(bindingConfig);
        rabbitMQConfig.setBindings(bindingList);

        when(cardanoMetabusTxwatcherProperties.getRabbitmq()).thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class, () -> jobService.pushJobToRabbit(job));
        verify(rabbitTemplate, times(0)).convertAndSend(anyString(), anyString(), eq(job));
        verify(cardanoMetabusTxwatcherProperties, times(1)).getRabbitmq();
    }


}
