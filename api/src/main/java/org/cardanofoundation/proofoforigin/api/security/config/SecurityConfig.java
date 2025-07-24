package org.cardanofoundation.proofoforigin.api.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BaseResponse;
import org.cardanofoundation.proofoforigin.api.exceptions.OriginatePilotErrors;
import org.cardanofoundation.proofoforigin.api.security.filter.TermsAuthenticationFilter;
import org.cardanofoundation.proofoforigin.api.security.properties.EndpointAuthorizationProperties;
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
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(value = {EndpointAuthorizationProperties.class})
public class SecurityConfig {

    JwtAuthenticationConverter jwtAuthenticationConverter;
    Converter<Jwt, Collection<GrantedAuthority>> jwtConverter;
    EndpointAuthorizationProperties endpointAuthorizationProperties;
    ObjectMapper objectMapper;

    TermsAuthenticationFilter termsAuthenticationFilter;


    public SecurityConfig(
            Converter<Jwt, Collection<GrantedAuthority>> jwtConverter,
            EndpointAuthorizationProperties endpointAuthorizationProperties,
            ObjectMapper objectMapper, TermsAuthenticationFilter termsAuthenticationFilter) {
        this.jwtConverter = jwtConverter;
        this.endpointAuthorizationProperties = endpointAuthorizationProperties;
        this.jwtAuthenticationConverter = new JwtAuthenticationConverter();
        this.jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtConverter);
        this.objectMapper = objectMapper;
        this.termsAuthenticationFilter = termsAuthenticationFilter;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // authorize endpoint declared in application.yml
        List<EndpointAuthorizationProperties.Authorization> endpointAuthorizations =
                endpointAuthorizationProperties.getEndpointAuthorizations();
        for (EndpointAuthorizationProperties.Authorization authorization : endpointAuthorizations) {
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
                .cors()
                .configurationSource(corsConfigurationSource())
                .and()
                .authorizeHttpRequests()
                .requestMatchers("/api/v1/pubkeys/**")  // Only this API is open
                .permitAll()
                .and()
                .csrf()
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and()
                .oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(jwtAuthenticationConverter)
                .and()
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    String json = objectMapper.writeValueAsString(BaseResponse.ofFailed(OriginatePilotErrors.UNAUTHORIZED,
                            "Invalid or expired token. Please obtain a new token by the keycloak login api"));
                    response.getWriter().write(json);
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    if (response.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.setCharacterEncoding("UTF-8");
                        String json = objectMapper.writeValueAsString(BaseResponse.ofFailed(OriginatePilotErrors.ACCOUNT_NOT_TERMS,
                                "account not terms."));
                        response.getWriter().write(json);
                    } else {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.setCharacterEncoding("UTF-8");
                        String json = objectMapper.writeValueAsString(BaseResponse.ofFailed(OriginatePilotErrors.FORBIDDEN,
                                "You do not have permission to access this resource."));
                        response.getWriter().write(json);
                    }
                }).and().addFilterBefore(termsAuthenticationFilter, SwitchUserFilter.class);
        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        EndpointAuthorizationProperties.Cors cors = endpointAuthorizationProperties.getCors();
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(cors.getAllowedOrigins());
        configuration.setAllowedMethods(cors.getAllowedMethods());
        configuration.setAllowedHeaders(cors.getAllowedHeaders());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
