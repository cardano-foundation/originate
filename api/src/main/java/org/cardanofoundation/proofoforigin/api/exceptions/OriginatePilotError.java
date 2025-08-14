package org.cardanofoundation.proofoforigin.api.exceptions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OriginatePilotError {
    int code;
    String message;
    HttpStatus httpStatus;

    public OriginatePilotError(int code, String message) {
        this.httpStatus = HttpStatus.BAD_REQUEST;
        this.code = code;
        this.message = message;
    }

    public OriginatePilotError(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
