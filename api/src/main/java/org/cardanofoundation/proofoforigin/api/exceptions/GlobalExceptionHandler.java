package org.cardanofoundation.proofoforigin.api.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler({OriginatePilotException.class})
    public ResponseEntity<BaseResponse<Void>> handleBusinessException(OriginatePilotException e) {
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<Void>> handleException(HttpMessageNotReadableException e){
        e.printStackTrace();
        var data=BaseResponse.ofFailed(OriginatePilotErrors.REQUEST_FORMAT,"incorrect request format");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(data);
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
        var data = BaseResponse.ofFailed(OriginatePilotErrors.INVALID_PARAMETERS, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(data);
    }
}
