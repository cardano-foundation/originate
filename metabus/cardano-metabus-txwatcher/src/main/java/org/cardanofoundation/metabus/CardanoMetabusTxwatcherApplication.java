package org.cardanofoundation.metabus;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories
@EntityScan("org.cardanofoundation.metabus.common.entities")
public class CardanoMetabusTxwatcherApplication {
    public static void main(String[] args) {
        SpringApplication.run(CardanoMetabusTxwatcherApplication.class, args);
    }
}
