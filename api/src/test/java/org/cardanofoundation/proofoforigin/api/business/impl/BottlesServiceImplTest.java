package org.cardanofoundation.proofoforigin.api.business.impl;

import org.cardanofoundation.proofoforigin.api.constants.CertStatus;
import org.cardanofoundation.proofoforigin.api.constants.CertUpdateStatus;
import org.cardanofoundation.proofoforigin.api.constants.Constants;
import org.cardanofoundation.proofoforigin.api.constants.Role;
import org.cardanofoundation.proofoforigin.api.constants.ScanningStatus;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.BottleIdBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.BottleRangeBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BottleDto;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BottleResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BottlesInformation;
import org.cardanofoundation.proofoforigin.api.exceptions.BolnisiPilotException;
import org.cardanofoundation.proofoforigin.api.repository.*;
import org.cardanofoundation.proofoforigin.api.repository.entities.*;
import org.cardanofoundation.proofoforigin.api.utils.SecurityContextHolderUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.security.cert.Certificate;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BottlesServiceImplTest {

    @Mock
    BottleRepository bottleRepository;
    @Mock
    WineryRepository wineryRepository;

    @Mock
    SecurityContextHolderUtil securityContextHolderUtil;

    @Mock
    CertificateLotEntryRepository certificateLotEntryRepository;

    @Mock
    CertificateRepository certificateRepository;

    @InjectMocks
    @Spy
    BottlesServiceImpl bottlesService;

    private final String keycloakUserId = "eb6d0a34-89e9-4dfd-bef2-93fb19139a34";
    private final String wineryId = "w01";
    private final String lotId = "lot1";
    private final String wineryName = "Winery1";
    private final String wineryRsCode = "111232444";
    private final String privateKey = "privateKey";
    private final String publicKey = "publicKey";
    private final String salt = "salt";


    @Test
    void winery_does_not_exist() {
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.empty());

        Assertions.assertThrows(BolnisiPilotException.class, () -> bottlesService.getBottlesByWineryId(wineryId));
    }

    @Test
    void getBottlesByWineryId_with_nwa_role() {
        Winery winery = new Winery(wineryId, keycloakUserId, wineryName, wineryRsCode, privateKey, publicKey, salt);
        List<String> roles = List.of(Role.NWA.toString());

        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getListRoles()).thenReturn(roles);

        Assertions.assertThrows(BolnisiPilotException.class, () -> bottlesService.getBottlesByWineryId(wineryId));
    }

    @Test
    void getBottlesByWineryId_with_another_winery_role() {
        Winery winery = new Winery(wineryId, keycloakUserId, wineryName, wineryRsCode, privateKey, publicKey, salt);
        List<String> roles = List.of(Role.WINERY.toString());

        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getListRoles()).thenReturn(roles);
        when(securityContextHolderUtil.getKeyCloakUserId()).thenReturn("anotherKeycloakUserId");

        Assertions.assertThrows(BolnisiPilotException.class, () -> bottlesService.getBottlesByWineryId(wineryId));
    }

    @Test
    void getBottlesByWineryId_with_winery_role() {
        Winery winery = new Winery(wineryId, keycloakUserId, wineryName, wineryRsCode, privateKey, publicKey, salt);
        List<String> roles = List.of(Role.WINERY.toString());
        List<Bottle> bottleList = List.of(Bottle.builder()
                .id("bot1")
                .lotId("lot1")
                .sequentialNumber(1)
                .reelNumber(1)
                .certificateId("cert1")
                .lotUpdateStatus(Constants.SCANTRUST.STATUS.UPDATED)
                .wineryId("w01")
                .build());

        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getListRoles()).thenReturn(roles);
        when(securityContextHolderUtil.getKeyCloakUserId()).thenReturn(keycloakUserId);
        when(bottleRepository.getBottlesByWineryId(wineryId)).thenReturn(bottleList);

        List<BottleResponse.BottleData> actualBottleResponses = bottlesService.getBottlesByWineryId(wineryId).getSuccess();

        Assertions.assertEquals(1, actualBottleResponses.size());
        BottleResponse.BottleData firstActualBottleResponse = actualBottleResponses.get(0);
        Assertions.assertEquals("bot1", firstActualBottleResponse.getId());
        Assertions.assertEquals("lot1", firstActualBottleResponse.getLotId());
        Assertions.assertEquals(1, firstActualBottleResponse.getSequentialNumber());
        Assertions.assertEquals(1, firstActualBottleResponse.getReelNumber());
    }

    @Test
    void getBottlesByWineryId_with_admin_role() {
        Winery winery = new Winery(wineryId, keycloakUserId, wineryName, wineryRsCode, privateKey, publicKey, salt);
        List<String> roles = List.of(Role.ADMIN.toString());
        List<Bottle> bottleList = List.of(Bottle.builder()
                .id("bot1")
                .lotId("lot1")
                .sequentialNumber(1)
                .reelNumber(1)
                .certificateId("cert1")
                .wineryId("w01")
                .lotUpdateStatus(Constants.SCANTRUST.STATUS.NOT_UPDATED)
                .build());

        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getListRoles()).thenReturn(roles);
        when(bottleRepository.getBottlesByWineryId(wineryId)).thenReturn(bottleList);

        List<BottleResponse.BottleData> actualBottleResponses = bottlesService.getBottlesByWineryId(wineryId).getScheduled();

        Assertions.assertEquals(1, actualBottleResponses.size());
        BottleResponse.BottleData firstActualBottleResponse = actualBottleResponses.get(0);
        Assertions.assertEquals("bot1", firstActualBottleResponse.getId());
        Assertions.assertEquals("lot1", firstActualBottleResponse.getLotId());
        Assertions.assertEquals(1, firstActualBottleResponse.getSequentialNumber());
        Assertions.assertEquals(1, firstActualBottleResponse.getReelNumber());
    }

    @Test
    void getBottlesByWineryId_scantrust_status_failed() {
        Winery winery = new Winery(wineryId, keycloakUserId, wineryName, wineryRsCode, privateKey, publicKey, salt);
        List<String> roles = List.of(Role.ADMIN.toString());
        List<Bottle> bottleList = List.of(Bottle.builder()
                .id("bot1")
                .lotId("lot1")
                .sequentialNumber(1)
                .reelNumber(1)
                .certificateId("cert1")
                .wineryId("w01")
                .lotUpdateStatus(Constants.SCANTRUST.STATUS.FAILED)
                .build());

        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getListRoles()).thenReturn(roles);
        when(bottleRepository.getBottlesByWineryId(wineryId)).thenReturn(bottleList);

        List<BottleResponse.BottleData> actualBottleResponses = bottlesService.getBottlesByWineryId(wineryId).getError();

        Assertions.assertEquals(1, actualBottleResponses.size());
        BottleResponse.BottleData firstActualBottleResponse = actualBottleResponses.get(0);
        Assertions.assertEquals("bot1", firstActualBottleResponse.getId());
        Assertions.assertEquals("lot1", firstActualBottleResponse.getLotId());
        Assertions.assertEquals(1, firstActualBottleResponse.getSequentialNumber());
        Assertions.assertEquals(1, firstActualBottleResponse.getReelNumber());
    }

    @Test
    void getBottlesByLotId_with_nwa_role() {
        Winery winery = new Winery(wineryId, keycloakUserId, wineryName, wineryRsCode, privateKey, publicKey, salt);
        List<String> roles = List.of(Role.NWA.toString());

        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getListRoles()).thenReturn(roles);

        Assertions.assertThrows(BolnisiPilotException.class, () -> bottlesService.getBottlesByLotId(wineryId, lotId));
    }

    @Test
    void getBottlesByLotId_with_another_winery_role() {
        Winery winery = new Winery(wineryId, keycloakUserId, wineryName, wineryRsCode, privateKey, publicKey, salt);
        List<String> roles = List.of(Role.WINERY.toString());

        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getListRoles()).thenReturn(roles);
        when(securityContextHolderUtil.getKeyCloakUserId()).thenReturn("anotherKeycloakUserId");

        Assertions.assertThrows(BolnisiPilotException.class, () -> bottlesService.getBottlesByLotId(wineryId, lotId));
    }

    @Test
    void getBottlesByLotId_with_lot_does_not_exist() {
        Winery winery = new Winery(wineryId, keycloakUserId, wineryName, wineryRsCode, privateKey, publicKey, salt);
        List<String> roles = List.of(Role.WINERY.toString());

        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getListRoles()).thenReturn(roles);
        when(securityContextHolderUtil.getKeyCloakUserId()).thenReturn(keycloakUserId);
        when(bottleRepository.existsByLotIdAndWineryId(lotId, wineryId)).thenReturn(false);

        Assertions.assertThrows(BolnisiPilotException.class, () -> bottlesService.getBottlesByLotId(wineryId, lotId));
    }

    @Test
    void getBottlesByLotId_with_winery_role() {
        Winery winery = new Winery(wineryId, keycloakUserId, wineryName, wineryRsCode, privateKey, publicKey, salt);
        List<String> roles = List.of(Role.WINERY.toString());
        List<Bottle> bottleList = List.of(Bottle.builder()
                .id("bot1")
                .lotId("lot1")
                .sequentialNumber(1)
                .reelNumber(1)
                .certificateId("cert1")
                .wineryId("w01")
                .build());

        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getListRoles()).thenReturn(roles);
        when(securityContextHolderUtil.getKeyCloakUserId()).thenReturn(keycloakUserId);
        when(bottleRepository.existsByLotIdAndWineryId(lotId, wineryId)).thenReturn(true);
        when(bottleRepository.getBottlesByWineryIdAndLotId(wineryId, lotId)).thenReturn(bottleList);

        List<BottleDto> actualBottleResponses = bottlesService.getBottlesByLotId(wineryId, lotId);

        Assertions.assertEquals(1, actualBottleResponses.size());
        BottleDto firstActualBottleResponse = actualBottleResponses.get(0);
        Assertions.assertEquals("bot1", firstActualBottleResponse.getId());
        Assertions.assertEquals("lot1", firstActualBottleResponse.getLotId());
        Assertions.assertEquals(1, firstActualBottleResponse.getSequentialNumber());
        Assertions.assertEquals(1, firstActualBottleResponse.getReelNumber());
        Assertions.assertEquals("cert1", firstActualBottleResponse.getCertificateId());
    }

    @Test
    void getBottlesByLotId_with_admin_role() {
        Winery winery = new Winery(wineryId, keycloakUserId, wineryName, wineryRsCode, privateKey, publicKey, salt);
        List<String> roles = List.of(Role.ADMIN.toString());
        List<Bottle> expectedBottleList = List.of(Bottle.builder()
                .id("bot1")
                .lotId("lot1")
                .sequentialNumber(1)
                .reelNumber(1)
                .certificateId("cert1")
                .wineryId("w01")
                .build());

        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getListRoles()).thenReturn(roles);
        when(bottleRepository.existsByLotIdAndWineryId(lotId, wineryId)).thenReturn(true);
        when(bottleRepository.getBottlesByWineryIdAndLotId(wineryId, lotId)).thenReturn(expectedBottleList);

        List<BottleDto> actualBottleResponses = bottlesService.getBottlesByLotId(wineryId, lotId);

        Assertions.assertEquals(1, actualBottleResponses.size());
        BottleDto firstActualBottleResponse = actualBottleResponses.get(0);
        Assertions.assertEquals("bot1", firstActualBottleResponse.getId());
        Assertions.assertEquals("lot1", firstActualBottleResponse.getLotId());
        Assertions.assertEquals(1, firstActualBottleResponse.getSequentialNumber());
        Assertions.assertEquals(1, firstActualBottleResponse.getReelNumber());
        Assertions.assertEquals("cert1", firstActualBottleResponse.getCertificateId());
    }

    @Test
    void test_Get_Bottles_Information() {

        String bottleId = "bot01";
        String keycloakUserId = "keycloakUserId";
        String wineryId = "wineryId";
        String lotId = "lotId";
        String certId = "certId";

        //test wineryID form token
        Winery winery = new Winery(wineryId, keycloakUserId, wineryName, wineryRsCode, privateKey, publicKey, salt);
        List<String> roles = List.of(Role.ADMIN.toString());

        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getListRoles()).thenReturn(roles);
        when(securityContextHolderUtil.getKeyCloakUserId()).thenReturn(keycloakUserId);

        Assertions.assertEquals(winery.getKeycloakUserId(), keycloakUserId);

        Bottle bottle = new Bottle();
        bottle.setLotId(lotId);
        bottle.setCertificateId(certId);

        CertificateEntity certEntity = new CertificateEntity();
        certEntity.setCertificateNumber("certNum");
        certEntity.setCertificateType("certType");

        CertificateLotEntryEntity certProductEntity = new CertificateLotEntryEntity();
        certProductEntity.setScanningStatus(ScanningStatus.SCANNING);
        certProductEntity.setCertificate(certEntity);

        // Configure mock behavior
        when(bottleRepository.findByWineryIdAndId(wineryId, bottleId)).thenReturn( Optional.of(bottle));

        when(certificateLotEntryRepository.findByCertificateLotEntryPkCertificateIdAndCertificateLotEntryPkLotIdAndCertificateCertStatus(bottle.getCertificateId()
                , bottle.getLotId(), CertStatus.ACTIVE)).thenReturn(certProductEntity);

        // Call the method under test
        BottlesInformation result = bottlesService.getBottlesInformation(wineryId, bottleId);

        // Verify the result
        assertEquals(bottle.getCertificateId(), result.getCertId());
        assertEquals(bottle.getLotId(), result.getLotId());
        assertEquals(certProductEntity.getScanningStatus(), result.getScanningStatus());
        assertEquals(certEntity.getCertificateType(), result.getCertType());
        assertEquals(certEntity.getCertificateNumber(), result.getCertNumber());
    }

    @Test
    void test_Get_Bottles_Information_BottleNotFound() {
        // Mock data
        String bottleId = "123";
        String keycloakUserId = "keycloakUserId";
        String wineryId = "wineryId";

        //test wineryID form token
        Winery winery = new Winery(wineryId, keycloakUserId, "", "", "", "", "");
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getKeyCloakUserId()).thenReturn(keycloakUserId);

        Assertions.assertEquals(winery.getKeycloakUserId(), keycloakUserId);

        // Configure mock behavior
        when(bottleRepository.findById(bottleId)).thenReturn(Optional.empty());

        // Call the method under test and assert that it throws an exception
        assertThrows(BolnisiPilotException.class, () -> {
            bottlesService.getBottlesInformation(wineryId, bottleId);
        });
    }

    @Test
    void test_Get_Bottles_Information_WineryIdNotFound() {
        // Mock data
        String wineryId = "4567";
        String bottleId = "bot01";

        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.empty());

        // Call the method under test and assert that it throws an exception
        assertThrows(BolnisiPilotException.class, () -> {
            bottlesService.getBottlesInformation(wineryId, bottleId);
        });
    }

    @Test
    void test_Get_Bottles_Information_WineryIdExistButNotWrongToken() {
        // Mock data
        String bottleId = "123";
        String notFoundWinery = "4567";

        Winery winery = new Winery(notFoundWinery, keycloakUserId, "", "", "", "", "");
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getKeyCloakUserId()).thenReturn(null);

        // Call the method under test and assert that it throws an exception
        assertThrows(BolnisiPilotException.class, () -> {
            bottlesService.getBottlesInformation(wineryId, bottleId);
        });
    }

    @Test
    void test_saveScannedBottlesForCertificate() {
        String certId = "certificateId";
        String lotId = "lotId";
        String keycloakUserId = "keycloakUserId";
        String wineryId = "wineryId";

        Winery winery = new Winery(wineryId, keycloakUserId, wineryName, wineryRsCode, privateKey, publicKey, salt);
        List<String> roles = List.of(Role.ADMIN.toString());

        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getListRoles()).thenReturn(roles);
        when(securityContextHolderUtil.getKeyCloakUserId()).thenReturn(keycloakUserId);

        Assertions.assertEquals(winery.getKeycloakUserId(), keycloakUserId);

        when(certificateRepository.findByCertificateIdAndTxIdIsNotNullAndCertStatus(certId, CertStatus.ACTIVE)).thenReturn(Arrays.asList(new CertificateEntity()));

        //test certID, lotID is exits with wineryID
        when(certificateLotEntryRepository
                .countByCertificateLotEntryPkCertificateIdAndCertificateLotEntryPkLotIdAndWineryIdAndCertificateCertStatus(certId, lotId, wineryId, CertStatus.ACTIVE))
                .thenReturn(1L);

        //Check CertificateLotEntryEntity has certID and lotID corresponding to param input
        CertificateLotEntryPK certificateLotEntryPK = new CertificateLotEntryPK();
        certificateLotEntryPK.setLotId(lotId);
        certificateLotEntryPK.setCertificateId(certId);
        CertificateLotEntryEntity clb = new CertificateLotEntryEntity();
        clb.setCertificateLotEntryPk(certificateLotEntryPK);
        clb.setWineryId(wineryId);
        clb.setScanningStatus(ScanningStatus.NOT_STARTED);

        when(certificateLotEntryRepository.findByCertificateLotEntryPkCertificateIdAndCertificateLotEntryPkLotIdAndScanningStatusNotAndCertificateCertStatus(certId, lotId, ScanningStatus.APPROVED, CertStatus.ACTIVE))
                .thenReturn(Optional.of(clb));

        BottleIdBody bottlesBody = new BottleIdBody();
        bottlesBody.setAdd(List.of("bot01", "bot02", "bot01"));
        bottlesBody.setRemove(List.of("bot03", "bot03"));
        bottlesBody.setFinalise(true);

        //Test Bottle has been scan duplicate
        Set<String> bottleIdsAddSet = new HashSet<>(bottlesBody.getAdd());
        Set<String> bottleIdsRemoveSet = new HashSet<>(bottlesBody.getRemove());

        //Bottle ID has been associated with other certificate so not Save
        List<Bottle> bottleAddExist = new ArrayList<>();
        bottleAddExist.add(new Bottle("bot01", "lot01", 1, 2, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));
        bottleAddExist.add(new Bottle("bot02", "lot01", 3, 4, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));

        List<Bottle> bottleRemoveExist = new ArrayList<>();
        bottleRemoveExist.add(new Bottle("bot03", "lot01", 1, 2, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));

        when(bottleRepository
                .findByCertificateIdAndIdIn(certId, bottleIdsRemoveSet))
                .thenReturn(bottleRemoveExist);

        Assertions.assertEquals(bottleRemoveExist.size(), bottleIdsRemoveSet.size());

        bottleIdsAddSet.forEach(add -> {
            if (bottleIdsRemoveSet.contains(add)) {
                assertThrows(BolnisiPilotException.class,
                        () -> bottlesService.updateCertificateAssociations(wineryId, certId, lotId, bottlesBody));
            }
        });

        List<Bottle> bottlesUpdateCertId = new ArrayList<>();
        bottlesUpdateCertId.addAll(bottleAddExist);
        bottlesUpdateCertId.addAll(bottleRemoveExist);

        when(bottleRepository
                .findByLotIdAndIdIn(lotId, bottleIdsAddSet))
                .thenReturn(bottleAddExist);

        Assertions.assertEquals(bottleAddExist.size(), bottleIdsAddSet.size());

        when(bottleRepository
                .findByLotIdAndIdIn(lotId, bottleIdsRemoveSet))
                .thenReturn(bottleRemoveExist);

        Assertions.assertEquals(bottleRemoveExist.size(), bottleIdsRemoveSet.size());
        Assertions.assertNotEquals(clb.getScanningStatus(), ScanningStatus.SCANNING);
        bottlesService.updateCertificateAssociations(wineryId, certId, lotId, bottlesBody);
        verify(certificateLotEntryRepository).save(clb);
        verify(bottleRepository).saveAll(bottlesUpdateCertId);
    }

    @Test
    void test_bottlesIsExitAtRemoveAndAdd() {
        String certId = "certificateId";
        String lotId = "lotId";
        String keycloakUserId = "keycloakUserId";
        String wineryId = "wineryId";

        Winery winery = new Winery(wineryId, keycloakUserId, "", "", "", "", "");
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getKeyCloakUserId()).thenReturn(keycloakUserId);

        Assertions.assertEquals(winery.getKeycloakUserId(), keycloakUserId);

        when(certificateRepository.findByCertificateIdAndTxIdIsNotNullAndCertStatus(certId, CertStatus.ACTIVE)).thenReturn(Arrays.asList(new CertificateEntity()));

        //test certID, lotID is exits with wineryID
        when(certificateLotEntryRepository
                .countByCertificateLotEntryPkCertificateIdAndCertificateLotEntryPkLotIdAndWineryIdAndCertificateCertStatus(certId, lotId, wineryId, CertStatus.ACTIVE))
                .thenReturn(1L);

        //Check CertificateLotEntryEntity has certID and lotID corresponding to param input
        CertificateLotEntryPK certificateLotEntryPK = new CertificateLotEntryPK();
        certificateLotEntryPK.setLotId(lotId);
        certificateLotEntryPK.setCertificateId(certId);
        CertificateLotEntryEntity clb = new CertificateLotEntryEntity();
        clb.setCertificateLotEntryPk(certificateLotEntryPK);
        clb.setWineryId(wineryId);

        when(certificateLotEntryRepository.findByCertificateLotEntryPkCertificateIdAndCertificateLotEntryPkLotIdAndScanningStatusNotAndCertificateCertStatus(certId, lotId, ScanningStatus.APPROVED, CertStatus.ACTIVE))
                .thenReturn(Optional.of(clb));

        BottleIdBody bottlesBody = new BottleIdBody();
        bottlesBody.setAdd(List.of("bot01", "bot02", "bot01"));
        bottlesBody.setRemove(List.of("bot01", "bot03"));
        bottlesBody.setFinalise(true);

        //Test Bottle has been scan duplicate
        Set<String> bottleIdsAddSet = new HashSet<>(bottlesBody.getAdd());
        Set<String> bottleIdsRemoveSet = new HashSet<>(bottlesBody.getRemove());

        bottleIdsAddSet.forEach(add -> {
            if (bottleIdsRemoveSet.contains(add)) {
                assertThrows(BolnisiPilotException.class,
                        () -> bottlesService.updateCertificateAssociations(wineryId, certId, lotId, bottlesBody));
            }
        });

    }

    @Test
    void test_get_WineryId_ThrowsException() {
        String keycloakUserId = "keycloakUserId";
        String keycloakUserIdOther = "keycloakUserIdOther";
        String certId = "certificateId";
        BottleIdBody bottlesBody = new BottleIdBody();

        Winery winery = new Winery(wineryId, keycloakUserId, "", "", "", "", "");
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getKeyCloakUserId()).thenReturn(keycloakUserIdOther);

        Assertions.assertNotEquals(winery.getKeycloakUserId(), keycloakUserIdOther);

        assertThrows(BolnisiPilotException.class,
                () -> bottlesService.updateCertificateAssociations(wineryId, certId, lotId, bottlesBody));
    }

    @Test
    void test_get_WineryId_NotFound_ThrowsException() {
        String certId = "certificateId";
        BottleIdBody bottlesBody = new BottleIdBody();

        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.empty());

        assertThrows(BolnisiPilotException.class,
                () -> bottlesService.updateCertificateAssociations(wineryId, certId, lotId, bottlesBody));
    }

    @Test
    void test_CertID_LotID_is_exits_with_wineryID_ThrowsException() {
        String certId = "certificateId";
        String lotId = "lotId";
        String keycloakUserId = "keycloakUserId";
        String wineryId = "wineryId";

        Winery winery = new Winery(wineryId, keycloakUserId, "", "", "", "", "");
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getKeyCloakUserId()).thenReturn(keycloakUserId);

        Assertions.assertEquals(winery.getKeycloakUserId(), keycloakUserId);

        //test certID, lotID is exits with wineryID
        when(certificateLotEntryRepository
                .countByCertificateLotEntryPkCertificateIdAndCertificateLotEntryPkLotIdAndWineryIdAndCertificateCertStatus(certId, lotId, wineryId, CertStatus.ACTIVE))
                .thenReturn(0L);

        BottleIdBody bottlesBody = new BottleIdBody();
        bottlesBody.setAdd(List.of("bottle", "bottle1", "bottle2", "bottle1"));
        bottlesBody.setFinalise(true);

        assertThrows(BolnisiPilotException.class, () -> bottlesService.updateCertificateAssociations(wineryId, certId, lotId, bottlesBody));
    }


    @Test
    void test_bottleID_Remove_has_been_associated_with_cert() {
        String certId = "certificateId";
        String lotId = "lotId";
        String keycloakUserId = "keycloakUserId";
        String wineryId = "wineryId";

        //test wineryID form token
        Winery winery = new Winery(wineryId, keycloakUserId, "", "", "", "", "");
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getKeyCloakUserId()).thenReturn(keycloakUserId);

        Assertions.assertEquals(winery.getKeycloakUserId(), keycloakUserId);

        when(certificateRepository.findByCertificateIdAndTxIdIsNotNullAndCertStatus(certId, CertStatus.ACTIVE)).thenReturn(Arrays.asList(new CertificateEntity()));

        //test certID, lotID is exits with wineryID
        when(certificateLotEntryRepository
                .countByCertificateLotEntryPkCertificateIdAndCertificateLotEntryPkLotIdAndWineryIdAndCertificateCertStatus(certId, lotId, wineryId, CertStatus.ACTIVE))
                .thenReturn(1L);

        //Check CertificateLotEntryEntity has certID and lotID corresponding to param input
        CertificateLotEntryPK certificateLotEntryPK = new CertificateLotEntryPK();
        certificateLotEntryPK.setLotId(lotId);
        certificateLotEntryPK.setCertificateId(certId);
        CertificateLotEntryEntity clb = new CertificateLotEntryEntity();
        clb.setCertificateLotEntryPk(certificateLotEntryPK);
        clb.setWineryId(wineryId);

        when(certificateLotEntryRepository.findByCertificateLotEntryPkCertificateIdAndCertificateLotEntryPkLotIdAndScanningStatusNotAndCertificateCertStatus(certId, lotId, ScanningStatus.APPROVED, CertStatus.ACTIVE))
                .thenReturn(Optional.of(clb));

        BottleIdBody bottlesBody = new BottleIdBody();
        bottlesBody.setAdd(List.of("bot01", "bot02", "bot01", "bot03"));
        bottlesBody.setRemove(List.of("bot04", "bot05"));
        bottlesBody.setFinalise(true);

        //Test Bottle has been scan duplicate
        Set<String> bottleIdsAddSet = new HashSet<>(bottlesBody.getAdd());
        Set<String> bottleIdsRemoveSet = new HashSet<>(bottlesBody.getRemove());

        //Bottle ID has been associated with other certificate so not Save
        List<Bottle> bottleAddExist = new ArrayList<>();
        bottleAddExist.add(new Bottle("bot01", "lot01", 1, 2, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));
        bottleAddExist.add(new Bottle("bot02", "lot01", 3, 4, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));

        List<Bottle> bottleRemoveExist = new ArrayList<>();
        bottleRemoveExist.add(new Bottle("bot04", "lot01", 1, 2, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));


        when(bottleRepository
                .findByCertificateIdAndIdIn(certId, bottleIdsRemoveSet))
                .thenReturn(bottleRemoveExist);

        Assertions.assertNotEquals(bottleRemoveExist.size(), bottleIdsRemoveSet.size());

        assertThrows(BolnisiPilotException.class,
                () -> bottlesService.updateCertificateAssociations(wineryId, certId, lotId, bottlesBody));
    }


    @Test
    void test_bottleID_Add_has_been_associated_with_lot() {
        String certId = "certificateId";
        String lotId = "lotId";
        String keycloakUserId = "keycloakUserId";
        String wineryId = "wineryId";

        //test wineryID form token
        Winery winery = new Winery(wineryId, keycloakUserId, "", "", "", "", "");
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getKeyCloakUserId()).thenReturn(keycloakUserId);

        Assertions.assertEquals(winery.getKeycloakUserId(), keycloakUserId);

        when(certificateRepository.findByCertificateIdAndTxIdIsNotNullAndCertStatus(certId, CertStatus.ACTIVE)).thenReturn(Arrays.asList(new CertificateEntity()));

        //test certID, lotID is exits with wineryID
        when(certificateLotEntryRepository
                .countByCertificateLotEntryPkCertificateIdAndCertificateLotEntryPkLotIdAndWineryIdAndCertificateCertStatus(certId, lotId, wineryId, CertStatus.ACTIVE))
                .thenReturn(1L);

        //Check CertificateLotEntryEntity has certID and lotID corresponding to param input
        CertificateLotEntryPK certificateLotEntryPK = new CertificateLotEntryPK();
        certificateLotEntryPK.setLotId(lotId);
        certificateLotEntryPK.setCertificateId(certId);
        CertificateLotEntryEntity clb = new CertificateLotEntryEntity();
        clb.setCertificateLotEntryPk(certificateLotEntryPK);
        clb.setWineryId(wineryId);

        when(certificateLotEntryRepository.findByCertificateLotEntryPkCertificateIdAndCertificateLotEntryPkLotIdAndScanningStatusNotAndCertificateCertStatus(certId, lotId, ScanningStatus.APPROVED, CertStatus.ACTIVE))
                .thenReturn(Optional.of(clb));

        BottleIdBody bottlesBody = new BottleIdBody();
        bottlesBody.setAdd(List.of("bot01", "bot02", "bot01", "bot03"));
        bottlesBody.setRemove(List.of("bot04", "bot05"));
        bottlesBody.setFinalise(true);

        //Test Bottle has been scan duplicate
        Set<String> bottleIdsAddSet = new HashSet<>(bottlesBody.getAdd());
        Set<String> bottleIdsRemoveSet = new HashSet<>(bottlesBody.getRemove());

        //Bottle ID has been associated with other certificate so not Save
        List<Bottle> bottleAddExist = new ArrayList<>();
        bottleAddExist.add(new Bottle("bot01", "lot01", 1, 2, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));
        bottleAddExist.add(new Bottle("bot02", "lot01", 3, 4, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));

        List<Bottle> bottleRemoveExist = new ArrayList<>();
        bottleRemoveExist.add(new Bottle("bot04", "lot01", 1, 2, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));
        bottleRemoveExist.add(new Bottle("bot05", "lot02", 1, 2, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));


        when(bottleRepository
                .findByCertificateIdAndIdIn(certId, bottleIdsRemoveSet))
                .thenReturn(bottleRemoveExist);

        Assertions.assertEquals(bottleRemoveExist.size(), bottleIdsRemoveSet.size());

        bottleIdsAddSet.forEach(add -> {
            if (bottleIdsRemoveSet.contains(add)) {
                assertThrows(BolnisiPilotException.class,
                        () -> bottlesService.updateCertificateAssociations(wineryId, certId, lotId, bottlesBody));
            }
        });

        when(bottleRepository
                .findByLotIdAndIdIn(lotId, bottleIdsAddSet))
                .thenReturn(bottleAddExist);

        Assertions.assertNotEquals(bottleAddExist.size(), bottleIdsAddSet.size());

        assertThrows(BolnisiPilotException.class,
                () -> bottlesService.updateCertificateAssociations(wineryId, certId, lotId, bottlesBody));
    }

    @Test
    void test_bottleID_Remove_has_been_associated_with_lot() {
        String certId = "certificateId";
        String lotId = "lotId";
        String keycloakUserId = "keycloakUserId";
        String wineryId = "wineryId";

        //test wineryID form token
        Winery winery = new Winery(wineryId, keycloakUserId, "", "", "", "", "");
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getKeyCloakUserId()).thenReturn(keycloakUserId);

        Assertions.assertEquals(winery.getKeycloakUserId(), keycloakUserId);

        when(certificateRepository.findByCertificateIdAndTxIdIsNotNullAndCertStatus(certId, CertStatus.ACTIVE)).thenReturn(Arrays.asList(new CertificateEntity()));

        //test certID, lotID is exits with wineryID
        when(certificateLotEntryRepository
                .countByCertificateLotEntryPkCertificateIdAndCertificateLotEntryPkLotIdAndWineryIdAndCertificateCertStatus(certId, lotId, wineryId, CertStatus.ACTIVE))
                .thenReturn(1L);

        //Check CertificateLotEntryEntity has certID and lotID corresponding to param input
        CertificateLotEntryPK certificateLotEntryPK = new CertificateLotEntryPK();
        certificateLotEntryPK.setLotId(lotId);
        certificateLotEntryPK.setCertificateId(certId);
        CertificateLotEntryEntity clb = new CertificateLotEntryEntity();
        clb.setCertificateLotEntryPk(certificateLotEntryPK);
        clb.setWineryId(wineryId);

        when(certificateLotEntryRepository.findByCertificateLotEntryPkCertificateIdAndCertificateLotEntryPkLotIdAndScanningStatusNotAndCertificateCertStatus(certId, lotId, ScanningStatus.APPROVED, CertStatus.ACTIVE))
                .thenReturn(Optional.of(clb));

        BottleIdBody bottlesBody = new BottleIdBody();
        bottlesBody.setAdd(List.of("bot01", "bot02", "bot01", "bot03"));
        bottlesBody.setRemove(List.of("bot04", "bot05"));
        bottlesBody.setFinalise(true);

        //Test Bottle has been scan duplicate
        Set<String> bottleIdsAddSet = new HashSet<>(bottlesBody.getAdd());
        Set<String> bottleIdsRemoveSet = new HashSet<>(bottlesBody.getRemove());

        //Bottle ID has been associated with other certificate so not Save
        List<Bottle> bottleAddExist = new ArrayList<>();
        bottleAddExist.add(new Bottle("bot01","lot01", 1, 2, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));
        bottleAddExist.add(new Bottle("bot02","lot01", 3, 4, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));
        bottleAddExist.add(new Bottle("bot03","lot01", 3, 4, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));

        List<Bottle> bottleRemoveExistWithLot = new ArrayList<>();
        bottleRemoveExistWithLot.add(new Bottle("bot04","lot01", 1, 2, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));

        List<Bottle> bottleRemoveExistWithCert = new ArrayList<>();
        bottleRemoveExistWithCert.add(new Bottle("bot04","lot01", 1, 2, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));
        bottleRemoveExistWithCert.add(new Bottle("bot05","lot02", 1, 2, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));

        when(bottleRepository
                .findByCertificateIdAndIdIn(certId, bottleIdsRemoveSet))
                .thenReturn(bottleRemoveExistWithCert);

        Assertions.assertEquals(bottleRemoveExistWithCert.size(), bottleIdsRemoveSet.size());


        bottleIdsAddSet.forEach(add -> {
            if (bottleIdsRemoveSet.contains(add)) {
                assertThrows(BolnisiPilotException.class,
                        () -> bottlesService.updateCertificateAssociations(wineryId, certId, lotId, bottlesBody));
            }
        });

        when(bottleRepository
                .findByLotIdAndIdIn(lotId, bottleIdsAddSet))
                .thenReturn(bottleAddExist);

        Assertions.assertEquals(bottleAddExist.size(), bottleIdsAddSet.size());

        when(bottleRepository
                .findByLotIdAndIdIn(lotId, bottleIdsRemoveSet))
                .thenReturn(bottleRemoveExistWithLot);

        Assertions.assertNotEquals(bottleRemoveExistWithLot.size(), bottleIdsRemoveSet.size());

        assertThrows(BolnisiPilotException.class,
                () ->  bottlesService.updateCertificateAssociations(wineryId, certId, lotId, bottlesBody));
    }

    @Test
    void test_certificateLotEntryEntity_has_not_certID_and_lotID_ThrowsException() {
        String certId = "certificateId";
        String lotId = "lotId";
        String keycloakUserId = "keycloakUserId";
        String wineryId = "wineryId";

        //test wineryID form token
        Winery winery = new Winery(wineryId, keycloakUserId, "", "", "", "", "");
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getKeyCloakUserId()).thenReturn(keycloakUserId);

        Assertions.assertEquals(winery.getKeycloakUserId(), keycloakUserId);

        when(certificateRepository.findByCertificateIdAndTxIdIsNotNullAndCertStatus(certId, CertStatus.ACTIVE)).thenReturn(Arrays.asList(new CertificateEntity()));

        //test certID, lotID is exits with wineryID
        when(certificateLotEntryRepository
                .countByCertificateLotEntryPkCertificateIdAndCertificateLotEntryPkLotIdAndWineryIdAndCertificateCertStatus(certId, lotId, wineryId, CertStatus.ACTIVE))
                .thenReturn(1L);

        BottleIdBody bottlesBody = new BottleIdBody();
        bottlesBody.setAdd(List.of("bottle", "bottle1", "bottle2"));
        bottlesBody.setRemove(List.of());
        bottlesBody.setFinalise(true);

        CertificateLotEntryPK certificateLotEntryPK = new CertificateLotEntryPK();
        certificateLotEntryPK.setLotId(lotId);
        certificateLotEntryPK.setCertificateId(certId);

        CertificateLotEntryEntity clb = new CertificateLotEntryEntity();
        clb.setCertificateLotEntryPk(certificateLotEntryPK);
        clb.setWineryId(wineryId);


        //Check CertificateLotEntryEntity has certID and lotID corresponding to param input
        when(certificateLotEntryRepository
                .findByCertificateLotEntryPkCertificateIdAndCertificateLotEntryPkLotIdAndScanningStatusNotAndCertificateCertStatus(
                        certId, lotId, ScanningStatus.APPROVED, CertStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(BolnisiPilotException.class,
                () -> bottlesService.updateCertificateAssociations(wineryId, certId, lotId, bottlesBody));
    }

    @Test
    void test_bottleId_have_been_associated_ThrowsException() {
        String certId = "certificateId";
        String lotId = "lotId";
        String keycloakUserId = "keycloakUserId";
        String wineryId = "wineryId";

        BottleIdBody bottlesBody = new BottleIdBody();
        bottlesBody.setAdd(List.of("bot01", "bot02", "bot01"));
        bottlesBody.setRemove(List.of("bot02", "bot03"));
        bottlesBody.setFinalise(true);

        Set<String> bottleIds = Set.of("bot01", "bot02", "bot03");
        Bottle bottle = new Bottle();
        bottle.setId("bot01");
        List<Bottle> bottles = List.of(bottle);

        //test wineryID form token
        Winery winery = new Winery(wineryId, keycloakUserId, "", "", "", "", "");
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getKeyCloakUserId()).thenReturn(keycloakUserId);

        Assertions.assertEquals(winery.getKeycloakUserId(), keycloakUserId);

        when(bottleRepository.findByIdInAndCertificateIdNotNull(bottleIds))
                .thenReturn(bottles);
        assertThrows(BolnisiPilotException.class,
                () -> bottlesService.updateCertificateAssociations(wineryId, certId, lotId, bottlesBody));
    }

    @Test
    void test_bottle_can_only_be_in_1_of_2_lists_ThrowsException() {
        String certId = "certificateId";
        String lotId = "lotId";
        String keycloakUserId = "keycloakUserId";
        String wineryId = "wineryId";

        Winery winery = new Winery(wineryId, keycloakUserId, "", "", "", "", "");

        when(wineryRepository.findByWineryId(wineryId)).
                thenReturn(Optional.of(winery));

        when(securityContextHolderUtil.getKeyCloakUserId()).
                thenReturn(keycloakUserId);

        Assertions.assertEquals(winery.getKeycloakUserId(), keycloakUserId);

        when(certificateRepository.findByCertificateIdAndTxIdIsNotNullAndCertStatus(certId, CertStatus.ACTIVE)).thenReturn(Arrays.asList(new CertificateEntity()));

        //test certID, lotID is exits with wineryID
        when(certificateLotEntryRepository
                .countByCertificateLotEntryPkCertificateIdAndCertificateLotEntryPkLotIdAndWineryIdAndCertificateCertStatus(certId, lotId, wineryId, CertStatus.ACTIVE))
                .thenReturn(1L);

        //Check CertificateLotEntryEntity has certID and lotID corresponding to param input
        CertificateLotEntryPK certificateLotEntryPK = new CertificateLotEntryPK();
        certificateLotEntryPK.setLotId(lotId);
        certificateLotEntryPK.setCertificateId(certId);
        CertificateLotEntryEntity clb = new CertificateLotEntryEntity();
        clb.setCertificateLotEntryPk(certificateLotEntryPK);
        clb.setWineryId(wineryId);
        clb.setScanningStatus(ScanningStatus.NOT_STARTED);

        when(certificateLotEntryRepository.findByCertificateLotEntryPkCertificateIdAndCertificateLotEntryPkLotIdAndScanningStatusNotAndCertificateCertStatus(certId, lotId, ScanningStatus.APPROVED, CertStatus.ACTIVE))
                .thenReturn(Optional.of(clb));

        BottleIdBody bottlesBody = new BottleIdBody();
        bottlesBody.setAdd(List.of("bot01", "bot02", "bot01"));
        bottlesBody.setRemove(List.of("bot02", "bot03"));
        bottlesBody.setFinalise(true);

        //Test Bottle has been scan duplicate
        Set<String> bottleIdsAddSet = new HashSet<>(bottlesBody.getAdd());
        Set<String> bottleIdsRemoveSet = new HashSet<>(bottlesBody.getRemove());

        //Bottle ID has been associated with other certificate so not Save
        List<Bottle> bottleAddExist = new ArrayList<>();
        bottleAddExist.add(new Bottle("bot01", "lot01", 1, 2, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));
        bottleAddExist.add(new Bottle("bot02", "lot01", 3, 4, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));

        List<Bottle> bottleRemoveExist = new ArrayList<>();
        bottleRemoveExist.add(new Bottle("bot02", "lot01", 1, 2, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));
        bottleRemoveExist.add(new Bottle("bot03", "lot01", 1, 2, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));

        when(bottleRepository
                .findByCertificateIdAndIdIn(certId, bottleIdsRemoveSet))
                .thenReturn(bottleRemoveExist);

        Assertions.assertEquals(bottleRemoveExist.size(), bottleIdsRemoveSet.size());

        bottleIdsAddSet.forEach(add ->
        {
            if (bottleIdsRemoveSet.contains(add)) {
                assertThrows(BolnisiPilotException.class,
                        () -> bottlesService.updateCertificateAssociations(wineryId, certId, lotId, bottlesBody));
            }
        });
    }

    @Test
    void test_saveWhenFinaliseIsTrue() {
        String certId = "certificateId";
        String lotId = "lotId";
        String keycloakUserId = "keycloakUserId";
        String wineryId = "wineryId";

        Winery winery = new Winery(wineryId, keycloakUserId, wineryName, wineryRsCode, privateKey, publicKey, salt);
        List<String> roles = List.of(Role.ADMIN.toString());

        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getListRoles()).thenReturn(roles);
        when(securityContextHolderUtil.getKeyCloakUserId()).thenReturn(keycloakUserId);

        Assertions.assertEquals(winery.getKeycloakUserId(), keycloakUserId);

        when(certificateRepository.findByCertificateIdAndTxIdIsNotNullAndCertStatus(certId, CertStatus.ACTIVE)).thenReturn(Arrays.asList(new CertificateEntity()));

        //test certID, lotID is exits with wineryID
        when(certificateLotEntryRepository
                .countByCertificateLotEntryPkCertificateIdAndCertificateLotEntryPkLotIdAndWineryIdAndCertificateCertStatus(certId, lotId, wineryId, CertStatus.ACTIVE))
                .thenReturn(1L);

        //Check CertificateLotEntryEntity has certID and lotID corresponding to param input
        CertificateLotEntryPK certificateLotEntryPK = new CertificateLotEntryPK();
        certificateLotEntryPK.setLotId(lotId);
        certificateLotEntryPK.setCertificateId(certId);
        CertificateLotEntryEntity clb = new CertificateLotEntryEntity();
        clb.setCertificateLotEntryPk(certificateLotEntryPK);
        clb.setWineryId(wineryId);
        clb.setScanningStatus(ScanningStatus.NOT_STARTED);

        when(certificateLotEntryRepository.findByCertificateLotEntryPkCertificateIdAndCertificateLotEntryPkLotIdAndScanningStatusNotAndCertificateCertStatus(certId, lotId, ScanningStatus.APPROVED, CertStatus.ACTIVE))
                .thenReturn(Optional.of(clb));

        BottleIdBody bottlesBody = new BottleIdBody();
        bottlesBody.setAdd(List.of("bot01", "bot02", "bot01"));
        bottlesBody.setRemove(List.of("bot03", "bot03"));
        bottlesBody.setFinalise(true);

        //Test Bottle has been scan duplicate
        Set<String> bottleIdsAddSet = new HashSet<>(bottlesBody.getAdd());
        Set<String> bottleIdsRemoveSet = new HashSet<>(bottlesBody.getRemove());

        //Bottle ID has been associated with other certificate so not Save
        List<Bottle> bottleAddExist = new ArrayList<>();
        bottleAddExist.add(new Bottle("bot01", "lot01", 1, 2, certId, wineryId));
        bottleAddExist.add(new Bottle("bot02", "lot01", 3, 4, certId, wineryId));

        List<Bottle> bottleRemoveExist = new ArrayList<>();
        bottleRemoveExist.add(new Bottle("bot03", "lot01", 1, 2, certId, wineryId));

        when(bottleRepository
                .findByCertificateIdAndIdIn(certId, bottleIdsRemoveSet))
                .thenReturn(bottleRemoveExist);

        Assertions.assertEquals(bottleRemoveExist.size(), bottleIdsRemoveSet.size());

        bottleIdsAddSet.forEach(add -> {
            if (bottleIdsRemoveSet.contains(add)) {
                assertThrows(BolnisiPilotException.class,
                        () -> bottlesService.updateCertificateAssociations(wineryId, certId, lotId, bottlesBody));
            }
        });

        List<Bottle> bottlesUpdateCertId = new ArrayList<>();
        bottlesUpdateCertId.addAll(bottleAddExist);
        bottlesUpdateCertId.addAll(bottleRemoveExist);

        when(bottleRepository
                .findByLotIdAndIdIn(lotId, bottleIdsAddSet))
                .thenReturn(bottleAddExist);

        Assertions.assertEquals(bottleAddExist.size(), bottleIdsAddSet.size());

        when(bottleRepository
                .findByLotIdAndIdIn(lotId, bottleIdsRemoveSet))
                .thenReturn(bottleRemoveExist);

        Assertions.assertEquals(bottleRemoveExist.size(), bottleIdsRemoveSet.size());
        Assertions.assertNotEquals(clb.getScanningStatus(), ScanningStatus.SCANNING);
        bottlesService.updateCertificateAssociations(wineryId, certId, lotId, bottlesBody);
        verify(certificateLotEntryRepository).save(clb);
        verify(bottleRepository).saveAll(bottlesUpdateCertId);
        ArgumentCaptor<CertificateLotEntryEntity> saveAllCaptor = ArgumentCaptor.forClass(CertificateLotEntryEntity.class);
        verify(certificateLotEntryRepository, times(1)).save(saveAllCaptor.capture());
        CertificateLotEntryEntity value = saveAllCaptor.getValue();
        Assertions.assertEquals(value.getCertificateLotEntryPk().getCertificateId(), certId);
        Assertions.assertEquals(value.getCertificateLotEntryPk().getLotId(), lotId);
        Assertions.assertEquals(value.getScanningStatus(), ScanningStatus.APPROVED);
    }

    @Test
    void test_ApprovedBottles_when_cert_is_not_on_chain_ThrowsException() {
        String certId = "certificateId";
        String lotId = "lotId";
        String keycloakUserId = "keycloakUserId";
        String wineryId = "wineryId";

        BottleIdBody bottlesBody = new BottleIdBody();
        bottlesBody.setAdd(List.of("bot01", "bot02", "bot01"));
        bottlesBody.setRemove(List.of("bot02", "bot03"));
        bottlesBody.setFinalise(true);

        //test wineryID form token
        Winery winery = new Winery(wineryId, keycloakUserId, "", "", "", "", "");
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getKeyCloakUserId()).thenReturn(keycloakUserId);

        Assertions.assertEquals(winery.getKeycloakUserId(), keycloakUserId);

        when(certificateRepository.findByCertificateIdAndTxIdIsNotNullAndCertStatus(certId, CertStatus.ACTIVE)).thenReturn(Arrays.asList());

        assertThrows(BolnisiPilotException.class,
                () -> bottlesService.updateCertificateAssociations(wineryId, certId, lotId, bottlesBody));
    }

    @Test
    void test_convertRangeToBottleIdBody_ParamInputIsNumeric() {
        String certId = "certificateId";
        String lotId = "lotId";
        String wineryId = "wineryId";

        BottleRangeBody bottleRangeBody = new BottleRangeBody();
        bottleRangeBody.setStartRange("1");
        bottleRangeBody.setEndRange("10");
        bottleRangeBody.setIsSequentialNumber(true);
        bottleRangeBody.setFinalise(true);

        List<Bottle> bottleFromRange = new ArrayList<>();
        bottleFromRange.add(new Bottle("5XEDIMQBXN041SM8312606036826711", "lot01", 1, 2, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));
        bottleFromRange.add(new Bottle("5XEDIMQBXN041SM8312606036826712", "lot01", 2, 4, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));
        bottleFromRange.add(new Bottle("5XEDIMQBXN041SM8312606036826713", "lot01", 3, 4, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));

        when(bottleRepository.findBySequentialNumberBetween(1, 10)).thenReturn(bottleFromRange);
        doNothing().when(bottlesService).updateCertificateAssociations(eq(wineryId), eq(certId), eq(lotId), any(BottleIdBody.class));

        BottleIdBody bottleBody = bottlesService.convertBottleRangeBodyToBottleIdBody(bottleRangeBody);
        Assertions.assertEquals(bottleBody.getFinalise(), bottleRangeBody.getFinalise());
        Assertions.assertEquals(bottleBody.getAdd().get(0), "5XEDIMQBXN041SM8312606036826711");
        Assertions.assertEquals(bottleBody.getAdd().get(1), "5XEDIMQBXN041SM8312606036826712");
        Assertions.assertEquals(bottleBody.getAdd().get(2), "5XEDIMQBXN041SM8312606036826713");
        Assertions.assertEquals(bottleBody.getAdd().size(), 3);
        Assertions.assertEquals(bottleBody.getRemove().size(), 0);
    }

    @Test
    void test_convertRangeToBottleIdBody_ParamInputIsNotNumeric() {
        String certId = "certificateId";
        String lotId = "lotId";
        String wineryId = "wineryId";

        BottleRangeBody bottleRangeBody = new BottleRangeBody();
        bottleRangeBody.setStartRange("5XEDIMQBXN041SM8312606036826711");
        bottleRangeBody.setEndRange("5XEDIMQBXN041SM8312606036826715");
        bottleRangeBody.setIsSequentialNumber(false);
        bottleRangeBody.setFinalise(true);

        List<Bottle> bottleCorrespondingExtendId = new ArrayList<>();
        bottleCorrespondingExtendId.add(new Bottle("5XEDIMQBXN041SM8312606036826711", "lot01", 1, 2, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));
        bottleCorrespondingExtendId.add(new Bottle("5XEDIMQBXN041SM8312606036826715", "lot01", 10, 4, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));

        List<Bottle> bottleInRange = new ArrayList<>();
        bottleInRange.add(new Bottle("5XEDIMQBXN041SM8312606036826711", "lot01", 1, 2, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));
        bottleInRange.add(new Bottle("5XEDIMQBXN041SM8312606036826712", "lot01", 7, 4, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));
        bottleInRange.add(new Bottle("5XEDIMQBXN041SM8312606036826713", "lot01", 8, 4, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));
        bottleInRange.add(new Bottle("5XEDIMQBXN041SM8312606036826715", "lot01", 10, 4, certId, wineryId, Constants.SCANTRUST.STATUS.NOT_UPDATED, CertUpdateStatus.NOT_UPDATED));

        List<String> bottleIdsRange = new ArrayList<>();
        bottleIdsRange.add(bottleRangeBody.getStartRange());
        bottleIdsRange.add(bottleRangeBody.getEndRange());
        when(bottleRepository.findByIdIn(bottleIdsRange)).thenReturn(bottleCorrespondingExtendId);

        List<Integer> lstSequentialNumber = bottleCorrespondingExtendId.stream().map(Bottle::getSequentialNumber).toList();

        when(bottleRepository.findBySequentialNumberBetween(lstSequentialNumber.get(0), lstSequentialNumber.get(1))).thenReturn(bottleInRange);

        doNothing().when(bottlesService).updateCertificateAssociations(eq(wineryId), eq(certId), eq(lotId), any(BottleIdBody.class));

        BottleIdBody bottleBody = bottlesService.convertBottleRangeBodyToBottleIdBody(bottleRangeBody);
        Assertions.assertEquals(bottleBody.getFinalise(), bottleRangeBody.getFinalise());
        Assertions.assertEquals(bottleBody.getAdd().get(0), "5XEDIMQBXN041SM8312606036826711");
        Assertions.assertEquals(bottleBody.getAdd().get(1), "5XEDIMQBXN041SM8312606036826712");
        Assertions.assertEquals(bottleBody.getAdd().get(2), "5XEDIMQBXN041SM8312606036826713");
        Assertions.assertEquals(bottleBody.getAdd().get(3), "5XEDIMQBXN041SM8312606036826715");
        Assertions.assertEquals(bottleBody.getAdd().size(), 4);
        Assertions.assertEquals(bottleBody.getRemove().size(), 0);
    }
}