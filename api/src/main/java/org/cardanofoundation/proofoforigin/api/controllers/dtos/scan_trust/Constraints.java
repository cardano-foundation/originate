package org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nimbusds.jose.shaded.gson.annotations.SerializedName;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Constraints {

    @SerializedName("extended_id")
    private List<String> extendedId;

    @JsonProperty("lot_number")
    @SerializedName("lot_id")
    private String lotId;

    public static Constraints initWithLotId(String lotId) {
        Constraints constraints = new Constraints();
        constraints.setLotId(lotId);
        return constraints;
    }
}
