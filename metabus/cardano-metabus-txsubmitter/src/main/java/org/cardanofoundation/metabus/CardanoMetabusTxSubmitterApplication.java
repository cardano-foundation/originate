package org.cardanofoundation.metabus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan( {"org.cardanofoundation.metabus.common.*", "org.cardanofoundation.*"})
public class CardanoMetabusTxSubmitterApplication {
    public static void main(String[] args) {
        SpringApplication.run(CardanoMetabusTxSubmitterApplication.class, args);
    }
}
