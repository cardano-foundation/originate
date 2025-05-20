package org.cardanofoundation.proofoforigin.api.configuration.modal;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessData {
    String type;
    Object data;
    String signature;
    String pubKey;
}
