package org.cardanofoundation.proofoforigin.api.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.RequiredArgsConstructor;
import org.cardanofoundation.proofoforigin.api.business.WineryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.cardanofoundation.proofoforigin.api.constants.BaseUri.CARDANO_BOLNISI_PILOT_API.P_KEY;
import static org.cardanofoundation.proofoforigin.api.constants.BaseUri.CARDANO_BOLNISI_PILOT_API.V1;

@RestController
@RequestMapping(V1 + P_KEY)
@RequiredArgsConstructor
public class PublicKeyApiController {
    @Autowired
    private WineryService wineryService;

    @GetMapping("/{wineryId}/v/0")
    public ResponseEntity<byte[]> getWineryPublicKey(
            @Parameter(name = "wineryId", description = "wineryId", required = true, in = ParameterIn.PATH)
            @PathVariable("wineryId")
            String wineryId) {
        return new ResponseEntity<>(wineryService.getWineryPublicKey(wineryId), HttpStatus.OK);
    }
}
