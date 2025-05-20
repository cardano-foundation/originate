package org.cardanofoundation.metabus.repos;

import org.cardanofoundation.metabus.common.entities.UnconfirmedTxJPA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * <p>
 * UnconfirmedTx Entity Repository Class.
 * </p>
 * 
 * @author (Sotatek) joey.dao
 * @version 0.01
 * @category Repository
 * @since 2023/08
 */
public interface UnconfirmedTxRepository extends JpaRepository<UnconfirmedTxJPA, Long> {

    /**
     * <p>
     * Find Unconfirmed transaction by txHash
     * </p>
     * 
     * @param txHash The txHash
     * @return The optional result
     */
    List<UnconfirmedTxJPA> findAllByTxHash(String txHash);
}
