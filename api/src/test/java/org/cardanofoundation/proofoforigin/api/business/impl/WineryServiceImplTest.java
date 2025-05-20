package org.cardanofoundation.proofoforigin.api.business.impl;

import org.cardanofoundation.proofoforigin.api.constants.Constants;
import org.cardanofoundation.proofoforigin.api.constants.Role;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.UserCreateDto;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.WineryUpdateBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.WineryUserBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BaseResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BriefWineryResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.WineryInfoUserResponse;
import org.cardanofoundation.proofoforigin.api.exceptions.BolnisiPilotErrors;
import org.cardanofoundation.proofoforigin.api.exceptions.BolnisiPilotException;
import org.cardanofoundation.proofoforigin.api.repository.WineryRepository;
import org.cardanofoundation.proofoforigin.api.repository.entities.Winery;
import org.cardanofoundation.proofoforigin.api.utils.SecurityContextHolderUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WineryServiceImplTest {

    private WineryRepository wineryRepository;
    private KeycloakCallServiceImpl keycloakCallService;
    private WineryServiceImpl wineryService;

    private SecurityContextHolderUtil securityContextHolderUtil;

    @BeforeEach
    public void init() {
        wineryRepository = mock(WineryRepository.class);
        keycloakCallService = mock(KeycloakCallServiceImpl.class);
        securityContextHolderUtil = mock(SecurityContextHolderUtil.class);
        wineryService = new WineryServiceImpl(wineryRepository, keycloakCallService, securityContextHolderUtil);
    }

    @Test
    public void createUserSuccessSendMailSuccess() {
        Winery mockWinery = new Winery();
        mockWinery.setWineryId("winery_id");
        when(keycloakCallService.createUser(any(),any())).thenReturn(getUserCreateDtoMailSuss());
        when(wineryRepository.save(any())).thenReturn(mockWinery);
        BaseResponse<BriefWineryResponse> response = wineryService.createWinery(getWineryUserBody());
        assertEquals(response.getData().getWineryId(), "winery_id");
        assertEquals(response.getMeta().getCode(), "201");
        assertEquals(response.getMeta().getMessage(), Constants.SEND_MAIL.SEND_MAIL_WINERY_ID_SUCCESS);
    }

    @Test
    public void createUserSuccessSendMailFail() {
        when(keycloakCallService.createUser(any(),any())).thenReturn(getUserCreateDtoMailFail());
        when(wineryRepository.save(any())).thenReturn(new Winery());
        BaseResponse<BriefWineryResponse> response = wineryService.createWinery(getWineryUserBody());
        assertEquals(response.getMeta().getCode(), "201");
        assertEquals(response.getMeta().getMessage(), Constants.SEND_MAIL.SEND_MAIL_WINERY_ID_FAIL);
    }

    @Test
    public void checkPatternMatchesFail() {
        List<String> list = new ArrayList<>();
        list.add("test@.com");
        list.add("test12345678901234567890123456789012345678901234567890123456789012345678901234567890@gmail.com.");
        list.add("test@ttess@gmail.com.");
        list.add("test@ttess@gmail.com..");
        list.add("test@ttess@..");

        for (String s : list) {
            WineryUserBody wineryUserBody = getWineryUserBody(s);
            Assertions.assertThrows(BolnisiPilotException.class, () -> {
                wineryService.createWinery(wineryUserBody);
            });
        }
    }

    @Test
    public void canUpdateWineryName() {
        String wineryId = "wineryA";
        String newName = "new-winery-name";

        Winery winery = getWinery();
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        WineryUpdateBody wineryUpdateBody = new WineryUpdateBody();
        wineryUpdateBody.setName(newName);
        wineryService.updateWinery(wineryId, wineryUpdateBody);
        ArgumentCaptor<Winery> wineryCaptor = ArgumentCaptor.forClass(Winery.class);
        verify(wineryRepository, times(1)).save(wineryCaptor.capture());
        assertEquals(wineryCaptor.getValue().getWineryName(), newName);
        assertNull(wineryCaptor.getValue().getWineryRsCode());
    }

    @Test
    public void canUpdateWineryRsCode() {
        String wineryId = "wineryA";
        String newRsCode = "new-rs-code";

        Winery winery = getWinery();
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        WineryUpdateBody wineryUpdateBody = new WineryUpdateBody();
        wineryUpdateBody.setRsCode(newRsCode);
        wineryService.updateWinery(wineryId, wineryUpdateBody);
        ArgumentCaptor<Winery> wineryCaptor = ArgumentCaptor.forClass(Winery.class);
        verify(wineryRepository, times(1)).save(wineryCaptor.capture());
        assertEquals(wineryCaptor.getValue().getWineryRsCode(), newRsCode);
        assertEquals(wineryCaptor.getValue().getWineryName(), "Tesst 1");
    }

    @Test
    public void canUpdateWineryNameAndRsCode() {
        String wineryId = "wineryA";
        String newName = "new-name-for-winery";
        String newRsCode = "new-rs-code";

        Winery winery = getWinery();
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        WineryUpdateBody wineryUpdateBody = new WineryUpdateBody();
        wineryUpdateBody.setName(newName);
        wineryUpdateBody.setRsCode(newRsCode);
        wineryService.updateWinery(wineryId, wineryUpdateBody);
        ArgumentCaptor<Winery> wineryCaptor = ArgumentCaptor.forClass(Winery.class);
        verify(wineryRepository, times(1)).save(wineryCaptor.capture());
        assertEquals(wineryCaptor.getValue().getWineryName(), newName);
        assertEquals(wineryCaptor.getValue().getWineryRsCode(), newRsCode);
    }

    @Test
    public void emptyWineryUpdateBodyDoesNotFail() {
        String wineryId = "wineryA";
        Winery winery = getWinery();
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(winery));
        WineryUpdateBody wineryUpdateBody = new WineryUpdateBody();
        wineryService.updateWinery(wineryId, wineryUpdateBody);
        ArgumentCaptor<Winery> wineryCaptor = ArgumentCaptor.forClass(Winery.class);
        verify(wineryRepository, times(1)).save(wineryCaptor.capture());
        assertEquals(wineryCaptor.getValue().getWineryName(), "Tesst 1");
        assertNull(wineryCaptor.getValue().getWineryRsCode());
    }


    @Test
    public void testSendEmailError() {

    }

    @Test
    public void getSuccessWineryByAdmin() {
        when(securityContextHolderUtil.getListRoles()).thenReturn(List.of(Role.ADMIN.toString()));
        when(wineryRepository.findByOrderByWineryIdLPad()).thenReturn(getListWinery());
        List<WineryInfoUserResponse> list = wineryService.getAllWinery();
        assertEquals(list.size(), 2);
    }

    @Test
    public void getSuccessWineryByDataProvider() {
        when(securityContextHolderUtil.getListRoles()).thenReturn(List.of(Role.DATA_PROVIDER.toString()));
        when(wineryRepository.findByOrderByWineryIdLPad()).thenReturn(getListWinery());
        List<WineryInfoUserResponse> list = wineryService.getAllWinery();
        assertEquals(list.size(), 2);
    }

    @Test
    public void getSuccessWineryByWinery() {
        when(securityContextHolderUtil.getListRoles()).thenReturn(List.of(Role.WINERY.toString()));
        String keyCloak = "1234566789";
        when(securityContextHolderUtil.getKeyCloakUserId()).thenReturn(keyCloak);
        Winery winery = getWinery();
        when(wineryRepository.findFirstByKeycloakUserId(keyCloak)).thenReturn(Optional.of(winery));
        List<WineryInfoUserResponse> list = wineryService.getAllWinery();
        assertEquals(list.size(), 1);
        assertEquals(winery.getWineryId(), list.get(0).getWineryId());
        assertEquals(winery.getWineryName(), list.get(0).getWineryName());
    }

    @Test
    public void getSuccessWineryByWineryWithRsCode() {
        when(securityContextHolderUtil.getListRoles()).thenReturn(List.of(Role.WINERY.toString()));
        String keyCloak = "1234566789";
        when(securityContextHolderUtil.getKeyCloakUserId()).thenReturn(keyCloak);
        Winery winery = getWineryRsCode();
        when(wineryRepository.findFirstByKeycloakUserId(keyCloak)).thenReturn(Optional.of(winery));
        List<WineryInfoUserResponse> list = wineryService.getAllWinery();
        assertEquals(list.size(), 1);
        assertEquals(winery.getWineryId(), list.get(0).getWineryId());
        assertEquals(winery.getWineryName(), list.get(0).getWineryName());
    }

    @Test
    public void getSuccessWineryByWineryNotDb() {
        when(securityContextHolderUtil.getListRoles()).thenReturn(List.of(Role.WINERY.toString()));
        String keyCloak = "1234566789";
        when(securityContextHolderUtil.getKeyCloakUserId()).thenReturn(keyCloak);
        when(wineryRepository.findFirstByKeycloakUserId(keyCloak)).thenReturn(Optional.empty());
        List<WineryInfoUserResponse> list=wineryService.getAllWinery();
        assertEquals(list.size(),0);
    }

    @Test
    public void saveWinery_WhenHasWineryId(){
        Winery winery = getWinery();
        Winery savedWinery = getWinery();
        when(wineryRepository.save(winery)).thenReturn(savedWinery);
        wineryService.saveWinery(winery);
        assertEquals(winery.getWineryId(), savedWinery.getWineryId());
        assertEquals(winery.getWineryName(), savedWinery.getWineryName());
    }

    @Test
    public void saveWinery_WineryIdIncreases() {
        Winery winery = new Winery();
        winery.setWineryName("Test 1");
        Winery wineryFromDB = new Winery();
        wineryFromDB.setWineryId("5");
        when(wineryRepository.existsById("6")).thenReturn(false);
        when(wineryRepository.findTopByOrderByWineryIdLPadDesc()).thenReturn(Optional.of(wineryFromDB));
        wineryService.saveWinery(winery);
        ArgumentCaptor<Winery> wineryCaptor = ArgumentCaptor.forClass(Winery.class);
        verify(wineryRepository, times(1)).save(wineryCaptor.capture());
        assertEquals(winery.getWineryName(), wineryCaptor.getValue().getWineryName());
        assertEquals("6", wineryCaptor.getValue().getWineryId());
    }

    @Test
    public void saveWinery_WineryIdIncreasesAtHexBoundary() {
        Winery winery = new Winery();
        winery.setWineryName("Test 1");
        Winery wineryFromDB = new Winery();
        wineryFromDB.setWineryId("f");
        when(wineryRepository.existsById("10")).thenReturn(false);
        when(wineryRepository.findTopByOrderByWineryIdLPadDesc()).thenReturn(Optional.of(wineryFromDB));
        wineryService.saveWinery(winery);
        ArgumentCaptor<Winery> wineryCaptor = ArgumentCaptor.forClass(Winery.class);
        verify(wineryRepository, times(1)).save(wineryCaptor.capture());
        assertEquals(winery.getWineryName(), wineryCaptor.getValue().getWineryName());
        assertEquals("10", wineryCaptor.getValue().getWineryId());
    }

    @Test
    public void saveWinery_WhenNotHasWineryIdAndWineryEmpty() {
        Winery winery = new Winery();
        winery.setWineryName("Test 1");
        when(wineryRepository.findTopByOrderByWineryIdLPadDesc()).thenReturn(Optional.empty());
        when(wineryRepository.existsById("1")).thenReturn(false);
        wineryService.saveWinery(winery);
        ArgumentCaptor<Winery> wineryCaptor = ArgumentCaptor.forClass(Winery.class);
        verify(wineryRepository, times(1)).save(wineryCaptor.capture());
        assertEquals(winery.getWineryName(), wineryCaptor.getValue().getWineryName());
        assertEquals("1", wineryCaptor.getValue().getWineryId());
    }

    @Test
    public void saveWinery_MiscalculatedWineryIDShouldThrow() {
        Winery winery = new Winery();
        winery.setWineryName("Test 1");
        Winery wineryFromDB = new Winery();
        wineryFromDB.setWineryId("f");
        when(wineryRepository.findTopByOrderByWineryIdLPadDesc()).thenReturn(Optional.of(wineryFromDB));
        when(wineryRepository.existsById("10")).thenReturn(true);
        Exception exception = assertThrows(BolnisiPilotException.class,
                () -> wineryService.saveWinery(winery));
        verify(wineryRepository, times(0)).save(any());
        assertEquals(BolnisiPilotErrors.CALCULATED_WINERY_ID_NOT_UNIQUE.getMessage(), exception.getMessage());
    }

    @Test
    public void canGetWineryPublicKey() {
        String wineryId = "winery1";
        Winery savedWinery = new Winery();
        savedWinery.setPublicKey("pkeyBase64");
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(savedWinery));
        Assertions.assertArrayEquals(wineryService.getWineryPublicKey(wineryId), new byte[]{-90, 71, -78, 5, -85, 30, -21});
    }

    @Test
    public void cannotGetPublicKeyForMissingWinery() {
        String wineryId = "winery1";
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.empty());
        Exception exception = assertThrows(BolnisiPilotException.class,
                () -> wineryService.getWineryPublicKey(wineryId));
        assertEquals(exception.getMessage(), "Winery does not exist");
    }

    @Test
    public void canGetPublicKeyForWineryWithNoKey() {
        // Keys are created just in time, so may be empty
        String wineryId = "winery1";
        Winery savedWinery = new Winery();
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(savedWinery));
        Assertions.assertArrayEquals(wineryService.getWineryPublicKey(wineryId), new byte[]{});
    }

    @Test
    public void canGetPublicKeyForWineryWithEmptyKey() {
        // Keys are created just in time, so may be empty
        String wineryId = "winery1";
        Winery savedWinery = new Winery();
        savedWinery.setPublicKey("");
        when(wineryRepository.findByWineryId(wineryId)).thenReturn(Optional.of(savedWinery));
        Assertions.assertArrayEquals(wineryService.getWineryPublicKey(wineryId), new byte[]{});
    }

    private UserCreateDto getUserCreateDtoMailSuss() {
        UserCreateDto userBody = new UserCreateDto();
        userBody.setEmail("test@gmail.com");
        userBody.setId("name_test");
        userBody.setIsSendMail(true);
        return userBody;
    }

    private UserCreateDto getUserCreateDtoMailFail() {
        UserCreateDto userBody = new UserCreateDto();
        userBody.setEmail("test@gmail.com");
        userBody.setId("name_test");
        userBody.setIsSendMail(false);
        return userBody;
    }
    private List<Winery> getListWinery() {
        List<Winery> list = new ArrayList<>();
        Winery winery = new Winery();
        winery.setWineryName("Tesst 1");
        winery.setWineryId("1234");
        list.add(winery);

        Winery winery1 = new Winery();
        winery.setWineryName("Tesst 2");
        winery.setWineryId("1235");
        list.add(winery1);
        return list;
    }

    private Winery getWinery() {
        Winery winery = new Winery();
        winery.setWineryName("Tesst 1");
        winery.setWineryId("1234");
        return winery;
    }

    private Winery getWineryRsCode() {
        Winery winery = new Winery();
        winery.setWineryName("Tesst 1");
        winery.setWineryId("1234");
        winery.setWineryRsCode("rs-code");
        return winery;
    }

    private WineryUserBody getWineryUserBody() {
        WineryUserBody wineryUserBody = new WineryUserBody();
        wineryUserBody.setEmail("test@gmail.com");
        wineryUserBody.setName("name_test");
        return wineryUserBody;
    }

    private WineryUserBody getWineryUserBody(String email) {
        WineryUserBody wineryUserBody = new WineryUserBody();
        wineryUserBody.setEmail(email);
        wineryUserBody.setName("name_test");
        return wineryUserBody;
    }
}
