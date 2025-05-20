package org.cardanofoundation.proofoforigin.api.controllers.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CertRequest {
    @NotNull
    @Valid
    CertBody cert;

    @NotNull
    String signature;

    @NotNull
    String publicKeyBase64Url;
}
