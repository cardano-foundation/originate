package org.cardanofoundation.proofoforigin.api.configuration;

import lombok.RequiredArgsConstructor;
import org.cardanofoundation.proofoforigin.api.configuration.properties.ScanTrustProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final ScanTrustProperties scanTrustProperties;

    @Bean
    public WebClient scanTrust() {
        return WebClient.builder()
                .baseUrl(scanTrustProperties.getDomain())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "UAT " + scanTrustProperties.getUatToken())
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().followRedirect(true)
                ))
                .build();
    }
}
