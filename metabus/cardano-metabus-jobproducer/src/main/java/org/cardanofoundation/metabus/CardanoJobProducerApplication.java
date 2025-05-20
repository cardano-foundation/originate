package org.cardanofoundation.metabus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan("org.cardanofoundation.metabus.infrastructure.config")
@EnableScheduling
public class CardanoJobProducerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CardanoJobProducerApplication.class, args);
    }
}
