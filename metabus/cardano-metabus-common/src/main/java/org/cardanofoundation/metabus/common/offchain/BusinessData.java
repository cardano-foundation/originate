package org.cardanofoundation.metabus.common.offchain;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessData {
    String type;
    String subType;
    Object data;
    byte[] jwsHeader;
    byte[] signature;
    byte[] pubKey;
}
