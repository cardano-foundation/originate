package org.cardanofoundation.proofoforigin.api.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cardanofoundation.proofoforigin.api.repository.entities.Bottle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.xml.bind.TypeConstraintException;

/**
 * <p>
 * Cert Revocation CM Data Handling Util Unit Test
 * </p>
 * 
 * @author (Sotatek) joey.dao
 * @category Unit-test
 * @since 2023/08
 */
@ExtendWith(MockitoExtension.class)
public class CertRevocationScmDataHandlingUtilTest {

	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	@Spy
	private CertRevocationScmDataHandlingUtil util;

	/**
	 * <p>
	 * Test the castingToBottleList method with a null classType:
	 * Verify that the method
	 * throws a TypeConstraintException with the message “Define the payload body
	 * class type”.
	 * </p>
	 */
	@Test
	public void testCastingPayloadObjectWithNullClassType() {
		// / Arrange
		final Object arg = new ArrayList<>();
		final Class<?> classType = null;

		// Act
		final TypeConstraintException exception = assertThrows(TypeConstraintException.class, () -> {
			CertRevocationScmDataHandlingUtil.castingToBottleList(arg, classType);
		});

		// Assert
		assertEquals("Lack of define class type of the arg", exception.getMessage());
	}

	/**
	 * <p>
	 * Test the castingToBottleList method with an incompatible classType:
	 * Verify that the method throws a TypeConstraintException
	 * with the message “The payload body is a type of {actual class name},
	 * instead of {expected class name}”.
	 * </p>
	 */
	@Test
	public void testCastingPayloadObjectWithIncompatibleClassType() {
		// Arrange
		final Object arg = new ArrayList<>();
		final Class<?> classType = String.class;

		// Act
		final TypeConstraintException exception = assertThrows(TypeConstraintException.class, () -> {
			CertRevocationScmDataHandlingUtil.castingToBottleList(arg, classType);
		});

		// Assert
		assertEquals("The arg object is a type of " + arg.getClass() + ", not of " + classType.getName(),
				exception.getMessage());
	}

	/**
	 * <p>
	 * Test the castingToBottleList method with a compatible classType:
	 * Verify that the method returns a List object that is casted from the
	 * input argument.
	 * </p>
	 */
	@Test
	public void testCastingPayloadObjectWithCompatibleClassType() {
		// Arrange
		final List<Bottle> arg = new ArrayList<>();
		final Class<?> classType = List.class;

		// Act
		final List<Bottle> result = CertRevocationScmDataHandlingUtil.castingToBottleList(arg, classType);

		// Assert
		assertEquals(arg, result);
	}

	/**
	 * Description:
	 * Test if the total of the bottles is 2. It will return 1 payload data.
	 */
	@Test
    void testBuildPayloadDataFromArgsCreateScmBottleData_2() throws JsonProcessingException {
        // Given
        final Class<?>[] customInputTypes = new Class<?>[]{List.class};
        final Bottle bottle1 = new Bottle();
        bottle1.setId("bottleId1");
        final Bottle bottle2 = new Bottle();
        bottle2.setId("bottleId2");
        final Object[] args = new Object[]{"arg1", "arg2", "arg3", "arg4", List.of(bottle1, bottle2)};

        // When
        final List<String> payloadData = util.buildPayloadDataFromArgs(customInputTypes, args);

        // Then
        assertEquals(1, payloadData.size());
    }

	/**
	 * Description:
	 * Test if the function creates a new ScmBottleData object with an empty list of
	 * items.
	 */
	@Test
    void testBuildPayloadDataFromArgsCreateScmBottleData_0() throws JsonProcessingException {
        // Given
        final Class<?>[] customInputTypes = new Class<?>[]{List.class};
        final Object[] args = new Object[]{"arg1", "arg2", "arg3", "arg4", List.of()};

        // When
        final List<String> payloadData = util.buildPayloadDataFromArgs(customInputTypes, args);

        // Then
        assertEquals(0, payloadData.size());
    }

	/**
	 * Description:
	 * Test if the function returns the payloadData list with 2 payloads data (> 100 bottles)
	 */
	@Test
    void testBuildPayloadDataFromArgsCreateScmBottleData_101() throws JsonProcessingException {
        // Given
        final Class<?>[] customInputTypes = new Class<?>[]{List.class};
        final Bottle bottle1 = new Bottle();
        bottle1.setId("bottleId1");
        final Object[] args = new Object[]{"arg1", "arg2", "arg3", "arg4", Collections.nCopies(101, bottle1)};

        // When
        final List<String> payloadData = util.buildPayloadDataFromArgs(customInputTypes, args);

        // Then
        assertEquals(2, payloadData.size());
    }

	/**
	 * Description:
	 * Test if the total of the bottles is 1. It will return 1 payload data.
	 */
	@Test
    void testBuildPayloadDataFromArgsCreateScmBottleData_1() throws JsonProcessingException {
        // Given
        final Class<?>[] customInputTypes = new Class<?>[]{List.class};
		final Bottle bottle1 = new Bottle();
        bottle1.setId("bottleId1");
        final Object[] args = new Object[]{"arg1", "arg2", "arg3", "arg4", List.of(bottle1)};

        // When
        final List<String> payloadData = util.buildPayloadDataFromArgs(customInputTypes, args);

        // Then
        assertEquals(1, payloadData.size());
    }
}
