package org.cardanofoundation.metabus.application;

import org.cardanofoundation.metabus.application.exceptions.JobProducerErrors;
import org.cardanofoundation.metabus.application.exceptions.JobProducerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler({JobProducerException.class})
    public ResponseEntity<BaseResponse<Void>> handleBusinessException(JobProducerException e) {
        var bolnisiPilotError = e.getError();
        var data = BaseResponse.ofFailed(bolnisiPilotError, e.getMessage());
        return ResponseEntity.status(bolnisiPilotError.getHttpStatus()).body(data);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleUnwantedException(Exception e) {
        e.printStackTrace();
        var data = BaseResponse.ofFailed(e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(data);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleInvalidParameterException(
            MethodArgumentNotValidException e) {
        String message =
                e.getBindingResult().getFieldErrors().stream()
                        .map(
                                fieldError ->
                                        (fieldError.getField() + ": " + fieldError.getDefaultMessage() + "\n"))
                        .reduce("", (pre, next) -> pre + next);
        var data = BaseResponse.ofFailed(JobProducerErrors.INVALID_PARAMETERS, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(data);
    }
}
