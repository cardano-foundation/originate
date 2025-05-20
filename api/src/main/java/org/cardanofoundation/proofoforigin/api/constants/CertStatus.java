package org.cardanofoundation.proofoforigin.api.constants;

import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * An enum to define the state of the certificate that is provided by NWA
 * The STATUS TYPES of the CERT are ACTIVE, REVOKED
 * </p>
 * 
 * @author (Sotatek) joey.dao
 * @category Common-enum
 * @since 2023/08
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum CertStatus {
	/**
	 * Indicate that the cert is currently active.
	 */
	ACTIVE(1),

	/**
	 * Indicate that the cert is currently revoked
	 */
	REVOKED(0);

	/**
	 * The inner value of the enum
	 */
	Integer value;

	@Getter(AccessLevel.NONE)
	static final Map<Integer, CertStatus> MAPPED_VALUE = new HashMap<>();

	/** Cached the enum to a map */
	static {
		for (CertStatus label : values()) {
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
	private CertStatus(final int value) {
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
	public static CertStatus getEnumByValue(int value) {
		return MAPPED_VALUE.get(value);
	}
}
