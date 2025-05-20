package org.cardanofoundation.proofoforigin.api.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.cardanofoundation.proofoforigin.api.exceptions.BolnisiPilotException;
import org.cardanofoundation.proofoforigin.api.security.properties.EndpointAuthorizationProperties;
import org.cardanofoundation.proofoforigin.api.utils.SecurityContextHolderUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class TermsAuthenticationFilterTest {
    private TermsAuthenticationFilter filter;
    public EndpointAuthorizationProperties endpointAuthorizationProperties;
    public SecurityContextHolderUtil securityContextHolderUtil;

    private final String webClientId = "frontend_dashboard";

    private final String appClientApp = "mobile-scan-app";

    @BeforeEach
    public void init() {
        endpointAuthorizationProperties = mock(EndpointAuthorizationProperties.class);
        securityContextHolderUtil = mock(SecurityContextHolderUtil.class);
        filter = new TermsAuthenticationFilter(endpointAuthorizationProperties, securityContextHolderUtil);
        setPrivateField(filter, "webClientId", webClientId);
        setPrivateField(filter, "appClientApp", appClientApp);

    }


    @Test
    public void apiWebSuss() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setServletPath("/api/v1/scm/lot/1234/approve");
        req.setMethod("PUT");
        FilterChain filterChain = mock(FilterChain.class);

        when(endpointAuthorizationProperties.getEndpointAuthorizations()).thenReturn(getListAuthorization());
        when(securityContextHolderUtil.hasAuthenticationHeader()).thenReturn(true);
        MockHttpServletResponse res = new MockHttpServletResponse();
        when(securityContextHolderUtil.getClientId()).thenReturn(webClientId);
        when(securityContextHolderUtil.getTerms("web_terms")).thenReturn(true);

        filter.doFilter(req, res, filterChain);
        verify(filterChain, times(1)).doFilter(eq(req), eq(res));

    }

    @Test
    public void apiAppSuss() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setServletPath("/api/v1/certs/1234");
        req.setMethod("GET");
        FilterChain filterChain = mock(FilterChain.class);
        when(endpointAuthorizationProperties.getEndpointAuthorizations()).thenReturn(getListAuthorization());
        when(securityContextHolderUtil.hasAuthenticationHeader()).thenReturn(true);
        MockHttpServletResponse res = new MockHttpServletResponse();
        when(securityContextHolderUtil.getClientId()).thenReturn(appClientApp);
        when(securityContextHolderUtil.getTerms("app_terms")).thenReturn(true);
        filter.doFilter(req, res, filterChain);
        verify(filterChain, times(1)).doFilter(eq(req), eq(res));
    }

    @Test
    public void roleNWA() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setServletPath("/api/v1/certs/1234");
        req.setMethod("GET");
        FilterChain filterChain = mock(FilterChain.class);
        MockHttpServletResponse res = new MockHttpServletResponse();
        when(securityContextHolderUtil.getListRoles()).thenReturn(List.of("NWA"));
        when(securityContextHolderUtil.hasAuthenticationHeader()).thenReturn(true);
        filter.doFilter(req, res, filterChain);
        verify(filterChain, times(1)).doFilter(eq(req), eq(res));
    }


    @Test
    public void apiWebFailNotTerms() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setServletPath("/api/v1/scm/lot/1234/approve");
        req.setMethod("PUT");
        FilterChain filterChain = mock(FilterChain.class);

        when(endpointAuthorizationProperties.getEndpointAuthorizations()).thenReturn(getListAuthorization());
        when(securityContextHolderUtil.hasAuthenticationHeader()).thenReturn(true);
        MockHttpServletResponse res = new MockHttpServletResponse();
        when(securityContextHolderUtil.getClientId()).thenReturn(webClientId);
        when(securityContextHolderUtil.getTerms("web_terms")).thenReturn(false);

        Assertions.assertThrows(BolnisiPilotException.class, () -> {
            filter.doFilter(req, res, filterChain);
        });

    }


    @Test
    public void apiAppFailNotTerms() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setServletPath("/api/v1/certs/1234");
        req.setMethod("GET");
        FilterChain filterChain = mock(FilterChain.class);

        when(endpointAuthorizationProperties.getEndpointAuthorizations()).thenReturn(getListAuthorization());
        when(securityContextHolderUtil.hasAuthenticationHeader()).thenReturn(true);
        MockHttpServletResponse res = new MockHttpServletResponse();
        when(securityContextHolderUtil.getClientId()).thenReturn(appClientApp);
        when(securityContextHolderUtil.getTerms("app_terms")).thenReturn(false);

        Assertions.assertThrows(BolnisiPilotException.class, () -> {
            filter.doFilter(req, res, filterChain);
        });

    }

    @Test
    public void apiAppAndWebNotTermsFail() {
        FilterChain filterChain = mock(FilterChain.class);
        when(endpointAuthorizationProperties.getEndpointAuthorizations()).thenReturn(getListAuthorization());

        //app call app
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setServletPath("/api/v1/user/winery");
        req.setMethod("GET");
        MockHttpServletResponse res = new MockHttpServletResponse();
        when(securityContextHolderUtil.getClientId()).thenReturn(appClientApp);
        when(securityContextHolderUtil.hasAuthenticationHeader()).thenReturn(true);
        when(securityContextHolderUtil.getTerms("app_terms")).thenReturn(false);
        Assertions.assertThrows(BolnisiPilotException.class, () -> {
            filter.doFilter(req, res, filterChain);
        });


        //web  call app
        MockHttpServletRequest req2 = new MockHttpServletRequest();
        req2.setServletPath("/api/v1/user/winery");
        req2.setMethod("GET");
        when(securityContextHolderUtil.getClientId()).thenReturn(webClientId);
        when(securityContextHolderUtil.getTerms("web_terms")).thenReturn(false);
        Assertions.assertThrows(BolnisiPilotException.class, () -> {
            filter.doFilter(req2, res, filterChain);
        });
    }


    @Test
    public void apiAppAndWebNotTermsSuccess() throws ServletException, IOException {
        FilterChain filterChain = mock(FilterChain.class);
        when(endpointAuthorizationProperties.getEndpointAuthorizations()).thenReturn(getListAuthorization());

        //app call app
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setServletPath("/api/v1/user/winery");
        req.setMethod("GET");
        MockHttpServletResponse res = new MockHttpServletResponse();
        when(securityContextHolderUtil.getClientId()).thenReturn(appClientApp);
        when(securityContextHolderUtil.hasAuthenticationHeader()).thenReturn(true);
        when(securityContextHolderUtil.getTerms("app_terms")).thenReturn(true);
        filter.doFilter(req, res, filterChain);
        verify(filterChain, times(1)).doFilter(eq(req), eq(res));


        //web  call app
        MockHttpServletRequest req2 = new MockHttpServletRequest();
        req2.setServletPath("/api/v1/user/winery");
        req2.setMethod("GET");
        when(securityContextHolderUtil.getClientId()).thenReturn(webClientId);
        when(securityContextHolderUtil.getTerms("web_terms")).thenReturn(true);
        filter.doFilter(req2, res, filterChain);
        verify(filterChain, times(1)).doFilter(eq(req2), eq(res));

    }
    @Test
    public void apiAppAndWebNotClient() {
        FilterChain filterChain = mock(FilterChain.class);
        when(securityContextHolderUtil.hasAuthenticationHeader()).thenReturn(true);
        when(endpointAuthorizationProperties.getEndpointAuthorizations()).thenReturn(getListAuthorization());

        //app call app
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setServletPath("/api/v1/user/winery");
        req.setMethod("GET");
        MockHttpServletResponse res = new MockHttpServletResponse();

        when(securityContextHolderUtil.getClientId()).thenReturn("test");
        Assertions.assertThrows(BolnisiPilotException.class, () -> {
            filter.doFilter(req, res, filterChain);
        });
    }

    @Test
    public void termsFilterOnlyAppliesIfAuthSupplied() throws ServletException, IOException {
        FilterChain filterChain = mock(FilterChain.class);
        when(securityContextHolderUtil.hasAuthenticationHeader()).thenReturn(false);
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setServletPath("/api/v1/pubkeys/1/v/0");
        req.setMethod("GET");
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(req, res, filterChain);
        verify(filterChain, times(1)).doFilter(eq(req), eq(res));
        verify(securityContextHolderUtil, never()).getListRoles();
    }

    public static void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Field privateField = target.getClass().getDeclaredField(fieldName);
            privateField.setAccessible(true);
            privateField.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<EndpointAuthorizationProperties.Authorization> getListAuthorization() {
        List<EndpointAuthorizationProperties.Authorization> list = new ArrayList<>();
        EndpointAuthorizationProperties.Authorization authorization = new EndpointAuthorizationProperties.Authorization();
        authorization.setUrlPattern("/api/v1/certs/**");
        authorization.setMethod("GET");
        authorization.setDevice(List.of("APP"));
        list.add(authorization);

        EndpointAuthorizationProperties.Authorization authorization1 = new EndpointAuthorizationProperties.Authorization();
        authorization1.setUrlPattern("/api/v1/scm/lot/*/approve");
        authorization1.setMethod("PUT");
        authorization1.setDevice(List.of("WEB"));
        list.add(authorization1);


        EndpointAuthorizationProperties.Authorization authorization2 = new EndpointAuthorizationProperties.Authorization();
        authorization2.setUrlPattern("/api/v1/user/*");
        authorization2.setMethod("GET");
        authorization2.setDevice(List.of("WEB", "APP"));
        list.add(authorization2);


        EndpointAuthorizationProperties.Authorization authorization3 = new EndpointAuthorizationProperties.Authorization();
        authorization3.setUrlPattern("/api/v1/certs/*/revoke");
        authorization3.setMethod("PUT");
        authorization3.setDevice(new ArrayList<>());
        list.add(authorization3);


        return list;
    }
}
