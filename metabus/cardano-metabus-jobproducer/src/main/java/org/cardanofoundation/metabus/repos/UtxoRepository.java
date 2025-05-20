package org.cardanofoundation.metabus.repos;

import org.cardanofoundation.metabus.common.entities.UtxoJPA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * <p>
 * UTxO Repository
 * </p>
 *
 * @author (Sotatek) joey.dao
 * @since 2023/08
 */
public interface UtxoRepository extends JpaRepository<UtxoJPA, Long> {

    List<UtxoJPA> deleteByUnconfirmedTxIdIn(List<Long> unconfirmedTxIds);
}
