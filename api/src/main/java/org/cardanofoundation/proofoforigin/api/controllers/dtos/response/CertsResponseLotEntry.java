package org.cardanofoundation.proofoforigin.api.controllers.dtos.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CertsResponseLotEntry {
  String lotId;
  String scanningStatus;
}
