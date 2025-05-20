package org.cardanofoundation.proofoforigin.api.repository.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.proofoforigin.api.constants.ScanningStatus;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.time.LocalDate;

@Entity
@Table(name = "certificate_lot_entry")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CertificateLotEntryEntity {
    @EmbeddedId
    private CertificateLotEntryPK certificateLotEntryPk;

    @Column(name = "wine_name")
    @NotNull
    String wineName;

    @Column(name = "wine_description")
    String wineDescription;

    @Column(name = "serial_name")
    String serialName;

    @Column(name = "origin")
    String origin;

    @Column(name = "viticulture_area")
    String viticultureArea;

    @Column(name = "type")
    String type;

    @Column(name = "color")
    String color;

    @Column(name = "sugar_content_category")
    String sugarContentCategory;

    @Column(name = "grape_variety")
    String grapeVariety;

    @Column(name = "harvest_year")
    @Positive
    Integer harvestYear;

    @Column(name = "delayed_on_chacha")
    Boolean delayedOnChacha;

    @Column(name = "bottle_type")
    String bottleType;

    @Column(name = "bottling_date")
    LocalDate bottlingDate;

    @Column(name = "bottle_volume")
    @Positive
    Double bottleVolume;

    @Column(name = "bottle_count_in_lot")
    @Positive
    Integer bottleCountInLot;

    @Column(name = "scanning_status")
    @Enumerated(EnumType.STRING)
    ScanningStatus scanningStatus = ScanningStatus.NOT_STARTED;

    @Column(name = "winery_id")
    private String wineryId;

    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "certificate_id", insertable = false, updatable = false)
    CertificateEntity certificate;
}
