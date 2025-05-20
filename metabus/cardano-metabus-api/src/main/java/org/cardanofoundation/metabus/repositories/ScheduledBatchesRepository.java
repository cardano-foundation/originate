package org.cardanofoundation.metabus.repositories;

import java.util.List;

import org.cardanofoundation.metabus.common.entities.ScheduledBatchesJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * <p>
 * ScheduledBatchesRepository class
 * </p>
 * <p>
 * The class acts like DAO class to query the data (scheduled_batches) from the
 * database
 * </p>
 * 
 * @author (Sotatek) joey.dao
 * @version 0.01
 * @category Repository
 * @since 2023/08
 */
public interface ScheduledBatchesRepository extends JpaRepository<ScheduledBatchesJPA, Long> {

    /**
     * <p>
     * Find scheduled batch by job type
     * </p>
     * 
     * @param jobType The job type.
     * @return The target scheduled batch
     */
    List<ScheduledBatchesJPA> findByJobType(final String jobType);

    /**
     * <p>
     * Insert the schedule batch to database.
     * This query is to prevent the conflict between multiple transactions to do
     * the insert/update to database.
     * </p>
     * 
     * @param batchInfo The scheduledBatch info
     * @return the number of the records that are inserted
     */
    @Modifying
    @Query(value = "INSERT INTO scheduled_batches(job_type, batch_status, first_consumed_job_time)" 
                   + " VALUES (:#{#insertBatch.jobType}, :#{#insertBatch.batchStatus.getValue()}, :#{#insertBatch.consumedJobTime})"
                   + " ON CONFLICT (job_type) DO NOTHING", nativeQuery = true)
    int insertScheduledBatchesDoNothingOnConflict(@Param("insertBatch") final ScheduledBatchesJPA insertBatch);

    /**
     * <p>
     * Update the scheduled batch to database.
     * This query is to prevent the conflict in updating between multiple
     * transactions
     * </p>
     * 
     * @param batchInfo The scheduledBatch info
     * @return the number of the records that are updated
     */
    @Modifying
    @Query(value = "UPDATE ScheduledBatchesJPA batch" 
                 + " SET batch.batchStatus = :#{#updateBatch.batchStatus}, batch.consumedJobTime = :#{#updateBatch.consumedJobTime}"
                 + " WHERE batch.jobType = :#{#updateBatch.jobType} AND batch.batchStatus = BatchStatus.NONE")
    int updateScheduledBatchesByJobType(@Param("updateBatch") final ScheduledBatchesJPA updateBatch);
}
