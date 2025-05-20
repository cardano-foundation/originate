package org.cardanofoundation.proofoforigin.api.controllers.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
public class KeycloakUserBody {
    @NotBlank
    @Length(max = 320)
    private String email;

    @NotBlank
    private String name;
}
