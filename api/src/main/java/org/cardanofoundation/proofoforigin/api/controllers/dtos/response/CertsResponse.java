package org.cardanofoundation.proofoforigin.api.controllers.dtos.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CertsResponse {
  String id;
  String certificateNumber;
  String certificateType;
  Set<CertsResponseLotEntry> lotEntries;
}
