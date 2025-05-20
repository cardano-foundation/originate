package org.cardanofoundation.metabus.integrationtest.rabbitmq;

import lombok.Getter;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

@Service
@Getter
public class Receiver {
    AtomicReference<Job> jobAtomicReference = new AtomicReference<>();

    @RabbitListener(queues = {"bolnisi"}, messageConverter = "jsonMessageConverter")
    public void consume(Job job){
        jobAtomicReference = new AtomicReference<>(job);
    }

}
