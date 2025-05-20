package org.cardanofoundation.proofoforigin.api.security.properties;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "cardano-bolnisipilot-api.security")
public class EndpointAuthorizationProperties {
    List<Authorization> endpointAuthorizations;
    Cors cors;

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Authorization {
        String urlPattern;
        String method;
        List<String> roles;
        List<String> device;
    }

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Cors {
        List<String> allowedOrigins;
        List<String> allowedMethods;
        List<String> allowedHeaders;
    }
}
