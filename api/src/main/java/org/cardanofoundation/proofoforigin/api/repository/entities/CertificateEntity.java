package org.cardanofoundation.proofoforigin.api.repository.entities;


import org.cardanofoundation.proofoforigin.api.constants.CertStatus;
import org.cardanofoundation.proofoforigin.api.utils.CertStatusEnumToIntValueUtil;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "certificate")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CertificateEntity {

    @Id
    @Column(name = "certificate_id", nullable = false, insertable = false, updatable = false)
    String certificateId;

    @Column(name = "certificate_type")
    @NotNull
    @Size(max = 255)
    String certificateType;

    @Column(name = "certificate_number")
    @NotNull
    @Size(max = 255)
    String certificateNumber;

    @Column(name = "export_country")
    @NotNull
    @Size(max = 255)
    String exportCountry;

    @Column(name = "exam_protocol_number")
    @Size(max = 255)
    String examProtocolNumber;

    @Column(name = "tasting_protocol_number")
    @Size(max = 255)
    String tastingProtocolNumber;

    @Column(name = "signature")
    @NotNull
    String signature;

    @Column(name = "pub_key")
    @NotNull
    String pubKey;

    @Column(name = "tx_id")
    @Size(max = 255)
    String txId;

    @Column(name = "job_id")
    Long jobId;

    /**
     * The job index is represent the order of the object in the transaction
     * that is submitted to the node.
     */
    @Column(name = "job_index")
    String jobIndex;

    @Column(name = "winery_id")
    String wineryId;

    /**
     * The job index is represent the order of the object in the transaction
     * that is submitted to the node.
     */
    @Column(name = "cert_status")
    @Builder.Default
    @Convert(converter = CertStatusEnumToIntValueUtil.class)
    CertStatus certStatus = CertStatus.ACTIVE;

    @Column(name = "revoke_tx_id")
    @Size(max = 255)
    String revokeTxId;


    @Column(name = "revoke_job_id")
    Long revokeJobId;

    @Column(name = "revoke_job_index")
    String revokeJobIndex;

    @Column(name = "revoke_signature")
    String revokeSignature;

    @Column(name = "revoke_pub_key")
    String revokePubKey;
}

