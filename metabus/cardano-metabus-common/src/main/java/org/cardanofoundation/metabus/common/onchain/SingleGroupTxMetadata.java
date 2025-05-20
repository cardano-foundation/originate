package org.cardanofoundation.metabus.common.onchain;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class SingleGroupTxMetadata extends TxMetadata {
    byte[] pubKey;
    byte[] jwsHeader;
    List<byte[]> signatures;
}
