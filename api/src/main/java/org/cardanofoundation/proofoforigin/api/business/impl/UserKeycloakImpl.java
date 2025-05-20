package org.cardanofoundation.proofoforigin.api.business.impl;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.proofoforigin.api.business.KeycloakCallService;
import org.cardanofoundation.proofoforigin.api.business.UserKeycloak;
import org.cardanofoundation.proofoforigin.api.constants.Constants;
import org.cardanofoundation.proofoforigin.api.constants.Role;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.UserCreateDto;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.KeycloakUserBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BaseResponse;
import org.cardanofoundation.proofoforigin.api.exceptions.BolnisiPilotError;
import org.cardanofoundation.proofoforigin.api.exceptions.BolnisiPilotErrors;
import org.cardanofoundation.proofoforigin.api.exceptions.BolnisiPilotException;
import org.cardanofoundation.proofoforigin.api.utils.SecurityContextHolderUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
@Slf4j
public class UserKeycloakImpl implements UserKeycloak {

    private final KeycloakCallService keycloakCallService;

    private final SecurityContextHolderUtil securityContextHolderUtil;

    @Value("${key-cloak-config.clientIdFontEnd}")
    private String clientIdFontEnd;

    @Value("${key-cloak-config.clientIdApp}")
    private String clientIdApp;

    public UserKeycloakImpl(KeycloakCallService keycloakCallService, SecurityContextHolderUtil securityContextHolderUtil) {
        this.keycloakCallService = keycloakCallService;
        this.securityContextHolderUtil = securityContextHolderUtil;
    }

    @Override
    public <T extends KeycloakUserBody> BaseResponse<Void> createUser(T user, Role role) {
        if (!Constants.emailFormatCorrect(user.getEmail())) {
            throw new BolnisiPilotException(new BolnisiPilotError(HttpStatus.BAD_REQUEST.value(), "Invalid email.", HttpStatus.BAD_REQUEST));
        }
        UserCreateDto userCreateDto = keycloakCallService.createUser(user, role);
        if (userCreateDto.getIsSendMail()) {
            return BaseResponse.ofSucceededCreate(Constants.SEND_MAIL.SEND_MAIL_WINERY_ID_SUCCESS);
        } else {
            return BaseResponse.ofSucceededCreate(Constants.SEND_MAIL.SEND_MAIL_WINERY_ID_FAIL);
        }
    }

    @Override
    public void updateTermsUser() {
        String userId = securityContextHolderUtil.getKeyCloakUserId();
        String clientId = securityContextHolderUtil.getClientId();
        String terms = getTerms.apply(clientId);
        if (terms == null) {
            throw new BolnisiPilotException(BolnisiPilotErrors.ACCOUNT_NOT_TERMS);
        }
        keycloakCallService.updateTermsKeyCloak(terms, userId);
    }

    private final Function<String, String> getTerms = s -> {
        if (s.equals(clientIdFontEnd)) {
            return Constants.TERMS.WEB_TERMS;
        }
        if (s.equals(clientIdApp)) {
            return Constants.TERMS.APP_TERMS;
        }
        return null;
    };
}
