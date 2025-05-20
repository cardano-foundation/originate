package org.cardanofoundation.proofoforigin.api.utils;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * <p>
 * Payload Data Handling Util (for Submitting to ScanTrust)
 * </p>
 * 
 * @author (Sotatek) joey.dao
 * @category Utility
 * @since 2023/07
 */
public abstract class ScanTrustDataHandlingUtil {

    /**
     * <p>
     * Build Payload Data
     * </p>
     * 
     * @param customInputTypes The class type of the payload
     * @param args             The list of input.
     * @return payload Data (String)
     */
    public abstract List<String> buildPayloadDataFromArgs(final Class<?>[] customInputTypes, final Object... args)
            throws JsonProcessingException;

    /**
     * <p>
     * Execute procedure after sync to ScanTrust successfully.
     * </p>
     * <p>
     * <h4>Based on Payload</h4>
     * </p>
     * 
     * @param payload payload
     */
    public abstract void executeProcedureAfterSubmitByPayload(final String payload)
            throws JsonMappingException, JsonProcessingException;

    /**
     * <p>
     * Execute procedure after sync to ScanTrust failed.
     * </p>
     * <p>
     * <h4>Based on Payload</h4>
     * </p>
     * 
     * @param payload payload
     */
    public abstract void executeProcedureAfterSubmitFailedByPayload(final String payload)
            throws JsonMappingException, JsonProcessingException;
}
