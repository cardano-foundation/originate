package org.cardanofoundation.metabus.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.metabus.controllers.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler({MetabusException.class})
    public ResponseEntity<BaseResponse<Void>> handleBusinessException(MetabusException e) {
        var enterpriseBackendError = e.getError();
        var data = BaseResponse.ofFailed(enterpriseBackendError, e.getMessage());
        return ResponseEntity.status(enterpriseBackendError.getHttpStatus()).body(data);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleUnwantedException(Exception e) {
        var data = BaseResponse.ofFailed(e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(data);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleInvalidParameterException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> (fieldError.getField() + ": " + fieldError.getDefaultMessage() + "\n"))
                .reduce("", (pre, next) -> pre + next);
        var data = BaseResponse.ofFailed(MetabusErrors.INVALID_PARAMETERS, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(data);
    }
}
