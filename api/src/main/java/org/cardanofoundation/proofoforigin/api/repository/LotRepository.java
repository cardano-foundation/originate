package org.cardanofoundation.proofoforigin.api.repository;

import org.cardanofoundation.proofoforigin.api.repository.entities.Lot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface LotRepository extends JpaRepository<Lot, String> {
    List<Lot> findByLotIdIn(List<String> lotIds);

    List<Lot> findByWineryId(@Param("wineryId") String wineryId);

    List<Lot> findByWineryIdAndLotIdIn(@Param("wineryId") String wineryId, @Param("lotIds") Set<String> lotIds);

    Optional<Lot> findByTxId(@Param("txId") String txId);

    Optional<Lot> findByJobId(Long jobId);

    List<Lot> findByLotIdInAndStatus(List<String> lotIds, Integer status);
}
