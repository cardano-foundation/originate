package org.cardanofoundation.proofoforigin.api.repository.entities;

import lombok.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.time.LocalDate;

@Entity
@Table(name = "Lot")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class Lot {

    @Id
    @Column(name = "lot_id", length = 11, nullable = false)
    private String lotId;

    @Column(name = "wine_name", nullable = false)
    private String wineName;

    @Column(name = "origin", nullable = false)
    private String origin;

    @Column(name = "country_of_origin", nullable = false)
    private String countryOfOrigin;

    @Column(name = "produced_by", nullable = false)
    private String producedBy;

    @Column(name = "producer_address", nullable = false)
    private String producerAddress;

    @Column(name = "producer_latitude")
    private Double producerLatitude;

    @Column(name = "producer_longitude")
    private Double producerLongitude;

    @Column(name = "varietal_name", nullable = false)
    private String varietalName;

    @Column(name = "vintage_year", nullable = false)
    private Integer vintageYear;

    @Column(name = "wine_type", nullable = false)
    private String wineType;

    @Column(name = "wine_color", nullable = false)
    private String wineColor;

    @Column(name = "harvest_date", nullable = false)
    private LocalDate harvestDate;

    @Column(name = "harvest_location", nullable = false)
    private String harvestLocation;

    @Column(name = "pressing_date", nullable = false)
    private LocalDate pressingDate;

    @Column(name = "processing_location", nullable = false)
    private String processingLocation;

    @Column(name = "fermentation_vessel", nullable = false)
    private String fermentationVessel;

    @Column(name = "fermentation_duration", nullable = false)
    private String fermentationDuration;

    @Column(name = "aging_recipient")
    private String agingRecipient;

    @Column(name = "aging_time")
    private String agingTime;

    @Column(name = "storage_vessel", nullable = false)
    private String storageVessel;

    @Column(name = "bottling_date")
    private LocalDate bottlingDate;

    @Column(name = "bottling_location", nullable = false)
    private String bottlingLocation;

    @Column(name = "number_of_bottles", nullable = false)
    private Integer numberOfBottles;

    @Column(name = "winery_signature")
    private String winerySignature;

    @Column(name = "tx_id")
    private String txId;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "winery_id")
    private String wineryId;

    @Column(name = "job_id")
    private Long jobId;

    /**
     * The job index is represent the order of the object in the transaction 
     * that is submitted to the node.
     */
    @Column(name = "job_index")
    private String jobIndex;
}
