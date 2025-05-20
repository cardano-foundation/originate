package org.cardanofoundation.proofoforigin.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class ProofOfOriginAPIApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProofOfOriginAPIApplication.class, args);
    }
}
