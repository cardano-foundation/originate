package org.cardanofoundation.metabus.common.offchain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.cardanofoundation.metabus.common.enums.GroupType;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobBatch {
    String jobType;
    String jobSubType;
    GroupType groupType;
    List<Job> jobs;
    String cid;
}
