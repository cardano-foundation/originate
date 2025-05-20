package org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ScmAsyncResponse {

    @JsonProperty("task_id")
    private String taskId;

    public static Mono<ScmAsyncResponse> fromClientRequest(ClientResponse response) {
        if (HttpStatus.OK == response.statusCode()) {
            return response.bodyToMono(ScmAsyncResponse.class);
        } else {
            return response.createException().flatMap(Mono::error);
        }
    }
}
