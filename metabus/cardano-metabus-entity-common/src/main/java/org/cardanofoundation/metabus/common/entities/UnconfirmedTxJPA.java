package org.cardanofoundation.metabus.common.entities;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.metabus.common.JsonConverter;
import org.hibernate.annotations.Proxy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@Table(name = "unconfirmed_txs")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Proxy(lazy=false)
public class UnconfirmedTxJPA extends BaseEntity {
    @Column(name = "tx_hash", nullable = false)
    String txHash;
    // Optimize later
    @Column(name = "metadata")
    @Convert(converter = JsonConverter.class)
    Object metadata;
}
