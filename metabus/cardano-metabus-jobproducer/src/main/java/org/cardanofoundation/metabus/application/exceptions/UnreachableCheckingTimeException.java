package org.cardanofoundation.metabus.application.exceptions;

/**
 * <p>
 * The Unreachable Checking Time Exception.
 * </p>
 * <b>
 * This exception is used for retrying message.
 * </b>
 *
 * @author (Sotatek) joey.dao
 * @version 0.01
 * @category Exception
 * @since 2023/08
 */
public class UnreachableCheckingTimeException extends RuntimeException {

    /**
     * Constructor with message
     * 
     * @param message The message
     */
    public UnreachableCheckingTimeException(String message) {
        super(message);
    }

    /**
     * Constructor
     */
    public UnreachableCheckingTimeException() {
        super();
    }
}
