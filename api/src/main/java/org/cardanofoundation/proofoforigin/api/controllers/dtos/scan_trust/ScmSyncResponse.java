package org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import reactor.core.publisher.Mono;

/**
 * <p>
 * An Response Object of the Upload SCM API (SYNC)
 * </p>
 *
 * @author (Sotatek) joey.dao
 * @category Response-object
 * @since 2023/07
 */
@Getter
@Setter
public class ScmSyncResponse {

    @JsonProperty("total_updates")
    private Integer totalUpdates;

    @JsonProperty("changes")
    private List<Object> changes;

    /**
     * "items" will only list the affected field key names, not the values.
     */
    @JsonProperty("items")
    private Map<String, List<String>> items;

    @JsonProperty("codes_affected")
    private Integer codesAffected;

    /**
     * <p>
     * The function is for converting response body to object.
     * </p>
     *
     * @param response the response body
     * @return Mono Object
     */
    public static Mono<ScmSyncResponse> fromClientRequest(final ClientResponse response) {
        if (HttpStatus.OK == response.statusCode()) {
            return response.bodyToMono(ScmSyncResponse.class);
        } else {
            return response.createException().flatMap(Mono::error);
        }
    }
}
