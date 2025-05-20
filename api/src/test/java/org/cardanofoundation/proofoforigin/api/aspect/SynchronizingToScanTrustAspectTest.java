package org.cardanofoundation.proofoforigin.api.aspect;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.cardanofoundation.proofoforigin.api.business.ScanTrustService;
import org.cardanofoundation.proofoforigin.api.configuration.UploadScmDataSync;
import org.cardanofoundation.proofoforigin.api.constants.UploadType;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust.ScmSyncResponse;
import org.cardanofoundation.proofoforigin.api.utils.ScanTrustDataHandlingUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.annotation.AnnotationUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <p>
 * SynchronizingToScanTrustAspect Unit Test
 * </p>
 * 
 * @author (Sotatek) joey.dao
 * @category Unit-test
 * @since 2023/07
 */
@ExtendWith(MockitoExtension.class)
public class SynchronizingToScanTrustAspectTest {

    @Mock
    private Logger log;

    @InjectMocks
    @Spy
    private SynchronizingToScanTrustAspect aspect;

    @Mock
    private ScanTrustService scanTrustService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private BeanFactory beanFactory;

    /**
     * <p>
     * Test that the method buildPayloadData output is not empty list.
     * </p>
     * 
     * @throws JsonProcessingException
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    @Test
    public void testBuildPayloadDataMethodOutputEmptyList()
            throws JsonProcessingException, NoSuchMethodException, SecurityException {
        final JoinPoint joinPoint = mock(JoinPoint.class);
        final UploadScmDataSync uploadScmDataSync = mock(UploadScmDataSync.class);
        final Signature signature = mock(Signature.class);
        final ScanTrustDataHandlingUtil util = mock(ScanTrustDataHandlingUtil.class);

        final boolean isSyncProcess = false;
        final UploadType uploadType = UploadType.BOTTLE_SYNC;
        final Class<?>[] inputCustomClassTypes = { ScanTrustDataHandlingUtil.class };

        try (MockedStatic<AnnotationUtils> annotationStatic = mockStatic(AnnotationUtils.class)) {
            final Method method = getClass().getDeclaredMethod("mockedMethod");
            final MethodSignature methodSignature = spy(MethodSignature.class);
            doReturn(signature, methodSignature).when(joinPoint).getSignature();
            doReturn(method).when(methodSignature).getMethod();
            doReturn("testMethod").when(signature).getName();
            annotationStatic.when(() -> AnnotationUtils.findAnnotation(method, UploadScmDataSync.class))
                    .thenReturn(uploadScmDataSync);
            doReturn(isSyncProcess).when(uploadScmDataSync).doSync();
            doReturn(uploadType).when(uploadScmDataSync).uploadType();
            doReturn(inputCustomClassTypes).when(uploadScmDataSync).inputCustomClassTypes();

            doReturn(util).when(beanFactory).getBean(UploadType.getDataBuildUtilClass(uploadType),
                    inputCustomClassTypes[0]);
            doReturn(List.of()).when(util).buildPayloadDataFromArgs(eq(inputCustomClassTypes), any());

            aspect.uploadScmDataToScanTrustBySyncApiAfter(joinPoint);

            verify(util).buildPayloadDataFromArgs(eq(inputCustomClassTypes), any());
            verify(aspect, times(0)).sendScmDataToScanTrust(any(), eq(util));
        }

    }

    /**
     * <p>
     * Test that the method buildPayloadData output is empty list.
     * </p>
     * 
     * @throws JsonProcessingException
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    @Test
    public void testBuildPayloadDataMethodOutputNotEmptyList()
            throws JsonProcessingException, NoSuchMethodException, SecurityException {
        final JoinPoint joinPoint = mock(JoinPoint.class);
        final UploadScmDataSync uploadScmDataSync = mock(UploadScmDataSync.class);
        final Signature signature = mock(Signature.class);
        final ScanTrustDataHandlingUtil util = mock(ScanTrustDataHandlingUtil.class);

        final boolean isSyncProcess = false;
        final UploadType uploadType = UploadType.BOTTLE_SYNC;
        final Class<?>[] inputCustomClassTypes = { ScanTrustDataHandlingUtil.class };
        final List<String> payload = List.of("testString", "testString_2");

        try (MockedStatic<AnnotationUtils> annotationStatic = mockStatic(AnnotationUtils.class)) {
            final Method method = getClass().getDeclaredMethod("mockedMethod");
            final MethodSignature methodSignature = spy(MethodSignature.class);
            doReturn(signature).doReturn(methodSignature).when(joinPoint).getSignature();
            doReturn(method).when(methodSignature).getMethod();
            doReturn("testMethod").when(signature).getName();
            annotationStatic.when(() -> AnnotationUtils.findAnnotation(method, UploadScmDataSync.class))
                    .thenReturn(uploadScmDataSync);
            doReturn(isSyncProcess).when(uploadScmDataSync).doSync();
            doReturn(uploadType).when(uploadScmDataSync).uploadType();
            doReturn(inputCustomClassTypes).when(uploadScmDataSync).inputCustomClassTypes();

            doReturn(util).when(beanFactory).getBean(UploadType.getDataBuildUtilClass(uploadType),
                    inputCustomClassTypes[0]);
            doReturn(payload).when(util).buildPayloadDataFromArgs(eq(inputCustomClassTypes), any());

            aspect.uploadScmDataToScanTrustBySyncApiAfter(joinPoint);

            verify(util).buildPayloadDataFromArgs(eq(inputCustomClassTypes), any());
            verify(aspect, times(1)).sendScmDataToScanTrust(payload, util);
        }
    }

    /**
     * <p>
     * Test that the method buildPayloadData output is empty list.
     * And do the sync process.
     * </p>
     * 
     * @throws JsonProcessingException
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    @Test
    public void testBuildPayloadDataMethodOutputNotEmptyListSync()
            throws JsonProcessingException, NoSuchMethodException, SecurityException {
        final JoinPoint joinPoint = mock(JoinPoint.class);
        final UploadScmDataSync uploadScmDataSync = mock(UploadScmDataSync.class);
        final Signature signature = mock(Signature.class);
        final ScanTrustDataHandlingUtil util = mock(ScanTrustDataHandlingUtil.class);

        final boolean isSyncProcess = true;
        final UploadType uploadType = UploadType.BOTTLE_SYNC;
        final Class<?>[] inputCustomClassTypes = { ScanTrustDataHandlingUtil.class };
        final List<String> payload = List.of("testString", "testString_2");

        try (MockedStatic<AnnotationUtils> annotationStatic = mockStatic(AnnotationUtils.class)) {
            final Method method = getClass().getDeclaredMethod("mockedMethod");
            final MethodSignature methodSignature = spy(MethodSignature.class);
            doReturn(signature).doReturn(methodSignature).when(joinPoint).getSignature();
            doReturn(method).when(methodSignature).getMethod();
            doReturn("testMethod").when(signature).getName();
            annotationStatic.when(() -> AnnotationUtils.findAnnotation(method, UploadScmDataSync.class))
                    .thenReturn(uploadScmDataSync);
            doReturn(isSyncProcess).when(uploadScmDataSync).doSync();
            doReturn(uploadType).when(uploadScmDataSync).uploadType();
            doReturn(inputCustomClassTypes).when(uploadScmDataSync).inputCustomClassTypes();

            doReturn(util).when(beanFactory).getBean(UploadType.getDataBuildUtilClass(uploadType),
                    inputCustomClassTypes[0]);
            doReturn(payload).when(util).buildPayloadDataFromArgs(eq(inputCustomClassTypes), any());

            aspect.uploadScmDataToScanTrustBySyncApiAfter(joinPoint);

            verify(util).buildPayloadDataFromArgs(eq(inputCustomClassTypes), any());
            verify(aspect, times(1)).sendScmDataToScanTrust(payload, util);
        }
    }

    /**
     * <p>
     * Test that the method buildPayloadData throw exception.
     * </p>
     * 
     * @throws JsonProcessingException
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    @Test
    public void testBuildPayloadDataThrowException()
            throws JsonProcessingException, NoSuchMethodException, SecurityException {
        final JoinPoint joinPoint = mock(JoinPoint.class);
        final UploadScmDataSync uploadScmDataSync = mock(UploadScmDataSync.class);
        final Signature signature = mock(Signature.class);
        final ScanTrustDataHandlingUtil util = mock(ScanTrustDataHandlingUtil.class);

        final boolean isSyncProcess = false;
        final UploadType uploadType = UploadType.BOTTLE_SYNC;
        final Class<?>[] inputCustomClassTypes = { ScanTrustDataHandlingUtil.class };
        try (MockedStatic<AnnotationUtils> annotationStatic = mockStatic(AnnotationUtils.class)) {
            final Method method = getClass().getDeclaredMethod("mockedMethod");
            final MethodSignature methodSignature = spy(MethodSignature.class);
            doReturn(signature).doReturn(methodSignature).when(joinPoint).getSignature();
            doReturn(method).when(methodSignature).getMethod();
            doReturn("testMethod").when(signature).getName();
            annotationStatic.when(() -> AnnotationUtils.findAnnotation(method, UploadScmDataSync.class))
                    .thenReturn(uploadScmDataSync);
            doReturn(isSyncProcess).when(uploadScmDataSync).doSync();
            doReturn(uploadType).when(uploadScmDataSync).uploadType();
            doReturn(inputCustomClassTypes).when(uploadScmDataSync).inputCustomClassTypes();

            doReturn(util).when(beanFactory).getBean(UploadType.getDataBuildUtilClass(uploadType),
                    inputCustomClassTypes[0]);
            doThrow(JsonProcessingException.class).when(util).buildPayloadDataFromArgs(eq(inputCustomClassTypes),
                    any());

            assertDoesNotThrow(() -> aspect.uploadScmDataToScanTrustBySyncApiAfter(joinPoint));
            verify(util).buildPayloadDataFromArgs(eq(inputCustomClassTypes), any());
            verify(aspect, times(0)).sendScmDataToScanTrust(any(), eq(util));
        }
    }

    /**
     * <p>
     * Test with an empty payload:
     * Verify that the function handles an empty payload correctly and does not
     * throw any exceptions.
     * </p>
     * 
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    @Test
    public void testSendScmDataToScanTrustWithValidPayload() throws JsonMappingException, JsonProcessingException {
        // Create test data
        final List<String> payload = Arrays.asList("payload1", "payload2");
        final ScmSyncResponse mockResponse = new ScmSyncResponse();
        final ScanTrustDataHandlingUtil util = mock(ScanTrustDataHandlingUtil.class);

        // Define mock behavior
        doReturn(mockResponse).when(scanTrustService).sendScmDataBySyncApi(payload.get(0));
        doReturn(mockResponse).when(scanTrustService).sendScmDataBySyncApi(payload.get(1));

        // Call method under test
        aspect.sendScmDataToScanTrust(payload, util);

        // Verify interactions with mock objects
        verify(scanTrustService, times(1)).sendScmDataBySyncApi(payload.get(0));
        verify(scanTrustService, times(1)).sendScmDataBySyncApi(payload.get(1));

        verify(util, times(1)).executeProcedureAfterSubmitByPayload(payload.get(0));
        verify(util, times(1)).executeProcedureAfterSubmitByPayload(payload.get(1));
    }

    /**
     * <p>
     * Test with a non-empty payload:
     * Verify that the function iterates over the payload
     * and calls the sendScmDataBySyncApi method for each element in the payload.
     * </p>
     */
    @Test
    public void testSendScmDataToScanTrustWithEmptyPayload() {
        // Create test data
        List<String> payload = List.of();
        final ScanTrustDataHandlingUtil util = mock(ScanTrustDataHandlingUtil.class);

        // Call method under test
        aspect.sendScmDataToScanTrust(payload, util);

        // Verify interactions with mock objects
        verify(scanTrustService, never()).sendScmDataBySyncApi(anyString());
    }

