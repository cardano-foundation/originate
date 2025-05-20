package org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cardanofoundation.proofoforigin.api.constants.TaskState;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class ScmTaskResponse {
    private String id;
    private String reference;
    private TaskState state;
    private Long codesAffected;
    private Long createdBy;
    private Date createdAt;
    private Date startedAt;
    private Date completedAt;

    public static Mono<ScmTaskResponse> fromClientRequest(ClientResponse response) {
        if (HttpStatus.OK == response.statusCode()) {
            return response.bodyToMono(ScmTaskResponse.class);
        } else {
            return null;
        }
    }
}
