package org.cardanofoundation.metabus.repos;


import java.util.List;

import org.cardanofoundation.metabus.common.entities.JobJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<JobJPA, Long> {
    List<JobJPA> findAllByUnconfirmedTxId(Long unconfirmedTxId);
}
