package org.cardanofoundation.proofoforigin.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.cardanofoundation.proofoforigin.api.business.CertificateService;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.RevokeCertBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.CertRequest;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.CertsResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.WineryCertsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.cardanofoundation.proofoforigin.api.constants.BaseUri.CARDANO_BOLNISI_PILOT_API.CERTIFICATES;
import static org.cardanofoundation.proofoforigin.api.constants.BaseUri.CARDANO_BOLNISI_PILOT_API.V1;

@RestController
@RequestMapping(V1 + CERTIFICATES)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CertApiController {

    @Autowired
    CertificateService certificateService;

    @Operation(
            operationId = "createCertificate",
            summary = "create a new certificate of conformity",
            tags = {"cert"},
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "successfully created certificate of conformity"),
                    @ApiResponse(responseCode = "400", description = "incorrect request format"),
                    @ApiResponse(responseCode = "409", description = "certificate already exists")
            })
    @PostMapping("/winery/{wineryId}/{certId}")
    public ResponseEntity<Void> createCertificate(
            @Parameter(name = "certId", description = "certId", required = true, in = ParameterIn.PATH)
            @PathVariable("wineryId") String wineryId,
            @PathVariable("certId") String certId,
            @Parameter(name = "CertRequest", description = "CertRequest", required = true) @Valid @RequestBody CertRequest certRequest) {

        certificateService.createCertificate(certId, wineryId, certRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * <p>
     * On-chain certificate revocation API
     * </p>
     *
     * @param certId the target certification (request param)
     * @return The operation result
     */
    @Operation(operationId = "revokeCertificate", summary = "revoke a certificate",
            tags = {"cert" },
            responses = {
                @ApiResponse(responseCode = "204", description = "successfully revoked certificate"),
                @ApiResponse(responseCode = "409", description = "certificate already revoked"),
                @ApiResponse(responseCode = "404", description = "The certificate does not exist"),
            })
    @PutMapping("/{certId}/revoke")
    public ResponseEntity<Void> revokeCertificate(
            @Parameter(name = "certId", description = "certId", required = true, in = ParameterIn.PATH) @PathVariable("certId") final String certId,
            @Parameter(name = "signature", description = "A signature from NWA", required = true) @Valid @RequestBody final RevokeCertBody body) {

        certificateService.revokeCertificate(certId, body.getSignature(), body.getPublicKeyBase64Url());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            operationId = "getAllCerts",
            summary = "get all certificate lot entries (products) from non-revoked (active) certificates",
            tags = {"cert"},
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "all certificates of conformity",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            array = @ArraySchema(schema = @Schema(implementation = CertsResponse.class)))
                            })
            })
    @GetMapping
    public ResponseEntity<List<WineryCertsResponse>> getCertificates() {
        return ResponseEntity.ok(certificateService.getAllCertAll());
    }

    @Operation(
            operationId = "getCertOfWinery",
            summary = "get all certificate lot entries (products) from non-revoked (active) certificates that are on-chain related to a given winery",
            tags = {"cert"},
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "related certificates of conformity",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            array = @ArraySchema(schema = @Schema(implementation = CertsResponse.class)))
                            })
            })
    @GetMapping("/winery/{wineryId}")
    public ResponseEntity<List<CertsResponse>> getCertificates(
            @Parameter(name = "wineryId", description = "", required = true, in = ParameterIn.PATH)
            @PathVariable("wineryId")
            String wineryId) {
        return ResponseEntity.ok(certificateService.getByWineryId(wineryId));
    }
}
