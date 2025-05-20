package org.cardanofoundation.proofoforigin.api.repository.entities;

import jakarta.persistence.*;
import org.cardanofoundation.proofoforigin.api.constants.CertUpdateStatus;
import org.cardanofoundation.proofoforigin.api.utils.CertUpdateStatusEnumToIntValueUtil;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "bottle")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Bottle {

    @Id
    @Column(name = "id", nullable = false, insertable = false, updatable = false)
    String id;

    @Column(name = "lot_id")
    String lotId;

    @Column(name = "sequential_number")
    Integer sequentialNumber;

    @Column(name = "reel_number")
    Integer reelNumber;

    @Column(name = "sequential_number_in_lot")
    Integer sequentialNumberInLot;

    @Column(name = "cert_id")
    String certificateId;

    @Column(name = "winery_id")
    private String wineryId;

    @Column(name = "lot_update_status")
    Integer lotUpdateStatus;

    /**
     * The status of the bottle that indicated
     * whether it is synced to ScanTrust successfully or not.
     */
    @Column(name = "cert_update_status")
    @Builder.Default
    @Convert(converter = CertUpdateStatusEnumToIntValueUtil.class)
    CertUpdateStatus certUpdateStatus = CertUpdateStatus.NOT_UPDATED;

    /**
     * <p>
     * An Alternative Constructor.
     * </p>
     *
     * @param id               The bottle id
     * @param lotId            The lot id
     * @param sequentialNumber The sequential number
     * @param reelNumber       The reel number
     * @param certificateId    the certificate id
     * @param wineryId         the winery id
     */
    public Bottle(String id, String lotId, Integer sequentialNumber, Integer reelNumber, String certificateId,
            String wineryId) {
        this.id = id;
        this.lotId = lotId;
        this.sequentialNumber = sequentialNumber;
        this.reelNumber = reelNumber;
        this.certificateId = certificateId;
        this.wineryId = wineryId;
    }

    public Bottle(String id, String lotId, int sequentialNumber, int reelNumber, String certificateId, String wineryId, int lotUpdateStatus, CertUpdateStatus certUpdateStatus) {
        this.id = id;
        this.lotId = lotId;
        this.sequentialNumber = sequentialNumber;
        this.reelNumber = reelNumber;
        this.certificateId = certificateId;
        this.wineryId = wineryId;
        this.lotUpdateStatus = lotUpdateStatus;
        this.certUpdateStatus = certUpdateStatus;
    }
}
