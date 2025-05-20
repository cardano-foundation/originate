package org.cardanofoundation.metabus.repos;

import java.util.List;

import org.cardanofoundation.metabus.common.entities.UnconfirmedTxJPA;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnconfirmedTxRepository extends JpaRepository<UnconfirmedTxJPA, Long> {
    List<UnconfirmedTxJPA> findAllByTxHashIn(List<String> txHashes);
}
