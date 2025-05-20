package org.cardanofoundation.metabus.common.offchain;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.cardanofoundation.metabus.common.enums.GroupType;
import org.cardanofoundation.metabus.common.enums.JobState;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {
    Long id;
    JobState state;
    BusinessData businessData;
    String txHash;
    GroupType groupType;
    String group;
    String jobIndex;
    /**
     * Retry Count for submitting
     */
    Integer retryCount;

    /**
     * <p>
     * Update the job before retry submitting.
     * </p>
     *
     */
    public void updateBeforeRetry() {
        this.retryCount = this.retryCount - 1;
        this.state = JobState.PENDING;
        this.jobIndex = null;
        this.txHash = null;
    }
}
