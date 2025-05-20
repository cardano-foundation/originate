package org.cardanofoundation.proofoforigin.api.business;

import org.cardanofoundation.proofoforigin.api.constants.Role;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.KeycloakUserBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BaseResponse;

public interface UserKeycloak {
    <T extends KeycloakUserBody> BaseResponse<Void> createUser(T user, Role role);

    void updateTermsUser();
}
