package org.cardanofoundation.proofoforigin.api.business.impl;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.proofoforigin.api.business.KeycloakCallService;
import org.cardanofoundation.proofoforigin.api.constants.Constants;
import org.cardanofoundation.proofoforigin.api.constants.Role;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.UserCreateDto;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.KeycloakUserBody;
import org.cardanofoundation.proofoforigin.api.exceptions.OriginatePilotError;
import org.cardanofoundation.proofoforigin.api.exceptions.OriginatePilotErrors;
import org.cardanofoundation.proofoforigin.api.exceptions.OriginatePilotException;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.*;

@Slf4j
@Service
public class KeycloakCallServiceImpl implements KeycloakCallService {
    private final RealmResource realmResource;


    @Value("${key-cloak-config.clientIdFontEnd}")
    private String client;

    @Value("${key-cloak-config.frontendUrl}")
    private String url;

    public KeycloakCallServiceImpl(@Qualifier("BolnisiPilotApplication") RealmResource realmResource) {
        this.realmResource = realmResource;
    }

    public <T extends KeycloakUserBody> UserCreateDto createUser(T user, Role role) {
        UserCreateDto userCreateDto = new UserCreateDto();
        List<RoleRepresentation> representations = realmResource.roles().list();
        UsersResource usersResource = realmResource.users();
        List<RoleRepresentation> idRoleAdd = representations.stream()
                .filter(roleRepresentation -> role.toString().equals(roleRepresentation.getName()))
                .toList();
        if (idRoleAdd.size() == 0) {
            throw new OriginatePilotException(new OriginatePilotError(HttpStatus.BAD_REQUEST.value(), "Role does not exist.", HttpStatus.BAD_REQUEST));
        }
        UserRepresentation keycloakUser = new UserRepresentation();
        keycloakUser.setEmail(user.getEmail());
        if (!StringUtils.isBlank(user.getName())) {
            Map<String, List<String>> attributes = new HashMap<>();
            attributes.put("name", Collections.singletonList(user.getName()));
            keycloakUser.setAttributes(attributes);
        }
        keycloakUser.setEmailVerified(true);

        Response response = usersResource.create(keycloakUser);
        if (HttpStatus.CREATED.value() == response.getStatus()) {
            log.info("[KeycloakCallService] create user success username {}", user.getEmail());
        } else {
            log.info("[KeycloakCallService] create user fail username {} error {}", user.getEmail(), response.getMetadata());
            throw new OriginatePilotException(new OriginatePilotError(response.getStatus(), "Email already exists (there's unlikely any other reason than this)", HttpStatus.CONFLICT));
        }
        UserRepresentation userResource = usersResource.search(keycloakUser.getEmail()).stream().findFirst().orElse(new UserRepresentation());
        userResource.setEnabled(true);
        userResource.setRequiredActions(List.of(Constants.STATUS_SEND_EMAIL_KEYCLOAK.UPDATE_PASSWORD.name()));
        usersResource.get(userResource.getId()).update(userResource);
        log.info("[KeycloakCallService] create user success update status user  {}", user.getEmail());
        // update role
        UserResource resource = usersResource.get(userResource.getId());
        resource.roles().realmLevel().add(idRoleAdd);
        log.info("[KeycloakCallService] create user success update role user  {}", user.getEmail());
        //  send mail update password
        userCreateDto.setId(userResource.getId());
        userCreateDto.setEmail(userResource.getEmail());
        try {
            resource.executeActionsEmail(client, url, List.of(Constants.STATUS_SEND_EMAIL_KEYCLOAK.UPDATE_PASSWORD.name()));
            userCreateDto.setIsSendMail(true);
        } catch (Exception e) {
            userCreateDto.setIsSendMail(false);
            log.error("[KeycloakCallService] send error keycloak {} to email {} role {}", e.getMessage(), userResource.getEmail(), role);
        }
        return userCreateDto;
    }

    @Override
    public void updateTermsKeyCloak(String terms, String userId) {
        try {
            UsersResource usersResource = realmResource.users();
            // get user keycloak by id
            UserResource userResource = usersResource.get(userId);
            UserRepresentation keycloakUser = userResource.toRepresentation();
            Map<String, List<String>> attributes = keycloakUser.getAttributes();
            if (Objects.isNull(attributes)) {
                attributes = new HashMap<>();
            }
            attributes.put(terms, Collections.singletonList("true"));
            keycloakUser.setAttributes(attributes);
            // update user keycloak
            userResource.update(keycloakUser);
        } catch (NotFoundException e) {
            throw new OriginatePilotException(OriginatePilotErrors.USER_NOT_FOUND_KEYCLOAK);
        }
    }
}
