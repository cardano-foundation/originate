package org.cardanofoundation.metabus.repos;

import org.cardanofoundation.metabus.common.entities.JobJPA;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * <p>
 * Job Entity Repository Class.
 * </p>
 * 
 * @author (Sotatek) joey.dao
 * @version 0.01
 * @category Repository
 * @since 2023/08
 */
public interface JobRepository extends JpaRepository<JobJPA, Long> {

}
