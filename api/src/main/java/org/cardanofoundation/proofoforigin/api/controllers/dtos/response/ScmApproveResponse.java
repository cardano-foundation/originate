package org.cardanofoundation.proofoforigin.api.controllers.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScmApproveResponse {
    @JsonProperty("succeed")
    List<String> succeed;
    @JsonProperty("failLotsAlreadyApproved")
    List<String> failLotsAlreadyApproved;
    @JsonProperty("failLotsNotFinalised")
    List<String> failLotsNotFinalised;
    @JsonProperty("failLotsNotFound")
    List<String> failLotsNotFound;
    @JsonProperty("failJobsNotScheduled")
    List<String> failJobsNotScheduled;

    public ScmApproveResponse () {
        succeed = new ArrayList<>();
        failLotsAlreadyApproved = new ArrayList<>();
        failLotsNotFinalised = new ArrayList<>();
        failLotsNotFound = new ArrayList<>();
        failJobsNotScheduled = new ArrayList<>();
    }
}
