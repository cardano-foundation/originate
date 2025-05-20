package org.cardanofoundation.proofoforigin.api.controllers.dtos.response;

import lombok.*;
import org.cardanofoundation.proofoforigin.api.repository.entities.Bottle;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BottleDto {

    private String id;

    private String lotId;

    private Integer sequentialNumber;

    private Integer reelNumber;

    private String certificateId;

    public static BottleDto toBottleDto(Bottle bottle){
        return BottleDto.builder()
                .id(bottle.getId())
                .lotId(bottle.getLotId())
                .sequentialNumber(bottle.getSequentialNumber())
                .reelNumber(bottle.getReelNumber())
                .certificateId(bottle.getCertificateId())
                .build();
    }
}
