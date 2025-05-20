package org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * A object is represent Scm Bottle Data.
 * </p>
 * 
 * @author (Sotatek) joey.dao
 * @category POJO
 * @since 2023/07
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScmBottleData {
    @JsonProperty("data_key")
    @Builder.Default
    String dataKey = "extended_id";

    @JsonProperty("items")
    List<ApprovedBottleData> items;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ApprovedBottleData {
        @JsonProperty("conformity_cert_txid")
        String conformityCertTxId;

        @JsonProperty("conformity_cert_batch_info")
        String conformityCertBatchInfo;

        @JsonProperty("extended_id")
        String extendedId;
    }
}
