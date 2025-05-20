package org.cardanofoundation.proofoforigin.api.business;

import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.ScmApproveResponse;

import java.util.List;

public interface ScmApproveLotService
{
    ScmApproveResponse approveLots(String wineryId, List<String> lotIds);
}
