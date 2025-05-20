package org.cardanofoundation.proofoforigin.api.business;

import org.cardanofoundation.proofoforigin.api.constants.Role;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.UserCreateDto;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.KeycloakUserBody;

public interface KeycloakCallService {
    <T extends KeycloakUserBody> UserCreateDto createUser(T user, Role role);

    void updateTermsKeyCloak(String terms, String userId);
}
