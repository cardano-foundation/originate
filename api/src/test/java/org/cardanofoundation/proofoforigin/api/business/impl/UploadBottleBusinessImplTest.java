package org.cardanofoundation.proofoforigin.api.business.impl;

import org.cardanofoundation.proofoforigin.api.business.ScanTrustService;
import org.cardanofoundation.proofoforigin.api.exceptions.BolnisiPilotErrors;
import org.cardanofoundation.proofoforigin.api.exceptions.BolnisiPilotException;
import org.cardanofoundation.proofoforigin.api.repository.BottleRepository;
import org.cardanofoundation.proofoforigin.api.repository.WineryRepository;
import org.cardanofoundation.proofoforigin.api.repository.entities.Bottle;
import org.cardanofoundation.proofoforigin.api.repository.entities.Winery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UploadBottleBusinessImplTest {
    @InjectMocks
    private UploadBottleBusinessImpl uploadBottleBusinessImpl;

    @Mock
    private BottleRepository bottleRepository;

    @Mock
    private ScanTrustService scanTrustService;

    @Mock
    private WineryRepository wineryRepository;

    private static final String WINERY_ID = "1234";
    private static final String TYPE = "text/csv";

    @Test
    void uploadCSV() throws InterruptedException {
        setupWinery();
        uploadBottleBusinessImpl.uploadCsvFile(createValidFile(), WINERY_ID);

        Thread.sleep(10000);

        verify(wineryRepository,times(1)).findById(WINERY_ID);

        ArgumentCaptor<List<Bottle>> saveAllCaptor = ArgumentCaptor.forClass(List.class);
        verify(bottleRepository,times(1)).saveAll(saveAllCaptor.capture());
        checkSavedBottlesData(saveAllCaptor.getValue());

        ArgumentCaptor<List<Bottle>> sendScmDataCaptor = ArgumentCaptor.forClass(List.class);
        verify(scanTrustService,times(1)).sendScmDataWhenMappingBottle(sendScmDataCaptor.capture());
        checkSavedBottlesData(sendScmDataCaptor.getValue());
    }

    @Test
    void uploadCSV_InvalidFileType_ThrowBolnisiPilotException(){
        BolnisiPilotException expectedException = new BolnisiPilotException(BolnisiPilotErrors.INVALID_FILE_TYPE);
        MultipartFile invalidFile = createMockMultipartFile("data","pdf", "extended_id,lot_id,sequential_number,reel_number,sequential_number_in_lot\n" +
                "5XEDIMQBXN041SMQ1081S1DB4ZLY3YI,1234ABC5678,245,5,1");

        BolnisiPilotException exception =  assertThrows(BolnisiPilotException.class, () -> {
            uploadBottleBusinessImpl.uploadCsvFile(invalidFile, WINERY_ID);
        });
        verify(wineryRepository,never()).findById(WINERY_ID);
        verify(bottleRepository,never()).saveAll(any());
        assertExceptionEquals(expectedException, exception);
    }

    @Test
    void uploadCSV_NonExsistWineryId_ThrowBolnisiPilotException(){
        when(wineryRepository.findById(any())).thenReturn(java.util.Optional.empty());
        BolnisiPilotException expectedException = new BolnisiPilotException(BolnisiPilotErrors.NOT_FOUND);
        BolnisiPilotException exception =  assertThrows(BolnisiPilotException.class, () -> {
            uploadBottleBusinessImpl.uploadCsvFile(createValidFile(), WINERY_ID);
        });
        verify(wineryRepository,times(1)).findById(WINERY_ID);
        verify(bottleRepository,never()).saveAll(any());
        assertExceptionEquals(expectedException, exception);
    }

    @Test
    void uploadCSV_BlankExtendId_ThrowBolnisiPilotException(){
        setupWinery();
        BolnisiPilotException expectedException = new BolnisiPilotException(BolnisiPilotErrors.INVALID_DATA);
        MultipartFile invalidFile = createMockMultipartFile("data",TYPE, "extended_id,lot_id,sequential_number,reel_number,sequential_number_in_lot\n" +
                ",1234ABC5678,245,5,1");

        BolnisiPilotException exception =  assertThrows(BolnisiPilotException.class, () -> {
            uploadBottleBusinessImpl.uploadCsvFile(invalidFile, WINERY_ID);
        });
        verify(wineryRepository,times(1)).findById(WINERY_ID);
        verify(bottleRepository,never()).saveAll(any());
        assertExceptionEquals(expectedException, exception);
    }

    @Test
    void uploadCSV_BlankLotId_ThrowBolnisiPilotException(){
        setupWinery();
        BolnisiPilotException expectedException = new BolnisiPilotException(BolnisiPilotErrors.INVALID_DATA);
        MultipartFile invalidFile = createMockMultipartFile("data",TYPE, "extended_id,lot_id,sequential_number,reel_number,sequential_number_in_lot\n" +
                "5XEDIMQBXN041SMQ1081S1DB4ZLY3YI,,245,5,1");

        BolnisiPilotException exception =  assertThrows(BolnisiPilotException.class, () -> {
            uploadBottleBusinessImpl.uploadCsvFile(invalidFile, WINERY_ID);
        });
        verify(wineryRepository,times(1)).findById(WINERY_ID);
        verify(bottleRepository,never()).saveAll(any());
        assertExceptionEquals(expectedException, exception);
    }

    @Test
    void uploadCSV_BlankSequentialNumber_ThrowBolnisiPilotException() {
        setupWinery();
        BolnisiPilotException expectedException = new BolnisiPilotException(BolnisiPilotErrors.INVALID_DATA);
        MultipartFile invalidFile = createMockMultipartFile("data",TYPE, "extended_id,lot_id,sequential_number,reel_number,sequential_number_in_lot\n" +
                "5XEDIMQBXN041SMQ1081S1DB4ZLY3YI,1234ABC5678,,5,1");

        BolnisiPilotException exception =  assertThrows(BolnisiPilotException.class, () -> {
            uploadBottleBusinessImpl.uploadCsvFile(invalidFile, WINERY_ID);
        });
        verify(wineryRepository,times(1)).findById(WINERY_ID);
        verify(bottleRepository,never()).saveAll(any());
        assertExceptionEquals(expectedException, exception);
    }

    @Test
    void uploadCSV_BlankReelNumber_ThrowBolnisiPilotException() {
        setupWinery();
        BolnisiPilotException expectedException = new BolnisiPilotException(BolnisiPilotErrors.INVALID_DATA);
        MultipartFile invalidFile = createMockMultipartFile("data",TYPE, "extended_id,lot_id,sequential_number,reel_number,sequential_number_in_lot\n" +
                "5XEDIMQBXN041SMQ1081S1DB4ZLY3YI,1234ABC5678,245,1");

        BolnisiPilotException exception =  assertThrows(BolnisiPilotException.class, () -> {
            uploadBottleBusinessImpl.uploadCsvFile(invalidFile, WINERY_ID);
        });
        verify(wineryRepository,times(1)).findById(WINERY_ID);
        verify(bottleRepository,never()).saveAll(any());
        assertExceptionEquals(expectedException, exception);
    }

    @Test
    void uploadCSV_BlankSequentialNumberInLot_ThrowBolnisiPilotException() {
        setupWinery();
        BolnisiPilotException expectedException = new BolnisiPilotException(BolnisiPilotErrors.INVALID_DATA);
        MultipartFile invalidFile = createMockMultipartFile("data",TYPE, "extended_id,lot_id,sequential_number,reel_number,sequential_number_in_lot\n" +
                "5XEDIMQBXN041SMQ1081S1DB4ZLY3YI,1234ABC5678,1,245");

        BolnisiPilotException exception =  assertThrows(BolnisiPilotException.class, () -> {
            uploadBottleBusinessImpl.uploadCsvFile(invalidFile, WINERY_ID);
        });
        verify(wineryRepository,times(1)).findById(WINERY_ID);
        verify(bottleRepository,never()).saveAll(any());
        assertExceptionEquals(expectedException, exception);
    }

    @Test
    void csvToBottles_with_negative_sequentialNumber() {
        BolnisiPilotException expectedException = new BolnisiPilotException(BolnisiPilotErrors.INVALID_DATA);
        MultipartFile invalidFile = createMockMultipartFile("data", TYPE, "extended_id,lot_id,sequential_number,reel_number,sequential_number_in_lot\n" +
                "5XEDIMQBXN041SMQ1081S1DB4ZLY3YI,1234ABC5678,-1,10,1");

        BolnisiPilotException exception = assertThrows(BolnisiPilotException.class, () -> {
            uploadBottleBusinessImpl.csvToBottles(invalidFile, WINERY_ID);
        });
        assertExceptionEquals(expectedException, exception);
    }

    @Test
    void csvToBottles_with_negative_reelNumber() {
        BolnisiPilotException expectedException = new BolnisiPilotException(BolnisiPilotErrors.INVALID_DATA);
        MultipartFile invalidFile = createMockMultipartFile("data", TYPE, "extended_id,lot_id,sequential_number,reel_number,sequential_number_in_lot\n" +
                "5XEDIMQBXN041SMQ1081S1DB4ZLY3YI,1234ABC5678,10,-1,1");

        BolnisiPilotException exception = assertThrows(BolnisiPilotException.class, () -> {
            uploadBottleBusinessImpl.csvToBottles(invalidFile, WINERY_ID);
        });
        assertExceptionEquals(expectedException, exception);
    }

    @Test
    void csvToBottles_with_negative_sequentialNumberInLot() {
        BolnisiPilotException expectedException = new BolnisiPilotException(BolnisiPilotErrors.INVALID_DATA);
        MultipartFile invalidFile = createMockMultipartFile("data", TYPE, "extended_id,lot_id,sequential_number,reel_number,sequential_number_in_lot\n" +
                "5XEDIMQBXN041SMQ1081S1DB4ZLY3YI,1234ABC5678,10,245,-1");

        BolnisiPilotException exception = assertThrows(BolnisiPilotException.class, () -> {
            uploadBottleBusinessImpl.csvToBottles(invalidFile, WINERY_ID);
        });
        assertExceptionEquals(expectedException, exception);
    }

    private void setupWinery() {
        Winery winery = new Winery();
        winery.setWineryId(WINERY_ID);
        winery.setPublicKey("1234");
        winery.setWineryName("1234");
        winery.setPrivateKey("1234");
        winery.setKeycloakUserId("1234");

        when(wineryRepository.findById(any())).thenReturn(java.util.Optional.of(winery));
    }

    private void checkSavedBottlesData(List<Bottle> savedBottles) {
        assertEquals(savedBottles.size(), 1);

        Bottle bottleData = savedBottles.get(0);

        assertEquals("5XEDIMQBXN041SMQ1081S1DB4ZLY3YI", bottleData.getId());
        assertEquals("1234ABC5678", bottleData.getLotId());
        assertEquals(245, bottleData.getSequentialNumber());
        assertEquals(5, bottleData.getReelNumber());
        assertNull(bottleData.getCertificateId());

    }

    private MultipartFile createValidFile() {
        return createMockMultipartFile("data", UploadBottleBusinessImplTest.TYPE, "extended_id,lot_id,sequential_number,reel_number,sequential_number_in_lot\n" +
                "5XEDIMQBXN041SMQ1081S1DB4ZLY3YI,1234ABC5678,245,5,1");
    }

    private MultipartFile createMockMultipartFile(String fileName,String contentType, String fileContent) {
        return new MockMultipartFile(fileName, fileName,contentType, fileContent.getBytes(StandardCharsets.UTF_8));
    }

    private void assertExceptionEquals(BolnisiPilotException expectedException, BolnisiPilotException actualException) {
        assertAll(
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );
    }
}