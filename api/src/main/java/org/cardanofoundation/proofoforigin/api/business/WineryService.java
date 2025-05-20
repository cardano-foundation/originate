package org.cardanofoundation.proofoforigin.api.business;

import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.WineryUpdateBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.WineryUserBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BaseResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BriefWineryResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.WineryInfoUserResponse;
import org.cardanofoundation.proofoforigin.api.repository.entities.Winery;

import java.util.List;

public interface WineryService {
    BaseResponse<BriefWineryResponse> createWinery(WineryUserBody wineryUserBody);

    void updateWinery(String wineryId, WineryUpdateBody wineryUpdateBody);

    List<WineryInfoUserResponse> getAllWinery();

    byte[] getWineryPublicKey(String wineryId);

    Winery saveWinery(Winery winery);
}
