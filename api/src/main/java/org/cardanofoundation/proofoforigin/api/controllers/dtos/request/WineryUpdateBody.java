package org.cardanofoundation.proofoforigin.api.controllers.dtos.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
public class WineryUpdateBody {
    private String name;

    @Length(max = 32)
    private String rsCode;
}
