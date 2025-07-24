package org.cardanofoundation.proofoforigin.api.business.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.cardanofoundation.proofoforigin.api.business.ScmUploadLotService;
import org.cardanofoundation.proofoforigin.api.constants.Constants;
import org.cardanofoundation.proofoforigin.api.exceptions.OriginatePilotError;
import org.cardanofoundation.proofoforigin.api.exceptions.OriginatePilotException;
import org.cardanofoundation.proofoforigin.api.repository.LotRepository;
import org.cardanofoundation.proofoforigin.api.repository.WineryRepository;
import org.cardanofoundation.proofoforigin.api.repository.entities.Lot;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScmUploadLotServiceImpl implements ScmUploadLotService {
    @Required
    private final LotRepository lotRepository;
    @Required
    private final WineryRepository wineryRepository;
    public static final String ACCEPT_FILE_TYPE = "text/csv";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String FILE_MISSING_MSG = "Missing file. Please, try again.";
    public static final String INVALID_FILE_TYPE_MSG = "Invalid file type. Please, try uploading a CSV file.";
    public static final String WINERY_NOT_FOUND_MSG = "Winery does not exist";
    public static final String INVALID_DATA_MSG = "Some data may be missing or in wrong format. Please, try again.";

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void uploadCsvFile(MultipartFile file, String wineryId) {
        try {
            validateFileType(file);
            validateWinery(wineryId);

            List<Lot> csvLots = csvToLots(file.getInputStream(), wineryId);
            List<Lot> nonFinalizedLots = filterNonFinalizedLot(csvLots, wineryId);
            lotRepository.saveAll(nonFinalizedLots);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (e instanceof OriginatePilotException) throw (OriginatePilotException) e;
            throw new OriginatePilotException(new OriginatePilotError(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    private List<Lot> filterNonFinalizedLot(List<Lot> lots, String wineryId) {
        // We need to lock the record for update, so here we select all lot existed in CSV file
        List<Lot> existingLots = lotRepository.findByLotIdIn(lots.stream().map(Lot::getLotId).toList());
        List<Lot> readOnlyLots = existingLots.stream().filter(lot -> {
            if (!lot.getWineryId().equals(wineryId)) {//Same lot but different winery -> mark readonly
                return true;
            }
            int lotStatus = lot.getStatus();
            return Constants.LOT_STATUS.FINALIZED == lotStatus || Constants.LOT_STATUS.APPROVED == lotStatus;
        }).toList();
        return lots.stream().filter(lot -> readOnlyLots.stream().noneMatch(exclusionLot -> exclusionLot.getLotId().equalsIgnoreCase(lot.getLotId()))).toList();
    }

    private void validateFileType(MultipartFile file) {
        if (file == null) {
            throw new OriginatePilotException(new OriginatePilotError(Constants.ERROR_CODE.FILE_MISSING, FILE_MISSING_MSG, HttpStatus.BAD_REQUEST));
        }

        if (!ACCEPT_FILE_TYPE.equalsIgnoreCase(file.getContentType())) {
            throw new OriginatePilotException(new OriginatePilotError(Constants.ERROR_CODE.INVALID_FILE_TYPE, INVALID_FILE_TYPE_MSG, HttpStatus.BAD_REQUEST));
        }
    }

    private void validateWinery(String wineryId) {
        if (wineryRepository.findById(wineryId).isEmpty()) {
            throw new OriginatePilotException(new OriginatePilotError(HttpStatus.NOT_FOUND.value(), WINERY_NOT_FOUND_MSG, HttpStatus.NOT_FOUND));
        }
    }

    private List<Lot> csvToLots(InputStream is, String wineryId) throws IOException {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setIgnoreHeaderCase(true).setTrim(true).build())) {

            List<Lot> lots = new ArrayList<>();
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecords) {
                Lot lot = parseCSV(csvRecord);
                lot.setStatus(Constants.LOT_STATUS.NOT_FINALIZED);
                lot.setWineryId(wineryId);
                if (!validateLot(lot)) {
                    throw new OriginatePilotException(new OriginatePilotError(Constants.ERROR_CODE.INVALID_DATA, INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));
                }

                lots.add(lot);
            }

            if (lots.isEmpty()) {
                throw new OriginatePilotException(new OriginatePilotError(Constants.ERROR_CODE.INVALID_DATA, INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));
            }

            return lots;
        } catch (UncheckedIOException | IllegalArgumentException e) {
            // This will handle the case when file with .csv extension but do not have csv format
            throw new OriginatePilotException(new OriginatePilotError(Constants.ERROR_CODE.INVALID_DATA, INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));
        }
    }

    private Lot parseCSV(CSVRecord csvRecord) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
            Lot lot = new Lot();
            lot.setLotId(csvRecord.get(Headers.LOT_NUMBER.getName()));
            lot.setWineName(csvRecord.get(Headers.WINE_NAME.getName()));
            lot.setOrigin(csvRecord.get(Headers.ORIGIN.getName()));
            lot.setCountryOfOrigin(csvRecord.get(Headers.COUNTRY_OF_ORIGIN.getName()));
            lot.setProducedBy(csvRecord.get(Headers.PRODUCED_BY.getName()));
            lot.setProducerAddress(csvRecord.get(Headers.PRODUCER_ADDRESS.getName()));
            if (!csvRecord.get(Headers.PRODUCER_LATITUDE.getName()).equals("")) {
                lot.setProducerLatitude(Double.parseDouble(csvRecord.get(Headers.PRODUCER_LATITUDE.getName())));
            }
            if (!csvRecord.get(Headers.PRODUCER_LONGITUDE.getName()).equals("")) {
                lot.setProducerLongitude(Double.parseDouble(csvRecord.get(Headers.PRODUCER_LONGITUDE.getName())));
            }
            lot.setVarietalName(csvRecord.get(Headers.VARIETAL_NAME.getName()));
            lot.setVintageYear(Integer.parseInt(csvRecord.get(Headers.VINTAGE_YEAR.getName())));
            lot.setWineType(csvRecord.get(Headers.WINE_TYPE.getName()));
            lot.setWineColor(csvRecord.get(Headers.WINE_COLOR.getName()));
            lot.setHarvestDate(LocalDate.parse(csvRecord.get(Headers.HARVEST_DATE.getName()), formatter));
            lot.setHarvestLocation(csvRecord.get(Headers.HARVEST_LOCATION.getName()));
            lot.setPressingDate(LocalDate.parse(csvRecord.get(Headers.PRESSING_DATE.getName()), formatter));
            lot.setProcessingLocation(csvRecord.get(Headers.PROCESSING_LOCATION.getName()));
            lot.setFermentationVessel(csvRecord.get(Headers.FERMENTATION_VESSEL.getName()));
            lot.setFermentationDuration(csvRecord.get(Headers.FERMENTATION_DURATION.getName()));
            if (!csvRecord.get(Headers.AGING_RECIPIENT.getName()).equals("")) {
                lot.setAgingRecipient(csvRecord.get(Headers.AGING_RECIPIENT.getName()));
            }
            if (!csvRecord.get(Headers.AGING_TIME.getName()).equals("")) {
                lot.setAgingTime(csvRecord.get(Headers.AGING_TIME.getName()));
            }
            lot.setStorageVessel(csvRecord.get(Headers.STORAGE_VESSEL.getName()));
            if (!csvRecord.get(Headers.BOTTLING_DATE.getName()).equals("")) {
                lot.setBottlingDate(LocalDate.parse(csvRecord.get(Headers.BOTTLING_DATE.getName()), formatter));
            }
            lot.setBottlingLocation(csvRecord.get(Headers.BOTTLING_LOCATION.getName()));
            lot.setNumberOfBottles(Integer.parseInt(csvRecord.get(Headers.NUMBER_OF_BOTTLES.getName())));

            return lot;
        } catch (DateTimeParseException | NullPointerException | IllegalArgumentException e) {
            throw new OriginatePilotException(new OriginatePilotError(Constants.ERROR_CODE.INVALID_DATA, INVALID_DATA_MSG, HttpStatus.BAD_REQUEST));
        }
    }

    private boolean validateLot(Lot lot) {
        if (lot.getLotId().isBlank()) {
            return false;
        }
        if (lot.getLotId().length() != Constants.LOT_ID.LENGTH) {
            return false;
        }
        if (lot.getWineName().isBlank()) {
            return false;
        }
        if (lot.getOrigin().isBlank()) {
            return false;
        }
        if (lot.getCountryOfOrigin().isBlank()) {
            return false;
        }
        if (lot.getProducedBy().isBlank()) {
            return false;
        }
        if (lot.getProducerAddress().isBlank()) {
            return false;
        }
        if (lot.getProducerLatitude() != null && Math.abs(lot.getProducerLatitude()) > Constants.LAT_LONG_RANGE.MAX_LATITUDE) {
            return false;
        }
        if (lot.getProducerLongitude() != null && Math.abs(lot.getProducerLongitude()) > Constants.LAT_LONG_RANGE.MAX_LONGITUDE) {
            return false;
        }
        if (lot.getVarietalName().isBlank()) {
            return false;
        }
        if (lot.getWineType().isBlank()) {
            return false;
        }
        if (lot.getWineColor().isBlank()) {
            return false;
        }
        if (lot.getHarvestLocation().isBlank()) {
            return false;
        }
        if (lot.getProcessingLocation().isBlank()) {
            return false;
        }
        if (lot.getFermentationVessel().isBlank()) {
            return false;
        }
        if (lot.getFermentationDuration().isBlank()) {
            return false;
        }
        if (lot.getStorageVessel().isBlank()) {
            return false;
        }
        if (lot.getBottlingLocation().isBlank()) {
            return false;
        }
        if (lot.getNumberOfBottles() <= 0) {
            return false;
        }
        return true;
    }


    public enum Headers {
        LOT_NUMBER("lot_number"), WINE_NAME("wine_name"), ORIGIN("origin"), COUNTRY_OF_ORIGIN("country_of_origin"), PRODUCED_BY("produced_by"), PRODUCER_ADDRESS("producer_address"), PRODUCER_LATITUDE("producer_latitude"), PRODUCER_LONGITUDE("producer_longitude"), VARIETAL_NAME("varietal_name"), VINTAGE_YEAR("vintage_year"), WINE_TYPE("wine_type"), WINE_COLOR("wine_color"), HARVEST_DATE("harvest_date"), HARVEST_LOCATION("harvest_location"), PRESSING_DATE("pressing_date"), PROCESSING_LOCATION("processing_location"), FERMENTATION_VESSEL("fermentation_vessel"), FERMENTATION_DURATION("fermentation_duration"), AGING_RECIPIENT("aging_recipient"), AGING_TIME("aging_time"), STORAGE_VESSEL("storage_vessel"), BOTTLING_DATE("bottling_date"), BOTTLING_LOCATION("bottling_location"), NUMBER_OF_BOTTLES("number_of_bottles");

        private final String name;

        Headers(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
