package org.cardanofoundation.metabus.common.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.metabus.common.JsonConverter;
import org.cardanofoundation.metabus.common.enums.GroupType;
import org.cardanofoundation.metabus.common.enums.JobState;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class JobJPA extends BaseEntity {
    @Column(name = "job_state", nullable = false)
    @Enumerated(EnumType.STRING)
    JobState state;
    @Column(name = "type")
    String type;
    @Column(name = "sub_type")
    String subType;
    // Optimize later
    @Column(name = "data")
    @Convert(converter = JsonConverter.class)
    Object data;
    @Column(name = "jws_header")
    byte[] jwsHeader;
    @Column(name = "signature")
    byte[] signature;
    @Column(name = "pub_key")
    byte[] pubKey;
    @Column(name = "\"group_type\"")
    @Enumerated(EnumType.STRING)
    GroupType groupType;
    @Column(name = "\"group\"")
    String group;
    @Column(name = "job_index")
    String jobIndex;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "unconfirmed_tx_id", nullable = false)
    @EqualsAndHashCode.Exclude
    UnconfirmedTxJPA unconfirmedTx;
    
    /**
     * Retry Count for submitting
     */
    @Column(name = "retry_count")
    Integer retryCount;

    /**
     * <p>
     * Subtract the retry count of current job
     * </p>
     */
    public void subtractRetryCount() {
        this.retryCount = this.retryCount - 1;
    }

    /**
     * <p>
     * Update the job info before retry the job.
     * </p>
     *
     */
    public void updateJobInfoBeforeRetry() {
        subtractRetryCount();
        this.state = JobState.PENDING;
        this.unconfirmedTx = null;
        this.jobIndex = null;
    }

    /**
     * <p>
     * Update the job info before push job to dlq.
     * </p>
     */
    public void updateJobInfoBeforeDLQ() {
        subtractRetryCount();
        this.state = JobState.FAILED;
        this.unconfirmedTx = null;
        this.jobIndex = null;
    }
}
