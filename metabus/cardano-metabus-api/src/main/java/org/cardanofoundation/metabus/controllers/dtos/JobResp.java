package org.cardanofoundation.metabus.controllers.dtos;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.cardanofoundation.metabus.common.enums.GroupType;
import org.cardanofoundation.metabus.common.enums.JobState;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class JobResp {
    Long id;
    JobState state;
    BusinessDataResp businessData;
    String txHash;
    GroupType groupType;
    String group;
    String jobIndex;
}
