package org.cardanofoundation.proofoforigin.api.security.properties;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "key-cloak-config")
public class KeycloakTokenMetabusApiProperties {
    KeyCloakConfig metabusKeyCloakConfig;

    String apiMetabus;
    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class KeyCloakConfig {
        String clientId;
        String clientSecret;
        String scope;
        String grantYype;
        String url;
    }
}
