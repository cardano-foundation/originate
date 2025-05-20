package org.cardanofoundation.metabus.application.exceptions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobProducerException extends RuntimeException {
    JobProducerError error;

    public JobProducerException(JobProducerError error) {
        super(error.getMessage());
        this.error = error;
    }

    public JobProducerException(JobProducerError error, String message) {
        super(message);
        this.error = error;
    }

    public JobProducerException(JobProducerError error, String message, Throwable cause) {
        super(message, cause);
        this.error = error;
    }
}
