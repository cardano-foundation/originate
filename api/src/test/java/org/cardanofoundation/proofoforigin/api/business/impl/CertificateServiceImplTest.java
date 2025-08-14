package org.cardanofoundation.proofoforigin.api.business.impl;

import org.cardanofoundation.proofoforigin.api.configuration.AppConfig;
import org.cardanofoundation.proofoforigin.api.constants.CertStatus;
import org.cardanofoundation.proofoforigin.api.constants.CertUpdateStatus;
import org.cardanofoundation.proofoforigin.api.constants.Role;
import org.cardanofoundation.proofoforigin.api.constants.ScanningStatus;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.CertificateDataDTO;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.CertificateRevokeDTO;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.Unit;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.Unit.MetabusJobType;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.response.JobResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.CertBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.CertRequest;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.CertLotEntryBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.CertsResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.WineryCertsResponse;
import org.cardanofoundation.proofoforigin.api.exceptions.OriginatePilotErrors;
import org.cardanofoundation.proofoforigin.api.exceptions.OriginatePilotException;
import org.cardanofoundation.proofoforigin.api.repository.BottleRepository;
import org.cardanofoundation.proofoforigin.api.repository.CertificateLotEntryRepository;
import org.cardanofoundation.proofoforigin.api.repository.CertificateRepository;
import org.cardanofoundation.proofoforigin.api.repository.WineryRepository;
import org.cardanofoundation.proofoforigin.api.repository.entities.*;
import org.cardanofoundation.proofoforigin.api.utils.SecurityContextHolderUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.AopContext;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class CertificateServiceImplTest {

    @Mock
    CertificateLotEntryRepository certificateLotEntryRepository;

    @Mock
    CertificateRepository certificateRepository;

    @Mock
    MetabusCallerServiceImpl metabusCallerService;

    @InjectMocks
    @Spy
    CertificateServiceImpl service;

    @Mock
    WineryRepository wineryRepository;

    @Mock
    SecurityContextHolderUtil securityContextHolderUtil;

    @Mock
    BottleRepository bottleRepository;

    @Mock
    AppConfig.CertificateVerificationConfig certificateVerificationConfig = new AppConfig.CertificateVerificationConfig();

    private static final String pubKey = "CV-aGlld3nVdgnhoZK0D36Wk-9aIMlZjZOK2XhPMnkQ";
    private static final String createSignature = "eyJhbGciOiJFZERTQSJ9.TyZvSeCptdmfWpbZDxmdei3y97d68AROpmYA-Az0YyPP3UAmgzKBVpF8XBI19OIiKBOH8U2y1ydCsJXebe0tBw";
    private static final String revokeSignature = "eyJhbGciOiJFZERTQSJ9.-h8ljGMaO6aq5ha5raG3Ms2RFFNoLkcPqiB3cNhwFutNrwXk9uia3TPgh2-02TvypRLdjjtETphQIrV-PjuPDw";
    private static final String wineryId = "WINERY-001";
    private static final String certId = "CERT-001";
    private static final String certType = "TYPE-001";
    private static final String certNumber = "GE-12345";
    private static final String exportCountry = "USA";
    private static final String examNumber = "Analysis 2a";
    private static final String tastingProtocolNumber = "Tasting 5p";

    @BeforeEach
    public  void initBean() {
        certificateVerificationConfig.signatureVerificationDisabled = false;
    }

    @Test
    void test_create_cert_success() {
        CertLotEntryBody lotEntry = mockLotEntry();
        CertRequest certRequest = validCertRequest(lotEntry);
        Winery winery = new Winery(wineryId, "keycloakUserId", "wineryName", "wineryRsCode", "privateKey", "publicKey", "salt");
        when(certificateRepository.existsById(certId)).thenReturn(false);
        when(certificateRepository.save(any(CertificateEntity.class))).thenAnswer(invocationOnMock -> {
            CertificateEntity certificate = invocationOnMock.getArgument(0);
            assertEquals(certificate.getCertificateId(), certId);
            assertEquals(certificate.getCertificateNumber(), certNumber);
            assertEquals(certificate.getCertificateType(), certType);
            assertEquals(certificate.getExportCountry(), exportCountry);
            assertEquals(certificate.getExamProtocolNumber(), examNumber);
            assertEquals(certificate.getTastingProtocolNumber(), tastingProtocolNumber);
            assertEquals(certificate.getSignature(), createSignature);
            assertEquals(certificate.getPubKey(), pubKey);
            return certificate;
        });
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(certificateLotEntryRepository.saveAll(any(ArrayList.class))).thenAnswer(invocationOnMock -> {
            ArrayList<CertificateLotEntryEntity> list = invocationOnMock.getArgument(0);
            CertificateLotEntryEntity certificateLotEntryEntity = list.get(0);
            assertEquals(certificateLotEntryEntity.getCertificateLotEntryPk().getLotId(), lotEntry.getLotNumber());
            assertEquals(certificateLotEntryEntity.getOrigin(), lotEntry.getOrigin());
            assertEquals(certificateLotEntryEntity.getViticultureArea(), lotEntry.getViticultureArea());
            assertEquals(certificateLotEntryEntity.getType(), lotEntry.getType());
            assertEquals(certificateLotEntryEntity.getColor(), lotEntry.getColor());
            assertEquals(certificateLotEntryEntity.getSugarContentCategory(), lotEntry.getSugarContentCategory());
            assertEquals(certificateLotEntryEntity.getGrapeVariety(), lotEntry.getGrapeVariety());
            assertEquals(certificateLotEntryEntity.getHarvestYear(), lotEntry.getHarvestYear());
            assertEquals(certificateLotEntryEntity.getDelayedOnChacha(), lotEntry.getDelayedOnChacha());
            assertEquals(certificateLotEntryEntity.getBottleType(), lotEntry.getBottleType());
            assertEquals(certificateLotEntryEntity.getBottlingDate(), lotEntry.getBottlingDate());
            assertEquals(certificateLotEntryEntity.getBottleVolume(), lotEntry.getBottleVolume());
            assertEquals(certificateLotEntryEntity.getBottleCountInLot(), lotEntry.getBottleCountInLot());

            return list;
        });
        when(metabusCallerService.createJob(any(CertificateDataDTO.class), any(Unit.MetabusJobType.class), any(String.class), any())).thenReturn(JobResponse.builder().id(1L).build());
        // Act
        service.createCertificate(certId, wineryId, certRequest);
        // Assert
        verify(certificateRepository, times(2)).save(any(CertificateEntity.class));
        verify(certificateLotEntryRepository).saveAll(any(ArrayList.class));
    }

    @Test
    void test_create_cert_fail_duplicate() {
        when(certificateRepository.existsById("cert_id")).thenReturn(true);
        Exception exception = assertThrows(OriginatePilotException.class, () -> service.createCertificate("cert_id", "winery_id", new CertRequest()));
        assertEquals(OriginatePilotErrors.CONFLICT.getMessage(), exception.getMessage());
    }

    @Test
    void test_create_cert_fail_missing_rscode() {
        Winery winery = new Winery(wineryId, "keycloakUserId", "wineryName", null, "privateKey", "publicKey", "salt");
        when(certificateRepository.existsById("cert_id")).thenReturn(false);
        when(wineryRepository.findByWineryId("winery_id")).thenReturn(Optional.of(winery));
        Exception exception = assertThrows(OriginatePilotException.class, () -> service.createCertificate("cert_id", "winery_id", new CertRequest()));
        assertEquals(exception.getMessage(), OriginatePilotErrors.WINERY_MISSING_RS_CODE.getMessage());
    }

    @Test
    void test_create_cert_fail_invalid_signature_formatA() {
        Winery winery = new Winery(wineryId, "keycloakUserId", "wineryName", "rsCode", "privateKey", "publicKey", "salt");
        when(certificateRepository.existsById("cert_id")).thenReturn(false);
        when(wineryRepository.findByWineryId("winery_id")).thenReturn(Optional.of(winery));
        CertLotEntryBody lotEntry = mockLotEntry();
        CertRequest certRequest = validCertRequest(lotEntry);
        certRequest.setSignature("signature");
        Exception exception = assertThrows(OriginatePilotException.class, () -> service.createCertificate("cert_id", "winery_id", certRequest));
        assertEquals(OriginatePilotErrors.SIGNATURE_INVALID_OR_FAILED_VERIFICATION.getMessage(), exception.getMessage());
    }

    @Test
    void test_create_cert_fail_invalid_signature_formatB() {
        Winery winery = new Winery(wineryId, "keycloakUserId", "wineryName", "rsCode", "privateKey", "publicKey", "salt");
        when(certificateRepository.existsById("cert_id")).thenReturn(false);
        when(wineryRepository.findByWineryId("winery_id")).thenReturn(Optional.of(winery));
        CertLotEntryBody lotEntry = mockLotEntry();
        CertRequest certRequest = validCertRequest(lotEntry);
        certRequest.setSignature("header.signature");
        Exception exception = assertThrows(OriginatePilotException.class, () -> service.createCertificate("cert_id", "winery_id", certRequest));
        assertEquals(OriginatePilotErrors.SIGNATURE_INVALID_OR_FAILED_VERIFICATION.getMessage(), exception.getMessage());
    }

    @Test
    void test_create_cert_fail_invalid_pubkey() {
        Winery winery = new Winery(wineryId, "keycloakUserId", "wineryName", "rsCode", "privateKey", "publicKey", "salt");
        when(certificateRepository.existsById("cert_id")).thenReturn(false);
        when(wineryRepository.findByWineryId("winery_id")).thenReturn(Optional.of(winery));
        CertLotEntryBody lotEntry = mockLotEntry();
        CertRequest certRequest = validCertRequest(lotEntry);
        certRequest.setPublicKeyBase64Url("notlength32");
        Exception exception = assertThrows(OriginatePilotException.class, () -> service.createCertificate("cert_id", "winery_id", certRequest));
        assertEquals(OriginatePilotErrors.SIGNATURE_INVALID_OR_FAILED_VERIFICATION.getMessage(), exception.getMessage());
    }

    @Test
    void test_create_cert_fail_invalid_signature() {
        // Fails as wineryRsCode is different here
        Winery winery = new Winery(wineryId, "keycloakUserId", "wineryName", "rsCode", "privateKey", "publicKey", "salt");
        when(certificateRepository.existsById("cert_id")).thenReturn(false);
        when(wineryRepository.findByWineryId("winery_id")).thenReturn(Optional.of(winery));

        CertLotEntryBody lotEntry = mockLotEntry();
        CertRequest certRequest = validCertRequest(lotEntry);
        Exception exception = assertThrows(OriginatePilotException.class, () -> service.createCertificate("cert_id", "winery_id", certRequest));
        assertEquals(OriginatePilotErrors.SIGNATURE_INVALID_OR_FAILED_VERIFICATION.getMessage(), exception.getMessage());
    }

    @Test
    void test_create_cert_when_winery_does_not_exist() {
        CertRequest mockCertRequest = new CertRequest();
        when(wineryRepository.findByWineryId("winery_id")).thenReturn(Optional.empty());
        Exception exception = assertThrows(OriginatePilotException.class, () -> service.createCertificate("cert_id", "winery_id", mockCertRequest));
        assertEquals(OriginatePilotErrors.WINERY_NOT_FOUND.getMessage(), exception.getMessage());
    }

    @Test
    void test_get_all() {
        List<CertificateLotEntryEntity> list = new ArrayList<>();
        CertificateLotEntryEntity dto = CertificateLotEntryEntity.builder()
                .wineryId("winery_id")
                .scanningStatus(ScanningStatus.SCANNING)
                .certificate(CertificateEntity.builder()
                        .certificateId("cert_id")
                        .certificateType("cert_type")
                        .certificateNumber("cert_number")
                        .build())
                .certificateLotEntryPk(CertificateLotEntryPK.builder()
                        .certificateId("cert_id")
                        .lotId("lot_id")
                        .build())
                .build();
        list.add(dto);

        when(certificateLotEntryRepository.findAllByCertificateCertStatus(CertStatus.ACTIVE)).thenReturn(list);
        List<WineryCertsResponse> actual = service.getAllCertAll();

        assertEquals(1, actual.size());
    }

    @Test
    void test_get_by_id_with_wineryId_not_existed() {
        //GIVEN
        String wineryId = "wi01";

        //WHEN
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.empty());

        //THEN
        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> service.getByWineryId(wineryId));
        assertEquals(OriginatePilotErrors.NOT_FOUND, actualException.getError());
    }

    @Test
    void test_get_by_id_with_admin_role() {
        //GIVEN
        String wineryId = "wi01";
        Winery winery = new Winery(wineryId, "keycloakUserId", "wineryName", "wineryRsCode", "privateKey", "publicKey", "salt");
        List<String> roles = List.of(Role.ADMIN.toString());

        List<CertificateLotEntryEntity> certificateLotEntryEntityList = new ArrayList<>();
        CertificateLotEntryEntity certificateLotEntryEntity = CertificateLotEntryEntity.builder()
                .wineryId("winner_id")
                .scanningStatus(ScanningStatus.SCANNING)
                .certificateLotEntryPk(CertificateLotEntryPK.builder()
                        .certificateId("cert_id")
                        .lotId("lot_id")
                        .build())
                .certificate(CertificateEntity.builder()
                        .certificateId("cert_id")
                        .certificateType("cert_type")
                        .certificateNumber("cert_number")
                        .build())
                .build();
        certificateLotEntryEntityList.add(certificateLotEntryEntity);

        //WHEN
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getListRoles()).thenReturn(roles);
        when(certificateLotEntryRepository.findByWineryIdAndCertificateTxIdIsNotNullAndCertificateCertStatus(wineryId, CertStatus.ACTIVE)).thenReturn(certificateLotEntryEntityList);

        //THEN
        List<CertsResponse> actualCertsResponses = service.getByWineryId(wineryId);
        assertEquals(1, actualCertsResponses.size());
        CertsResponse certsResponse = actualCertsResponses.get(0);
        assertEquals("cert_id", certsResponse.getId());
        assertEquals(1, certsResponse.getLotEntries().size());
    }

    @Test
    void test_get_by_id_with_other_winery() {
        //GIVEN
        String wineryId = "wi01";
        Winery winery = new Winery(wineryId, "keycloakUserId", "wineryName", "wineryRsCode", "privateKey", "publicKey", "salt");
        List<String> roles = List.of(Role.WINERY.toString());

        //WHEN
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getListRoles()).thenReturn(roles);
        when(securityContextHolderUtil.getKeyCloakUserId()).thenReturn("anotherKeycloakUserId");

        //THEN
        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> service.getByWineryId(wineryId));
        assertEquals(OriginatePilotErrors.FORBIDDEN, actualException.getError());
    }

    @Test
    void test_get_by_id_with_winery_role() {
        //GIVEN
        String wineryId = "wi01";
        Winery winery = new Winery(wineryId, "keycloakUserId", "wineryName", "wineryRsCode", "privateKey", "publicKey", "salt");
        List<String> roles = List.of(Role.WINERY.toString());

        List<CertificateLotEntryEntity> certificateLotEntryEntityList = new ArrayList<>();
        CertificateLotEntryEntity certificateLotEntryEntity = CertificateLotEntryEntity.builder()
                .wineryId("winery_id")
                .scanningStatus(ScanningStatus.SCANNING)
                .certificateLotEntryPk(CertificateLotEntryPK.builder()
                        .certificateId("cert_id")
                        .lotId("lot_id")
                        .build())
                .certificate(CertificateEntity.builder()
                        .certificateId("cert_id")
                        .certificateType("cert_type")
                        .certificateNumber("cert_number")
                        .build())
                .build();
        certificateLotEntryEntityList.add(certificateLotEntryEntity);

        //WHEN
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getListRoles()).thenReturn(roles);
        when(securityContextHolderUtil.getKeyCloakUserId()).thenReturn("keycloakUserId");
        when(certificateLotEntryRepository.findByWineryIdAndCertificateTxIdIsNotNullAndCertificateCertStatus(wineryId, CertStatus.ACTIVE)).thenReturn(certificateLotEntryEntityList);

        //THEN
        List<CertsResponse> actualCertsResponses = service.getByWineryId(wineryId);
        assertEquals(1, actualCertsResponses.size());
        CertsResponse certsResponse = actualCertsResponses.get(0);
        assertEquals("cert_id", certsResponse.getId());
        assertEquals(1, certsResponse.getLotEntries().size());
    }

    @Test
    void test_get_by_id_with_nwa_role() {
        //GIVEN
        String wineryId = "wi01";
        Winery winery = new Winery(wineryId, "keycloakUserId", "wineryName", "wineryRsCode", "privateKey", "publicKey", "salt");
        List<String> roles = List.of(Role.NWA.toString());

        List<CertificateLotEntryEntity> certificateLotEntryEntityList = new ArrayList<>();
        CertificateLotEntryEntity certificateLotEntryEntity = CertificateLotEntryEntity.builder()
                .wineryId("winery_id")
                .scanningStatus(ScanningStatus.SCANNING)
                .certificateLotEntryPk(CertificateLotEntryPK.builder()
                        .certificateId("cert_id")
                        .lotId("lot_id")
                        .build())
                .certificate(CertificateEntity.builder()
                        .certificateId("cert_id")
                        .certificateType("cert_type")
                        .certificateNumber("cert_number")
                        .build())
                .certificate(CertificateEntity.builder()
                        .certificateId("cert_id")
                        .certificateType("cert_type")
                        .certificateNumber("cert_number")
                        .build())
                .build();
        certificateLotEntryEntityList.add(certificateLotEntryEntity);

        //WHEN
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getListRoles()).thenReturn(roles);
        when(certificateLotEntryRepository.findByWineryIdAndCertificateTxIdIsNotNullAndCertificateCertStatus(wineryId, CertStatus.ACTIVE)).thenReturn(certificateLotEntryEntityList);

        //THEN
        List<CertsResponse> actualCertsResponses = service.getByWineryId(wineryId);
        assertEquals(1, actualCertsResponses.size());
        CertsResponse certsResponse = actualCertsResponses.get(0);
        assertEquals("cert_id", certsResponse.getId());
        assertEquals(1, certsResponse.getLotEntries().size());
    }

    @Test
    void test_get_by_id_with_data_provider_role() {
        //GIVEN
        String wineryId = "wi01";
        Winery winery = new Winery(wineryId, "keycloakUserId", "wineryName", "wineryRsCode", "privateKey", "publicKey", "salt");
        List<String> roles = List.of(Role.DATA_PROVIDER.toString());

        //WHEN
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        when(securityContextHolderUtil.getListRoles()).thenReturn(roles);

        //THEN
        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> service.getByWineryId(wineryId));
        assertEquals(OriginatePilotErrors.FORBIDDEN, actualException.getError());
    }

    @Test
    void test_create_cert_with_job_response_id_null() {
        // Arrange
        String certId = "CERT-001";
        String wineryId = "WINERY-001";
        CertBody certBody = new CertBody();
        certBody.setCertificateType("TYPE-001");
        certBody.setExportCountry("USA");
        certBody.setExamProtocolNumber("Analysis 2a");
        certBody.setTastingProtocolNumber("Tasting 5p");
        CertLotEntryBody lotEntry = mockLotEntry();
        certBody.setProducts(List.of(lotEntry));
        CertRequest certRequest = new CertRequest();
        certRequest.setCert(certBody);
        certRequest.setSignature("eyJhbGciOiJFZERTQSJ9.emQoGLB61Gqb_VzcrqnULFLRmda0g9vk4s0yfL9hMxUNRHD_0OM0PiJS8KFLwamzPH9JjQW1n-0Ck-tvwp6vCw");
        certRequest.setPublicKeyBase64Url(pubKey);

        CertificateEntity certificateEntity = new CertificateEntity();
        Winery winery = new Winery(wineryId, "keycloakUserId", "wineryName", "wineryRsCode", "privateKey", "publicKey", "salt");

        // Mock the repository methods
        when(certificateRepository.existsById(certId)).thenReturn(false);
        when(certificateRepository.save(any(CertificateEntity.class))).thenReturn(certificateEntity);
        when(wineryRepository.findByWineryId(   wineryId)).thenReturn(Optional.of(winery));
        when(certificateLotEntryRepository.saveAll(any(ArrayList.class))).thenReturn(new ArrayList<>());
        when(metabusCallerService.createJob(any(CertificateDataDTO.class), any(Unit.MetabusJobType.class), any(), any()))
                .thenReturn(JobResponse.builder().build());

        // Assert
        Exception ex = assertThrows(OriginatePilotException.class, () -> service.createCertificate(certId, wineryId, certRequest));
        assertEquals(OriginatePilotErrors.METABUS_ERROR.getMessage(), ex.getMessage());
    }

    private CertRequest validCertRequest(CertLotEntryBody lotEntry) {
        CertBody certBody = new CertBody();
        certBody.setCertificateType(certType);
        certBody.setCertificateNumber(certNumber);
        certBody.setExportCountry(exportCountry);
        certBody.setExamProtocolNumber(examNumber);
        certBody.setTastingProtocolNumber(tastingProtocolNumber);
        certBody.setProducts(List.of(lotEntry));
        CertRequest certRequest = new CertRequest();
        certRequest.setSignature(createSignature);
        certRequest.setPublicKeyBase64Url(pubKey);
        certRequest.setCert(certBody);
        return certRequest;
    }

    private CertLotEntryBody mockLotEntry() {
        CertLotEntryBody lotEntry = new CertLotEntryBody();
        lotEntry.setLotNumber("LOT-001");
        lotEntry.setWineName("Wine Name");
        lotEntry.setWineDescription("Description of this wine");
        lotEntry.setSerialName("Wine Serial No");
        lotEntry.setOrigin("Product origin");
        lotEntry.setViticultureArea("Originate area");
        lotEntry.setType("TYPE-001");
        lotEntry.setColor("WHITE");
        lotEntry.setSugarContentCategory("Dry");
        lotEntry.setGrapeVariety("GrapeA");
        lotEntry.setHarvestYear(2022);
        lotEntry.setDelayedOnChacha(false);
        lotEntry.setBottleType("Ceramic");
        lotEntry.setBottlingDate(LocalDate.of(2022, 1, 22));
        lotEntry.setBottleVolume(5.5);
        lotEntry.setBottleCountInLot(1000);
        return lotEntry;
    }

    /**
     * <p>
     * Description:
     * Test if the function throws a OriginatePilotException with error
     * OriginatePilotErrors.FORBIDDEN when the user does not have the NWA role.
     * </p>
     */
    @Test
    void testRevokeCertificateForbidden() {
        // Given
        final String certId = "certId";
        final String publicKey = "publicKey";

        // When
        doReturn(false).when(service).hasPermissionOnlyForNWA();

        // Then
        assertThrows(OriginatePilotException.class,
                () -> service.revokeCertificate(certId, createSignature, publicKey));
    }


    /**
     * <p>
     * Description:
     * Test if the function throws a OriginatePilotException with error OriginatePilotErrors.CERT_DOES_NOT_EXIST
     * when the certificate with the given certId does not exist.
     * </p>
     */
    @Test
    void testRevokeCertificateCertDoesNotExist() {
        // Given
        final String certId = "certId";
        final String publicKey = "publicKey";

        // When
        when(service.hasPermissionOnlyForNWA()).thenReturn(true);
        when(certificateRepository.findById(certId)).thenReturn(Optional.empty());

        // Then
        assertThrows(OriginatePilotException.class,
                () -> service.revokeCertificate(certId, createSignature, publicKey));
    }

    /**
     * <p>
     * Description:
     * Test if the function throws a OriginatePilotException with error OriginatePilotErrors.CERT_HAD_ALREADY_BEEN_REVOKED
     * when the certificate with the given certId has already been revoked.
     * </p>
     */
    @Test
    void testRevokeCertificateCertAlreadyRevoked() {
        // Given
        final String certId = "certId";
        final String publicKey = "publicKey";

        final CertificateEntity certificateEntity = new CertificateEntity();
        certificateEntity.setCertStatus(CertStatus.REVOKED);

        // When
        when(service.hasPermissionOnlyForNWA()).thenReturn(true);
        when(certificateRepository.findById(certId)).thenReturn(Optional.of(certificateEntity));

        // Then
        assertThrows(OriginatePilotException.class,
                () -> service.revokeCertificate(certId, createSignature, publicKey));
    }

    /**
     * <p>
     * Description:
     * Test if the function changes the status of the certificate to REVOKED and saves it in the repository.
     * </p>
     */
    @Test
    void testRevokeCertificateChangeCertStatus() {
        // Given
        final String certId = "certId";

        final CertificateEntity certificateEntity = new CertificateEntity();
        certificateEntity.setCertStatus(CertStatus.ACTIVE);
        certificateEntity.setCertificateId(certId);
        certificateEntity.setCertificateNumber(certNumber);
        certificateEntity.setCertificateType(certType);
        final JobResponse jobResponse = new JobResponse();
        jobResponse.setId(1L);

        try (MockedStatic<AopContext> mockedContext = mockStatic(AopContext.class)) {
            // When
            when(service.hasPermissionOnlyForNWA()).thenReturn(true);
            when(certificateRepository.findById(certId)).thenReturn(Optional.of(certificateEntity));
            doReturn(jobResponse).when(metabusCallerService).createJob(any(CertificateRevokeDTO.class), eq(MetabusJobType.CERT_REVOCATION), eq(revokeSignature),
                    eq(pubKey));
            mockedContext.when(AopContext::currentProxy).thenReturn(service);

            service.revokeCertificate(certId, revokeSignature, pubKey);

            // Then
            verify(certificateRepository, times(2)).save(certificateEntity);
            assertEquals(CertStatus.REVOKED, certificateEntity.getCertStatus());
            assertEquals(revokeSignature, certificateEntity.getRevokeSignature());
            assertEquals(pubKey, certificateEntity.getRevokePubKey());
        }
    }

    /**
     * <p>
     * Description:
     * Test if the function finds all bottles that belong to the certificate
     * and passes them to the syncCertRevocation method along with other parameters.
     * </p>
     */
    @Test
    void testRevokeCertificateFindBottles() {
        // Given
        final String certId = "certId";

        final CertificateEntity certificateEntity = new CertificateEntity();
        certificateEntity.setCertStatus(CertStatus.ACTIVE);
        certificateEntity.setCertificateId(certId);
        certificateEntity.setCertificateNumber(certNumber);
        certificateEntity.setCertificateType(certType);

        final List<Bottle> listOfRevokedBottle = List.of(new Bottle(), new Bottle());
        final JobResponse jobResponse = new JobResponse();
        jobResponse.setId(1L);

        try (MockedStatic<AopContext> mockedContext = mockStatic(AopContext.class)) {
            // When
            when(service.hasPermissionOnlyForNWA()).thenReturn(true);
            when(certificateRepository.findById(certId)).thenReturn(Optional.of(certificateEntity));
            when(bottleRepository.findAllByCertificateId(certId)).thenReturn(listOfRevokedBottle);
            doReturn(jobResponse).when(metabusCallerService).createJob(any(CertificateRevokeDTO.class), eq(MetabusJobType.CERT_REVOCATION), eq(revokeSignature),
                    eq(pubKey));
            mockedContext.when(AopContext::currentProxy).thenReturn(service);

            service.revokeCertificate(certId, revokeSignature, pubKey);

            // Then
            verify(service).syncCertRevocation(eq(certId), any(CertificateRevokeDTO.class), eq(revokeSignature), eq(pubKey), eq(listOfRevokedBottle));
        }
    }

    /**
     * <p>
     * Description:
     * Test if the function finds all bottles that belong to the certificate
     * and passes them to the syncCertRevocation method along with other parameters.
     * </p>
     */
    @Test
    void testRevokeCertificateSendJobToMetabusFailed() {
        // Given
        final String certId = "certId";

        final CertificateEntity certificateEntity = new CertificateEntity();
        certificateEntity.setCertStatus(CertStatus.ACTIVE);
        certificateEntity.setCertificateNumber(certNumber);
        certificateEntity.setCertificateType(certType);

        final List<Bottle> listOfRevokedBottle = List.of(new Bottle(), new Bottle());
        final JobResponse jobResponse = new JobResponse();

        try (MockedStatic<AopContext> mockedContext = mockStatic(AopContext.class)) {
            // When
            when(service.hasPermissionOnlyForNWA()).thenReturn(true);
            when(certificateRepository.findById(certId)).thenReturn(Optional.of(certificateEntity));
            when(bottleRepository.findAllByCertificateId(certId)).thenReturn(listOfRevokedBottle);
            doReturn(jobResponse).when(metabusCallerService).createJob(any(CertificateRevokeDTO.class), eq(MetabusJobType.CERT_REVOCATION), eq(revokeSignature),
                    eq(pubKey));
            mockedContext.when(AopContext::currentProxy).thenReturn(service);

            assertThrows(OriginatePilotException.class, () -> service.revokeCertificate(certId, revokeSignature, pubKey));

            // Then
            verify(service).syncCertRevocation(eq(certId), any(CertificateRevokeDTO.class), eq(revokeSignature), eq(pubKey), eq(listOfRevokedBottle));
        }
    }

    @Test
    void testRevokeFailsWithInvalidSignatureFormatA() {
        final CertificateEntity certificateEntity = new CertificateEntity();
        certificateEntity.setCertStatus(CertStatus.ACTIVE);
        certificateEntity.setCertificateNumber(certNumber);
        certificateEntity.setCertificateType(certType);

        when(service.hasPermissionOnlyForNWA()).thenReturn(true);
        when(certificateRepository.findById(certId)).thenReturn(Optional.of(certificateEntity));

        Exception ex = assertThrows(OriginatePilotException.class, () -> service.revokeCertificate(certId, "signature", pubKey));
        assertEquals(OriginatePilotErrors.SIGNATURE_INVALID_OR_FAILED_VERIFICATION.getMessage(), ex.getMessage());
    }

    @Test
    void testRevokeFailsWithInvalidSignatureFormatB() {
        final CertificateEntity certificateEntity = new CertificateEntity();
        certificateEntity.setCertStatus(CertStatus.ACTIVE);
        certificateEntity.setCertificateNumber(certNumber);
        certificateEntity.setCertificateType(certType);

        when(service.hasPermissionOnlyForNWA()).thenReturn(true);
        when(certificateRepository.findById(certId)).thenReturn(Optional.of(certificateEntity));

        Exception ex = assertThrows(OriginatePilotException.class, () -> service.revokeCertificate(certId, "header.signature", pubKey));
        assertEquals(OriginatePilotErrors.SIGNATURE_INVALID_OR_FAILED_VERIFICATION.getMessage(), ex.getMessage());
    }

    @Test
    void testRevokeFailsWithInvalidPubKey() {
        final CertificateEntity certificateEntity = new CertificateEntity();
        certificateEntity.setCertStatus(CertStatus.ACTIVE);
        certificateEntity.setCertificateNumber(certNumber);
        certificateEntity.setCertificateType(certType);

        when(service.hasPermissionOnlyForNWA()).thenReturn(true);
        when(certificateRepository.findById(certId)).thenReturn(Optional.of(certificateEntity));

        Exception ex = assertThrows(OriginatePilotException.class, () -> service.revokeCertificate(certId, revokeSignature, "pubKey"));
        assertEquals(OriginatePilotErrors.SIGNATURE_INVALID_OR_FAILED_VERIFICATION.getMessage(), ex.getMessage());
    }

    @Test
    void testRevokeFailsWithInvalidSignature() {
        final CertificateEntity certificateEntity = new CertificateEntity();
        certificateEntity.setCertStatus(CertStatus.ACTIVE);
        certificateEntity.setCertificateNumber(certNumber);
        certificateEntity.setCertificateType("DIFF-CERT-TYPE");

        when(service.hasPermissionOnlyForNWA()).thenReturn(true);
        when(certificateRepository.findById(certId)).thenReturn(Optional.of(certificateEntity));

        Exception ex = assertThrows(OriginatePilotException.class, () -> service.revokeCertificate(certId, revokeSignature, pubKey));
        assertEquals(OriginatePilotErrors.SIGNATURE_INVALID_OR_FAILED_VERIFICATION.getMessage(), ex.getMessage());
    }

    /**
     * <p>
     * Description:
     * Test if the function resets the certificateId of all bottles in the listOfRevokedBottle to an empty string
     * and sets their certUpdateStatus to NOT_UPDATED.
     * </p>
     */
    @Test
    void testSyncCertRevocationResetBottlesCert() {
        // Given
        final String certId = "certId";

        final Bottle bottle1 = new Bottle();
        bottle1.setCertificateId("certId1");
        bottle1.setCertUpdateStatus(CertUpdateStatus.UPDATED);

        final Bottle bottle2 = new Bottle();
        bottle2.setCertificateId("certId2");
        bottle2.setCertUpdateStatus(CertUpdateStatus.UPDATED);

        final List<Bottle> listOfRevokedBottle = List.of(bottle1, bottle2);
        final JobResponse jobResponse = new JobResponse();
        jobResponse.setId(1L);

        final CertificateEntity certificateEntity = new CertificateEntity();
        certificateEntity.setCertStatus(CertStatus.REVOKED);
        certificateEntity.setCertificateId(certId);

        final CertificateRevokeDTO certRevokeDto = CertificateRevokeDTO.builder()
                .certificateNumber(certNumber)
                .certificateType(certType)
                .build();

        // When
        when(certificateRepository.findById(certId)).thenReturn(Optional.of(certificateEntity));
        doReturn(jobResponse).when(metabusCallerService).createJob(any(CertificateRevokeDTO.class), eq(MetabusJobType.CERT_REVOCATION), eq(revokeSignature),
                eq(pubKey));
        service.syncCertRevocation(certId, certRevokeDto, revokeSignature, pubKey, listOfRevokedBottle);

        // Then
        assertEquals(jobResponse.getId(), certificateEntity.getRevokeJobId());
        for (final Bottle bottle : listOfRevokedBottle) {
            assertNull(bottle.getCertificateId());
            assertEquals(CertUpdateStatus.NOT_UPDATED, bottle.getCertUpdateStatus());
        }
    }

    /**
     * <p>
     * Description:
     * Test if the function saves all bottles in the listOfRevokedBottle to the repository.
     * </p>
     */
    @Test
    void testSyncCertRevocationSaveBottles() {
        // Given
        final String certId = "certId";
        final String pubKey = "pubKey";

        final Bottle bottle1 = new Bottle();
        bottle1.setCertificateId("certId1");
        bottle1.setCertUpdateStatus(CertUpdateStatus.UPDATED);

        final Bottle bottle2 = new Bottle();
        bottle2.setCertificateId("certId2");
        bottle2.setCertUpdateStatus(CertUpdateStatus.UPDATED);

        final List<Bottle> listOfRevokedBottle = List.of(bottle1, bottle2);
        final JobResponse jobResponse = new JobResponse();
        jobResponse.setId(1L);
        final CertificateEntity certificateEntity = new CertificateEntity();
        certificateEntity.setCertStatus(CertStatus.REVOKED);
        certificateEntity.setCertificateId(certId);

        final CertificateRevokeDTO certRevokeDto = CertificateRevokeDTO.builder()
                .certificateNumber(certNumber)
                .certificateType(certType)
                .build();

        // When
        doReturn(jobResponse).when(metabusCallerService).createJob(any(CertificateRevokeDTO.class), eq(MetabusJobType.CERT_REVOCATION), eq(revokeSignature),
                eq(pubKey));
        when(certificateRepository.findById(certId)).thenReturn(Optional.of(certificateEntity));
        service.syncCertRevocation(certId, certRevokeDto, revokeSignature, pubKey, listOfRevokedBottle);

        // Then
        verify(bottleRepository).saveAll(listOfRevokedBottle);
    }

    /**
     * <p>
     * Description:
     * Test if the function sends a cert revocation job to metabus with the given
     * parameters.
     * </p>
     */
    @Test
    void testSyncCertRevocationSendJobToMetabus() {
        // Given
        final String certId = "certId";
        final String pubKey = "pubKey";
        final String certNumber = "certNumber";
        final String certType = "certType";

        final Bottle bottle1 = new Bottle();
        bottle1.setCertificateId("certId1");
        bottle1.setCertUpdateStatus(CertUpdateStatus.UPDATED);

        final Bottle bottle2 = new Bottle();
        bottle2.setCertificateId("certId2");
        bottle2.setCertUpdateStatus(CertUpdateStatus.UPDATED);

        final List<Bottle> listOfRevokedBottle = List.of(bottle1, bottle2);

        final JobResponse jobResponse = new JobResponse();
        jobResponse.setId(1L);
        final CertificateEntity certificateEntity = new CertificateEntity();
        certificateEntity.setCertStatus(CertStatus.REVOKED);
        certificateEntity.setCertificateId(certId);
        certificateEntity.setCertificateNumber(certNumber);
        certificateEntity.setCertificateType(certType);

        final CertificateRevokeDTO certRevokeDto = CertificateRevokeDTO.builder()
                .certificateNumber(certNumber)
                .certificateType(certType)
                .build();

        // When
        when(certificateRepository.findById(certId)).thenReturn(Optional.of(certificateEntity));
        when(metabusCallerService.createJob(any(CertificateRevokeDTO.class), eq(Unit.MetabusJobType.CERT_REVOCATION), eq(revokeSignature), eq(pubKey)))
                .thenReturn(jobResponse);

        service.syncCertRevocation(certId, certRevokeDto, revokeSignature, pubKey, listOfRevokedBottle);

        // Then
        ArgumentCaptor<CertificateRevokeDTO> revokeDtoCaptor = ArgumentCaptor.forClass(CertificateRevokeDTO.class);
        verify(metabusCallerService).createJob(revokeDtoCaptor.capture(), eq(Unit.MetabusJobType.CERT_REVOCATION), eq(revokeSignature), eq(pubKey));

        CertificateRevokeDTO revokeDto = revokeDtoCaptor.getValue();
        assertEquals(certNumber, revokeDto.getCertificateNumber());
        assertEquals(certType, revokeDto.getCertificateType());
    }

    /**
     * <p>
     * Description:
     * Test if the function throws a OriginatePilotException with error OriginatePilotErrors.METABUS_ERROR
     * when the jobResponse returned by metabusCallerService.createJob has a null id.
     * </p>
     */
    @Test
    void testSyncCertRevocationMetabusError() {
        // Given
        final String pubKey = "pubKey";

        final Bottle bottle1 = new Bottle();
        bottle1.setCertificateId("certId1");
        bottle1.setCertUpdateStatus(CertUpdateStatus.UPDATED);

        final Bottle bottle2 = new Bottle();
        bottle2.setCertificateId("certId2");
        bottle2.setCertUpdateStatus(CertUpdateStatus.UPDATED);

        final List<Bottle> listOfRevokedBottle = List.of(bottle1, bottle2);

        final JobResponse jobResponse = new JobResponse();
        jobResponse.setId(null);

        // When
        when(metabusCallerService.createJob(any(CertificateRevokeDTO.class), eq(Unit.MetabusJobType.CERT_REVOCATION), eq(revokeSignature), eq(pubKey)))
                .thenReturn(jobResponse);

        // Then
        Exception ex = assertThrows(OriginatePilotException.class,
                () -> service.syncCertRevocation(certId, CertificateRevokeDTO.builder().build(), revokeSignature, pubKey, listOfRevokedBottle));
        assertEquals(OriginatePilotErrors.METABUS_ERROR.getMessage(), ex.getMessage());
    }

    @Test
    void test_updateTxIdAndJobIndexForCertificate_not_found_certificate() {
        Long jobId = 1L;
        String txId = "txHash";
        String jobIndex = "jobIndex";
        when(certificateRepository.findByJobId(jobId)).thenReturn(Optional.empty());
        assertNull(service.updateTxIdAndJobIndexForCertificate(jobId, txId, jobIndex));
    }

    @Test
    void test_updateTxIdAndJobIndexForCertificate_valid() {
        Long jobId = 1L;
        String txId = "txHash";
        String jobIndex = "jobIndex";
        CertificateEntity certificateEntity = new CertificateEntity();
        when(certificateRepository.findByJobId(jobId)).thenReturn(Optional.of(certificateEntity));
        when(certificateRepository.save(any(CertificateEntity.class))).thenReturn(certificateEntity);

        service.updateTxIdAndJobIndexForCertificate(jobId, txId, jobIndex);
        ArgumentCaptor<CertificateEntity> captor = ArgumentCaptor.forClass(CertificateEntity.class);
        verify(certificateRepository).save(captor.capture());
        CertificateEntity result = captor.getValue();
        assertEquals(txId, result.getTxId());
    }

    @Test
    void test_updateTxIdAndJobIndexForCertificateRevoke_not_found_certificate() {
        Long revokeJobId = 1L;
        String revokeTxId = "txHash";
        String revokeJobIndex = "jobIndex";
        when(certificateRepository.findByRevokeJobId(revokeJobId)).thenReturn(Optional.empty());
        assertNull(service.updateTxIdAndJobIndexForCertificateRevoke(revokeJobId, revokeTxId, revokeJobIndex));
    }

    @Test
    void test_updateTxIdAndJobIndexForCertificateRevoke_valid() {
        Long revokeJobId = 1L;
        String revokeTxId = "txHash";
        String revokeJobIndex = "jobIndex";
        CertificateEntity certificateEntity = new CertificateEntity();
        when(certificateRepository.findByRevokeJobId(revokeJobId)).thenReturn(Optional.of(certificateEntity));
        when(certificateRepository.save(any(CertificateEntity.class))).thenReturn(certificateEntity);

        service.updateTxIdAndJobIndexForCertificateRevoke(revokeJobId, revokeTxId, revokeJobIndex);
        ArgumentCaptor<CertificateEntity> captor = ArgumentCaptor.forClass(CertificateEntity.class);
        verify(certificateRepository).save(captor.capture());
        CertificateEntity result = captor.getValue();
        assertEquals(revokeTxId, result.getRevokeTxId());
        assertEquals(revokeJobIndex, result.getRevokeJobIndex());
    }
}