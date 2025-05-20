package org.cardanofoundation.proofoforigin.api.business.impl;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScmServiceImplTest {

    @Mock
    private WineryRepository wineryRepository;
    @Mock
    private SecurityContextHolderUtil securityContextHolderUtil;
    @Mock
    private LotRepository lotRepository;
    @InjectMocks
    private ScmServiceImpl scmService;

    private final String keycloakUserId = "eb6d0a34-89e9-4dfd-bef2-93fb19139a34";
    private final String wineryId = "w01";
    private final String wineryName = "Winery1";
    private final String wineryRsCode = "111232444";
    private final String privateKey = "privateKey";
    private final String publicKey = "publicKey";
    private final String salt = "salt";

    private final LocalDate date = LocalDate.of(2023, 1, 10);
    private final Lot lot = Lot.builder()
            .lotId("lot1")
            .wineName("wine_name")
            .origin("origin")
            .countryOfOrigin("country")
            .producedBy("producer")
            .producerAddress("producer_address")
            .producerLatitude(10.0)
            .producerLongitude(100.0)
            .varietalName("variatal_name")
            .vintageYear(2022)
            .wineType("type")
            .wineColor("red")
            .harvestDate(date)
            .harvestLocation("harvest_location")
            .pressingDate(date)
            .processingLocation("processing_location")
            .fermentationVessel("fermentation_vessel")
            .fermentationDuration("fermentation duration")
            .agingRecipient("aging_recipient")
            .agingTime("aging time")
            .storageVessel("storage_vessel")
            .bottlingDate(date)
            .bottlingLocation("bottling_location")
            .numberOfBottles(10)
            .winerySignature("signature")
            .status(Constants.LOT_STATUS.NOT_FINALIZED)
            .txId("")
            .build();


    @Test
    void getWineryById_not_exist() {
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.empty());

        Throwable actualThrow = assertThrows(BolnisiPilotException.class, () -> scmService.getLotsSCMData(wineryId));

        assertEquals(BolnisiPilotErrors.NOT_FOUND.getMessage(), actualThrow.getMessage());
    }

    @Test
    void getLotsSCMData_with_nwa_role() {
        Winery winery = new Winery(wineryId, keycloakUserId, wineryName, wineryRsCode, privateKey, publicKey, salt);
        List<String> roles = List.of(Role.NWA.toString());

        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getListRoles()).thenReturn(roles);

        Throwable actualThrow = assertThrows(BolnisiPilotException.class, () -> scmService.getLotsSCMData(wineryId));

        assertEquals(BolnisiPilotErrors.FORBIDDEN.getMessage(), actualThrow.getMessage());
    }

    @Test
    void getLotsSCMData_with_another_winery_role() {
        Winery winery = new Winery(wineryId, keycloakUserId, wineryName, wineryRsCode, privateKey, publicKey, salt);
        List<String> roles = List.of(Role.WINERY.toString());

        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getListRoles()).thenReturn(roles);
        when(securityContextHolderUtil.getKeyCloakUserId()).thenReturn("anotherKeycloakUserId");

        Throwable actualThrow = assertThrows(BolnisiPilotException.class, () -> scmService.getLotsSCMData(wineryId));

        assertEquals(BolnisiPilotErrors.FORBIDDEN.getMessage(), actualThrow.getMessage());
    }

    @Test
    void getLotsSCMData_with_winery_role() {
        Winery winery = new Winery(wineryId, keycloakUserId, wineryName, wineryRsCode, privateKey, publicKey, salt);
        List<String> roles = List.of(Role.WINERY.toString());

        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getListRoles()).thenReturn(roles);
        when(securityContextHolderUtil.getKeyCloakUserId()).thenReturn(keycloakUserId);
        when(lotRepository.findByWineryId(wineryId)).thenReturn(
                List.of(lot.toBuilder().status(Constants.LOT_STATUS.FINALIZED).build(),
                        lot.toBuilder().status(Constants.LOT_STATUS.APPROVED).build(),
                        lot.toBuilder().status(Constants.LOT_STATUS.NOT_FINALIZED).build()));

        List<LotSCMResponse> actual = scmService.getLotsSCMData(wineryId);

        assertEquals(2, actual.size());
        assertEquals(Constants.LOT_STATUS_VALUE.FINALISED, actual.get(0).getStatus());
        assertEquals(Constants.LOT_STATUS_VALUE.APPROVED, actual.get(1).getStatus());
    }

    @Test
    void getLotsSCMData_with_admin_role() {
        Winery winery = new Winery(wineryId, keycloakUserId, wineryName, wineryRsCode, privateKey, publicKey, salt);
        List<String> roles = List.of(Role.ADMIN.toString());

        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getListRoles()).thenReturn(roles);
        when(lotRepository.findByWineryId(wineryId)).thenReturn(
                List.of(lot.toBuilder().status(Constants.LOT_STATUS.FINALIZED).build(),
                        lot.toBuilder().status(Constants.LOT_STATUS.APPROVED).build(),
                        lot.toBuilder().status(Constants.LOT_STATUS.NOT_FINALIZED).build())
        );

        List<LotSCMResponse> actual = scmService.getLotsSCMData(wineryId);

        assertEquals(3, actual.size());
        assertEquals(Constants.LOT_STATUS_VALUE.NOT_FINALISED, actual.get(0).getStatus());
        assertEquals(Constants.LOT_STATUS_VALUE.FINALISED, actual.get(1).getStatus());
        assertEquals(Constants.LOT_STATUS_VALUE.APPROVED, actual.get(2).getStatus());
    }

    @Test
    void deleteUnfinalisedLot_with_wineryId_not_existed() {
        Set<String> lotIds = Set.of("lot1", "lot2", "lot3");

        when(wineryRepository.existsById(wineryId)).thenReturn(false);

        Throwable actualThrow = assertThrows(BolnisiPilotException.class, () -> scmService.deleteUnfinalisedLot(wineryId, lotIds));
        assertEquals(BolnisiPilotErrors.NOT_FOUND.getMessage(), actualThrow.getMessage());
    }

    @Test
    void deleteUnfinalisedLot_with_lotIds_empty() {
        Set<String> lotIds = Collections.emptySet();

        Throwable actualThrow = assertThrows(BolnisiPilotException.class, () -> scmService.deleteUnfinalisedLot(wineryId, lotIds));

        assertEquals(BolnisiPilotErrors.INVALID_PARAMETERS.getMessage(), actualThrow.getMessage());
    }

    @Test
    void deleteUnfinalisedLot_failed() {
        Set<String> lotIds = Set.of("lot1", "lot2", "lot3", "lot4");
        List<Lot> lotList = List.of(lot.toBuilder().lotId("lot1").status(Constants.LOT_STATUS.FINALIZED).build(),
                lot.toBuilder().lotId("lot2").status(Constants.LOT_STATUS.FINALIZED).build());

        Set<String> lotIdsSucceed = Collections.emptySet();
        Set<String> lotIdsNotFound = new HashSet<>(List.of("lot3", "lot4"));
        Set<String> lotIdsAlreadyFinalised = new HashSet<>(List.of("lot1", "lot2"));

        when(wineryRepository.existsById(wineryId)).thenReturn(true);
        when(lotRepository.findByWineryIdAndLotIdIn(wineryId, lotIds)).thenReturn(lotList);

        DeleteLotResponse actualDeleteLotResponse = scmService.deleteUnfinalisedLot(wineryId, lotIds);
        assertEquals(lotIdsSucceed, actualDeleteLotResponse.getSucceed());
        assertEquals(lotIdsNotFound, actualDeleteLotResponse.getFailLotsNotFound());
        assertEquals(lotIdsAlreadyFinalised, actualDeleteLotResponse.getFailLotsAlreadyFinalised());
    }

    @Test
    void deleteUnfinalisedLot_succeed() {
        Set<String> lotIds = Set.of("lot1", "lot2", "lot3", "lot4");
        List<Lot> lotList = List.of(lot.toBuilder().lotId("lot1").status(Constants.LOT_STATUS.NOT_FINALIZED).build(),
                lot.toBuilder().lotId("lot2").status(Constants.LOT_STATUS.FINALIZED).build());

        Set<String> lotIdsSucceed = new HashSet<>(List.of("lot1"));
        Set<String> lotIdsNotFound = new HashSet<>(List.of("lot3", "lot4"));
        Set<String> lotIdsAlreadyFinalised = new HashSet<>(List.of("lot2"));

        when(wineryRepository.existsById(wineryId)).thenReturn(true);
        when(lotRepository.findByWineryIdAndLotIdIn(wineryId, lotIds)).thenReturn(lotList);

        DeleteLotResponse actualDeleteLotResponse = scmService.deleteUnfinalisedLot(wineryId, lotIds);
        assertEquals(lotIdsSucceed, actualDeleteLotResponse.getSucceed());
        assertEquals(lotIdsNotFound, actualDeleteLotResponse.getFailLotsNotFound());
        assertEquals(lotIdsAlreadyFinalised, actualDeleteLotResponse.getFailLotsAlreadyFinalised());
    }

    @Test
    void finaliseLot_with_wineryId_not_existed() {
        Set<String> lotIds = Set.of("lot1", "lot2", "lot3");

        when(wineryRepository.existsById(wineryId)).thenReturn(false);

        Throwable actualThrow = assertThrows(BolnisiPilotException.class, () -> scmService.finaliseLot(wineryId, lotIds));
        assertEquals(BolnisiPilotErrors.NOT_FOUND.getMessage(), actualThrow.getMessage());

    }

    @Test
    void finaliseLot_with_lotIds_empty() {
        Set<String> lotIds = Collections.emptySet();

        Throwable actualThrow = assertThrows(BolnisiPilotException.class, () -> scmService.finaliseLot(wineryId, lotIds));

        assertEquals(BolnisiPilotErrors.INVALID_PARAMETERS.getMessage(), actualThrow.getMessage());
    }

    @Test
    void finaliseLot_failed() {
        Set<String> lotIds = Set.of("lot1", "lot2", "lot3", "lot4");
        List<Lot> lotList = List.of(lot.toBuilder().lotId("lot1").status(Constants.LOT_STATUS.FINALIZED).build(),
                lot.toBuilder().lotId("lot2").status(Constants.LOT_STATUS.FINALIZED).build());

        Set<String> lotIdsSucceed = Collections.emptySet();
        Set<String> lotIdsNotFound = new HashSet<>(List.of("lot3", "lot4"));
        Set<String> lotIdsAlreadyFinalised = new HashSet<>(List.of("lot1", "lot2"));

        when(wineryRepository.existsById(wineryId)).thenReturn(true);
        when(lotRepository.findByWineryIdAndLotIdIn(wineryId, lotIds)).thenReturn(lotList);

        FinaliseLotResponse actualFinaliseLotResponse = scmService.finaliseLot(wineryId, lotIds);
        assertEquals(lotIdsSucceed, actualFinaliseLotResponse.getSucceed());
        assertEquals(lotIdsNotFound, actualFinaliseLotResponse.getFailLotsNotFound());
        assertEquals(lotIdsAlreadyFinalised, actualFinaliseLotResponse.getFailLotsAlreadyFinalised());
    }

    @Test
    void finaliseLot_succeed() {
        Set<String> lotIds = Set.of("lot1", "lot2", "lot3", "lot4");
        List<Lot> lotList = List.of(lot.toBuilder().lotId("lot1").status(Constants.LOT_STATUS.NOT_FINALIZED).build(),
                lot.toBuilder().lotId("lot2").status(Constants.LOT_STATUS.FINALIZED).build());

        Set<String> lotIdsSucceed = new HashSet<>(List.of("lot1"));
        Set<String> lotIdsNotFound = new HashSet<>(List.of("lot3", "lot4"));
        Set<String> lotIdsAlreadyFinalised = new HashSet<>(List.of("lot2"));

        when(wineryRepository.existsById(wineryId)).thenReturn(true);
        when(lotRepository.findByWineryIdAndLotIdIn(wineryId, lotIds)).thenReturn(lotList);

       FinaliseLotResponse actualFinaliseLotResponse = scmService.finaliseLot(wineryId, lotIds);
        assertEquals(lotIdsSucceed, actualFinaliseLotResponse.getSucceed());
        assertEquals(lotIdsNotFound, actualFinaliseLotResponse.getFailLotsNotFound());
        assertEquals(lotIdsAlreadyFinalised, actualFinaliseLotResponse.getFailLotsAlreadyFinalised());
    }
}