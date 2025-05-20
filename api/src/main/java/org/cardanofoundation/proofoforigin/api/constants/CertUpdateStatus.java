package org.cardanofoundation.proofoforigin.api.constants;

import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * An enum to define the state of the update process to ScanTrust of a bottle
 * The UPDATE STATUS TYPES of the BOTTLE are UPDATED, NOT_UPDATED
 * </p>
 * 
 * @author (Sotatek) joey.dao
 * @category Common-enum
 * @since 2023/07
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum CertUpdateStatus {
	/**
	 * Indicate that the bottle is updated to ScanTrust.
	 */
	UPDATED(1),

	/**
	 * Indicate that the bottle have not been updated to ScanTrust yet.
	 */
	NOT_UPDATED(0);

	/**
	 * The inner value of the enum
	 */
	Integer value;

	@Getter(AccessLevel.NONE)
	static final Map<Integer, CertUpdateStatus> MAPPED_VALUE = new HashMap<>();

	/** Cached the enum to a map */
	static {
		for (CertUpdateStatus label : values()) {
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
	private CertUpdateStatus(final int value) {
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
	public static CertUpdateStatus getEnumByValue(int value) {
		return MAPPED_VALUE.get(value);
	}
}
