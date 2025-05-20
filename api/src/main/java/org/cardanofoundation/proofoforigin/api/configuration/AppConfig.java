package org.cardanofoundation.proofoforigin.api.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class AppConfig {

    @Component
    public static class CertificateVerificationConfig {

        @Value("${certificate.signature.verification.disabled}")
        public boolean signatureVerificationDisabled;

    }

}
