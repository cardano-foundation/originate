package org.cardanofoundation.proofoforigin.api.controllers.dtos.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;


@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WineryCertsResponse {
    List<CertsResponse> listCerts;
    String wineryId;
}
