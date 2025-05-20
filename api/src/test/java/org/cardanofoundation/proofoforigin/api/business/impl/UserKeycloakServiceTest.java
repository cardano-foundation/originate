package org.cardanofoundation.proofoforigin.api.business.impl;

import org.cardanofoundation.proofoforigin.api.business.UserKeycloak;
import org.cardanofoundation.proofoforigin.api.constants.Constants;
import org.cardanofoundation.proofoforigin.api.constants.Role;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.UserCreateDto;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.KeycloakUserBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BaseResponse;
import org.cardanofoundation.proofoforigin.api.exceptions.BolnisiPilotException;
import org.cardanofoundation.proofoforigin.api.utils.SecurityContextHolderUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)

public class UserKeycloakServiceTest {
    private KeycloakCallServiceImpl keycloakCallService;

    private UserKeycloak userKeycloak;

    private SecurityContextHolderUtil securityContextHolderUtil;

    @BeforeEach
    public void init() {
        keycloakCallService = mock(KeycloakCallServiceImpl.class);
        securityContextHolderUtil = mock(SecurityContextHolderUtil.class);
        userKeycloak = new UserKeycloakImpl(keycloakCallService, securityContextHolderUtil);
    }

    @Test
    public void createUserSuccess() {
        when(keycloakCallService.createUser(any(), any())).thenReturn(sendMailSuccess());
        BaseResponse<Void> userCreateDto = userKeycloak.createUser(getUser(), Role.ADMIN);
        Assertions.assertEquals(userCreateDto.getMeta().getCode(), "201");
        Assertions.assertEquals(userCreateDto.getMeta().getMessage(), Constants.SEND_MAIL.SEND_MAIL_WINERY_ID_SUCCESS);
        //role DATA_PROVIDER
        BaseResponse<Void> userCreateDataProvider = userKeycloak.createUser(getUser(), Role.DATA_PROVIDER);
        Assertions.assertEquals(userCreateDataProvider.getMeta().getCode(), "201");
        Assertions.assertEquals(userCreateDataProvider.getMeta().getMessage(), Constants.SEND_MAIL.SEND_MAIL_WINERY_ID_SUCCESS);

    }


    @Test
    public void createUserFail() {
        when(keycloakCallService.createUser(any(), any())).thenReturn(sendMailFail());
        BaseResponse<Void> userCreateDto = userKeycloak.createUser(getUser(), Role.ADMIN);
        Assertions.assertEquals(userCreateDto.getMeta().getCode(), "201");
        Assertions.assertEquals(userCreateDto.getMeta().getMessage(), Constants.SEND_MAIL.SEND_MAIL_WINERY_ID_FAIL);

        //role DATA_PROVIDER
        BaseResponse<Void> userCreateDataProvider = userKeycloak.createUser(getUser(), Role.DATA_PROVIDER);
        Assertions.assertEquals(userCreateDataProvider.getMeta().getCode(), "201");
        Assertions.assertEquals(userCreateDataProvider.getMeta().getMessage(), Constants.SEND_MAIL.SEND_MAIL_WINERY_ID_FAIL);
    }

    @Test
    public void createUserEmailError() {
        List<String> list = new ArrayList<>();
        list.add("test@.com");
        list.add("test12345678901234567890123456789012345678901234567890123456789012345678901234567890@gmail.com.");
        list.add("test@ttess@gmail.com.");
        list.add("test@ttess@gmail.com..");
        list.add("test@ttess@..");
        KeycloakUserBody keycloakUserBody = getUser();
        for (String s : list) {
            Assertions.assertThrows(BolnisiPilotException.class, () -> {
                keycloakUserBody.setEmail(s);
                userKeycloak.createUser(keycloakUserBody, Role.ADMIN);
            });
        }

        for (String s : list) {
            Assertions.assertThrows(BolnisiPilotException.class, () -> {
                keycloakUserBody.setEmail(s);
                userKeycloak.createUser(keycloakUserBody, Role.DATA_PROVIDER);
            });
        }
    }

    private UserCreateDto sendMailFail() {
        UserCreateDto createDto = new UserCreateDto();
        createDto.setIsSendMail(false);
        return createDto;
    }

    private UserCreateDto sendMailSuccess() {
        UserCreateDto createDto = new UserCreateDto();
        createDto.setIsSendMail(true);
        return createDto;
    }

    private KeycloakUserBody getUser() {
        KeycloakUserBody userBody = new KeycloakUserBody();
        userBody.setEmail("test@gmail.com");
        userBody.setName("test");
        return userBody;
    }
}
