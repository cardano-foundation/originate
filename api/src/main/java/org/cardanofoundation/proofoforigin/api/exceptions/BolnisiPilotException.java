package org.cardanofoundation.proofoforigin.api.exceptions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BolnisiPilotException extends RuntimeException {
    BolnisiPilotError error;

    public BolnisiPilotException(BolnisiPilotError error) {
        super(error.getMessage());
        this.error = error;
    }

    public BolnisiPilotException(BolnisiPilotError error, String message) {
        super(message);
        this.error = error;
    }

    public BolnisiPilotException(BolnisiPilotError error, String message, Throwable cause) {
        super(message, cause);
        this.error = error;
    }
}
