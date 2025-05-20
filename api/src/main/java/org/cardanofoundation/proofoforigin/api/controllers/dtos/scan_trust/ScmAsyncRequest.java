package org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nimbusds.jose.shaded.gson.annotations.SerializedName;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ScmAsyncRequest {
    private Constraints constraints;

    @SerializedName("scm_data")
    @JsonProperty("scm_data")
    private ScmData scmData;

}
