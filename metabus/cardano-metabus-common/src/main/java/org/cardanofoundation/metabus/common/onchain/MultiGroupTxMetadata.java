package org.cardanofoundation.metabus.common.onchain;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class MultiGroupTxMetadata extends TxMetadata{
    /** Key can be any value that client of metabus pass into the "group" field of the job, value is
     * a list of the business data signatures and the public key associate with that group.
     * */
    Map<String, VerificationInfo> verification;

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Builder
    public static class VerificationInfo {
        byte[] pubKey;
        byte[] jwsHeader;
        List<byte[]> signatures;
    }
}
