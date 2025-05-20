package org.cardanofoundation.metabus.service;

import co.nstant.in.cbor.model.*;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.cardanofoundation.metabus.common.enums.GroupType;
import org.cardanofoundation.metabus.common.offchain.JobBatch;
import org.cardanofoundation.metabus.common.onchain.TxMetadata;
import org.cardanofoundation.metabus.exceptions.MetabusErrors;
import org.cardanofoundation.metabus.exceptions.MetabusException;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * @Modified (Sotatek) joey.dao
 * @since 2023/08
 */
public abstract class MetadataService<T extends TxMetadata> implements TxSubmitterServiceInstance<T> {
    public abstract CBORMetadata buildMetadata(T txMetadata, BigInteger metadatumLabel);

    public abstract TxMetadata buildMetadata(JobBatch jobBatch);

    public abstract String buildOffchainJson(JobBatch jobBatch) throws JsonProcessingException;

    public static final String CID = "cid";
    public static final String TYPE = "t";
    public static final String SUB_TYPE = "st";
    public static final String PUB_KEY = "pk";
    public static final String JWS_HEADER = "h";
    public static final String SIGNATURES = "s";
    public static final String VERSION = "v";

    /**
     * <p>
     * Build the CborMetadata base on group type of the jobs.
     * </p>
     * 
     * @param txMetadata the transaction metadata
     * @param groupType the groupType of the job
     * @return the cbor map
     */
    protected Map getBaseCborMetadata(final TxMetadata txMetadata, final GroupType groupType) {
        final Map cborMap = new Map();

        String jobType = txMetadata.getType();
        // Trim post fix of job type before building metadata
        jobType = trimJobTypePostFix(jobType);

        cborMap.put(new UnicodeString(VERSION), new UnicodeString(txMetadata.getVersion()));
        cborMap.put(new UnicodeString(CID), new UnicodeString(txMetadata.getCid()));
        cborMap.put(new UnicodeString(TYPE), new UnicodeString(jobType));
        cborMap.put(new UnicodeString(SUB_TYPE), new UnicodeString(txMetadata.getSubType()));

        return cborMap;
    }

    /**
     * <p>
     * Trim the post fix of job type: <type><subType> to just <type>
     * For e.g: scm:georgianWine to just scm
     * </p>
     * @param jobType
     * @return
     */
    private String trimJobTypePostFix(String jobType){
        if(!jobType.matches("^[^:]+:[^:]+$")){
            throw new MetabusException(MetabusErrors.INVALID_JOB_TYPE);
        }
        return jobType.split(":")[0];
    }

    /**
     * <p>
     * Split the byte array into an array of CBOR ByteString less than 64 bytes as metadata on blockchain
     * doesn't accept more than 64 bytes string fields.
     * </p>
     *
     * @param byteArray
     * @return cbor array of CBOR ByteString if byteArray input > 64,
     * if byteaArray input <= 64, return just the ByteString
     */
    protected DataItem splitByteArray(byte[] byteArray){
        Array cborArray = new Array();

        for(int i = 0; i < byteArray.length; i += 64){
            int endIndex = Math.min(i + 64, byteArray.length);

            byte[] chunk = Arrays.copyOfRange(byteArray, i, endIndex);

            ByteString byteStringChunk = new ByteString(chunk);

            cborArray.add(byteStringChunk);
        }

        List<DataItem> dataItems = cborArray.getDataItems();

        // If the split list contains just 1 item, the field on chain will be just a string instead
        // of an array of chunks.
        return dataItems.size() == 1 ?
                (ByteString) dataItems.get(0) : cborArray;
    }
}
