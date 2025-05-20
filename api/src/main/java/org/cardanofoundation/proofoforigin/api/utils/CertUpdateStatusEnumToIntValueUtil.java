package org.cardanofoundation.proofoforigin.api.utils;

import org.cardanofoundation.proofoforigin.api.constants.CertUpdateStatus;

import jakarta.persistence.AttributeConverter;

/**
 * <p>
 * An Converter CertUpdateStatus Enum class to Int value.
 * </p>
 * 
 * @author (Sotatek) joey.dao
 * @category Utility
 * @since 2023/07
 */
public class CertUpdateStatusEnumToIntValueUtil implements AttributeConverter<CertUpdateStatus, Integer> {

	@Override
	public Integer convertToDatabaseColumn(CertUpdateStatus attribute) {
		return attribute == null ? CertUpdateStatus.NOT_UPDATED.getValue() : attribute.getValue();
	}

	@Override
	public CertUpdateStatus convertToEntityAttribute(Integer dbData) {
		return dbData == null ? CertUpdateStatus.NOT_UPDATED : CertUpdateStatus.getEnumByValue(dbData);
	}

}
