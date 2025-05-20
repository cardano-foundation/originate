package org.cardanofoundation.proofoforigin.api.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class SecurityContextHolderUtil {

    public boolean hasAuthenticationHeader() {
        Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();
        if (credentials instanceof String) {
            return !((String) credentials).isBlank();
        }
        return !Objects.isNull(credentials);
    }

    public Jwt getJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Jwt) authentication.getCredentials();
    }

    public String getKeyCloakUserId() {
        Map<String, Object> claim = getJwt().getClaims();
        return (String) claim.get("sub");
    }

    public List<String> getListRoles() {
        Map<String, Object> claim = getJwt().getClaims();
        Map<String, Object> realmAccess = (Map<String, Object>) claim.get("realm_access");
        return (List<String>) realmAccess.get("roles");
    }

    public Boolean getTerms(String key) {
        Map<String, Object> claim = getJwt().getClaims();
        return (Boolean) claim.getOrDefault(key, false);
    }

    public String getClientId() {
        Map<String, Object> claim = getJwt().getClaims();
        return (String) claim.getOrDefault("azp", null);
    }
}
