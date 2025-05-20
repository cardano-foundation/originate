package org.cardanofoundation.metabus.configuration;

import java.time.Duration;
import java.time.Instant;

import org.cardanofoundation.metabus.task.TimeBasedBatchConsumptionTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * <p>
 * A task scheduler that schedules the specific task to execute.
 * Furthermore, The tasks that are defined in the scheduler will be execute
 * asynchronously or not (based on dev)
 * </p>
 * 
 * @author (Sotatek) joey.dao
 * @version 0.01
 * @category Configuration
 * @since 2023/06
 */
@Configuration
@EnableScheduling
@EnableAsync
@ComponentScan
public class TaskSchedulerConfiguration implements SchedulingConfigurer {

	/**
	 * The pool size of the task scheduler
	 */
	@Value("${spring.task.scheduling.pool.size:1}")
	private int poolSize;

	/**
	 * The thread group name of the task scheduler
	 */
	@Value("${spring.task.scheduling.pool.threadGroupName:txSubmitterPool}")
	private String threadGroupName;

	/**
	 * The thread prefix name of the task scheduler
	 */
	@Value("${spring.task.scheduling.pool.threadPrefixName:txSubmitterThread-}")
	private String threadPrefixName;

	/**
	 * Using boundary time as a period time of the TimeBasedBatchConsumption Task
	 */
	@Value("${cardano-metabus-txsubmitter.consumeBaseOnTimeInterval:60000}")
	private long periodTime;

	/**
	 * <p>
	 * Create TimeBasedBatchConsumptionTask Bean
	 * </p>
	 *
	 * @return TimeBasedBatchConsumptionTask instance
	 */
	@Bean
	public TimeBasedBatchConsumptionTask createTimeBasedBatchConsumptionTaskBean() {
		return new TimeBasedBatchConsumptionTask();
	}

	/**
	 * <p>
	 * Create ThreadPoolTaskScheduler Bean
	 * </p>
	 *
	 * @return ThreadPoolTaskScheduler instance
	 */
	@Bean
	public ThreadPoolTaskScheduler poolScheduler() {
		final ThreadPoolTaskScheduler poolTaskScheduler = new ThreadPoolTaskScheduler();
		poolTaskScheduler.setPoolSize(poolSize);
		poolTaskScheduler.setThreadGroupName(threadGroupName);
		poolTaskScheduler.setThreadNamePrefix(threadPrefixName);
		poolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
		poolTaskScheduler.initialize();
		poolTaskScheduler.scheduleWithFixedDelay(createTimeBasedBatchConsumptionTaskBean(),
				Instant.now().plusMillis(periodTime),
				Duration.ofMillis(periodTime));

		return poolTaskScheduler;
	}

	@Override
	public void configureTasks(final ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setScheduler(poolScheduler());
	}
}
