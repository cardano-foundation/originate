package org.cardanofoundation.metabus.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * <p>
 * JPA Persistent Config 
 * </p>
 * 
 * @author (Sotatek) joey.dao
 * @version 0.01
 * @category Configuration
 * @since 2023/08
 */
@Configuration
@EnableTransactionManagement
@EnableJpaAuditing
public class PersistenceConfig {
}
