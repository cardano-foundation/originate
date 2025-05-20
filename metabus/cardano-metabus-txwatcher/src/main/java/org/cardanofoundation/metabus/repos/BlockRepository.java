package org.cardanofoundation.metabus.repos;

import org.cardanofoundation.metabus.common.entities.BlockJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockRepository extends JpaRepository<BlockJPA, Long> {
    Optional<BlockJPA> findByBlockNo(Long blockNo);

    boolean existsBlockByHash(String hash);

    int deleteBySlotNoGreaterThan(long slotNo);
}
