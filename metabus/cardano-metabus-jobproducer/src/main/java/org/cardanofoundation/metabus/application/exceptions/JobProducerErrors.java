package org.cardanofoundation.metabus.application.exceptions;

import org.springframework.http.HttpStatus;

public class JobProducerErrors {
    private JobProducerErrors() {
    }

    /**
     * 400
     */
    public static final JobProducerError INVALID_PARAMETERS =
            new JobProducerError(400000, "Invalid Parameter", HttpStatus.BAD_REQUEST);

    /**
     * 500
     */
    public static final JobProducerError ERROR_PUSHING_JOB_KAFKA =
            new JobProducerError(500000, "Error pushing job to kafka", HttpStatus.INTERNAL_SERVER_ERROR);

}
