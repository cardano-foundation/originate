package org.cardanofoundation.metabus.configuration.webclient;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "cardano-metabus-txsubmitter")
public class WebClientProperties {
    List<WebClient> webclients;

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class WebClient {
        String beanName;
        String baseUrl;
        Map<String, String> headers;
    }
}
