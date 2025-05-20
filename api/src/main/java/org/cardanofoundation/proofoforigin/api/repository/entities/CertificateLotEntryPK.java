package org.cardanofoundation.proofoforigin.api.repository.entities;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CertificateLotEntryPK implements Serializable {
    @Column(name = "certificate_id")
    String certificateId;

    @Column(name = "lot_id")
    String lotId;
}
