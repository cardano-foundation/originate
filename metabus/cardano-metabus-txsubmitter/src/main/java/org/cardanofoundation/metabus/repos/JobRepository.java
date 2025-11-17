package org.cardanofoundation.metabus.repos;

import org.cardanofoundation.metabus.common.entities.JobJPA;
import org.cardanofoundation.metabus.common.enums.JobState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<JobJPA, Long> {
    List<JobJPA> findAllByUnconfirmedTxId(Long unconfirmedTxId);

    List<JobJPA> findAllByIdIn(List<Long> ids);

    /**
     * <p>
     * Find all the jobs that was not submitted yet and with indicated type
     * </p>
     * 
     * @param state The state of the job
     * @param type  The type of the job
     * @return The list of the jobs
     */
    List<JobJPA> findAllByStateAndType(final JobState state, final String type);

        /**
     * <p>
     * Find all the jobs that was not submitted yet and with indicated type
     * </p>
     * 
     * @param state The state of the job
     * @param type  The type of the job
     * @return List of jobs with 20-job limit 
     */

    List<JobJPA> findTop20ByStateAndType(final JobState state, final String type);
}
