package org.cardanofoundation.metabus.integrationtest;

import org.cardanofoundation.metabus.common.entities.UtxoJPA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UtxoRepository extends JpaRepository<UtxoJPA, Long> {
    List<UtxoJPA> findAllByTxHashIn(List<String> txHashes);
}
