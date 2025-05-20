package org.cardanofoundation.proofoforigin.api.constants;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum TaskState {
    @JsonProperty("pending")
    PENDING(0),
    @JsonProperty("in-progress")
    IN_PROGRESS(1),
    @JsonProperty("complete")
    COMPLETED(2),
    @JsonProperty("failed")
    FAILED(3);

    public final int value;

    public Integer getValue() {
        return value;
    }


}
