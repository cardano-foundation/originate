package org.cardanofoundation.proofoforigin.api.business.impl;

import org.cardanofoundation.proofoforigin.api.business.ScmUploadLotService;
import org.cardanofoundation.proofoforigin.api.constants.Constants;
import org.cardanofoundation.proofoforigin.api.exceptions.OriginatePilotError;
import org.cardanofoundation.proofoforigin.api.exceptions.OriginatePilotException;
import org.cardanofoundation.proofoforigin.api.repository.LotRepository;
import org.cardanofoundation.proofoforigin.api.repository.WineryRepository;
import org.cardanofoundation.proofoforigin.api.repository.entities.Lot;
import org.cardanofoundation.proofoforigin.api.repository.entities.Winery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ScmUploadLotServiceImplTest {
    @Mock
    private LotRepository lotRepository;
    @Mock
    private WineryRepository wineryRepository;

    private ScmUploadLotService scmUploadLotService;

    private static final String WINERY_ID = "1234";

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        scmUploadLotService = new ScmUploadLotServiceImpl(lotRepository, wineryRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void uploadCsvFile_ValidFileAndWineryId_SaveAllLots() {
        MultipartFile file = createValidFile();
        setupWinery();

        List<Lot> lots = createMockLots();
        when(lotRepository.findByLotIdIn(any())).thenReturn(lots);
        when(lotRepository.saveAll(any())).thenReturn(lots);

        scmUploadLotService.uploadCsvFile(file, WINERY_ID);

        verify(wineryRepository, times(1)).findById(WINERY_ID);

        ArgumentCaptor<List<String>> findByLotIdsCaptor = ArgumentCaptor.forClass(List.class);
        verify(lotRepository, times(1)).findByLotIdIn(findByLotIdsCaptor.capture());
        checkSearchLotsId(findByLotIdsCaptor.getValue());

        ArgumentCaptor<List<Lot>> saveAllCaptor = ArgumentCaptor.forClass(List.class);
        verify(lotRepository, times(1)).saveAll(saveAllCaptor.capture());
        checkSavedLotsData(saveAllCaptor.getValue());
    }

    @Test
    void uploadCsvFile_InvalidFileType_ThrowOriginatePilotException() {
        MultipartFile file = createMockMultipartFile("invalid.txt", "text/plain", "abc");

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                Constants.ERROR_CODE.INVALID_FILE_TYPE, ScmUploadLotServiceImpl.INVALID_FILE_TYPE_MSG, HttpStatus.BAD_REQUEST));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(file, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(wineryRepository, never()).findById(any());
        verify(lotRepository, never()).findByLotIdIn(any());
        verify(lotRepository, never()).saveAll(any());
    }

    @Test
    void uploadCsvFile_InvalidWineryId_ThrowOriginatePilotException() {
        MultipartFile file = createValidFile();

        when(wineryRepository.findById(any())).thenReturn(java.util.Optional.empty());

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                HttpStatus.NOT_FOUND.value(), ScmUploadLotServiceImpl.WINERY_NOT_FOUND_MSG, HttpStatus.NOT_FOUND));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(file, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(wineryRepository, times(1)).findById(WINERY_ID);
        verify(lotRepository, never()).findByLotIdIn(any());
        verify(lotRepository, never()).saveAll(any());
    }

    @Test
    void uploadCsvFile_NoData_ThrowOriginatePilotException() {
        setupWinery();

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                Constants.ERROR_CODE.FILE_MISSING, ScmUploadLotServiceImpl.FILE_MISSING_MSG, HttpStatus.BAD_REQUEST));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(null, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(lotRepository, never()).findByLotIdIn(any());
        verify(lotRepository, never()).saveAll(any());
    }

    @Test
    void uploadCsvFile_BlankLotId_ThrowOriginatePilotException() {
        MultipartFile file = createMockMultipartFile("invalid.csv", "text/csv", "lot_number,wine_name,origin,country_of_origin,produced_by,producer_address,producer_latitude,producer_longitude,varietal_name,vintage_year,wine_type,wine_color,harvest_date,harvest_location,pressing_date,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,bottling_date,bottling_location,number_of_bottles\n" +
                ",wine_name,origin,country_of_origin,produced_by,producer_address,40.123776,40.8962975,varietal_name,2022,wine_type,wine_color,2022-10-07,harvest_location,2022-10-08,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,2022-10-09,bottling_location,2000");
        setupWinery();

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                Constants.ERROR_CODE.INVALID_DATA, ScmUploadLotServiceImpl.INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(file, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(lotRepository, never()).findByLotIdIn(any());
        verify(lotRepository, never()).saveAll(any());
    }

    @Test
    void uploadCsvFile_BlankWineName_ThrowOriginatePilotException() {
        MultipartFile file = createMockMultipartFile("invalid.csv", "text/csv", "lot_number,wine_name,origin,country_of_origin,produced_by,producer_address,producer_latitude,producer_longitude,varietal_name,vintage_year,wine_type,wine_color,harvest_date,harvest_location,pressing_date,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,bottling_date,bottling_location,number_of_bottles\n" +
                "12345678901,,origin,country_of_origin,produced_by,producer_address,40.123776,40.8962975,varietal_name,2022,wine_type,wine_color,2022-10-07,harvest_location,2022-10-08,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,2022-10-09,bottling_location,2000");
        setupWinery();

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                Constants.ERROR_CODE.INVALID_DATA, ScmUploadLotServiceImpl.INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(file, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(lotRepository, never()).findByLotIdIn(any());
        verify(lotRepository, never()).saveAll(any());
    }

    @Test
    void uploadCsvFile_BlankOrigin_ThrowOriginatePilotException() {
        MultipartFile file = createMockMultipartFile("invalid.csv", "text/csv", "lot_number,wine_name,origin,country_of_origin,produced_by,producer_address,producer_latitude,producer_longitude,varietal_name,vintage_year,wine_type,wine_color,harvest_date,harvest_location,pressing_date,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,bottling_date,bottling_location,number_of_bottles\n" +
                "12345678901,wine_name,,country_of_origin,produced_by,producer_address,40.123776,40.8962975,varietal_name,2022,wine_type,wine_color,2022-10-07,harvest_location,2022-10-08,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,2022-10-09,bottling_location,2000");
        setupWinery();

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                Constants.ERROR_CODE.INVALID_DATA, ScmUploadLotServiceImpl.INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(file, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(lotRepository, never()).findByLotIdIn(any());
        verify(lotRepository, never()).saveAll(any());
    }

    @Test
    void uploadCsvFile_BlankHCountryOfOrigin_ThrowOriginatePilotException() {
        MultipartFile file = createMockMultipartFile("invalid.csv", "text/csv", "lot_number,wine_name,origin,country_of_origin,produced_by,producer_address,producer_latitude,producer_longitude,varietal_name,vintage_year,wine_type,wine_color,harvest_date,harvest_location,pressing_date,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,bottling_date,bottling_location,number_of_bottles\n" +
                "12345678901,wine_name,origin,,produced_by,producer_address,40.123776,40.8962975,varietal_name,2022,wine_type,wine_color,2022-10-07,harvest_location,2022-10-08,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,2022-10-09,bottling_location,2000");
        setupWinery();

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                Constants.ERROR_CODE.INVALID_DATA, ScmUploadLotServiceImpl.INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(file, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(lotRepository, never()).findByLotIdIn(any());
        verify(lotRepository, never()).saveAll(any());
    }

    @Test
    void uploadCsvFile_BlankProducedBy_ThrowOriginatePilotException() {
        MultipartFile file = createMockMultipartFile("invalid.csv", "text/csv", "lot_number,wine_name,origin,country_of_origin,produced_by,producer_address,producer_latitude,producer_longitude,varietal_name,vintage_year,wine_type,wine_color,harvest_date,harvest_location,pressing_date,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,bottling_date,bottling_location,number_of_bottles\n" +
                "12345678901,wine_name,origin,country_of_origin,,producer_address,40.123776,40.8962975,varietal_name,2022,wine_type,wine_color,2022-10-07,harvest_location,2022-10-08,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,2022-10-09,bottling_location,2000");
        setupWinery();

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                Constants.ERROR_CODE.INVALID_DATA, ScmUploadLotServiceImpl.INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(file, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(lotRepository, never()).findByLotIdIn(any());
        verify(lotRepository, never()).saveAll(any());
    }

    @Test
    void uploadCsvFile_BlankProducerAddress_ThrowOriginatePilotException() {
        MultipartFile file = createMockMultipartFile("invalid.csv", "text/csv", "lot_number,wine_name,origin,country_of_origin,produced_by,producer_address,producer_latitude,producer_longitude,varietal_name,vintage_year,wine_type,wine_color,harvest_date,harvest_location,pressing_date,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,bottling_date,bottling_location,number_of_bottles\n" +
                "12345678901,wine_name,origin,country_of_origin,produced_by,,40.123776,40.8962975,varietal_name,2022,wine_type,wine_color,2022-10-07,harvest_location,2022-10-08,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,2022-10-09,bottling_location,2000");
        setupWinery();

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                Constants.ERROR_CODE.INVALID_DATA, ScmUploadLotServiceImpl.INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(file, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(lotRepository, never()).findByLotIdIn(any());
        verify(lotRepository, never()).saveAll(any());
    }

    @Test
    void uploadCsvFile_BlankVarietalName_ThrowOriginatePilotException() {
        MultipartFile file = createMockMultipartFile("invalid.csv", "text/csv", "lot_number,wine_name,origin,country_of_origin,produced_by,producer_address,producer_latitude,producer_longitude,varietal_name,vintage_year,wine_type,wine_color,harvest_date,harvest_location,pressing_date,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,bottling_date,bottling_location,number_of_bottles\n" +
                "12345678901,wine_name,origin,country_of_origin,produced_by,producer_address,41.1232016,40.8962975,,2022,wine_type,wine_color,2022-10-07,harvest_location,2022-10-08,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,2022-10-09,bottling_location,2000");
        setupWinery();

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                Constants.ERROR_CODE.INVALID_DATA, ScmUploadLotServiceImpl.INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(file, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(lotRepository, never()).findByLotIdIn(any());
        verify(lotRepository, never()).saveAll(any());
    }

    @Test
    void uploadCsvFile_BlankVintageYear_ThrowOriginatePilotException() {
        MultipartFile file = createMockMultipartFile("invalid.csv", "text/csv", "lot_number,wine_name,origin,country_of_origin,produced_by,producer_address,producer_latitude,producer_longitude,varietal_name,vintage_year,wine_type,wine_color,harvest_date,harvest_location,pressing_date,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,bottling_date,bottling_location,number_of_bottles\n" +
                "12345678901,wine_name,origin,country_of_origin,produced_by,producer_address,41.1232016,40.8962975,varietal_name,,wine_type,wine_color,2022-10-07,harvest_location,2022-10-08,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,2022-10-09,bottling_location,2000");
        setupWinery();

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                Constants.ERROR_CODE.INVALID_DATA, ScmUploadLotServiceImpl.INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(file, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(lotRepository, never()).findByLotIdIn(any());
        verify(lotRepository, never()).saveAll(any());
    }

    @Test
    void uploadCsvFile_BlankWineType_ThrowOriginatePilotException() {
        MultipartFile file = createMockMultipartFile("invalid.csv", "text/csv", "lot_number,wine_name,origin,country_of_origin,produced_by,producer_address,producer_latitude,producer_longitude,varietal_name,vintage_year,wine_type,wine_color,harvest_date,harvest_location,pressing_date,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,bottling_date,bottling_location,number_of_bottles\n" +
                "12345678901,wine_name,origin,country_of_origin,produced_by,producer_address,41.1232016,40.8962975,varietal_name,2022,,wine_color,2022-10-07,harvest_location,2022-10-08,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,2022-10-09,bottling_location,2000");
        setupWinery();

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                Constants.ERROR_CODE.INVALID_DATA, ScmUploadLotServiceImpl.INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(file, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(lotRepository, never()).findByLotIdIn(any());
        verify(lotRepository, never()).saveAll(any());
    }

    @Test
    void uploadCsvFile_BlankWineColor_ThrowOriginatePilotException() {
        MultipartFile file = createMockMultipartFile("invalid.csv", "text/csv", "lot_number,wine_name,origin,country_of_origin,produced_by,producer_address,producer_latitude,producer_longitude,varietal_name,vintage_year,wine_type,wine_color,harvest_date,harvest_location,pressing_date,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,bottling_date,bottling_location,number_of_bottles\n" +
                "12345678901,wine_name,origin,country_of_origin,produced_by,producer_address,41.1232016,40.8962975,varietal_name,2022,wine_type,,2022-10-07,harvest_location,2022-10-08,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,2022-10-09,bottling_location,2000");
        setupWinery();

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                Constants.ERROR_CODE.INVALID_DATA, ScmUploadLotServiceImpl.INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(file, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(lotRepository, never()).findByLotIdIn(any());
        verify(lotRepository, never()).saveAll(any());
    }

    @Test
    void uploadCsvFile_BlankHarvestDate_ThrowOriginatePilotException() {
        MultipartFile file = createMockMultipartFile("invalid.csv", "text/csv", "lot_number,wine_name,origin,country_of_origin,produced_by,producer_address,producer_latitude,producer_longitude,varietal_name,vintage_year,wine_type,wine_color,harvest_date,harvest_location,pressing_date,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,bottling_date,bottling_location,number_of_bottles\n" +
                "12345678901,wine_name,origin,country_of_origin,produced_by,producer_address,41.1232016,40.8962975,varietal_name,2022,wine_type,wine_color,,harvest_location,2022-10-08,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,2022-10-09,bottling_location,2000");
        setupWinery();

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                Constants.ERROR_CODE.INVALID_DATA, ScmUploadLotServiceImpl.INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(file, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(lotRepository, never()).findByLotIdIn(any());
        verify(lotRepository, never()).saveAll(any());
    }

    @Test
    void uploadCsvFile_BlankHarvestLocation_ThrowOriginatePilotException() {
        MultipartFile file = createMockMultipartFile("invalid.csv", "text/csv", "lot_number,wine_name,origin,country_of_origin,produced_by,producer_address,producer_latitude,producer_longitude,varietal_name,vintage_year,wine_type,wine_color,harvest_date,harvest_location,pressing_date,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,bottling_date,bottling_location,number_of_bottles\n" +
                "12345678901,wine_name,origin,country_of_origin,produced_by,producer_address,41.1232016,40.8962975,varietal_name,2022,wine_type,wine_color,2022-10-07,,2022-10-08,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,2022-10-09,bottling_location,2000");
        setupWinery();

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                Constants.ERROR_CODE.INVALID_DATA, ScmUploadLotServiceImpl.INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(file, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(lotRepository, never()).findByLotIdIn(any());
        verify(lotRepository, never()).saveAll(any());
    }

    @Test
    void uploadCsvFile_BlankPressingDate_ThrowOriginatePilotException() {
        MultipartFile file = createMockMultipartFile("invalid.csv", "text/csv", "lot_number,wine_name,origin,country_of_origin,produced_by,producer_address,producer_latitude,producer_longitude,varietal_name,vintage_year,wine_type,wine_color,harvest_date,harvest_location,pressing_date,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,bottling_date,bottling_location,number_of_bottles\n" +
                "12345678901,wine_name,origin,country_of_origin,produced_by,producer_address,41.1232016,40.8962975,varietal_name,2022,wine_type,wine_color,2022-10-07,harvest_location,,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,2022-10-09,bottling_location,2000");
        setupWinery();

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                Constants.ERROR_CODE.INVALID_DATA, ScmUploadLotServiceImpl.INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(file, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(lotRepository, never()).findByLotIdIn(any());
        verify(lotRepository, never()).saveAll(any());
    }

    @Test
    void uploadCsvFile_BlankFermentationLng_ThrowOriginatePilotException() {
        MultipartFile file = createMockMultipartFile("invalid.csv", "text/csv", "lot_number,wine_name,origin,country_of_origin,produced_by,producer_address,producer_latitude,producer_longitude,varietal_name,vintage_year,wine_type,wine_color,harvest_date,harvest_location,pressing_date,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,bottling_date,bottling_location,number_of_bottles\n" +
                "12345678901,wine_name,origin,country_of_origin,produced_by,producer_address,41.1232016,40.8962975,varietal_name,2022,wine_type,wine_color,2022-10-07,harvest_location,2022-10-08,processing_location,,fermentation_duration,aging_recipient,aging_time,storage_vessel,2022-10-09,bottling_location,2000");
        setupWinery();

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                Constants.ERROR_CODE.INVALID_DATA, ScmUploadLotServiceImpl.INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(file, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(lotRepository, never()).findByLotIdIn(any());
        verify(lotRepository, never()).saveAll(any());
    }

    @Test
    void uploadCsvFile_BlankFermentationDuration_ThrowOriginatePilotException() {
        MultipartFile file = createMockMultipartFile("invalid.csv", "text/csv", "lot_number,wine_name,origin,country_of_origin,produced_by,producer_address,producer_latitude,producer_longitude,varietal_name,vintage_year,wine_type,wine_color,harvest_date,harvest_location,pressing_date,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,bottling_date,bottling_location,number_of_bottles\n" +
                "12345678901,wine_name,origin,country_of_origin,produced_by,producer_address,41.1232016,40.8962975,varietal_name,2022,wine_type,wine_color,2022-10-07,harvest_location,2022-10-08,processing_location,fermentation_vessel,,aging_recipient,aging_time,storage_vessel,2022-10-09,bottling_location,2000");
        setupWinery();

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                Constants.ERROR_CODE.INVALID_DATA, ScmUploadLotServiceImpl.INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(file, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(lotRepository, never()).findByLotIdIn(any());
        verify(lotRepository, never()).saveAll(any());
    }

    @Test
    void uploadCsvFile_BlankStorageVessel_ThrowOriginatePilotException() {
        MultipartFile file = createMockMultipartFile("invalid.csv", "text/csv", "lot_number,wine_name,origin,country_of_origin,produced_by,producer_address,producer_latitude,producer_longitude,varietal_name,vintage_year,wine_type,wine_color,harvest_date,harvest_location,pressing_date,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,bottling_date,bottling_location,number_of_bottles\n" +
                "12345678901,wine_name,origin,country_of_origin,produced_by,producer_address,41.1232016,40.8962975,varietal_name,2022,wine_type,wine_color,2022-10-07,harvest_location,2022-10-08,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,,2022-10-09,bottling_location,2000");
        setupWinery();

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                Constants.ERROR_CODE.INVALID_DATA, ScmUploadLotServiceImpl.INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(file, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(lotRepository, never()).findByLotIdIn(any());
        verify(lotRepository, never()).saveAll(any());
    }

    @Test
    void uploadCsvFile_BlankBottlingLocation_ThrowOriginatePilotException() {
        MultipartFile file = createMockMultipartFile("invalid.csv", "text/csv", "lot_number,wine_name,origin,country_of_origin,produced_by,producer_address,producer_latitude,producer_longitude,varietal_name,vintage_year,wine_type,wine_color,harvest_date,harvest_location,pressing_date,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,bottling_date,bottling_location,number_of_bottles\n" +
                "12345678901,wine_name,origin,country_of_origin,produced_by,producer_address,41.1232016,40.8962975,varietal_name,2022,wine_type,wine_color,2022-10-07,harvest_location,2022-10-08,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,2022-10-09,,2000");
        setupWinery();

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                Constants.ERROR_CODE.INVALID_DATA, ScmUploadLotServiceImpl.INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(file, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(lotRepository, never()).findByLotIdIn(any());
        verify(lotRepository, never()).saveAll(any());
    }

    @Test
    void uploadCsvFile_BlankNumberOfBottles_ThrowOriginatePilotException() {
        MultipartFile file = createMockMultipartFile("invalid.csv", "text/csv", "lot_number,wine_name,origin,country_of_origin,produced_by,producer_address,producer_latitude,producer_longitude,varietal_name,vintage_year,wine_type,wine_color,harvest_date,harvest_location,pressing_date,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,bottling_date,bottling_location,number_of_bottles\n" +
                "12345678901,wine_name,origin,country_of_origin,produced_by,producer_address,41.1232016,40.8962975,varietal_name,2022,wine_type,wine_color,2022-10-07,harvest_location,2022-10-08,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,2022-10-09,bottling_location,");
        setupWinery();

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                Constants.ERROR_CODE.INVALID_DATA, ScmUploadLotServiceImpl.INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(file, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(lotRepository, never()).findByLotIdIn(any());
        verify(lotRepository, never()).saveAll(any());
    }

    @Test
    void uploadCsvFile_InvalidLotIdLength_ThrowOriginatePilotException() {
        MultipartFile file = createMockMultipartFile("invalid.csv", "text/csv", "lot_number,wine_name,origin,country_of_origin,produced_by,producer_address,producer_latitude,producer_longitude,varietal_name,vintage_year,wine_type,wine_color,harvest_date,harvest_location,pressing_date,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,bottling_date,bottling_location,number_of_bottles\n" +
                "1234567890,wine_name,origin,country_of_origin,produced_by,producer_address,41.1232016,40.8962975,varietal_name,2022,wine_type,wine_color,2022-10-07,harvest_location,2022-10-08,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,2022-10-09,bottling_location,2000");
        setupWinery();

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                Constants.ERROR_CODE.INVALID_DATA, ScmUploadLotServiceImpl.INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(file, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(lotRepository, never()).findByLotIdIn(any());
        verify(lotRepository, never()).saveAll(any());
    }

    @Test
    void uploadCsvFile_InvalidTotalBottles_ThrowOriginatePilotException() {
        MultipartFile file = createMockMultipartFile("invalid.csv", "text/csv", "lot_number,wine_name,origin,country_of_origin,produced_by,producer_address,producer_latitude,producer_longitude,varietal_name,vintage_year,wine_type,wine_color,harvest_date,harvest_location,pressing_date,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,bottling_date,bottling_location,number_of_bottles\n" +
                "12345678901,wine_name,origin,country_of_origin,produced_by,producer_address,41.1232016,40.8962975,varietal_name,2022,wine_type,wine_color,2022-10-07,harvest_location,2022-10-08,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,2022-10-09,bottling_location,0");
        setupWinery();

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                Constants.ERROR_CODE.INVALID_DATA, ScmUploadLotServiceImpl.INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(file, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(lotRepository, never()).findByLotIdIn(any());
        verify(lotRepository, never()).saveAll(any());
    }

    @Test
    void uploadCsvFile_InvalidRangeProducerLatitude_ThrowOriginatePilotException() {
        MultipartFile file = createMockMultipartFile("invalid.csv", "text/csv", "lot_number,wine_name,origin,country_of_origin,produced_by,producer_address,producer_latitude,producer_longitude,varietal_name,vintage_year,wine_type,wine_color,harvest_date,harvest_location,pressing_date,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,bottling_date,bottling_location,number_of_bottles\n" +
                "12345678901,wine_name,origin,country_of_origin,produced_by,producer_address,91,40.8962975,varietal_name,2022,wine_type,wine_color,2022-10-07,harvest_location,2022-10-08,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,2022-10-09,bottling_location,2000");
        setupWinery();

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                Constants.ERROR_CODE.INVALID_DATA, ScmUploadLotServiceImpl.INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(file, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(lotRepository, never()).findByLotIdIn(any());
        verify(lotRepository, never()).saveAll(any());
    }

    @Test
    void uploadCsvFile_InvalidRangeProducerLongitude_ThrowOriginatePilotException() {
        MultipartFile file = createMockMultipartFile("invalid.csv", "text/csv", "lot_number,wine_name,origin,country_of_origin,produced_by,producer_address,producer_latitude,producer_longitude,varietal_name,vintage_year,wine_type,wine_color,harvest_date,harvest_location,pressing_date,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,bottling_date,bottling_location,number_of_bottles\n" +
                "12345678901,wine_name,origin,country_of_origin,produced_by,producer_address,41.1232016,181,varietal_name,2022,wine_type,wine_color,2022-10-07,harvest_location,2022-10-08,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,2022-10-09,bottling_location,2000");
        setupWinery();

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                Constants.ERROR_CODE.INVALID_DATA, ScmUploadLotServiceImpl.INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(file, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(lotRepository, never()).findByLotIdIn(any());
        verify(lotRepository, never()).saveAll(any());
    }

    @Test
    void uploadCsvFile_InputFileNoData_ThrowOriginatePilotException() {
        MultipartFile file = createMockMultipartFile("invalid.csv", "text/csv", "lot_number,wine_name,origin,country_of_origin,produced_by,producer_address,producer_latitude,producer_longitude,varietal_name,vintage_year,wine_type,wine_color,harvest_date,harvest_location,pressing_date,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,bottling_date,bottling_location,number_of_bottles");
        setupWinery();

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                Constants.ERROR_CODE.INVALID_DATA, ScmUploadLotServiceImpl.INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(file, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(lotRepository, never()).findByLotIdIn(any());
        verify(lotRepository, never()).saveAll(any());
    }

    @Test
    void uploadCsvFile_InputFileHeader_ThrowOriginatePilotException() {
        MultipartFile file = createMockMultipartFile("invalid.csv", "text/csv",
                "wine_name,origin,country_of_origin,produced_by,producer_address,producer_latitude,producer_longitude,varietal_name,vintage_year,wine_type,wine_color,harvest_date,harvest_location,pressing_date,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,bottling_date,bottling_location,number_of_bottles");
        setupWinery();

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                Constants.ERROR_CODE.INVALID_DATA, ScmUploadLotServiceImpl.INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(file, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(lotRepository, never()).findByLotIdIn(any());
        verify(lotRepository, never()).saveAll(any());
    }

    @Test
    void uploadCsvFile_Exception_ThrowOriginatePilotException() {
        MultipartFile file = createValidFile();
        setupWinery();

        when(lotRepository.findByLotIdIn(any())).thenThrow(new RuntimeException());

        OriginatePilotException expectedException = new OriginatePilotException(new OriginatePilotError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), HttpStatus.INTERNAL_SERVER_ERROR));

        OriginatePilotException actualException = assertThrows(OriginatePilotException.class, () -> {
            scmUploadLotService.uploadCsvFile(file, WINERY_ID);
        }, "Expected OriginatePilotException to be thrown");

        assertAll("Exception properties",
                () -> assertEquals(expectedException.getError().getCode(), actualException.getError().getCode()),
                () -> assertEquals(expectedException.getError().getMessage(), actualException.getError().getMessage()),
                () -> assertEquals(expectedException.getError().getHttpStatus(), actualException.getError().getHttpStatus())
        );

        verify(lotRepository, never()).saveAll(any());
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

    private MultipartFile createValidFile() {
        return createMockMultipartFile("valid.csv", "text/csv", "lot_number,wine_name,origin,country_of_origin,produced_by,producer_address,producer_latitude,producer_longitude,varietal_name,vintage_year,wine_type,wine_color,harvest_date,harvest_location,pressing_date,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,bottling_date,bottling_location,number_of_bottles\n" +
                "12345678901,wine_name,origin,country_of_origin,produced_by,producer_address,40.123776,40.8962975,varietal_name,2022,wine_type,wine_color,2022-10-07,harvest_location,2022-10-08,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,2022-10-09,bottling_location,2000\n" +
                "12345678902,wine_name,origin,country_of_origin,produced_by,producer_address,40.123776,40.8962975,varietal_name,2022,wine_type,wine_color,2022-10-07,harvest_location,2022-10-08,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,2022-10-09,bottling_location,2000\n" +
                "12345678903,wine_name,origin,country_of_origin,produced_by,producer_address,40.123776,40.8962975,varietal_name,2022,wine_type,wine_color,2022-10-07,harvest_location,2022-10-08,processing_location,fermentation_vessel,fermentation_duration,aging_recipient,aging_time,storage_vessel,2022-10-09,bottling_location,2000\n" +
                "12345678904,wine_name,origin,country_of_origin,produced_by,producer_address,,,varietal_name,2022,wine_type,wine_color,2022-10-07,harvest_location,2022-10-08,processing_location,fermentation_vessel,fermentation_duration,,,storage_vessel,,bottling_location,2000");
    }

    private void checkSearchLotsId(List<String> lotsId) {
        assertEquals(4, lotsId.size());

        assertEquals("12345678901", lotsId.get(0));
        assertEquals("12345678902", lotsId.get(1));
        assertEquals("12345678903", lotsId.get(2));
        assertEquals("12345678904", lotsId.get(3));
    }

    private void checkSavedLotsData(List<Lot> savedLots) {
        assertEquals(2, savedLots.size());

        Lot lotData = savedLots.get(0);

        assertEquals("12345678901", lotData.getLotId());
        assertEquals("wine_name", lotData.getWineName());
        assertEquals("origin", lotData.getOrigin());
        assertEquals("country_of_origin", lotData.getCountryOfOrigin());
        assertEquals("produced_by", lotData.getProducedBy());
        assertEquals("producer_address", lotData.getProducerAddress());
        assertEquals(40.123776, lotData.getProducerLatitude());
        assertEquals(40.8962975, lotData.getProducerLongitude());
        assertEquals("varietal_name", lotData.getVarietalName());
        assertEquals(2022, lotData.getVintageYear());
        assertEquals("wine_type", lotData.getWineType());
        assertEquals("wine_color", lotData.getWineColor());
        assertEquals(LocalDate.parse("2022-10-07"), lotData.getHarvestDate());
        assertEquals("harvest_location", lotData.getHarvestLocation());
        assertEquals(LocalDate.parse("2022-10-08"), lotData.getPressingDate());
        assertEquals("processing_location", lotData.getProcessingLocation());
        assertEquals("fermentation_vessel", lotData.getFermentationVessel());
        assertEquals("fermentation_duration", lotData.getFermentationDuration());
        assertEquals("aging_recipient", lotData.getAgingRecipient());
        assertEquals("aging_time", lotData.getAgingTime());
        assertEquals("storage_vessel", lotData.getStorageVessel());
        assertEquals(LocalDate.parse("2022-10-09"), lotData.getBottlingDate());
        assertEquals("bottling_location", lotData.getBottlingLocation());
        assertEquals(2000, lotData.getNumberOfBottles());
        assertEquals(Constants.LOT_STATUS.NOT_FINALIZED, lotData.getStatus());

        Lot lotDataNew = savedLots.get(1);

        assertEquals("12345678904", lotDataNew.getLotId());
        assertEquals("wine_name", lotDataNew.getWineName());
        assertEquals("origin", lotDataNew.getOrigin());
        assertEquals("country_of_origin", lotDataNew.getCountryOfOrigin());
        assertEquals("produced_by", lotDataNew.getProducedBy());
        assertEquals("producer_address", lotDataNew.getProducerAddress());
        assertNull(lotDataNew.getProducerLatitude());
        assertNull(lotDataNew.getProducerLongitude());
        assertEquals("varietal_name", lotDataNew.getVarietalName());
        assertEquals(2022, lotDataNew.getVintageYear());
        assertEquals("wine_type", lotDataNew.getWineType());
        assertEquals("wine_color", lotDataNew.getWineColor());
        assertEquals(LocalDate.parse("2022-10-07"), lotDataNew.getHarvestDate());
        assertEquals("harvest_location", lotDataNew.getHarvestLocation());
        assertEquals(LocalDate.parse("2022-10-08"), lotDataNew.getPressingDate());
        assertEquals("processing_location", lotDataNew.getProcessingLocation());
        assertEquals("fermentation_vessel", lotDataNew.getFermentationVessel());
        assertEquals("fermentation_duration", lotDataNew.getFermentationDuration());
        assertNull(lotDataNew.getAgingRecipient());
        assertNull(lotDataNew.getAgingTime());
        assertEquals("storage_vessel", lotDataNew.getStorageVessel());
        assertNull(lotDataNew.getBottlingDate());
        assertEquals("bottling_location", lotDataNew.getBottlingLocation());
        assertEquals(2000, lotData.getNumberOfBottles());
        assertEquals(Constants.LOT_STATUS.NOT_FINALIZED, lotDataNew.getStatus());
    }

    private MultipartFile createMockMultipartFile(String fileName, String contentType, String fileContent) {
        return new MockMultipartFile(fileName, fileName, contentType, fileContent.getBytes(StandardCharsets.UTF_8));
    }

    private List<Lot> createMockLots() {
        List<Lot> lots = new ArrayList<>();
        Lot lot1 = new Lot();
        lot1.setLotId("12345678901");
        lot1.setStatus(Constants.LOT_STATUS.NOT_FINALIZED);
        lot1.setWineryId(WINERY_ID);
        lots.add(lot1);
        Lot lot2 = new Lot();
        lot2.setLotId("12345678902");
        lot2.setStatus(Constants.LOT_STATUS.FINALIZED);
        lot2.setWineryId(WINERY_ID);
        lots.add(lot2);
        Lot lot3 = new Lot();
        lot3.setLotId("12345678903");
        lot3.setStatus(Constants.LOT_STATUS.NOT_FINALIZED);
        lot3.setWineryId("OTHER_WINERY_ID");
        lots.add(lot3);
        return lots;
    }
}
