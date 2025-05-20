package org.cardanofoundation.proofoforigin.api.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.cardanofoundation.proofoforigin.api.constants.CertUpdateStatus;
import org.cardanofoundation.proofoforigin.api.constants.UploadType;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.BottleIdBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust.ScmBottleData;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust.ScmBottleData.ApprovedBottleData;
import org.cardanofoundation.proofoforigin.api.repository.BottleRepository;
import org.cardanofoundation.proofoforigin.api.repository.CertificateRepository;
import org.cardanofoundation.proofoforigin.api.repository.LotRepository;
import org.cardanofoundation.proofoforigin.api.repository.entities.Bottle;
import org.cardanofoundation.proofoforigin.api.repository.entities.CertificateEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.xml.bind.TypeConstraintException;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * Approved Bottle SCM Data Handling Util
 * </p>
 * 
 * @author (Sotatek) joey.dao
 * @category Utility
 * @since 2023/07
 */
@Slf4j
@Component(value = UploadType.BOTTLE_SYNC_UTIL)
public class ApprovedBottleScmDataHandlingUtil extends ScanTrustDataHandlingUtil {

    @Autowired
    private BottleRepository bottleRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<String> buildPayloadDataFromArgs(final Class<?>[] customInputTypes, final Object... args)
            throws JsonProcessingException {

        /** This is a input argument of the updateCertificateAssociations function */
        final String wineryId = (String) args[0];
        final String certId = (String) args[1];
        final String lotId = (String) args[2];
        final BottleIdBody payloadBody = castingPayloadObject(args[3], customInputTypes[0]);

        if (!payloadBody.getFinalise().booleanValue()) {
            return List.of();
        }

        final List<Bottle> listOfApprovedBottle = bottleRepository
                .findByWineryIdAndLotIdAndCertificateIdAndCertUpdateStatusNot(wineryId, lotId, certId, CertUpdateStatus.UPDATED);

        final Optional<CertificateEntity> certificateEntityOptional = certificateRepository.findById(certId);

        if (certificateEntityOptional.isEmpty() || listOfApprovedBottle.isEmpty()) {
            return List.of();
        }

        final CertificateEntity certificateEntity = certificateEntityOptional.get();
        final List<String> payloadData = new LinkedList<>();
        final ScmBottleData bottleData = ScmBottleData.builder().items(new LinkedList<>()).build();

        for (int index = 0; index < listOfApprovedBottle.size(); index++) {
            final String txId = certificateEntity.getTxId();
            final String jobIndex = certificateEntity.getJobIndex();

            bottleData.getItems()
                    .add(new ApprovedBottleData(txId, jobIndex, listOfApprovedBottle.get(index).getId()));

            if ((index == (listOfApprovedBottle.size() - 1)) || (((index + 1) % 100) == 0)) {
                payloadData.add(objectMapper.writeValueAsString(bottleData));
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
    public static <T> BottleIdBody castingPayloadObject(final Object arg, final Class<T> classType) {

        if (classType == null) {
            throw new TypeConstraintException("Lack of define class type of the arg");
        }

        if (!classType.isInstance(arg)) {
            throw new TypeConstraintException("The arg object is a type of " + arg.getClass() + ", not of "
                    + classType.getName());
        }

        return (BottleIdBody) classType.cast(arg);
    }

    @Override
    public void executeProcedureAfterSubmitByPayload(final String payload)
            throws JsonMappingException, JsonProcessingException {
        final ScmBottleData payloadData = objectMapper.readValue(payload, ScmBottleData.class);
        final List<String> listOfBottleId = payloadData.getItems().stream().map(item -> item.getExtendedId())
                .collect(Collectors.toList());
        final int numberOfRecords = bottleRepository.updateCertUpdateStatusOfBottles(CertUpdateStatus.UPDATED,
                listOfBottleId);

        log.info(">>> executeProcedureAfterSubmitByPayload: number of bottle records are updated {}",
                numberOfRecords);
    }

    @Override
    public void executeProcedureAfterSubmitFailedByPayload(final String payload)
            throws JsonMappingException, JsonProcessingException {
        final ScmBottleData payloadData = objectMapper.readValue(payload, ScmBottleData.class);
        final List<String> listOfBottleId = payloadData.getItems().stream().map(item -> item.getExtendedId())
                .collect(Collectors.toList());
        final int numberOfRecords = bottleRepository.updateCertUpdateStatusOfBottles(CertUpdateStatus.NOT_UPDATED,
                listOfBottleId);

        log.info(">>> executeProcedureAfterSubmitFailedByPayload: number of bottle records are updated {}",
                numberOfRecords);
    }
}
