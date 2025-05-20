package org.cardanofoundation.proofoforigin.api.controllers.dtos.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FinaliseLotResponse {
    Set<String> succeed;
    Set<String> failLotsNotFound;
    Set<String> failLotsAlreadyFinalised;
}
