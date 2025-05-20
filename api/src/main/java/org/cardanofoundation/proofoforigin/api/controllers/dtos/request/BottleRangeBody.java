package org.cardanofoundation.proofoforigin.api.controllers.dtos.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.NotBlank;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BottleRangeBody {

  @NotBlank
  String startRange;

  @NotBlank
  String endRange;

  Boolean isSequentialNumber = true;

  Boolean finalise = false;
}