    /**
     * <p>
     * Test with a null response from sendScmDataBySyncApi:
     * Verify that the function handles a null response
     * from the sendScmDataBySyncApi method correctly and does not throw any
     * exceptions.
     * </p>
     * 
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    @Test
    public void testSendScmDataToScanTrustWithNullResponse() throws JsonMappingException, JsonProcessingException {
        // Arrange
        final List<String> payload = Arrays.asList("payload1", "payload2");
        final ScanTrustDataHandlingUtil util = mock(ScanTrustDataHandlingUtil.class);

        when(scanTrustService.sendScmDataBySyncApi(payload.get(0))).thenReturn(null);
        when(scanTrustService.sendScmDataBySyncApi(payload.get(1))).thenReturn(null);

        // Act
        aspect.sendScmDataToScanTrust(payload, util);

        // Assert
        verify(scanTrustService, times(1)).sendScmDataBySyncApi(payload.get(0));
        verify(scanTrustService, times(1)).sendScmDataBySyncApi(payload.get(1));

        verify(util, never()).executeProcedureAfterSubmitByPayload(anyString());
    }

    /**
     * <p>
     * Test with a non-null response from sendScmDataBySyncApi:
     * Verify that the function calls the executeProcedureAfterSubmitByPayload
     * method
     * with the correct arguments when the response from sendScmDataBySyncApi is not
     * null.
     * </p>
     * 
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    @Test
    public void testSendScmDataToScanTrustWithNonNullResponse() throws JsonMappingException, JsonProcessingException {
        // Arrange
        final List<String> payload = Arrays.asList("payload1", "payload2");
        final ScanTrustDataHandlingUtil util = mock(ScanTrustDataHandlingUtil.class);
        final ScmSyncResponse mockResponse = new ScmSyncResponse();

        when(scanTrustService.sendScmDataBySyncApi(payload.get(1))).thenReturn(mockResponse);
        when(scanTrustService.sendScmDataBySyncApi(payload.get(0))).thenReturn(mockResponse);

        // Act
        aspect.sendScmDataToScanTrust(payload, util);

        // Assert
        verify(scanTrustService, times(1)).sendScmDataBySyncApi(payload.get(0));
        verify(scanTrustService, times(1)).sendScmDataBySyncApi(payload.get(1));

        verify(util, times(1)).executeProcedureAfterSubmitByPayload(payload.get(0));
        verify(util, times(1)).executeProcedureAfterSubmitByPayload(payload.get(1));
    }

    @Test
    public void testSendScmDataToScanTrustWithJsonProcessingException()
            throws JsonMappingException, JsonProcessingException {
        // Arrange
        final List<String> payload = Arrays.asList("payload1", "payload2");
        final ScanTrustDataHandlingUtil util = mock(ScanTrustDataHandlingUtil.class);
        final ScmSyncResponse mockResponse = new ScmSyncResponse();

        when(scanTrustService.sendScmDataBySyncApi(payload.get(0))).thenReturn(mockResponse);
        doThrow(new RuntimeException()).when(scanTrustService).sendScmDataBySyncApi(payload.get(1));

        // Act
        aspect.sendScmDataToScanTrust(payload, util);

        // Assert
        verify(scanTrustService, times(1)).sendScmDataBySyncApi(payload.get(0));
        verify(scanTrustService, times(1)).sendScmDataBySyncApi(payload.get(1));
        verify(util, times(1)).executeProcedureAfterSubmitByPayload(anyString());
    }

    /**
     * <p>
     * This one is used for getting java.lang.reflect.Method
     * </p>
     */
    public void mockedMethod() {
        return;
    }
}
