package org.cardanofoundation.proofoforigin.api.controllers.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CertBody {
  @NotNull
  String certificateType;

  @NotNull
  String certificateNumber;

  @NotNull
  String exportCountry;

  String examProtocolNumber;

  String tastingProtocolNumber;

  @NotNull
  @Valid
  List<CertLotEntryBody> products;
}
