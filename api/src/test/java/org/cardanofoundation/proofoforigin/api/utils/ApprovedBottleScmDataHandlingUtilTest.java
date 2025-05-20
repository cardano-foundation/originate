package org.cardanofoundation.proofoforigin.api.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.cardanofoundation.proofoforigin.api.constants.CertUpdateStatus;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.BottleIdBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust.ScmBottleData;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust.ScmBottleData.ApprovedBottleData;
import org.cardanofoundation.proofoforigin.api.repository.BottleRepository;
import org.cardanofoundation.proofoforigin.api.repository.CertificateRepository;
import org.cardanofoundation.proofoforigin.api.repository.entities.Bottle;
import org.cardanofoundation.proofoforigin.api.repository.entities.CertificateEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.xml.bind.TypeConstraintException;

/**
 * <p>
 * Approved Bottle SCM Data Handling Util Unit Test
 * </p>
 * 
 * @author (Sotatek) joey.dao
 * @category Unit-test
 * @since 2023/07
 */
@ExtendWith(MockitoExtension.class)
public class ApprovedBottleScmDataHandlingUtilTest {

    @Mock
    private BottleRepository bottleRepository;

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    @Spy
    private ApprovedBottleScmDataHandlingUtil util;

    /**
     * <p>
     * Test the castingPayloadObject method with a null classType:
     * Verify that the method
     * throws a TypeConstraintException with the message “Define the payload body
     * class type”.
     * </p>
     */
    @Test
    public void testCastingPayloadObjectWithNullClassType() {
        // / Arrange
        final Object arg = new BottleIdBody();
        final Class<?> classType = null;

        // Act
        final TypeConstraintException exception = assertThrows(TypeConstraintException.class, () -> {
            ApprovedBottleScmDataHandlingUtil.castingPayloadObject(arg, classType);
        });

        // Assert
        assertEquals("Lack of define class type of the arg", exception.getMessage());
    }

    /**
     * <p>
     * Test the castingPayloadObject method with an incompatible classType:
     * Verify that the method throws a TypeConstraintException
     * with the message “The payload body is a type of {actual class name},
     * instead of {expected class name}”.
     * </p>
     */
    @Test
    public void testCastingPayloadObjectWithIncompatibleClassType() {
        // Arrange
        final Object arg = new BottleIdBody();
        final Class<?> classType = String.class;

        // Act
        final TypeConstraintException exception = assertThrows(TypeConstraintException.class, () -> {
            ApprovedBottleScmDataHandlingUtil.castingPayloadObject(arg, classType);
        });

        // Assert
        assertEquals("The arg object is a type of " + arg.getClass() + ", not of " + classType.getName(),
                exception.getMessage());
    }

    /**
     * <p>
     * Test the castingPayloadObject method with a compatible classType:
     * Verify that the method returns a BottleIdBody object that is casted from the
     * input argument.
     * </p>
     */
    @Test
    public void testCastingPayloadObjectWithCompatibleClassType() {
        // Arrange
        final BottleIdBody arg = new BottleIdBody();
        final Class<?> classType = BottleIdBody.class;

        // Act
        final BottleIdBody result = ApprovedBottleScmDataHandlingUtil.castingPayloadObject(arg, classType);

        // Assert
        assertEquals(arg, result);
    }

    /**
     * <p>
     * Test with a valid payload:
     * Verify that the method parses the payload to a ScmBottleData object,
     * extracts the list of bottle IDs,
     * calls the updateCertUpdateStatusOfBottles method
     * of the bottleRepository with the correct arguments,
     * and logs the number of updated records.
     * </p>
     * 
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    @Test
    public void testExecuteProcedureAfterSubmitByPayloadWithValidPayload()
            throws JsonMappingException, JsonProcessingException {
        // Arrange
        final String payload = "{\"items\":[{\"extended_id\":\"bottle1\"},{\"extended_id\":\"bottle2\"}]}";
        final List<String> listOfBottleId = List.of("bottle1", "bottle2");
        final ScmBottleData data = new ScmBottleData();
        data.setItems(List.of(new ApprovedBottleData(null, null, listOfBottleId.get(0)),
                new ApprovedBottleData(null, null, listOfBottleId.get(1))));

        doReturn(data).when(objectMapper).readValue(payload, ScmBottleData.class);
        when(bottleRepository.updateCertUpdateStatusOfBottles(CertUpdateStatus.UPDATED, listOfBottleId))
                .thenReturn(2);

        // Act
        util.executeProcedureAfterSubmitByPayload(payload);

        // Assert
        verify(bottleRepository).updateCertUpdateStatusOfBottles(CertUpdateStatus.UPDATED, listOfBottleId);
    }

    /**
     * <p>
     * Test with an invalid payload:
     * Verify that the method handles a IllegalArgumentException correctly
     * and does not throw any exceptions.
     * </p>
     */
    @Test
    public void testExecuteProcedureAfterSubmitByPayloadWithInvalidPayload() throws JsonProcessingException {
        // Arrange
        final String payload = "invalid";
        when(objectMapper.readValue(payload, ScmBottleData.class))
                .thenThrow(IllegalArgumentException.class);

        // Act
        assertThrows(IllegalArgumentException.class, () -> util.executeProcedureAfterSubmitByPayload(payload));

        // Assert
        verify(bottleRepository, never()).updateCertUpdateStatusOfBottles(any(), any());
    }

