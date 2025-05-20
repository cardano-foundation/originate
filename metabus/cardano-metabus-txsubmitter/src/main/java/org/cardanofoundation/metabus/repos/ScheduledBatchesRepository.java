package org.cardanofoundation.metabus.repos;

import java.util.List;

import org.cardanofoundation.metabus.common.entities.ScheduledBatchesJPA;
import org.cardanofoundation.metabus.common.enums.BatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * <p>
 * ScheduledBatchesRepository class
 * </p>
 * <p>
 * The class acts like DAO class to query the data (scheduled_batches) from the database
 * </p>
 * 
 * @author (Sotatek) joey.dao
 * @version 0.01
 * @category Repository
 * @since 2023/06
 */
public interface ScheduledBatchesRepository extends JpaRepository<ScheduledBatchesJPA, Long> {

	/**
	 * <p>
	 * Find all scheduled batch by batch status
	 * </p>
	 * 
	 * @param batchStatus The batch status
	 * @return The scheduled batch list.
	 */
	List<ScheduledBatchesJPA> findAllByBatchStatus(final BatchStatus batchStatus);

	/**
	 * <p>
	 * Find scheduled batch by job type
	 * </p>
	 * 
	 * @param jobType The job type.
	 */
	List<ScheduledBatchesJPA> findByJobType(String jobType);
}
