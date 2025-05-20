package org.cardanofoundation.proofoforigin.api.constants;

import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * An enum to define the type of the upload task to ScanTrust
 * The TYPES of the UPLOAD are BOTTLE_SYNC, CERT_REVOCATION
 * </p>
 * 
 * <h4>
 * *NOTE: New type of the upload will define here
 * </h4>
 * 
 * @author (Sotatek) joey.dao
 * @category Common-enum
 * @since 2023/07
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum UploadType {

    BOTTLE_SYNC("BOTTLE_SYNC"),

    CERT_REVOCATION("CERT_REVOCATION");

    /**
     * The inner value of the enum
     */
    final String value;

    /** The constant name of the util */
    public static final String POSTFIX = "UTIL";

    /** The bottle sync util */
    public static final String BOTTLE_SYNC_UTIL = "BOTTLE_SYNC_" + POSTFIX;

    /** The bottle sync util */
    public static final String CERT_REVOCATION_UTIL = "CERT_REVOCATION_" + POSTFIX;

    /** The cached map */
    @Getter(AccessLevel.NONE)
    static final Map<String, UploadType> MAPPED_VALUE = new HashMap<>();

    /** Cached the enum to a map */
    static {
        for (final UploadType label : values()) {
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
    private UploadType(final String value) {
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
    public static UploadType getEnumByValue(final String value) {
        return MAPPED_VALUE.get(value);
    }

    /**
     * <p>
     * Get the util class qualified name based on enum
     * </p>
     * 
     * @param uploadType The upload type
     * @return qualified class name
     */
    public static String getDataBuildUtilClass(final UploadType uploadType) {
        return uploadType.value + "_" + POSTFIX;
    }
}
