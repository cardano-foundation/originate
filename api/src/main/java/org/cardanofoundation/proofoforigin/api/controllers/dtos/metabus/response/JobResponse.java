package org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.BusinessDataDto;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.JobState;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.Unit;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobResponse {
    Long id;
    JobState state;
    BusinessDataDto businessData;
    String txHash;
    Unit.GroupType groupType;
    String group;
}
