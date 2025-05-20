package org.cardanofoundation.metabus.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.cardanofoundation.metabus.controllers.BaseResponse;
import org.cardanofoundation.metabus.exceptions.MetabusErrors;
import org.cardanofoundation.metabus.security.properties.MetabusSecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(value = {MetabusSecurityProperties.class})
public class SecurityConfig {
    JwtAuthenticationConverter jwtAuthenticationConverter;
    Converter<Jwt, Collection<GrantedAuthority>> jwtConverter;
    MetabusSecurityProperties metabusSecurityProperties;
    ObjectMapper objectMapper;

    public SecurityConfig(Converter<Jwt, Collection<GrantedAuthority>> jwtConverter,
                          MetabusSecurityProperties metabusSecurityProperties, ObjectMapper objectMapper) {
        this.jwtConverter = jwtConverter;
        this.metabusSecurityProperties = metabusSecurityProperties;

        jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtConverter);
        this.objectMapper = objectMapper;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // authorize endpoint declared in application.yml
        List<MetabusSecurityProperties.Authorization> endpointAuthorizations =
                metabusSecurityProperties.getEndpointAuthorizations();
        for (MetabusSecurityProperties.Authorization authorization : endpointAuthorizations) {
            String urlPattern = authorization.getUrlPattern();
            String httpMethod = authorization.getMethod();
            String[] roles = authorization.getRoles().toArray(String[]::new);
            if (Objects.isNull(httpMethod)) {
                http.authorizeHttpRequests()
                        .requestMatchers(urlPattern)
                        .hasAnyRole(roles);
            }

            if (Objects.nonNull(httpMethod)) {
                http.authorizeHttpRequests()
                        .requestMatchers(HttpMethod.valueOf(httpMethod), urlPattern)
                        .hasAnyRole(roles);
            }
        }

        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(jwtAuthenticationConverter)
                .and()
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    String json = objectMapper.writeValueAsString(BaseResponse.ofFailed(MetabusErrors.UNAUTHORIZED,
                            "Invalid or expired token. Please obtain a new token by the keycloak login api"));
                    response.getWriter().write(json);
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    String json = objectMapper.writeValueAsString(BaseResponse.ofFailed(MetabusErrors.FORBIDDEN,
                            "You do not have permission to access this resource."));
                    response.getWriter().write(json);
                });

        return http.build();
    }
}
