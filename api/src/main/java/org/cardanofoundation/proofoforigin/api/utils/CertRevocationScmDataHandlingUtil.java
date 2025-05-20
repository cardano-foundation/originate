package org.cardanofoundation.proofoforigin.api.utils;

import java.util.LinkedList;
import java.util.List;

import org.cardanofoundation.proofoforigin.api.constants.UploadType;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust.ScmBottleData;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust.ScmBottleData.ApprovedBottleData;
import org.cardanofoundation.proofoforigin.api.repository.entities.Bottle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.xml.bind.TypeConstraintException;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * Approved Bottle SCM Data Handling Util for Cert Revocation.
 * </p>
 * 
 * @author (Sotatek) joey.dao
 * @category Utility
 * @since 2023/08
 */
@Slf4j
@Component(value = UploadType.CERT_REVOCATION_UTIL)
public class CertRevocationScmDataHandlingUtil extends ScanTrustDataHandlingUtil {

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public List<String> buildPayloadDataFromArgs(final Class<?>[] customInputTypes, final Object... args)
			throws JsonProcessingException {

		/** This is a input argument of the updateCertificateAssociations function */
		final List<Bottle> listOfTheBottle = castingToBottleList(args[4], customInputTypes[0]);
		final List<String> payloadData = new LinkedList<>();
		final ScmBottleData bottleData = ScmBottleData.builder().items(new LinkedList<>()).build();

		/** Adding the bottle that need to update to payload data. */
		/** Max 100 bottles information per request. */
		for (int index = 0; index < listOfTheBottle.size(); index++) {
			bottleData.getItems().add(new ApprovedBottleData("n/a", "", listOfTheBottle.get(index).getId()));

			if ((index == (listOfTheBottle.size() - 1)) || (((index + 1) % 100) == 0)) {
				final String payloadInJson = objectMapper.writeValueAsString(bottleData);
				payloadData.add(objectMapper.writeValueAsString(bottleData));
				log.info(">>> payload data sizes: (" + index + 1 + "), content: {}", payloadInJson);
				bottleData.getItems().clear();
			}
		}

		return payloadData;
	}

	/**
	 * <p>
	 * Casting payload Object to BottleIdBody.
	 * </p>
	 * 
	 * @param <T>       The generic type
	 * @param arg       The target argument
	 * @param classType The classType of the target
	 * @return The casted Object
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<Bottle> castingToBottleList(final Object arg, final Class<T> classType) {

		if (classType == null) {
			log.error(">>> bottle list error {}", arg);
			throw new TypeConstraintException("Lack of define class type of the arg");
		}

		if (!classType.isInstance(arg)) {
			log.error(">>> class type is wrong: {}", classType);
			throw new TypeConstraintException("The arg object is a type of " + arg.getClass() + ", not of "
					+ classType.getName());
		}

		return (List<Bottle>) classType.cast(arg);
	}

	@Override
	public void executeProcedureAfterSubmitByPayload(final String payload) throws JsonProcessingException {
		log.info(">>> executeProcedureAfterSubmitByPayload: bottle records are updated {}", payload);
	}

	@Override
	public void executeProcedureAfterSubmitFailedByPayload(final String payload) throws JsonProcessingException {
		log.info(">>> executeProcedureAfterSubmitFailedByPayload: bottle records are not updated {}", payload);
	}
}
