package org.cardanofoundation.proofoforigin.api.business.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.cardanofoundation.proofoforigin.api.business.ScanTrustService;
import org.cardanofoundation.proofoforigin.api.business.UploadBottleBusiness;
import org.cardanofoundation.proofoforigin.api.constants.CertUpdateStatus;
import org.cardanofoundation.proofoforigin.api.constants.Constants;
import org.cardanofoundation.proofoforigin.api.exceptions.OriginatePilotError;
import org.cardanofoundation.proofoforigin.api.exceptions.OriginatePilotErrors;
import org.cardanofoundation.proofoforigin.api.exceptions.OriginatePilotException;
import org.cardanofoundation.proofoforigin.api.repository.BottleRepository;
import org.cardanofoundation.proofoforigin.api.repository.WineryRepository;
import org.cardanofoundation.proofoforigin.api.repository.entities.Bottle;
import org.cardanofoundation.proofoforigin.api.utils.BatchUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class UploadBottleBusinessImpl implements UploadBottleBusiness {
    private static final String ACCEPT_FILE_TYPE = "text/csv";
    private static final int LOT_ID_LENGTH = 11;
    private static final int BATCH_SIZE = 1000;

    private final BottleRepository bottleRepository;
    private final WineryRepository wineryRepository;
    private final ScanTrustService scanTrustService;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void uploadCsvFile(MultipartFile csvFile, String wineryId) {
        validateFileType(csvFile);
        validateWinery(wineryId);
        List<Bottle> listBottle = csvToBottles(csvFile, wineryId);

        CompletableFuture.runAsync(() -> {
            if (listBottle.isEmpty()) {
                return;
            }
            // Remove duplicate bottle
            Map<String, Bottle> bottleMap = new HashMap<>();
            for (Bottle bottle : listBottle) {
                bottleMap.put(bottle.getId(), bottle);
            }
            List<Bottle> bottlesDistinct = new ArrayList<>(bottleMap.values());

            BatchUtil.doBatching(BATCH_SIZE, bottlesDistinct, bottleRepository::saveAll);

            BatchUtil.doBatching(Constants.SCANTRUST.SYNC_BATCH_SIZE, bottlesDistinct,
                    scanTrustService::sendScmDataWhenMappingBottle);
        });
    }

    public void validateFileType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new OriginatePilotException(OriginatePilotErrors.FILE_MISSING);
        } else if (!ACCEPT_FILE_TYPE.equalsIgnoreCase(file.getContentType())) {
            throw new OriginatePilotException(OriginatePilotErrors.INVALID_FILE_TYPE);
        }
    }

    public void validateWinery(String wineryId) {
        if (wineryRepository.findById(wineryId).isEmpty()) {
            throw new OriginatePilotException(new OriginatePilotError(HttpStatus.NOT_FOUND.value(), "Winery does not exist", HttpStatus.NOT_FOUND));
        }
    }

    public List<Bottle> csvToBottles(MultipartFile csvFile, String wineryId) {
        CSVFormat csvFormat = CSVFormat.DEFAULT
                .builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .build();
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(csvFile.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader, csvFormat)) {

            List<Bottle> bottles = new ArrayList<>();
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecords) {
                Bottle bottle = parseCSV(csvRecord);
                bottle.setWineryId(wineryId);
                bottle.setLotUpdateStatus(Constants.SCANTRUST.STATUS.NOT_UPDATED);
                bottle.setCertUpdateStatus(CertUpdateStatus.NOT_UPDATED);
                if (!validateBottle(bottle)) {
                    throw new OriginatePilotException(OriginatePilotErrors.INVALID_DATA);
                }
                bottles.add(bottle);
            }

            if (bottles.isEmpty()) {
                throw new OriginatePilotException(OriginatePilotErrors.INVALID_PARAMETERS);
            }

            return bottles;
        } catch (Exception e) {
            throw new OriginatePilotException(OriginatePilotErrors.INVALID_DATA, e.getMessage(), e.getCause());
        }
    }

    private Bottle parseCSV(CSVRecord csvRecord) {
        try {
            return Bottle.builder()
                    .id(csvRecord.get(BottleCsvHeaders.EXTENDED_ID.getName()))
                    .lotId(csvRecord.get(BottleCsvHeaders.LOT_ID.getName()))
                    .sequentialNumber(Integer.parseInt(csvRecord.get(BottleCsvHeaders.SEQUENTIAL_NUMBER.getName())))
                    .reelNumber(Integer.parseInt(csvRecord.get(BottleCsvHeaders.REEL_NUMBER.getName())))
                    .sequentialNumberInLot(Integer.parseInt(csvRecord.get(BottleCsvHeaders.SEQUENTIAL_NUMBER_IN_LOT.getName())))
                    .build();
        } catch (Exception e) {
            throw new OriginatePilotException(OriginatePilotErrors.INVALID_DATA, e.getMessage(), e.getCause());
        }
    }

    private boolean validateBottle(Bottle bottle) {
        String lotId = bottle.getLotId();
        Integer sequentialNumber = bottle.getSequentialNumber();
        Integer reelNumber = bottle.getReelNumber();
        Integer sequentialNumberInLot = bottle.getSequentialNumberInLot();
        if (bottle.getId() == null || bottle.getId().isBlank()) {
            return false;
        }
        if (lotId.length() != LOT_ID_LENGTH) {
            return false;
        }
        if (sequentialNumber == null || sequentialNumber < 0) {
            return false;
        }
        if (reelNumber == null || reelNumber < 0) {
            return false;
        }
        if (sequentialNumberInLot == null || sequentialNumberInLot < 1) {
            return false;
        }
        return true;
    }

    private enum BottleCsvHeaders {
        EXTENDED_ID("extended_id"), LOT_ID("lot_id"), SEQUENTIAL_NUMBER("sequential_number"), REEL_NUMBER("reel_number"), SEQUENTIAL_NUMBER_IN_LOT("sequential_number_in_lot");
        private final String name;

        BottleCsvHeaders(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
