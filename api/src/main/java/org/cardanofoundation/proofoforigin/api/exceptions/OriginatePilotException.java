package org.cardanofoundation.proofoforigin.api.exceptions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OriginatePilotException extends RuntimeException {
    OriginatePilotError error;

    public OriginatePilotException(OriginatePilotError error) {
        super(error.getMessage());
        this.error = error;
    }

    public OriginatePilotException(OriginatePilotError error, String message) {
        super(message);
        this.error = error;
    }

    public OriginatePilotException(OriginatePilotError error, String message, Throwable cause) {
        super(message, cause);
        this.error = error;
    }
}
