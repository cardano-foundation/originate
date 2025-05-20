package org.cardanofoundation.metabus.common.onchain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PROTECTED)
@SuperBuilder
public abstract class TxMetadata {
    String version;
    String type;
    String subType;
    String cid;
}
