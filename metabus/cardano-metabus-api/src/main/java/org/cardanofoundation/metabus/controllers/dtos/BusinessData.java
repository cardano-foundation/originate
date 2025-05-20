package org.cardanofoundation.metabus.controllers.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.cardanofoundation.metabus.annotation.MaxByteSize;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class BusinessData {
    @NotBlank
    @MaxByteSize
    String type;

    @NotNull
    Object data;

    // signature in format <jwsHeader><signature>
    @NotNull
    String signature;

    @NotNull
    @MaxByteSize
    String pubKey;
}