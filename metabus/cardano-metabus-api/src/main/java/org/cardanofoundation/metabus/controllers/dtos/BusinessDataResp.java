package org.cardanofoundation.metabus.controllers.dtos;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessDataResp {
    String type;
    String subType;
    Object data;
    String jwsHeader;
    String signature;
    String pubKey;
}
