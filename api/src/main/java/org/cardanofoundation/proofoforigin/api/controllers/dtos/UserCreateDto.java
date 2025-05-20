package org.cardanofoundation.proofoforigin.api.controllers.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateDto {
    private String email;
    private String id;
    private Boolean isSendMail;
}
