package org.cardanofoundation.proofoforigin.api.security.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeyCloakConfig {
    @Value("${key-cloak-config.bolnisiKeyCloakConfig.url}")
    private String url;

    @Value("${key-cloak-config.bolnisiKeyCloakConfig.realm}")
    private String realm;


    @Value("${key-cloak-config.bolnisiKeyCloakConfig.secret}")
    private String secret;

    @Value("${key-cloak-config.bolnisiKeyCloakConfig.clientId}")
    private String client;

    @Bean
    public Keycloak config() {
        return KeycloakBuilder.builder().realm(realm)
                .serverUrl(url)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientSecret(secret)
                .clientId(client).build();
    }

    @Bean(name = "BolnisiPilotApplication")
    public RealmResource configBolnisi(@Autowired Keycloak keycloak) {
        return keycloak.realm("BolnisiPilotApplication");
    }

}
