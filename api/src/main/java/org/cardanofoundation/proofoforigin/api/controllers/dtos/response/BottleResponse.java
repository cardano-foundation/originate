package org.cardanofoundation.proofoforigin.api.controllers.dtos.response;

import lombok.*;
import org.cardanofoundation.proofoforigin.api.constants.Constants;
import org.cardanofoundation.proofoforigin.api.repository.entities.Bottle;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BottleResponse {
    private List<BottleData> scheduled;
    private List<BottleData> success;
    private List<BottleData> error;

    public static BottleResponse toBottleResponse(List<Bottle> bottleList) {
        List<BottleData> scheduled = bottleList.stream()
                .filter(bottle -> bottle.getLotUpdateStatus() == Constants.SCANTRUST.STATUS.NOT_UPDATED)
                .map(BottleData::toBottleData).toList();
        List<BottleData> success = bottleList.stream()
                .filter(bottle -> bottle.getLotUpdateStatus() == Constants.SCANTRUST.STATUS.UPDATED)
                .map(BottleData::toBottleData).toList();
        List<BottleData> error = bottleList.stream()
                .filter(bottle -> bottle.getLotUpdateStatus() == Constants.SCANTRUST.STATUS.FAILED)
                .map(BottleData::toBottleData).toList();
        return BottleResponse.builder()
                .scheduled(scheduled).success(success).error(error).build();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BottleData {

        private String id;

        private String lotId;

        private Integer sequentialNumber;

        private Integer reelNumber;

        public static BottleData toBottleData(Bottle bottle){
            return BottleData.builder()
                    .id(bottle.getId())
                    .lotId(bottle.getLotId())
                    .sequentialNumber(bottle.getSequentialNumber())
                    .reelNumber(bottle.getReelNumber())
                    .build();
        }
    }
}
