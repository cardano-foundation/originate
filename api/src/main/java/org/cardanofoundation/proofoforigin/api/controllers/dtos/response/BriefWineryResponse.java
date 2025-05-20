package org.cardanofoundation.proofoforigin.api.controllers.dtos.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cardanofoundation.proofoforigin.api.repository.entities.Winery;

@Getter
@Setter
@NoArgsConstructor
public class BriefWineryResponse {
    private String wineryId;

    public static BriefWineryResponse fromWinery(Winery winery) {
        BriefWineryResponse wineryResponse = new BriefWineryResponse();
        wineryResponse.setWineryId(winery.getWineryId());
        return wineryResponse;
    }
}
