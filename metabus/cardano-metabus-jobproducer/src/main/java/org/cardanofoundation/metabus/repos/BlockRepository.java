package org.cardanofoundation.metabus.repos;

import org.cardanofoundation.metabus.common.entities.BlockJPA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlockRepository extends JpaRepository<BlockJPA, Long> {
    Optional<BlockJPA> findTopByOrderByIdDesc();
}
