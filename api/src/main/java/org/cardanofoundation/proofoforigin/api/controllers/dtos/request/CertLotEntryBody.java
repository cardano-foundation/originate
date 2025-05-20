package org.cardanofoundation.proofoforigin.api.controllers.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CertLotEntryBody {
    @NotNull
    @Pattern(regexp = "^.{11}$")
    String lotNumber;

    @NotNull
    String wineName;

    String wineDescription;

    String serialName;

    String origin;

    String viticultureArea;

    String type;

    String color;

    String sugarContentCategory;

    String grapeVariety;

    @Positive
    Integer harvestYear;

    Boolean delayedOnChacha;

    String bottleType;

    LocalDate bottlingDate;

    @Positive
    Double bottleVolume;

    @Positive
    Integer bottleCountInLot;
}
