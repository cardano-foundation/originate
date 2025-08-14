package org.cardanofoundation.proofoforigin.api.controllers;


import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.cardanofoundation.proofoforigin.api.business.ScmApproveLotService;
import org.cardanofoundation.proofoforigin.api.business.ScmService;
import org.cardanofoundation.proofoforigin.api.business.ScmUploadLotService;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.DeleteLotResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.FinaliseLotResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.LotSCMResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.ScmApproveResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.cardanofoundation.proofoforigin.api.constants.BaseUri.CARDANO_ORIGINATE_PILOT_API.SCM;
import static org.cardanofoundation.proofoforigin.api.constants.BaseUri.CARDANO_ORIGINATE_PILOT_API.V1;

@RestController
@RequestMapping(V1 + SCM)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScmApiController {
    @Required
    ScmUploadLotService scmUploadLotService;
    @Required
    ScmApproveLotService scmApproveLotService;
    ScmService scmService;

    @GetMapping("/{wineryId}")
    public List<LotSCMResponse> getLotsSCMData(
            @Parameter(name = "wineryId", description = "wineryId", required = true, in = ParameterIn.PATH)
            @PathVariable("wineryId")
            String wineryId) {
        return scmService.getLotsSCMData(wineryId);
    }

    @PutMapping("/{wineryId}/approve")
    public ResponseEntity<ScmApproveResponse> approveFinalisedLot(
            @Parameter(name = "wineryId", description = "wineryId", required = true, in = ParameterIn.PATH)
            @PathVariable("wineryId")
            String wineryId,
            @Parameter(name = "lotIds", description = "lotIds", required = true) @RequestBody
            List<@Size(min = 11, max = 11) String> lotIds) {
        ScmApproveResponse scmApproveResponse = scmApproveLotService.approveLots(wineryId, lotIds);
        HttpStatus status = HttpStatus.OK;

        if (scmApproveResponse.getSucceed().isEmpty() && scmApproveResponse.getFailJobsNotScheduled().isEmpty()) {
            status = HttpStatus.CONFLICT;
        }

        return new ResponseEntity<>(scmApproveResponse, status);
    }

    @PostMapping("/{wineryId}/delete")
    public ResponseEntity<DeleteLotResponse> deleteUnfinalisedLot(
            @Parameter(name = "wineryId", description = "wineryId", required = true, in = ParameterIn.PATH)
            @PathVariable("wineryId")
            String wineryId,
            @Parameter(name = "lotIds", description = "lotIds", required = true)
            @RequestBody List<@Size(min = 11, max = 11) String> lotIds) {
        DeleteLotResponse deleteLotResponse = scmService.deleteUnfinalisedLot(wineryId, new HashSet<>(lotIds));

        HttpStatus httpStatus = HttpStatus.OK;
        if (deleteLotResponse.getSucceed().isEmpty()) {
            httpStatus = HttpStatus.CONFLICT;
        }
        return new ResponseEntity<>(deleteLotResponse, httpStatus);
    }

    @PutMapping("/{wineryId}/finalise")
    public ResponseEntity<FinaliseLotResponse> finaliseLot(
            @Parameter(name = "wineryId", description = "wineryId", required = true, in = ParameterIn.PATH)
            @PathVariable("wineryId")
            String wineryId,
            @Parameter(name = "lotIds", description = "lotIds", required = true)
            @RequestBody List<@Size(min = 11, max = 11) String> lotIds) {
        FinaliseLotResponse finaliseLotResponse = scmService.finaliseLot(wineryId, new HashSet<>(lotIds));

        HttpStatus httpStatus = HttpStatus.OK;
        if (finaliseLotResponse.getSucceed().isEmpty()) {
            httpStatus = HttpStatus.CONFLICT;
        }
        return new ResponseEntity<>(finaliseLotResponse, httpStatus);
    }

    @PostMapping("/{wineryId}")
    public ResponseEntity<Void> uploadCSV(
            @Parameter(name = "wineryId", description = "wineryId", required = true, in = ParameterIn.PATH)
            @PathVariable("wineryId")
            String wineryId,
            @Parameter(name = "data", description = "data") @Valid @RequestParam(value = "data")
            MultipartFile data) {
        scmUploadLotService.uploadCsvFile(data, wineryId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
