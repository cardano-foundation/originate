package org.cardanofoundation.proofoforigin.api.controllers.dtos;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.cardanofoundation.proofoforigin.api.constants.ScanningStatus;

public interface CertificateLotEntryDto {
    Integer getQuantity();

    String getLotId();

    @Enumerated(EnumType.STRING)
    ScanningStatus getScanningStatus();

    String getCertificateId();

    String getWineryId();
}
