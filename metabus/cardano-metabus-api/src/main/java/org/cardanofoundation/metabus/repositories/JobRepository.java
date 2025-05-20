package org.cardanofoundation.metabus.repositories;


import org.cardanofoundation.metabus.common.entities.JobJPA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobRepository extends JpaRepository<JobJPA, Long> {
    Optional<JobJPA> findById(Long id);
}