    /**
     * <p>
     * Test with a payload that results in no updated records:
     * Verify that the method logs a message indicating that no bottle records were
     * updated.
     * </p>
     * 
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    @Test
    public void testExecuteProcedureAfterSubmitByPayloadWithNoUpdatedRecords()
            throws JsonMappingException, JsonProcessingException {
        // Arrange
        final String payload = "{\"items\":[{\"extendId\":\"bottle1\"},{\"extendId\":\"bottle2\"}]}";
        final List<String> listOfBottleId = List.of("bottle1", "bottle2");
        final ScmBottleData data = new ScmBottleData();
        data.setItems(List.of(new ApprovedBottleData(null, null, listOfBottleId.get(0)),
                new ApprovedBottleData(null, null, listOfBottleId.get(1))));

        doReturn(data).when(objectMapper).readValue(payload, ScmBottleData.class);
        when(bottleRepository.updateCertUpdateStatusOfBottles(CertUpdateStatus.UPDATED, listOfBottleId))
                .thenReturn(0);

        // Act
        util.executeProcedureAfterSubmitByPayload(payload);

        // Assert
        verify(bottleRepository).updateCertUpdateStatusOfBottles(CertUpdateStatus.UPDATED, listOfBottleId);
    }

    /**
     * <p>
     * Test with a payload body that has finalize set to false:
     * Verify that the method returns an empty list
     * and does not query the repositories or build any payload data.
     * </p>
     * 
     * @throws JsonProcessingException
     */
    @Test
    public void testBuildPayloadDataFromArgsWithFinalizeSetToFalse() throws JsonProcessingException {
        // Arrange
        final String wineryId = "winery1";
        final String certId = "cert1";
        final String lotId = "lot1";
        final BottleIdBody payloadBody = new BottleIdBody();
        payloadBody.setFinalise(false);
        final Class<?>[] customInputTypes = { BottleIdBody.class };

        // Act
        final List<String> result = util.buildPayloadDataFromArgs(customInputTypes, wineryId,
                certId, lotId, payloadBody);

        // Assert
        assertEquals(List.of(), result);
        verify(bottleRepository, never())
                .findByWineryIdAndLotIdAndCertificateIdAndCertUpdateStatusNot(any(),
                        any(),
                        any(), eq(CertUpdateStatus.UPDATED));
        verify(certificateRepository, never()).findById(any());
    }

    /**
     * <p>
     * Test with a payload body that has finalise set to true and valid arguments:
     * Verify that the method casts the payload body to a BottleIdBody object,
     * queries the repositories for the approved bottles and the lot information,
     * builds a list of ScmBottleData objects with the correct fields,
     * and returns it as a list of JSON strings.
     * </p>
     * 
     * @throws JsonProcessingException
     */
    @Test
    public void testBuildPayloadDataFromArgsWithFinalizeSetToTrueAndValidArguments()
            throws JsonProcessingException {
        // Arrange
        final String wineryId = "winery1";
        final String certId = "cert1";
        final String lotId = "lot1";
        final BottleIdBody payloadBody = new BottleIdBody();
        payloadBody.setFinalise(true);
        final Class<?>[] customInputTypes = { BottleIdBody.class };
        final List<Bottle> listOfApprovedBottle = List.of(new Bottle(), new Bottle());
        final CertificateEntity certificateEntity = new CertificateEntity();
        certificateEntity.setTxId("tx1");
        certificateEntity.setJobIndex("1L");

        when(bottleRepository.findByWineryIdAndLotIdAndCertificateIdAndCertUpdateStatusNot(
                wineryId, lotId, certId, CertUpdateStatus.UPDATED)).thenReturn(listOfApprovedBottle);

        when(certificateRepository.findById(certId)).thenReturn(Optional.of(certificateEntity));
        when(objectMapper.writeValueAsString(any(ScmBottleData.class))).thenReturn("{\"items\":[]}");

        // Act
        final List<String> result = util.buildPayloadDataFromArgs(customInputTypes,
                wineryId,
                certId, lotId, payloadBody);

        // Assert
        assertEquals(List.of("{\"items\":[]}"), result);
    }

