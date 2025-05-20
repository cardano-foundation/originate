package org.cardanofoundation.metabus.common.entities;
import java.io.Serializable;
import java.time.Instant;

import org.cardanofoundation.metabus.common.enums.BatchStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * An entity to manage the status information about batches
 * that is an target to be submitted to cardano node
 * </p>
 * <p>
 * The information is stored as table in PostgresDB
 * </p>
 * 
 * @author (Sotatek) joey.dao
 * @version 0.01
 * @category JPA-entity
 * @since 2023/06
 */
@Getter
@Setter
@Builder
@Table(name = "scheduled_batches")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ScheduledBatchesJPA implements Serializable {

    /**
     * The auto-generated id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, insertable = false, updatable = false, unique = true)
    Long id;

    /**
     * <p>
     * Job type is a key. Because a batch contains many jobs in the same job type.
     * </p>
     */
    @Column(name = "job_type", nullable = false, insertable = true, updatable = false, unique = true)
    String jobType;

    /**
     * <p>
     * Batch's status to monitor
     * </p>
     * 
     * @see BatchStatus
     */
    @Column(name = "batch_status", nullable = false, insertable = true, updatable = true)
    @Enumerated(EnumType.ORDINAL)
    BatchStatus batchStatus;

    /**
     * <p>
     * The time of the first consumed job (of the job type) is recorded as a start
     * point to know
     * that the batch is waited too long to be submitted to the node
     * </p>
     */
    @Column(name = "first_consumed_job_time", nullable = false, insertable = true, updatable = true)
    Instant consumedJobTime;
}
