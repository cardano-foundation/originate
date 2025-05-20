package org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.proofoforigin.api.constants.Constants;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.CertLotEntryBody;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CertificateLotEntryDataDto {
    String lotNumber;
    String wineName;
    String wineDescription;
    String serialName;
    String origin;
    String viticultureArea;
    String type;
    String color;
    String sugarContentCategory;
    String grapeVariety;
    Integer harvestYear;
    Boolean delayedOnChacha;
    String bottleType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.CERTIFICATE.DATE_FORMAT)
    LocalDate bottlingDate;

    Double bottleVolume;
    Integer bottleCountInLot;

    public static CertificateLotEntryDataDto toLotEntriesDto(CertLotEntryBody certLotEntry) {
        return CertificateLotEntryDataDto.builder()
                .lotNumber(certLotEntry.getLotNumber())
                .wineName(certLotEntry.getWineName())
                .wineDescription(certLotEntry.getWineDescription())
                .serialName(certLotEntry.getSerialName())
                .origin(certLotEntry.getOrigin())
                .viticultureArea(certLotEntry.getViticultureArea())
                .type(certLotEntry.getType())
                .color(certLotEntry.getColor())
                .sugarContentCategory(certLotEntry.getSugarContentCategory())
                .grapeVariety(certLotEntry.getGrapeVariety())
                .harvestYear(certLotEntry.getHarvestYear())
                .delayedOnChacha(certLotEntry.getDelayedOnChacha())
                .bottleType(certLotEntry.getBottleType())
                .bottlingDate(certLotEntry.getBottlingDate())
                .bottleVolume(certLotEntry.getBottleVolume())
                .bottleCountInLot(certLotEntry.getBottleCountInLot())
                .build();
    }
}
