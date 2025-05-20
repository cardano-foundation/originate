package org.cardanofoundation.metabus.application.exceptions;

/**
 * <p>
 * The Transaction Hash Is NOT FOUND on-chain Exception.
 * </p>
 * <b>
 * This exception is used for retrying job consumption.
 * </b>
 *
 * @author (Sotatek) joey.dao
 * @version 0.01
 * @category Exception
 * @since 2023/08
 */
public class TransactionHashNotFoundOnChainException extends RuntimeException {

    /**
     * Constructor
     * 
     * @param message The message
     */
    public TransactionHashNotFoundOnChainException(final String message) {
        super(message);
    }
}
