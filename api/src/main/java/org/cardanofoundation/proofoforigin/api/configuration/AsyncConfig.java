package org.cardanofoundation.proofoforigin.api.configuration;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * <p>
 * An TaskExecutor to handle the Async task.
 * </p>
 * 
 * @author (Sotatek) joey.dao
 * @category Configuration
 * @since 2023/07
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Value("${spring.task.execution.pool.core-size}")
    private int coreSize;

    @Value("${spring.task.execution.pool.max-size}")
    private int poolMaxSize;

    @Value("${spring.task.execution.thread-name-prefix}")
    private String threadNamePrefix;

    /**
     * <p>
     * Creating TaskExecutor Bean
     * </p>
     * 
     * @return TaskExecutor instance
     */
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(poolMaxSize);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.initialize();

        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return taskExecutor();
    }
}
