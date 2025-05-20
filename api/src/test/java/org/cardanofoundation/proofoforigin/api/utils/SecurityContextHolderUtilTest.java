package org.cardanofoundation.proofoforigin.api.utils;

import org.cardanofoundation.proofoforigin.api.constants.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityContextHolderUtilTest {

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Jwt jwt;

    @Mock
    Authentication authentication;

    @InjectMocks
    SecurityContextHolderUtil securityContextHolderUtil;


    @BeforeEach
    public void setup() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getJwt() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getCredentials()).thenReturn(jwt);

        Jwt actual = securityContextHolderUtil.getJwt();
        Assertions.assertEquals(jwt, actual);
    }

    @Test
    void getKeyCloakUserId() {
        String expectedKeyCloakUserId = "eb6d0a34-89e9-4dfd-bef2-93fb19139a34";
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", expectedKeyCloakUserId);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getCredentials()).thenReturn(jwt);
        when(jwt.getClaims()).thenReturn(claims);

        String actualKeyCloakUserId = securityContextHolderUtil.getKeyCloakUserId();

        Assertions.assertEquals(expectedKeyCloakUserId, actualKeyCloakUserId);
    }

    @Test
    void getListRoles() {
        Map<String, Object> realAccess = new HashMap<>();
        Map<String, Object> claims = new HashMap<>();
        List<String> expectedRoles = List.of(Role.ADMIN.toString(), Role.WINERY.toString());
        realAccess.put("roles", expectedRoles);
        claims.put("realm_access", realAccess);


        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getCredentials()).thenReturn(jwt);
        when(jwt.getClaims()).thenReturn(claims);

        List<String> actualRoles = securityContextHolderUtil.getListRoles();

        Assertions.assertEquals(expectedRoles, actualRoles);
    }
}