package org.cardanofoundation.metabus.exceptions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MetabusException extends RuntimeException {
    MetabusError error;

    public MetabusException(MetabusError error) {
        super(error.getMessage());
        this.error = error;
    }

    public MetabusException(MetabusError error, String message) {
        super(message);
        this.error = error;
    }

    public MetabusException(MetabusError error, String message, Throwable cause) {
        super(message, cause);
        this.error = error;
    }
}
