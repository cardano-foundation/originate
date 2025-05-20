package org.cardanofoundation.proofoforigin.api.repository;

import org.cardanofoundation.proofoforigin.api.constants.CertStatus;
import org.cardanofoundation.proofoforigin.api.repository.entities.CertificateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<CertificateEntity, String> {
    List<CertificateEntity> findByCertificateIdAndTxIdIsNotNullAndCertStatus(String certId,
            final CertStatus certStatus);
    Optional<CertificateEntity> findByJobId(Long jobId);
    Optional<CertificateEntity> findByRevokeJobId(Long jobId);
}
