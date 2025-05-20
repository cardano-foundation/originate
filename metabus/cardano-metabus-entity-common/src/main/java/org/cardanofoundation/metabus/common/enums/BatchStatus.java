package org.cardanofoundation.metabus.common.enums;

import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * An enum to define the state of the batch
 * The STATUS TYPES of the BATCH are NONE, PENDING, PROCESSING
 * </p>
 * 
 * @author (Sotatek) joey.dao
 * @version 0.01
 * @category Common-enum
 * @since 2023/06
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum BatchStatus {

    /**
     * <p>
     * The NONE status is determined that the Batch that is based on type does not
     * contain any job element
     * </p>
     */
    NONE(0),

    /**
     * <p>
     * The PENDING status is determined that the Batch that is based on type
     * contains jobs element
     * but waiting for another job to come to submit to the node
     * </p>
     */
    PENDING(1),

    /**
     * <p>
     * The PROCESSING status is determined that the Batch is being processed to
     * submit to the node
     * </p>
     */
    PROCESSING(2);

    /**
     * The original value of the enum.
     */
    Integer value;

    @Getter(AccessLevel.NONE)
    static Map<Integer, BatchStatus> MAPPED_VALUE = new HashMap<>();

    /** Cached the enum to a map */
    static {
        for (BatchStatus label : values()) {
            MAPPED_VALUE.put(label.value, label);
        }
    }

    /**
     * <p>
     * Private constructor of the enum.
     * </p>
     * 
     * @param value the implicit value
     */
    private BatchStatus(final int value) {
        this.value = value;
    }

    /**
     * <p>
     * Get the enum by VALUE
     * </p>
     * 
     * @param value The indicated value
     * @return The enum value
     */
    public static BatchStatus getEnumByValue(int value) {
        return MAPPED_VALUE.get(value);
    }
}
