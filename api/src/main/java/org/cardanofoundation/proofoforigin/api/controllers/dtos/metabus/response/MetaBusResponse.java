package org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
public class MetaBusResponse<T> {
    T data;
    Metadata meta;
}

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
class Metadata {
    private String code;
    private Integer page;
    private Integer size;
    private Long total;
    private String message;
    private String internalMessage;
    private String requestId;
}
