package org.cardanofoundation.proofoforigin.api.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.cardanofoundation.proofoforigin.api.constants.Constants;
import org.cardanofoundation.proofoforigin.api.constants.Role;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.RequestMatcherTermDto;
import org.cardanofoundation.proofoforigin.api.exceptions.OriginatePilotErrors;
import org.cardanofoundation.proofoforigin.api.exceptions.OriginatePilotException;
import org.cardanofoundation.proofoforigin.api.security.properties.EndpointAuthorizationProperties;
import org.cardanofoundation.proofoforigin.api.utils.SecurityContextHolderUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.List;


@Component
public class TermsAuthenticationFilter extends GenericFilterBean {

    private final EndpointAuthorizationProperties endpointAuthorizationProperties;

    private final SecurityContextHolderUtil securityContextHolderUtil;

    public TermsAuthenticationFilter(EndpointAuthorizationProperties endpointAuthorizationProperties,
                                     SecurityContextHolderUtil securityContextHolderUtil) {
        this.endpointAuthorizationProperties = endpointAuthorizationProperties;
        this.securityContextHolderUtil = securityContextHolderUtil;
    }

    @Value("${key-cloak-config.clientIdFontEnd}")
    private String webClientId;


    @Value("${key-cloak-config.clientIdApp}")
    private String appClientApp;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // Skip filter for open endpoints with no auth
        if (!securityContextHolderUtil.hasAuthenticationHeader()) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        List<String> listRole = securityContextHolderUtil.getListRoles();
        if (!listRole.contains(Role.NWA.name())) {
            String clientId = securityContextHolderUtil.getClientId();

            List<RequestMatcherTermDto> requestMatchersApp = endpointAuthorizationProperties.getEndpointAuthorizations().stream()
                    .filter(authorization -> (authorization.getDevice().contains(Constants.DRIVE.APP) && !authorization.getDevice().contains(Constants.DRIVE.WEB)))
                    .map(authorization -> {
                        RequestMatcherTermDto termDto = new RequestMatcherTermDto();
                        termDto.setRequestMatcher(new AntPathRequestMatcher(authorization.getUrlPattern()));
                        termDto.setMethod(authorization.getMethod());
                        return termDto;
                    }).toList();

            List<RequestMatcherTermDto> requestMatchersWeb = endpointAuthorizationProperties.getEndpointAuthorizations().stream()
                    .filter(authorization -> (authorization.getDevice().contains(Constants.DRIVE.WEB) && !authorization.getDevice().contains(Constants.DRIVE.APP)))
                    .map(authorization -> {
                        RequestMatcherTermDto termDto = new RequestMatcherTermDto();
                        termDto.setRequestMatcher(new AntPathRequestMatcher(authorization.getUrlPattern()));
                        termDto.setMethod(authorization.getMethod());
                        return termDto;
                    }).toList();

            List<RequestMatcherTermDto> requestMatchersWebAndApp = endpointAuthorizationProperties.getEndpointAuthorizations().stream()
                    .filter(authorization -> (authorization.getDevice().contains(Constants.DRIVE.WEB) && authorization.getDevice().contains(Constants.DRIVE.APP)))
                    .map(authorization -> {
                        RequestMatcherTermDto termDto = new RequestMatcherTermDto();
                        termDto.setRequestMatcher(new AntPathRequestMatcher(authorization.getUrlPattern()));
                        termDto.setMethod(authorization.getMethod());
                        return termDto;
                    }).toList();
            checkTerms(httpRequest, requestMatchersApp, Constants.TERMS.APP_TERMS);
            checkTerms(httpRequest, requestMatchersWeb, Constants.TERMS.WEB_TERMS);
            checkTermsClient(httpRequest, requestMatchersWebAndApp, clientId);

        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private void checkTerms(HttpServletRequest httpRequest, List<RequestMatcherTermDto> requestMatchersApp, String terms) {
        boolean isMathApp = requestMatchersApp.stream().anyMatch(requestMatcherTermDto -> requestMatcherTermDto.getRequestMatcher().matches(httpRequest) && httpRequest.getMethod().equals(requestMatcherTermDto.getMethod()));
        if (isMathApp) {
            Boolean utilTerms = securityContextHolderUtil.getTerms(terms);
            if (!utilTerms) {
                throw new OriginatePilotException(OriginatePilotErrors.ACCOUNT_NOT_TERMS);
            }
        }
    }

    private void checkTermsClient(HttpServletRequest httpRequest, List<RequestMatcherTermDto> requestMatchersApp, String clientId) {
        if (clientId.equals(webClientId)) {
            checkTerms(httpRequest, requestMatchersApp, Constants.TERMS.WEB_TERMS);
        } else if (clientId.equals(appClientApp)) {
            checkTerms(httpRequest, requestMatchersApp, Constants.TERMS.APP_TERMS);
        } else {
            throw new OriginatePilotException(OriginatePilotErrors.ACCOUNT_NOT_TERMS);
        }
    }
}
