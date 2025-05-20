package org.cardanofoundation.metabus.controllers.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.cardanofoundation.metabus.annotation.MaxByteSize;
import org.cardanofoundation.metabus.common.enums.GroupType;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class JobReq {
    @NotNull
    @Valid
    BusinessData businessData;

    @NotNull
    GroupType groupType;

    @MaxByteSize
    String group;
}
