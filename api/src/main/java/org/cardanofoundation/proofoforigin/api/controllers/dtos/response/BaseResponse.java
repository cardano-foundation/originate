package org.cardanofoundation.proofoforigin.api.controllers.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.proofoforigin.api.exceptions.OriginatePilotError;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
public class BaseResponse<T> {

    public static final String OK_CODE = "200";

    public static final String OK_CODE_CREATE = "201";

    private T data;
    private Metadata meta = new Metadata();

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Metadata {
        private String code;
        private Integer page;
        private Integer size;
        private Long total;
        private String message;
        private String internalMessage;
        private String requestId;
    }

    public static <T> BaseResponse<T> ofSucceeded(T data) {
        BaseResponse<T> response = new BaseResponse<>();
        response.data = data;
        response.meta.code = OK_CODE;
        return response;
    }

    public static <T> BaseResponse<T> ofSucceeded() {
        BaseResponse<T> response = new BaseResponse<>();
        response.meta.code = OK_CODE;
        return response;
    }

    public static BaseResponse<Void> ofFailed(Exception error) {
        return ofFailed(error, "Internal server error");
    }

    public static BaseResponse<Void> ofFailed(OriginatePilotError errorCode, String message) {
        BaseResponse<Void> response = new BaseResponse<>();
        response.meta.code = String.valueOf(errorCode.getCode());
        response.meta.message = errorCode.getMessage();
        response.meta.internalMessage = (message != null) ? message : errorCode.getMessage();
        return response;
    }

    public static BaseResponse<Void> ofFailed(Exception error, String message) {
        BaseResponse<Void> response = new BaseResponse<>();
        response.meta.code = "500";
        response.meta.message = message;
        response.meta.internalMessage = error.toString();
        return response;
    }

    public static BaseResponse<Void> ofSucceededCreate(String body) {
        BaseResponse<Void> response = new BaseResponse<>();
        response.meta.code = OK_CODE_CREATE;
        response.meta.message = body;
        return response;
    }

    public static <T> BaseResponse<T> ofSucceededCreate(T data, String message) {
        BaseResponse<T> response = new BaseResponse<>();
        response.meta.code = OK_CODE_CREATE;
        response.data = data;
        response.meta.message = message;
        return response;
    }

}
