package org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nimbusds.jose.shaded.gson.annotations.SerializedName;
import lombok.*;
import org.cardanofoundation.proofoforigin.api.constants.Constants;
import org.cardanofoundation.proofoforigin.api.repository.entities.Lot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class ScmUploadItem {
    @JsonProperty("extended_id")
    private String extendedId;

    @JsonProperty("sequential_number_in_lot")
    private String sequentialNumberInLot;

    @JsonProperty("lot_number")
    private String lotId;

    @JsonProperty("wine_name")
    private String wineName;

    @JsonProperty("origin")
    private String origin;

    @JsonProperty("country_of_origin")
    private String countryOfOrigin;

    @JsonProperty("produced_by")
    private String producedBy;

    @JsonProperty("producer_address")
    private String producerAddress;

    @JsonProperty("producer_latitude")
    private String producerLatitude;

    @JsonProperty("producer_longitude")
    private String producerLongitude;

    @JsonProperty("varietal_name")
    private String varietalName;

    @JsonProperty("vintage_year")
    private String vintageYear;

    @JsonProperty("wine_type")
    private String wineType;

    @JsonProperty("wine_color")
    private String wineColor;

    @JsonProperty("harvest_date")
    private String harvestDate;

    @JsonProperty("harvest_location")
    private String harvestLocation;

    @JsonProperty("pressing_date")
    private String pressingDate;

    @JsonProperty("processing_location")
    private String processingLocation;

    @JsonProperty("fermentation_vessel")
    private String fermentationVessel;

    @JsonProperty("fermentation_duration")
    private String fermentationDuration;

    @JsonProperty("aging_recipient")
    private String agingRecipient;

    @JsonProperty("aging_time")
    private String agingTime;

    @JsonProperty("storage_vessel")
    private String storageVessel;

    @JsonProperty("bottling_date")
    private String bottlingDate;

    @JsonProperty("bottling_location")
    private String bottlingLocation;

    @JsonProperty("number_of_bottles")
    private String numberOfBottles;

    @JsonProperty("supply_chain_data_txid")
    @SerializedName("supply_chain_data_txid")
    private String supplyChainDataTxid;

    @JsonProperty("supply_chain_data_batch_info")
    @SerializedName("supply_chain_data_batch_info")
    private String supplyChainDataBatchInfo;

    @JsonProperty("product")
    private String product;

    public static final String DUMMY_PRODUCT_SKU = "Dummy Product";

    public static ScmUploadItem init(String id, Integer sequentialNumberInLot, Lot lot) {
        return ScmUploadItem.builder()
                .extendedId(convertToString(id))
                .sequentialNumberInLot(convertToString(sequentialNumberInLot))
                .supplyChainDataTxid(convertToString(lot.getTxId()))
                .supplyChainDataBatchInfo(convertToString(lot.getJobIndex()))
                .lotId(convertToString(lot.getLotId()))
                .wineName(convertToString(lot.getWineName()))
                .origin(convertToString(lot.getOrigin()))
                .countryOfOrigin(convertToString(lot.getCountryOfOrigin()))
                .producedBy(convertToString(lot.getProducedBy()))
                .producerAddress(convertToString(lot.getProducerAddress()))
                .producerLatitude(convertToString(lot.getProducerLatitude()))
                .producerLongitude(convertToString(lot.getProducerLongitude()))
                .varietalName(convertToString(lot.getVarietalName()))
                .vintageYear(convertToString(lot.getVintageYear()))
                .wineType(convertToString(lot.getWineType()))
                .wineColor(convertToString(lot.getWineColor()))
                .harvestDate(convertToString(lot.getHarvestDate()))
                .harvestLocation(convertToString(lot.getHarvestLocation()))
                .pressingDate(convertToString(lot.getPressingDate()))
                .processingLocation(convertToString(lot.getProcessingLocation()))
                .fermentationVessel(convertToString(lot.getFermentationVessel()))
                .fermentationDuration(convertToString(lot.getFermentationDuration()))
                .agingRecipient(convertToString(lot.getAgingRecipient()))
                .agingTime(convertToString(lot.getAgingTime()))
                .storageVessel(convertToString(lot.getStorageVessel()))
                .bottlingDate(convertToString(lot.getBottlingDate()))
                .bottlingLocation(convertToString(lot.getBottlingLocation()))
                .numberOfBottles(convertToString(lot.getNumberOfBottles()))
                .product(lot.getWineName() != null && lot.getProducedBy() != null ?
                        convertToString(lot.getWineName()) + " - " + convertToString(lot.getProducedBy()) : DUMMY_PRODUCT_SKU)
                .build();
    }

    private static String convertToString(Object value) {
        if (Objects.isNull(value)) return "";
        if (value instanceof LocalDate) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.SCANTRUST.DATE_FORMAT);
            return ((LocalDate) value).format(formatter);
        }
        return value.toString();
    }
}