    /**
     * <p>
     * Test with a payload body that has finalize set to true and valid arguments:
     * The approved bottle result is empty
     * </p>
     * 
     * @throws JsonProcessingException
     */
    @Test
    public void testBuildPayloadDataFromArgsWithFinalizeSetToTrueAndEmptyApprovedBottle()
            throws JsonProcessingException {
        // Arrange
        final String wineryId = "winery1";
        final String certId = "cert1";
        final String lotId = "lot1";
        final BottleIdBody payloadBody = new BottleIdBody();
        payloadBody.setFinalise(true);
        final Class<?>[] customInputTypes = { BottleIdBody.class };
        final List<Bottle> listOfApprovedBottle = List.of();
        final CertificateEntity certificateEntity = new CertificateEntity();
        certificateEntity.setTxId("tx1");
        certificateEntity.setJobIndex("1L");

        when(bottleRepository.findByWineryIdAndLotIdAndCertificateIdAndCertUpdateStatusNot(
                wineryId, lotId, certId, CertUpdateStatus.UPDATED)).thenReturn(listOfApprovedBottle);

        when(certificateRepository.findById(certId)).thenReturn(Optional.of(certificateEntity));

        // Act
        final List<String> result = util.buildPayloadDataFromArgs(customInputTypes,
                wineryId,
                certId, lotId, payloadBody);

        // Assert
        assertEquals(List.of(), result);
    }

    /**
     * <p>
     * Test with a payload body that has finalize set to true and valid arguments:
     * The lot contain the approved bottle is not exist.
     * </p>
     * 
     * @throws JsonProcessingException
     */
    @Test
    public void testBuildPayloadDataFromArgsWithFinalizeSetToTrueAndCannotFindCertificate()
            throws JsonProcessingException {
        // Arrange
        final String wineryId = "winery1";
        final String certId = "cert1";
        final String lotId = "lot1";
        final BottleIdBody payloadBody = new BottleIdBody();
        payloadBody.setFinalise(true);
        final Class<?>[] customInputTypes = { BottleIdBody.class };
        final List<Bottle> listOfApprovedBottle = List.of();

        when(bottleRepository.findByWineryIdAndLotIdAndCertificateIdAndCertUpdateStatusNot(
                wineryId, lotId, certId, CertUpdateStatus.UPDATED)).thenReturn(listOfApprovedBottle);

        when(certificateRepository.findById(certId)).thenReturn(Optional.empty());

        // Act
        final List<String> result = util.buildPayloadDataFromArgs(customInputTypes,
                wineryId,
                certId, lotId, payloadBody);

        // Assert
        assertEquals(List.of(), result);
    }

    /**
     * <p>
     * Test with a payload body that has finalize set to true and valid arguments:
     * Number of bottles is larger than 100
     * </p>
     * 
     * @throws JsonProcessingException
     */
    @Test
    public void testBuildPayloadDataFromArgsWithFinalizeSetToTrueAndNumberOfBottlesIsLargerThan100()
            throws JsonProcessingException {
        // Arrange
        final String wineryId = "winery1";
        final String certId = "cert1";
        final String lotId = "lot1";
        final BottleIdBody payloadBody = new BottleIdBody();
        payloadBody.setFinalise(true);
        final Class<?>[] customInputTypes = { BottleIdBody.class };
        final List<Bottle> listOfApprovedBottle = new LinkedList<>();
        listOfApprovedBottle.addAll(Collections.nCopies(101, new Bottle()));
        final CertificateEntity certificateEntity = new CertificateEntity();
        certificateEntity.setTxId("tx1");
        certificateEntity.setJobIndex("1L");

        when(bottleRepository.findByWineryIdAndLotIdAndCertificateIdAndCertUpdateStatusNot(
                wineryId, lotId, certId, CertUpdateStatus.UPDATED)).thenReturn(listOfApprovedBottle);

        when(certificateRepository.findById(certId)).thenReturn(Optional.of(certificateEntity));

        // Act
        final List<String> result = util.buildPayloadDataFromArgs(customInputTypes, wineryId, certId, lotId,
                payloadBody);

        // Assert
        assertEquals(2, result.size());
    }
}
