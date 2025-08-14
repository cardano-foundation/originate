package org.cardanofoundation.proofoforigin.api.controllers;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cardanofoundation.proofoforigin.api.business.UserKeycloak;
import org.cardanofoundation.proofoforigin.api.business.WineryService;
import org.cardanofoundation.proofoforigin.api.constants.Role;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.KeycloakUserBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.WineryUpdateBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.WineryUserBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BaseResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BriefWineryResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.WineryInfoUserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.cardanofoundation.proofoforigin.api.constants.BaseUri.CARDANO_ORIGINATE_PILOT_API.USER;
import static org.cardanofoundation.proofoforigin.api.constants.BaseUri.CARDANO_ORIGINATE_PILOT_API.V1;

@RestController
@RequestMapping(V1 + USER)
@RequiredArgsConstructor
public class UserApiController {

    @Autowired
    private WineryService wineryService;

    @Autowired
    private UserKeycloak userKeycloak;

    @PostMapping("/winery")
    public ResponseEntity<BaseResponse<BriefWineryResponse>> createUserWinery(
            @RequestBody @Valid WineryUserBody wineryUserBody) {
        return new ResponseEntity<>(wineryService.createWinery(wineryUserBody), HttpStatus.CREATED);
    }

    @GetMapping("/winery")
    public ResponseEntity<List<WineryInfoUserResponse>> getDropdownResponseAllWinery() {
        return new ResponseEntity<>(wineryService.getAllWinery(), HttpStatus.OK);
    }

    @PutMapping("/winery/{wineryId}")
    public ResponseEntity<BaseResponse<Void>> updateWinery(
            @PathVariable("wineryId")
            String wineryId,
            @RequestBody @Valid WineryUpdateBody wineryUpdateBody
    ) {
        wineryService.updateWinery(wineryId, wineryUpdateBody);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/admin")
    public ResponseEntity<BaseResponse<Void>> createUserAdmin(@RequestBody @Valid KeycloakUserBody adminUserBody) {
        return new ResponseEntity<>(userKeycloak.createUser(adminUserBody, Role.ADMIN), HttpStatus.CREATED);
    }

    @PostMapping("/dataprovider")
    public ResponseEntity<BaseResponse<Void>> createUserDataProvider(@RequestBody @Valid KeycloakUserBody dataProviderUserBody) {
        return new ResponseEntity<>(userKeycloak.createUser(dataProviderUserBody, Role.DATA_PROVIDER), HttpStatus.CREATED);
    }

    @PostMapping("/terms/accept")
    public ResponseEntity<BaseResponse<Void>> updateTerms() {
        userKeycloak.updateTermsUser();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
