package org.cardanofoundation.proofoforigin.api.business;

import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.DeleteLotResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.FinaliseLotResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.LotSCMResponse;
import org.cardanofoundation.proofoforigin.api.repository.entities.Lot;

import java.util.List;
import java.util.Set;

public interface ScmService {
    List<LotSCMResponse> getLotsSCMData(String wineryId);

    DeleteLotResponse deleteUnfinalisedLot(String wineryId, Set<String> lotIds);

    FinaliseLotResponse finaliseLot(String wineryId, Set<String> lotIds);

    Lot updateTxIdAndJobIndexForLot(Long jobId, String txId, String jobIndex);
}
