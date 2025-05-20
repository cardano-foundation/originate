package org.cardanofoundation.metabus.repos;

import org.cardanofoundation.metabus.common.entities.UnconfirmedTxJPA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UnconfirmedTxRepository extends JpaRepository<UnconfirmedTxJPA, Long> {
    List<UnconfirmedTxJPA> findAllByTxHashIn(List<String> txHashes);
}
