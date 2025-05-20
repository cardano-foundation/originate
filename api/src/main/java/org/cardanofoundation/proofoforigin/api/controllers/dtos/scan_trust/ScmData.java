package org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nimbusds.jose.shaded.gson.annotations.SerializedName;
import lombok.*;
import org.cardanofoundation.proofoforigin.api.constants.Constants;
import org.cardanofoundation.proofoforigin.api.repository.entities.Lot;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScmData {
    @JsonProperty("lot_number")
    protected String lotId;

    @JsonProperty("wine_name")
    protected String wineName;

    @JsonProperty("origin")
    protected String origin;

    @JsonProperty("country_of_origin")
    protected String countryOfOrigin;

    @JsonProperty("produced_by")
    protected String producedBy;

    @JsonProperty("producer_address")
    protected String producerAddress;

    @JsonProperty("producer_latitude")
    protected Double producerLatitude;

    @JsonProperty("producer_longitude")
    protected Double producerLongitude;

    @JsonProperty("varietal_name")
    protected String varietalName;

    @JsonProperty("vintage_year")
    protected Integer vintageYear;

    @JsonProperty("wine_type")
    protected String wineType;

    @JsonProperty("wine_color")
    protected String wineColor;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.SCANTRUST.DATE_FORMAT)
    @JsonProperty("harvest_date")
    protected LocalDate harvestDate;

    @JsonProperty("harvest_location")
    protected String harvestLocation;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.SCANTRUST.DATE_FORMAT)
    @JsonProperty("pressing_date")
    protected LocalDate pressingDate;

    @JsonProperty("processing_location")
    protected String processingLocation;

    @JsonProperty("fermentation_vessel")
    protected String fermentationVessel;

    @JsonProperty("fermentation_duration")
    protected String fermentationDuration;

    @JsonProperty("aging_recipient")
    protected String agingRecipient;

    @JsonProperty("aging_time")
    protected String agingTime;

    @JsonProperty("storage_vessel")
    protected String storageVessel;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.SCANTRUST.DATE_FORMAT)
    @JsonProperty("bottling_date")
    protected LocalDate bottlingDate;

    @JsonProperty("bottling_location")
    protected String bottlingLocation;

    @JsonProperty("number_of_bottles")
    protected Integer numberOfBottles;

    @JsonProperty("product")
    protected String product;

    @JsonProperty("supply_chain_data_txid")
    @SerializedName("supply_chain_data_txid")
    private String supplyChainDataTxid;

    @JsonProperty("supply_chain_data_batch_info")
    @SerializedName("supply_chain_data_batch_info")
    private String supplyChainDataBatchInfo;

    public static ScmData initWithTxIdAndBatchInfo(String supplyChainDataTxid, String supplyChainDataBatchInfo) {
        ScmData scmData = new ScmData();
        scmData.setSupplyChainDataTxid(supplyChainDataTxid);
        scmData.setSupplyChainDataBatchInfo(supplyChainDataBatchInfo);
        return scmData;
    }

    public static ScmData fromEntity(Lot entity) {
        return ScmData.builder()
                .lotId(entity.getLotId())
                .wineName(entity.getWineName())
                .origin(entity.getOrigin())
                .countryOfOrigin(entity.getCountryOfOrigin())
                .producedBy(entity.getProducedBy())
                .producerAddress(entity.getProducerAddress())
                .producerLatitude(entity.getProducerLatitude())
                .producerLongitude(entity.getProducerLongitude())
                .varietalName(entity.getVarietalName())
                .vintageYear(entity.getVintageYear())
                .wineType(entity.getWineType())
                .wineColor(entity.getWineColor())
                .harvestDate(entity.getHarvestDate())
                .harvestLocation(entity.getHarvestLocation())
                .pressingDate(entity.getPressingDate())
                .processingLocation(entity.getProcessingLocation())
                .fermentationVessel(entity.getFermentationVessel())
                .fermentationDuration(entity.getFermentationDuration())
                .agingRecipient(entity.getAgingRecipient())
                .agingTime(entity.getAgingTime())
                .storageVessel(entity.getStorageVessel())
                .bottlingDate(entity.getBottlingDate())
                .bottlingLocation(entity.getBottlingLocation())
                .numberOfBottles(entity.getNumberOfBottles())
                .product(entity.getWineName() != null && entity.getProducedBy() != null ?
                        entity.getWineName() + " - " + entity.getProducedBy() : ScmUploadItem.DUMMY_PRODUCT_SKU)
                .build();
    }
}
