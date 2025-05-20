package org.cardanofoundation.proofoforigin.api.business.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.cardanofoundation.proofoforigin.api.business.ScmService;
import org.cardanofoundation.proofoforigin.api.constants.Constants;
import org.cardanofoundation.proofoforigin.api.constants.Role;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.DeleteLotResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.FinaliseLotResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.LotSCMResponse;
import org.cardanofoundation.proofoforigin.api.exceptions.BolnisiPilotErrors;
import org.cardanofoundation.proofoforigin.api.exceptions.BolnisiPilotException;
import org.cardanofoundation.proofoforigin.api.repository.LotRepository;
import org.cardanofoundation.proofoforigin.api.repository.WineryRepository;
import org.cardanofoundation.proofoforigin.api.repository.entities.Lot;
import org.cardanofoundation.proofoforigin.api.repository.entities.Winery;
import org.cardanofoundation.proofoforigin.api.utils.SecurityContextHolderUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScmServiceImpl implements ScmService {
    WineryRepository wineryRepository;
    SecurityContextHolderUtil securityContextHolderUtil;
    LotRepository lotRepository;

    @Override
    public List<LotSCMResponse> getLotsSCMData(String wineryId) {
        Winery winery = getWineryById(wineryId);
        List<String> roles = securityContextHolderUtil.getListRoles();
        boolean hasAdminOrDataProviderRole = hasAdminOrDataProviderRole(roles);
        boolean hasWineryRoleOnly = !hasAdminOrDataProviderRole && hasWineryRole(roles);
        if (hasAdminOrDataProviderRole || (hasWineryRoleOnly && winery.getKeycloakUserId().equals(securityContextHolderUtil.getKeyCloakUserId()))) {
            Stream<Lot> lotListStream = lotRepository.findByWineryId(wineryId).stream();
            if (hasWineryRoleOnly) {
                lotListStream = lotListStream
                        .filter(lot -> lot.getStatus() == Constants.LOT_STATUS.FINALIZED || lot.getStatus() == Constants.LOT_STATUS.APPROVED);
            }
            return lotListStream
                    .sorted(Comparator.comparingInt(Lot::getStatus))
                    .map(LotSCMResponse::buildLotSCMResponse)
                    .toList();
        } else {
            throw new BolnisiPilotException(BolnisiPilotErrors.FORBIDDEN);
        }
    }

    @Override
    @Transactional
    public DeleteLotResponse deleteUnfinalisedLot(String wineryId, Set<String> lotIds) {
        if (lotIds.isEmpty()) {
            throw new BolnisiPilotException(BolnisiPilotErrors.INVALID_PARAMETERS);
        }
        validateWineryExistence(wineryId);
        Set<String> lotIdsSucceed = new HashSet<>();
        Set<String> lotIdsAlreadyFinalised = new HashSet<>();
        List<Lot> lotToDelete = new ArrayList<>();
        List<Lot> lotList = lotRepository.findByWineryIdAndLotIdIn(wineryId, lotIds);

        for (Lot lot : lotList) {
            if (lot.getStatus() == Constants.LOT_STATUS.FINALIZED || lot.getStatus() == Constants.LOT_STATUS.APPROVED) {
                lotIdsAlreadyFinalised.add(lot.getLotId());
            } else {
                lotIdsSucceed.add(lot.getLotId());
                lotToDelete.add(lot);
            }
        }

        Set<String> lotIdsNotFound = lotIds.stream()
                .filter(lotId -> !lotIdsSucceed.contains(lotId) && !lotIdsAlreadyFinalised.contains(lotId))
                .collect(Collectors.toSet());

        if (!lotToDelete.isEmpty()) {
            lotRepository.deleteAll(lotToDelete);
        }

        return DeleteLotResponse.builder()
                .succeed(lotIdsSucceed)
                .failLotsNotFound(lotIdsNotFound)
                .failLotsAlreadyFinalised(lotIdsAlreadyFinalised)
                .build();
    }

    @Override
    @Transactional
    public FinaliseLotResponse finaliseLot(String wineryId, Set<String> lotIds) {
        if (lotIds.isEmpty()) {
            throw new BolnisiPilotException(BolnisiPilotErrors.INVALID_PARAMETERS);
        }
        validateWineryExistence(wineryId);
        Set<String> lotIdsSucceed = new HashSet<>();
        Set<String> lotIdsAlreadyFinalised = new HashSet<>();
        List<Lot> lotList = lotRepository.findByWineryIdAndLotIdIn(wineryId, lotIds);

        for (Lot lot : lotList) {
            if (lot.getStatus() == Constants.LOT_STATUS.FINALIZED || lot.getStatus() == Constants.LOT_STATUS.APPROVED) {
                lotIdsAlreadyFinalised.add(lot.getLotId());
            } else {
                lotIdsSucceed.add(lot.getLotId());
                lot.setStatus(Constants.LOT_STATUS.FINALIZED);
            }
        }

        Set<String> lotIdsNotFound = lotIds.stream()
                .filter(lotId -> !lotIdsSucceed.contains(lotId) && !lotIdsAlreadyFinalised.contains(lotId))
                .collect(Collectors.toSet());

        return FinaliseLotResponse.builder()
                .succeed(lotIdsSucceed)
                .failLotsNotFound(lotIdsNotFound)
                .failLotsAlreadyFinalised(lotIdsAlreadyFinalised)
                .build();
    }

    private Winery getWineryById(String wineryId) {
        return wineryRepository.findByWineryId(wineryId)
                .orElseThrow(() -> new BolnisiPilotException(BolnisiPilotErrors.NOT_FOUND));
    }

    private boolean hasAdminOrDataProviderRole(List<String> roles) {
        Predicate<String> isAdminOrDataProviderRole = role -> role.equals(Role.ADMIN.toString())
                || role.equals(Role.DATA_PROVIDER.toString());
        return roles.stream().anyMatch(isAdminOrDataProviderRole);
    }

    private boolean hasWineryRole(List<String> roles) {
        return roles.contains(Role.WINERY.toString());
    }

    private void validateWineryExistence(String wineryId) {
        if (!wineryRepository.existsById(wineryId)) {
            throw new BolnisiPilotException(BolnisiPilotErrors.NOT_FOUND);
        }
    }

    @Override
    public Lot updateTxIdAndJobIndexForLot(Long jobId, String txId, String jobIndex) {
        Optional<Lot> lotOpt = lotRepository.findByJobId(jobId);
        if (lotOpt.isEmpty()) {
            return null;
        }
        Lot lot = lotOpt.get();
        lot.setTxId(txId);
        lot.setJobIndex(jobIndex);
        return lotRepository.save(lot);
    }
}
