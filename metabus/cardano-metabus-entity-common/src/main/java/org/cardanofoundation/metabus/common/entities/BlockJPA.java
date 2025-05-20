package org.cardanofoundation.metabus.common.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.sql.Timestamp;

@Entity
@Table(name = "block")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class BlockJPA extends BaseEntity {
    @Column(
            name = "hash",
            nullable = false,
            length = 64
    )
    private String hash;
    @Column(
            name = "slot_no"
    )
    private Long slotNo;
    @Column(
            name = "block_no"
    )
    private Long blockNo;
    @Column(
            name = "previous_hash"
    )
    private String previous;
    @Column(
            name = "tx_onchain_hashes",
            columnDefinition = "TEXT"
    )
    private String txOnChainHashes;

}
