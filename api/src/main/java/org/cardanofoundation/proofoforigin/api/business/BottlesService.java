package org.cardanofoundation.proofoforigin.api.business;

import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.BottleIdBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.BottleRangeBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BottleDto;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BottleResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BottlesInformation;

import java.util.List;

public interface BottlesService {
    BottleResponse getBottlesByWineryId(String wineryId);

    List<BottleDto> getBottlesByLotId(String wineryId, String lotId);

    BottlesInformation getBottlesInformation(String wineryId, String bottleId);

    void updateCertificateAssociations(String wineryId, String certId, String lotId, BottleIdBody bottlesBody);

    BottleIdBody convertBottleRangeBodyToBottleIdBody(BottleRangeBody bottleRangeBody);
}