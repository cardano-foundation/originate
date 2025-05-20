package org.cardanofoundation.proofoforigin.api.repository;

import org.cardanofoundation.proofoforigin.api.repository.entities.CertificateLotEntryEntity;
import org.cardanofoundation.proofoforigin.api.repository.entities.CertificateLotEntryPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.cardanofoundation.proofoforigin.api.constants.CertStatus;
import org.cardanofoundation.proofoforigin.api.constants.ScanningStatus;

import java.util.List;
import java.util.Optional;

@EnableJpaRepositories
public interface CertificateLotEntryRepository extends JpaRepository<CertificateLotEntryEntity, CertificateLotEntryPK> {
    List<CertificateLotEntryEntity> findAllByCertificateCertStatus(final CertStatus certStatus);

    List<CertificateLotEntryEntity> findByWineryIdAndCertificateTxIdIsNotNullAndCertificateCertStatus(
            String wineryId, final CertStatus certStatus);

    Long countByCertificateLotEntryPkCertificateIdAndCertificateLotEntryPkLotIdAndWineryIdAndCertificateCertStatus(
            String certificateId, String lotId, String wineryId, final CertStatus certStatus);

    Optional<CertificateLotEntryEntity> findByCertificateLotEntryPkCertificateIdAndCertificateLotEntryPkLotIdAndScanningStatusNotAndCertificateCertStatus(
            String certificateId, String lotId, ScanningStatus scanningStatus, final CertStatus certStatus);

    CertificateLotEntryEntity findByCertificateLotEntryPkCertificateIdAndCertificateLotEntryPkLotIdAndCertificateCertStatus(
            String certificateId, String lotId, final CertStatus certStatus);
}
