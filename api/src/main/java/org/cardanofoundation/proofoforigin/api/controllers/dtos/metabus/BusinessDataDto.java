package org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class BusinessDataDto {
    private String type;

    private Object data;

    private String signature;

    private String pubKey;
}
