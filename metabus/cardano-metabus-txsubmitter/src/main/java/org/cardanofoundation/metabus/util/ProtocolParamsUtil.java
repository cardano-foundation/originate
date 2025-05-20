package org.cardanofoundation.metabus.util;

import java.math.BigInteger;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.common.cbor.CborSerializationUtil;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.TransactionOutput;
import com.bloxbean.cardano.client.transaction.spec.Value;
import org.springframework.beans.BeanUtils;

import com.bloxbean.cardano.client.api.model.ProtocolParams;

/**
 * <p>
 * The utility class for manipulate fields in ProtocolParams.
 * </p>
 *
 * @author (Sotatek) joey.dao
 * @version 0.01
 * @category Utilities
 * @since 2023/06
 */
public interface ProtocolParamsUtil {

    public static ProtocolParams cachedProtocolParams = new ProtocolParams();

    /**
     * <p>
     * Calculate the maximum fee value.
     * </p>
     *
     * @return The minimum value of the Utxo
     */
    static Integer calTheMaximumFee() {
        return (cachedProtocolParams.getMinFeeA() * cachedProtocolParams.getMaxTxSize())
                + cachedProtocolParams.getMinFeeB();
    }

    /**
     * <p>
     * Validate the txIn is valid for the transaction.
     * </p>
     *
     * @param totalTxInAmount The total txIn amount (lovelace)
     * @return is valid or not
     */
    static boolean isTxInValid(final long totalTxInAmount, long minUtxo) {
        return totalTxInAmount - calTheMaximumFee().longValue() >= minUtxo;
    }

    /**
     * <p>
     * Get minUtxo.
     * </p>
     *
     * @param senderPaymentAddress
     * @return minUtxo (lovelace)
     */
    static long getMinUtxo(String senderPaymentAddress) throws AddressExcepion, CborSerializationException, CborException {
        final TransactionOutput txOut = TransactionOutput.builder()
                .address(senderPaymentAddress)
                .value(Value.builder()
                        .coin(BigInteger.ZERO).build())
                .build();
        final byte[] serializedOutput;
        serializedOutput = CborSerializationUtil.serialize(txOut.serialize());
        final long minUtxo = (160 + serializedOutput.length) * Integer.valueOf(ProtocolParamsUtil.cachedProtocolParams.getCoinsPerUtxoSize());
        return minUtxo;
    }

    /**
     * <p>
     * Load the current ProtocolParams into memory.
     * </p>
     */
    static void loadCurrentProtocolParams(final ProtocolParams protocolParams) {
        BeanUtils.copyProperties(protocolParams, cachedProtocolParams);
    }
}
