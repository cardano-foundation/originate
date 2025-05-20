package org.cardanofoundation.proofoforigin.api.controllers.dtos;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Getter
@Setter
public class RequestMatcherTermDto {
    private RequestMatcher requestMatcher;
    private String method;
}
