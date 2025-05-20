package org.cardanofoundation.proofoforigin.api.configuration.modal;

import lombok.*;
import lombok.experimental.FieldDefaults;


@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Job {
    Long id;
    JobState state;
    BusinessData businessData;
    String txHash;
    GroupType groupType;
    String group;
    String jobIndex;
}
