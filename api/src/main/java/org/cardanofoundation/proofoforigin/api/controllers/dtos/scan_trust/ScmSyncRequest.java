package org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nimbusds.jose.shaded.gson.annotations.SerializedName;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ScmSyncRequest {
    @JsonProperty("data_key")
    private String dataKey;

    @JsonProperty("items")
    private List<ScmUploadItem> items;
}
