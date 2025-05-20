package org.cardanofoundation.proofoforigin.api.repository;

import org.cardanofoundation.proofoforigin.api.repository.entities.ScantrustTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScantrustTaskRepository extends JpaRepository<ScantrustTask, String> {
}
