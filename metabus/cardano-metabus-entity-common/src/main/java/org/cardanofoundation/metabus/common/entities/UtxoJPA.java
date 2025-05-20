package org.cardanofoundation.metabus.common.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigInteger;

// TODO: move schema value to env file
@Entity
@Table(name = "utxos")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class UtxoJPA extends BaseEntity {
    @Column(name = "address", nullable = false)
    String address;
    @Column(name = "tx_hash")
    String txHash;
    @Column(name = "output_index")
    Long outputIndex;
    @Column(name = "lovelace")
    BigInteger lovelace;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "unconfirmed_tx_id", nullable = false)
    @EqualsAndHashCode.Exclude
    UnconfirmedTxJPA unconfirmedTx;
}
