package org.cardanofoundation.proofoforigin.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.cardanofoundation.proofoforigin.api.business.BottlesService;
import org.cardanofoundation.proofoforigin.api.business.UploadBottleBusiness;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.BottleIdBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.BottleRangeBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BottleDto;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BottleResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BottlesInformation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.cardanofoundation.proofoforigin.api.constants.BaseUri.CARDANO_ORIGINATE_PILOT_API.BOTTLES;
import static org.cardanofoundation.proofoforigin.api.constants.BaseUri.CARDANO_ORIGINATE_PILOT_API.V1;

@RestController
@RequestMapping(V1 + BOTTLES)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BottlesApiController {

    UploadBottleBusiness uploadBottleBusiness;
    BottlesService bottlesService;

    @Operation(
            operationId = "updateCertificateAssociations",
            summary = "bulk associate or dissociate a certificate id with bottles (specific lot)",
            tags = {"bottles"},
            responses = {
                    @ApiResponse(responseCode = "204", description = "successfully set bottle certificate ids"),
                    @ApiResponse(responseCode = "400", description = "incorrect request format"),
                    @ApiResponse(responseCode = "404", description = "winery or certificate does not exist"),
                    @ApiResponse(
                            responseCode = "409",
                            description = "bottle IDs do not match lot requirements of certificate")
            })
    @PutMapping("/{wineryId}/certs/{certId}/{lotId}")
    public ResponseEntity<Void> updateCertificateAssociations(
            @Parameter(name = "wineryId", description = "", required = true, in = ParameterIn.PATH)
            @PathVariable("wineryId")
            String wineryId,
            @Parameter(name = "certId", description = "", required = true, in = ParameterIn.PATH)
            @PathVariable("certId")
            String certId,
            @Size(min = 11, max = 11)
            @Parameter(name = "lotId", description = "", required = true, in = ParameterIn.PATH)
            @PathVariable("lotId")
            String lotId,
            @Parameter(name = "BottleIdBody", description = "", required = true) @Valid @RequestBody
            BottleIdBody bottleIdBody) {

        bottlesService.updateCertificateAssociations(wineryId, certId, lotId, bottleIdBody);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            operationId = "getBottlesOfAWinery",
            summary = "get all bottle information for a given winery",
            tags = {"bottles"},
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "all bottles a winery produced",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            array = @ArraySchema(schema = @Schema(implementation = BottleResponse.class)))
                            }),
                    @ApiResponse(responseCode = "404", description = "winery does not exist")
            })
    @GetMapping("/{wineryId}")
    public BottleResponse getBottlesGivenByWinery(
            @Parameter(name = "wineryId", description = "", required = true, in = ParameterIn.PATH)
            @PathVariable("wineryId")
            String wineryId) {
        return bottlesService.getBottlesByWineryId(wineryId);
    }

    @Operation(
            operationId = "getBottlesOfALot",
            summary = "get all bottle information for a given lot",
            tags = {"bottles"},
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "all bottles in a specific lot",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            array = @ArraySchema(schema = @Schema(implementation = BottleResponse.class)))
                            }),
                    @ApiResponse(responseCode = "404", description = "winery does not exist")
            })
    @GetMapping("/{wineryId}/lots/{lotId}")
    public List<BottleDto> getBottlesGivenByLot(
            @Parameter(name = "wineryId", description = "", required = true, in = ParameterIn.PATH)
            @PathVariable("wineryId")
            String wineryId,
            @Size(min = 11, max = 11)
            @Parameter(name = "lotId", description = "", required = true, in = ParameterIn.PATH)
            @PathVariable("lotId")
            String lotId) {
        return bottlesService.getBottlesByLotId(wineryId, lotId);
    }

    @Operation(
            operationId = "uploadBottleCSV",
            summary = "csv upload bottle information for a specific winery (overwrites conflicting IDs)",
            tags = {"bottles"},
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "winery bottle information successfully updated"),
                    @ApiResponse(responseCode = "400", description = "incorrect request format"),
                    @ApiResponse(responseCode = "404", description = "winery does not exist")
            })
    @PostMapping("/{wineryId}")
    public ResponseEntity<Void> uploadCSV(
            @Parameter(name = "wineryId", description = "", required = true, in = ParameterIn.PATH)
            @PathVariable("wineryId")
            String wineryId,
            @Parameter(name = "data", description = "") @Valid @RequestParam(value = "data")
            MultipartFile data) {
        uploadBottleBusiness.uploadCsvFile(data, wineryId);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @Operation(
            operationId = "getBottleInformationWithBottleId",
            summary = "Get bottle information by an bottleId",
            tags = {"bottles"},
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Information bottle",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            array = @ArraySchema(schema = @Schema(implementation = BottleResponse.class)))
                            }),
                    @ApiResponse(responseCode = "404", description = "bottleId does not exist")
            })

    @GetMapping("/{wineryId}/bottle/{bottleId}")
    public ResponseEntity<BottlesInformation> getBottleInformationWithBottleId(
            @Parameter(name = "bottleId", description = "", required = true, in = ParameterIn.PATH)
            @PathVariable("wineryId")
            String wineryId,
            @PathVariable("bottleId")
            String bottleId) {

        return ResponseEntity.ok(bottlesService.getBottlesInformation(wineryId, bottleId));
    }

    @PutMapping("/range-scan/{wineryId}/certs/{certId}/{lotId}")
    public ResponseEntity<Void> updateCertificateAssociationsByRange(
            @Parameter(name = "wineryId", description = "", required = true, in = ParameterIn.PATH)
            @PathVariable("wineryId")
            String wineryId,
            @Parameter(name = "certId", description = "", required = true, in = ParameterIn.PATH)
            @PathVariable("certId")
            String certId,
            @Size(min = 11, max = 11)
            @Parameter(name = "lotId", description = "", required = true, in = ParameterIn.PATH)
            @PathVariable("lotId")
            String lotId,
            @Parameter(name = "BottleRangeBody", description = "", required = true) @Valid @RequestBody
            BottleRangeBody bottleRangeBody) {

        BottleIdBody bottleBody = bottlesService.convertBottleRangeBodyToBottleIdBody(bottleRangeBody);
        bottlesService.updateCertificateAssociations(wineryId, certId, lotId, bottleBody);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
