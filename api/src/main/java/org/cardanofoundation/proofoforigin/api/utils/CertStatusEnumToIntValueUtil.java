package org.cardanofoundation.proofoforigin.api.utils;

import org.cardanofoundation.proofoforigin.api.constants.CertStatus;

import jakarta.persistence.AttributeConverter;

/**
 * <p>
 * An Converter CertStatus Enum class to Int value.
 * </p>
 * 
 * @author (Sotatek) joey.dao
 * @category Utility
 * @since 2023/07
 */
public class CertStatusEnumToIntValueUtil implements AttributeConverter<CertStatus, Integer> {

	@Override
	public Integer convertToDatabaseColumn(CertStatus attribute) {
		return attribute == null ? CertStatus.ACTIVE.getValue() : attribute.getValue();
	}

	@Override
	public CertStatus convertToEntityAttribute(Integer dbData) {
		return dbData == null ? CertStatus.ACTIVE : CertStatus.getEnumByValue(dbData);
	}

}
