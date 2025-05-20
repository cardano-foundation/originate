package org.cardanofoundation.proofoforigin.api.controllers.dtos.request;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BottleIdBody {
  // list bottle ids to associate
  @Valid List<String> add;
  // list bottle ids to dis-associate
  List<String> remove;
  Boolean finalise;
}
