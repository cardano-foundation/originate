package org.cardanofoundation.proofoforigin.api.controllers.dtos.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.cardanofoundation.proofoforigin.api.constants.Constants;
import org.cardanofoundation.proofoforigin.api.repository.entities.Lot;

import java.time.format.DateTimeFormatter;


@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class LotSCMResponse {
    String lotNumber;

    String wineName;

    String origin;

    String countryOfOrigin;

    String producedBy;

    String producerAddress;

    Double producerLatitude;

    Double producerLongitude;

    String varietalName;

    Integer vintageYear;

    String wineType;

    String wineColor;

    String harvestDate;

    String harvestLocation;

    String pressingDate;

    String processingLocation;

    String fermentationVessel;

    String fermentationDuration;

    String agingRecipient;

    String agingTime;

    String storageVessel;

    String bottlingDate;

    String bottlingLocation;

    Integer numberOfBottles;

    String winerySignature;

    String txId;

    String status;

    public static LotSCMResponse buildLotSCMResponse(Lot lot) {
        final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LotSCMResponse.builder()
                .lotNumber(lot.getLotId())
                .wineName(lot.getWineName())
                .origin(lot.getOrigin())
                .countryOfOrigin(lot.getCountryOfOrigin())
                .producedBy(lot.getProducedBy())
                .producerAddress(lot.getProducerAddress())
                .producerLatitude(lot.getProducerLatitude())
                .producerLongitude(lot.getProducerLongitude())
                .varietalName(lot.getVarietalName())
                .vintageYear(lot.getVintageYear())
                .wineType(lot.getWineType())
                .wineColor(lot.getWineColor())
                .harvestDate(lot.getHarvestDate().format(DATE_FORMAT))
                .harvestLocation(lot.getHarvestLocation())
                .pressingDate(lot.getPressingDate().format(DATE_FORMAT))
                .processingLocation(lot.getProcessingLocation())
                .fermentationVessel(lot.getFermentationVessel())
                .fermentationDuration(lot.getFermentationDuration())
                .agingRecipient(lot.getAgingRecipient())
                .agingTime(lot.getAgingTime())
                .storageVessel(lot.getStorageVessel())
                .bottlingDate(lot.getBottlingDate() == null ? null : lot.getBottlingDate().format(DATE_FORMAT))
                .bottlingLocation(lot.getBottlingLocation())
                .numberOfBottles(lot.getNumberOfBottles())
                .winerySignature(lot.getWinerySignature())
                .txId(lot.getTxId())
                .status(getLotStatus(lot.getStatus()))
                .build();
    }

    private static String getLotStatus(Integer status) {
        return switch (status) {
            case Constants.LOT_STATUS.FINALIZED -> Constants.LOT_STATUS_VALUE.FINALISED;
            case Constants.LOT_STATUS.APPROVED -> Constants.LOT_STATUS_VALUE.APPROVED;
            default -> Constants.LOT_STATUS_VALUE.NOT_FINALISED;
        };
    }
}
