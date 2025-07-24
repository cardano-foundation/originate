package org.cardanofoundation.proofoforigin.api.business.impl;

import com.google.common.collect.Sets;
import org.cardanofoundation.proofoforigin.api.business.MetabusCallerService;
import org.cardanofoundation.proofoforigin.api.business.ScanTrustService;
import org.cardanofoundation.proofoforigin.api.constants.Constants;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.Unit;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.response.JobResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.ScmApproveResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust.ScmData;
import org.cardanofoundation.proofoforigin.api.exceptions.OriginatePilotErrors;
import org.cardanofoundation.proofoforigin.api.exceptions.OriginatePilotException;
import org.cardanofoundation.proofoforigin.api.repository.LotRepository;
import org.cardanofoundation.proofoforigin.api.repository.WineryRepository;
import org.cardanofoundation.proofoforigin.api.repository.entities.Lot;
import org.cardanofoundation.proofoforigin.api.repository.entities.Winery;
import org.cardanofoundation.proofoforigin.api.utils.SecurityContextHolderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ScmApproveLotServiceImplTest {
    private ScmApproveLotServiceImpl scmApproveLotService;

    @Mock
    private LotRepository lotRepository;

    @Mock
    private WineryRepository wineryRepository;

    @Mock
    private SecurityContextHolderUtil securityContextHolderUtil;

    @Mock
    private MetabusCallerService metabusCallerService;

    @Mock
    private ScanTrustService scanTrustService;

    private static final String PUBLIC_KEY = "BHLHmsTNwuVmiEX73ZpiUiRedR9m4qzoHDmqN_KJJ8g";
    private static final String PRIVATE_KEY = "UrwLomlmKM5LgQAnTg92XYn/mXZQma1xtZZf6nCk2ggg4NFcqWbhqJh7XSOARha1";
    private static final String SALT = "NHbCjbfFu1zEK6D1je0eCQ==";
    private static final String WINERY_ID = "1234";
    private static final String KEYCLOAK_USER_ID = "KEYCLOAK_USER_ID";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scmApproveLotService = new ScmApproveLotServiceImpl(lotRepository, wineryRepository,
                securityContextHolderUtil, metabusCallerService, scanTrustService);

        // Set up the mock value for encryptPassword
        ReflectionTestUtils.setField(scmApproveLotService, "encryptPassword", "thisisarandompassword");

        // Mock security context
        when(securityContextHolderUtil.getKeyCloakUserId()).thenReturn(KEYCLOAK_USER_ID);

        // Mock WineryRepository
        Winery winery = new Winery();
        winery.setWineryId(WINERY_ID);
        winery.setPrivateKey(PRIVATE_KEY);
        winery.setPublicKey(PUBLIC_KEY);
        winery.setSalt(SALT);
        winery.setKeycloakUserId(KEYCLOAK_USER_ID);
        when(wineryRepository.findById(eq(WINERY_ID))).thenReturn(Optional.of(winery));

        // Mock MetabusCallerService
        JobResponse jobResponse = new JobResponse();
        jobResponse.setId(1L);
        when(metabusCallerService.createJob(any(), eq(Unit.MetabusJobType.LOT), any(), any(), eq(WINERY_ID))).thenReturn(jobResponse);

        // Mock scanTrustService
        doNothing().when(scanTrustService).sendScmDataWhenApproved(any(ScmData.class));
    }

    @Test
    void approveLots_WineryKeyExist_SuccessfulApproval() {
        // Mock input data
        List<String> lotIds = Arrays.asList("12345678901", "12345678902", "12345678903");

        // Mock LotRepository
        List<Lot> lots = new ArrayList<>();
        for (String lotId : lotIds) {
            Lot lot = new Lot();
            lot.setLotId(lotId);
            lot.setStatus(Constants.LOT_STATUS.FINALIZED);
            lots.add(lot);
        }
        when(lotRepository.findByWineryIdAndLotIdIn(anyString(), anySet())).thenReturn(lots);

        // Call the method
        ScmApproveResponse response = scmApproveLotService.approveLots(WINERY_ID, lotIds);

        // Verify the results
        verify(wineryRepository).findById(WINERY_ID);
        verify(wineryRepository, times(0)).save(any());
        verify(lotRepository).findByWineryIdAndLotIdIn(WINERY_ID, Sets.newHashSet(lotIds));
        verify(metabusCallerService, times(lotIds.size())).createJob(any(), eq(Unit.MetabusJobType.LOT), any(), eq(PUBLIC_KEY), eq(WINERY_ID));
        verify(scanTrustService, times(lotIds.size())).sendScmDataWhenApproved(any(ScmData.class));

        // Assertions
        assertEquals(lotIds, response.getSucceed());
        assertTrue(response.getFailLotsNotFound().isEmpty());
        assertTrue(response.getFailLotsAlreadyApproved().isEmpty());
        assertTrue(response.getFailLotsNotFinalised().isEmpty());
        assertTrue(response.getFailJobsNotScheduled().isEmpty());
    }

    @Test
    void approveLots_WineryKeyNotExist_SuccessfulApproval() {
        // Mock input data
        List<String> lotIds = Arrays.asList("12345678901", "12345678902", "12345678903");

        // Mock LotRepository
        List<Lot> lots = new ArrayList<>();
        for (String lotId : lotIds) {
            Lot lot = new Lot();
            lot.setLotId(lotId);
            lot.setStatus(Constants.LOT_STATUS.FINALIZED);
            lots.add(lot);
        }
        when(lotRepository.findByWineryIdAndLotIdIn(anyString(), anySet())).thenReturn(lots);

        // Mock WineryRepository
        Winery winery = new Winery();
        winery.setWineryId(WINERY_ID);
        winery.setKeycloakUserId(KEYCLOAK_USER_ID);
        when(wineryRepository.findById(eq(WINERY_ID))).thenReturn(Optional.of(winery));

        // Call the method
        ScmApproveResponse response = scmApproveLotService.approveLots(WINERY_ID, lotIds);

        // Verify the results
        verify(wineryRepository).findById(WINERY_ID);
        verify(lotRepository).findByWineryIdAndLotIdIn(WINERY_ID, Sets.newHashSet(lotIds));
        verify(metabusCallerService, times(lotIds.size())).createJob(any(), eq(Unit.MetabusJobType.LOT), any(), any(), eq(WINERY_ID));
        verify(scanTrustService, times(lotIds.size())).sendScmDataWhenApproved(any(ScmData.class));

        ArgumentCaptor<Winery> saveWineryCaptor = ArgumentCaptor.forClass(Winery.class);
        verify(wineryRepository).save(saveWineryCaptor.capture());
        Winery savedWinery = saveWineryCaptor.getValue();
        assertEquals(savedWinery.getWineryId(), WINERY_ID);
        assertEquals(savedWinery.getKeycloakUserId(), KEYCLOAK_USER_ID);
        assertNotNull(savedWinery.getPublicKey());
        assertNotNull(savedWinery.getPrivateKey());
        assertNotNull(savedWinery.getSalt());

        // Assertions
        assertEquals(lotIds, response.getSucceed());
        assertTrue(response.getFailLotsNotFound().isEmpty());
        assertTrue(response.getFailLotsAlreadyApproved().isEmpty());
        assertTrue(response.getFailLotsNotFinalised().isEmpty());
        assertTrue(response.getFailJobsNotScheduled().isEmpty());
    }

    @Test
    void approveLots_InvalidWineryId_ThrowsOriginatePilotException() {
        // Mock input data
        List<String> lotIds = Arrays.asList("lot1", "lot2", "lot3");

        // Mock WineryRepository
        when(wineryRepository.findById(eq(WINERY_ID))).thenReturn(Optional.empty());

        // Call the method and assert exception
        OriginatePilotException exception = assertThrows(OriginatePilotException.class,
                () -> scmApproveLotService.approveLots(WINERY_ID, lotIds));

        // Verify the results
        verify(wineryRepository).findById(WINERY_ID);
        verifyNoInteractions(lotRepository);
        verifyNoInteractions(metabusCallerService);
        verifyNoInteractions(scanTrustService);

        // Assertions
        assertEquals(OriginatePilotErrors.NOT_FOUND.getCode(), exception.getError().getCode());
        assertEquals(OriginatePilotErrors.NOT_FOUND.getMessage(), exception.getError().getMessage());
        assertEquals(OriginatePilotErrors.NOT_FOUND.getHttpStatus(), exception.getError().getHttpStatus());
    }

    @Test
    void approveLots_InvalidKeycloakUserId_ThrowsOriginatePilotException() {
        // Mock input data
        List<String> lotIds = Arrays.asList("lot1", "lot2", "lot3");

        // Mock WineryRepository
        Winery winery = new Winery();
        winery.setWineryId(WINERY_ID);
        winery.setPrivateKey(PRIVATE_KEY);
        winery.setPublicKey(PUBLIC_KEY);
        winery.setSalt(SALT);
        winery.setKeycloakUserId("NOT_EXIST");
        when(wineryRepository.findById(eq(WINERY_ID))).thenReturn(Optional.of(winery));

        // Call the method and assert exception
        OriginatePilotException exception = assertThrows(OriginatePilotException.class,
                () -> scmApproveLotService.approveLots(WINERY_ID, lotIds));

        // Verify the results
        verify(wineryRepository).findById(WINERY_ID);
        verifyNoInteractions(lotRepository);
        verifyNoInteractions(metabusCallerService);
        verifyNoInteractions(scanTrustService);

        // Assertions
        assertEquals(OriginatePilotErrors.FORBIDDEN.getCode(), exception.getError().getCode());
        assertEquals(OriginatePilotErrors.FORBIDDEN.getMessage(), exception.getError().getMessage());
        assertEquals(OriginatePilotErrors.FORBIDDEN.getHttpStatus(), exception.getError().getHttpStatus());
    }

    @Test
    void approveLots_Exception_ThrowsOriginatePilotException() {
        // Mock input data
        List<String> lotIds = Arrays.asList("12345678901", "12345678902", "12345678903");

        // Mock LotRepository
        when(lotRepository.findByWineryIdAndLotIdIn(anyString(), anySet())).thenThrow(new RuntimeException());

        OriginatePilotException exception = assertThrows(OriginatePilotException.class, () -> {
            scmApproveLotService.approveLots(WINERY_ID, lotIds);
        });

        // Verify the results
        verify(wineryRepository).findById(WINERY_ID);
        verify(lotRepository).findByWineryIdAndLotIdIn(WINERY_ID, Sets.newHashSet(lotIds));
        verifyNoInteractions(metabusCallerService);
        verifyNoInteractions(scanTrustService);

        // Assertions
        assertEquals(OriginatePilotErrors.INTERNAL_SERVER_ERROR.getCode(), exception.getError().getCode());
        assertEquals(OriginatePilotErrors.INTERNAL_SERVER_ERROR.getMessage(), exception.getError().getMessage());
        assertEquals(OriginatePilotErrors.INTERNAL_SERVER_ERROR.getHttpStatus(), exception.getError().getHttpStatus());
    }


    @Test
    void approveLots_LotsNotFoundInRepository_SetsFailLotsNotFound() {
        // Mock input data
        List<String> lotIds = Arrays.asList("12345678901", "12345678902", "12345678903");

        // Mock LotRepository
        List<Lot> lots = new ArrayList<>();
        when(lotRepository.findByWineryIdAndLotIdIn(WINERY_ID, Sets.newHashSet(lotIds))).thenReturn(lots);

        // Call the method
        ScmApproveResponse response = scmApproveLotService.approveLots(WINERY_ID, lotIds);

        // Verify the results
        verify(wineryRepository).findById(WINERY_ID);
        verify(lotRepository).findByWineryIdAndLotIdIn(WINERY_ID, Sets.newHashSet(lotIds));
        verifyNoInteractions(metabusCallerService);
        verifyNoInteractions(scanTrustService);

        // Assertions
        assertTrue(response.getSucceed().isEmpty());
        assertEquals(lotIds, response.getFailLotsNotFound());
        assertTrue(response.getFailLotsAlreadyApproved().isEmpty());
        assertTrue(response.getFailLotsNotFinalised().isEmpty());
        assertTrue(response.getFailJobsNotScheduled().isEmpty());
    }

    @Test
    void approveLots_SomeLotsAlreadyApproved_SetsFailLotsAlreadyApproved() {
        // Mock input data
        List<String> lotIds = Arrays.asList("12345678901", "12345678902", "12345678903");

        // Mock LotRepository
        List<Lot> lots = Arrays.asList(
                createLot("12345678901", Constants.LOT_STATUS.APPROVED),
                createLot("12345678902", Constants.LOT_STATUS.FINALIZED),
                createLot("12345678903", Constants.LOT_STATUS.APPROVED)
        );
        when(lotRepository.findByWineryIdAndLotIdIn(anyString(), anySet())).thenReturn(lots);

        // Call the method
        ScmApproveResponse response = scmApproveLotService.approveLots(WINERY_ID, lotIds);

        // Verify the results
        verify(wineryRepository).findById(WINERY_ID);
        verify(lotRepository).findByWineryIdAndLotIdIn(WINERY_ID, Sets.newHashSet(lotIds));
        verify(metabusCallerService, times(1)).createJob(any(), eq(Unit.MetabusJobType.LOT), any(), eq(PUBLIC_KEY), eq(WINERY_ID));
        verify(scanTrustService, times(1)).sendScmDataWhenApproved(any(ScmData.class));


        // Assertions
        assertEquals(List.of("12345678902"), response.getSucceed());
        assertTrue(response.getFailLotsNotFound().isEmpty());
        assertEquals(Arrays.asList("12345678901", "12345678903"), response.getFailLotsAlreadyApproved());
        assertTrue(response.getFailLotsNotFinalised().isEmpty());
        assertTrue(response.getFailJobsNotScheduled().isEmpty());
    }

    @Test
    void approveLots_SomeLotsNotFinalized_SetsFailLotsNotFinalised() {
        // Mock input data
        List<String> lotIds = Arrays.asList("12345678901", "12345678902", "12345678903");

        // Mock LotRepository
        List<Lot> lots = Arrays.asList(
                createLot("12345678901", Constants.LOT_STATUS.NOT_FINALIZED),
                createLot("12345678902", Constants.LOT_STATUS.FINALIZED),
                createLot("12345678903", Constants.LOT_STATUS.NOT_FINALIZED)
        );
        when(lotRepository.findByWineryIdAndLotIdIn(anyString(), anySet())).thenReturn(lots);

        // Call the method
        ScmApproveResponse response = scmApproveLotService.approveLots(WINERY_ID, lotIds);

        // Verify the results
        verify(wineryRepository).findById(WINERY_ID);
        verify(lotRepository).findByWineryIdAndLotIdIn(WINERY_ID, Sets.newHashSet(lotIds));
        verify(metabusCallerService, times(1)).createJob(any(), eq(Unit.MetabusJobType.LOT), any(), eq(PUBLIC_KEY), eq(WINERY_ID));
        verify(scanTrustService, times(1)).sendScmDataWhenApproved(any(ScmData.class));

        // Assertions
        assertEquals(List.of("12345678902"), response.getSucceed());
        assertTrue(response.getFailLotsNotFound().isEmpty());
        assertTrue(response.getFailLotsAlreadyApproved().isEmpty());
        assertEquals(Arrays.asList("12345678901", "12345678903"), response.getFailLotsNotFinalised());
        assertTrue(response.getFailJobsNotScheduled().isEmpty());
    }

    @Test
    void approveLots_MetabusFail_SetsFailJobsNotScheduled() {
        // Mock input data
        List<String> lotIds = Arrays.asList("12345678901", "12345678902", "12345678903");

        // Mock LotRepository
        List<Lot> lots = Arrays.asList(
                createLot("12345678901", Constants.LOT_STATUS.FINALIZED),
                createLot("12345678902", Constants.LOT_STATUS.FINALIZED),
                createLot("12345678903", Constants.LOT_STATUS.FINALIZED)
        );
        when(lotRepository.findByWineryIdAndLotIdIn(anyString(), anySet())).thenReturn(lots);

        // Mock MetabusCallerService
        JobResponse jobResponse = new JobResponse();
        jobResponse.setId(null);
        JobResponse jobResponse1 = new JobResponse();
        jobResponse1.setId(1L);
        JobResponse jobResponse2 = new JobResponse();
        jobResponse2.setId(null);
        when(metabusCallerService.createJob(any(ScmData.class), eq(Unit.MetabusJobType.LOT), any(String.class), anyString(), eq(WINERY_ID)))
                .thenReturn(jobResponse)
                .thenReturn(jobResponse1)
                .thenReturn(jobResponse2);

        // Call the method
        ScmApproveResponse response = scmApproveLotService.approveLots(WINERY_ID, lotIds);

        // Verify the results
        verify(wineryRepository).findById(WINERY_ID);
        verify(lotRepository).findByWineryIdAndLotIdIn(WINERY_ID, Sets.newHashSet(lotIds));
        verify(metabusCallerService, times(3)).createJob(any(), eq(Unit.MetabusJobType.LOT), any(), eq(PUBLIC_KEY), eq(WINERY_ID));
        verify(scanTrustService, times(3)).sendScmDataWhenApproved(any(ScmData.class));

        // Assertions
        assertEquals(List.of("12345678902"), response.getSucceed());
        assertTrue(response.getFailLotsNotFound().isEmpty());
        assertTrue(response.getFailLotsAlreadyApproved().isEmpty());
        assertTrue(response.getFailLotsNotFinalised().isEmpty());
        assertEquals(Arrays.asList("12345678901", "12345678903"), response.getFailJobsNotScheduled());
    }

    @Test
    void approveLots_MetabusException_SetsFailJobsNotScheduled() {
        // Mock input data
        List<String> lotIds = Arrays.asList("12345678901", "12345678902", "12345678903");

        // Mock LotRepository
        List<Lot> lots = Arrays.asList(
                createLot("12345678901", Constants.LOT_STATUS.FINALIZED),
                createLot("12345678902", Constants.LOT_STATUS.FINALIZED),
                createLot("12345678903", Constants.LOT_STATUS.FINALIZED)
        );
        when(lotRepository.findByWineryIdAndLotIdIn(anyString(), anySet())).thenReturn(lots);

        // Mock MetabusCallerService
        JobResponse jobResponse1 = new JobResponse();
        jobResponse1.setId(1L);
        when(metabusCallerService.createJob(any(ScmData.class), eq(Unit.MetabusJobType.LOT), any(String.class), anyString(), eq(WINERY_ID)))
                .thenThrow(new RuntimeException())
                .thenReturn(jobResponse1)
                .thenThrow(new RuntimeException());
        when(lotRepository.saveAll(anyList())).thenReturn(lots);
        // Call the method
        ScmApproveResponse response = scmApproveLotService.approveLots(WINERY_ID, lotIds);

        // Verify the results
        verify(wineryRepository).findById(WINERY_ID);
        verify(lotRepository).findByWineryIdAndLotIdIn(WINERY_ID, Sets.newHashSet(lotIds));
        verify(metabusCallerService, times(3)).createJob(any(), eq(Unit.MetabusJobType.LOT), any(), eq(PUBLIC_KEY), eq(WINERY_ID));
        verify(scanTrustService, times(3)).sendScmDataWhenApproved(any(ScmData.class));

        // Assertions
        assertEquals(List.of("12345678902"), response.getSucceed());
        assertTrue(response.getFailLotsNotFound().isEmpty());
        assertTrue(response.getFailLotsAlreadyApproved().isEmpty());
        assertTrue(response.getFailLotsNotFinalised().isEmpty());
        assertEquals(Arrays.asList("12345678901", "12345678903"), response.getFailJobsNotScheduled());
        assertEquals(Constants.LOT_STATUS.FINALIZED, lots.get(0).getStatus());
        assertNotNull(lots.get(0).getWinerySignature());
        assertEquals(Constants.LOT_STATUS.APPROVED, lots.get(1).getStatus());
        assertNull(lots.get(1).getWinerySignature());
        assertEquals(Constants.LOT_STATUS.FINALIZED, lots.get(2).getStatus());
        assertNotNull(lots.get(2).getWinerySignature());
    }

    private Lot createLot(String lotId, int status) {
        Lot lot = new Lot();
        lot.setLotId(lotId);
        lot.setStatus(status);
        return lot;
    }
}