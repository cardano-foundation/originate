package org.cardanofoundation.proofoforigin.api.controllers.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * The POJO class define the structure of the request body of the On-chain
 * revocation request API
 * </p>
 * 
 * @author (Sotatek) joey.dao
 * @since 2023/08
 * @version 0.01
 * @category Request-Body
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RevokeCertBody {
    @NotNull
    @NotBlank
    String signature;

    @NotNull
    @NotBlank
    String publicKeyBase64Url;
}
