package org.cardanofoundation.proofoforigin.api.business.impl;


import org.cardanofoundation.proofoforigin.api.constants.Role;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.UserCreateDto;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.KeycloakUserBody;
import org.cardanofoundation.proofoforigin.api.exceptions.BolnisiPilotException;
import org.jboss.resteasy.core.ServerResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class KeycloakCallServiceTest {

    private RealmResource realmResource;


    private KeycloakCallServiceImpl keycloakCallService;


    @BeforeEach
    public void init() {
        realmResource = mock(RealmResource.class);
        keycloakCallService = new KeycloakCallServiceImpl(realmResource);
    }

    @Test
    public void createUserFailKeycloakError() {
        KeycloakUserBody userBody = getUserBody();

        UsersResource mock1 = mock(UsersResource.class);
        when(realmResource.users()).thenReturn(mock1);
        RolesResource roleMock = mock(RolesResource.class);
        when(realmResource.roles()).thenReturn(roleMock);
        when(roleMock.list()).thenReturn(List.of(getRoleRepresentation()));
        when(mock1.create(any())).thenReturn(fail());

        Assertions.assertThrows(BolnisiPilotException.class, () -> keycloakCallService.createUser(userBody, Role.WINERY));
        Assertions.assertThrows(BolnisiPilotException.class, () -> keycloakCallService.createUser(userBody, Role.ADMIN));
        Assertions.assertThrows(BolnisiPilotException.class, () -> keycloakCallService.createUser(userBody, Role.DATA_PROVIDER));
    }

    @Test
    public void createUserFailKeycloakErrorNotRole() {
        KeycloakUserBody userBody = getUserBody();

        RolesResource roleMock = mock(RolesResource.class);
        when(realmResource.roles()).thenReturn(roleMock);
        List<RoleRepresentation> list = new ArrayList<>();
        when(roleMock.list()).thenReturn(list);

        Assertions.assertThrows(BolnisiPilotException.class, () -> keycloakCallService.createUser(userBody, Role.WINERY));
        Assertions.assertThrows(BolnisiPilotException.class, () -> keycloakCallService.createUser(userBody, Role.ADMIN));
        Assertions.assertThrows(BolnisiPilotException.class, () -> keycloakCallService.createUser(userBody, Role.DATA_PROVIDER));
    }

    @Test
    public void  createUserRoleWinerySendMailError() {
        KeycloakUserBody userBody = getUserBody();
        UsersResource mock1 = mock(UsersResource.class);
        when(realmResource.users()).thenReturn(mock1);

        when(mock1.create(any())).thenReturn(success());
        UserRepresentation userRepresentation = getUserRepresentation();
        when(mock1.search(any())).thenReturn(List.of(userRepresentation));
        UserResource usersGet = mock(UserResource.class);
        when(mock1.get(any())).thenReturn(usersGet);
        RolesResource roleMock = mock(RolesResource.class);
        when(realmResource.roles()).thenReturn(roleMock);
        when(roleMock.list()).thenReturn(getListRoleRepresentation());


//        mock role
        RoleMappingResource roleMappingResource = mock(RoleMappingResource.class);
        when(usersGet.roles()).thenReturn(roleMappingResource);
        RoleScopeResource roleScopeResource = mock(RoleScopeResource.class);
        when(usersGet.roles().realmLevel()).thenReturn(roleScopeResource);


        doThrow(new BadRequestException()).when(usersGet).executeActionsEmail(any(), any(), anyList());

        UserCreateDto userCreateDto = keycloakCallService.createUser(userBody, Role.WINERY);
        Assertions.assertEquals(userCreateDto.getEmail(), userBody.getEmail());
        Assertions.assertEquals(userCreateDto.getId(), userRepresentation.getId());
        Assertions.assertEquals(userCreateDto.getIsSendMail(), false);


        // role admin
        UserCreateDto createDtoAdmin = keycloakCallService.createUser(userBody, Role.ADMIN);
        Assertions.assertEquals(createDtoAdmin.getEmail(), userBody.getEmail());
        Assertions.assertEquals(createDtoAdmin.getIsSendMail(), false);

        // role DataProvider
        KeycloakUserBody userBodyDataProvider = getUserBody();
        UserCreateDto createDtoDataProvider = keycloakCallService.createUser(userBodyDataProvider, Role.DATA_PROVIDER);
        Assertions.assertEquals(createDtoDataProvider.getEmail(), userBody.getEmail());
        Assertions.assertEquals(createDtoDataProvider.getIsSendMail(), false);
    }

    @Test
    public void createUserSendMailSuccess() {
        KeycloakUserBody userBody = getUserBody();
        UsersResource mock1 = mock(UsersResource.class);
        when(realmResource.users()).thenReturn(mock1);
        when(mock1.create(any())).thenReturn(success());
        UserRepresentation userRepresentation = getUserRepresentation();
        when(mock1.search(any())).thenReturn(List.of(userRepresentation));
        UserResource usersGet = mock(UserResource.class);
        when(mock1.get(any())).thenReturn(usersGet);
        RolesResource roleMock = mock(RolesResource.class);
        when(realmResource.roles()).thenReturn(roleMock);
        when(roleMock.list()).thenReturn(getListRoleRepresentation());
//        mock role
        RoleMappingResource roleMappingResource = mock(RoleMappingResource.class);
        when(usersGet.roles()).thenReturn(roleMappingResource);
        RoleScopeResource roleScopeResource = mock(RoleScopeResource.class);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

        //role winery
        UserCreateDto userCreateDto = keycloakCallService.createUser(userBody, Role.WINERY);
        Assertions.assertEquals(userCreateDto.getEmail(), userBody.getEmail());
        Assertions.assertEquals(userCreateDto.getId(), userRepresentation.getId());
        Assertions.assertEquals(userCreateDto.getIsSendMail(), true);


        // role admin
        UserCreateDto createDtoAdmin = keycloakCallService.createUser(userBody, Role.ADMIN);
        Assertions.assertEquals(createDtoAdmin.getEmail(), userBody.getEmail());
        Assertions.assertEquals(createDtoAdmin.getIsSendMail(), true);

        // role DataProvider
        KeycloakUserBody userBodyDataProvider = getUserBody();
        UserCreateDto createDtoDataProvider = keycloakCallService.createUser(userBodyDataProvider, Role.DATA_PROVIDER);
        Assertions.assertEquals(createDtoDataProvider.getEmail(), userBody.getEmail());
        Assertions.assertEquals(createDtoDataProvider.getIsSendMail(), true);

    }

    @Test
    public void updateTermsFail() {
        String keyCloakId = "test";
        String terms = "app";

        UsersResource mock = mock(UsersResource.class);
        when(realmResource.users()).thenReturn(mock);

        UserResource mock2 = mock(UserResource.class);
        when(mock.get(keyCloakId)).thenReturn(mock2);
        when(mock2.toRepresentation()).thenThrow(new NotFoundException());
        Assertions.assertThrows(BolnisiPilotException.class, () -> {
            keycloakCallService.updateTermsKeyCloak(terms, keyCloakId);
        });
    }

    @Test
    public void updateTermsSuccess() {
        String keyCloakId = "test";
        String terms = "app";

        UsersResource mock = mock(UsersResource.class);
        when(realmResource.users()).thenReturn(mock);

        UserResource mock2 = mock(UserResource.class);
        when(mock.get(keyCloakId)).thenReturn(mock2);
        UserRepresentation representation = new UserRepresentation();
        when(mock2.toRepresentation()).thenReturn(representation);
        keycloakCallService.updateTermsKeyCloak(terms,keyCloakId);
        verify(mock2, times(1)).update(representation);
    }
    private Response fail() {
        ServerResponse serverResponse = new ServerResponse();
        serverResponse.setStatus(400);
        return serverResponse;
    }

    private Response success() {
        ServerResponse serverResponse = new ServerResponse();
        serverResponse.setStatus(201);
        return serverResponse;
    }

    private KeycloakUserBody getUserBody() {
        KeycloakUserBody userBody = new KeycloakUserBody();
        userBody.setEmail("test@gmail.com");
        userBody.setName("name_test");
        return userBody;
    }

    private UserRepresentation getUserRepresentation() {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEmail("test@gmail.com");
        userRepresentation.setId("test");
        return userRepresentation;
    }

    private RoleRepresentation getRoleRepresentation() {
        RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setName("WINERY");
        roleRepresentation.setId("test");
        return roleRepresentation;
    }

    private List<RoleRepresentation> getListRoleRepresentation() {
        List<RoleRepresentation> representations = new ArrayList<>();
        RoleRepresentation roleWinery = new RoleRepresentation();
        roleWinery.setName(Role.WINERY.name());
        roleWinery.setId("WINERY");
        representations.add(roleWinery);
        RoleRepresentation roleAdmin = new RoleRepresentation();
        roleAdmin.setName(Role.ADMIN.name());
        roleAdmin.setId("ADMIN");

        representations.add(roleAdmin);
        RoleRepresentation roleData = new RoleRepresentation();
        roleData.setName(Role.DATA_PROVIDER.name());
        roleData.setId("DATA_PROVIDER");

        representations.add(roleData);
        return representations;
    }


}
