package org.cardanofoundation.proofoforigin.api.controllers.dtos.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.cardanofoundation.proofoforigin.api.constants.ScanningStatus;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class BottlesInformation {
    String certId;

    String certNumber;

    String certType;

    String lotId;

    ScanningStatus scanningStatus;

    Integer sequentialNumber;

    Integer reelNumber;
}
