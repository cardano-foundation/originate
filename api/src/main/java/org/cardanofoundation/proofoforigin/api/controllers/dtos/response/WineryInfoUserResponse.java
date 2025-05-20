package org.cardanofoundation.proofoforigin.api.controllers.dtos.response;

import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.proofoforigin.api.repository.entities.Winery;

@Getter
@Setter
public class WineryInfoUserResponse {

    private String wineryId;
    private String wineryName;
    private String wineryRsCode;

    public static WineryInfoUserResponse buildWineryUserInfoResponse(Winery winery) {
        WineryInfoUserResponse wineryUserInfoUserResponse = new WineryInfoUserResponse();
        wineryUserInfoUserResponse.setWineryId(winery.getWineryId());
        wineryUserInfoUserResponse.setWineryName(winery.getWineryName());
        wineryUserInfoUserResponse.setWineryRsCode(winery.getWineryRsCode());
        return wineryUserInfoUserResponse;
    }
}
