package org.cardanofoundation.metabus.task;

import org.cardanofoundation.metabus.service.BatchConsumptionService;
import org.springframework.beans.factory.annotation.Autowired;

import com.bloxbean.cardano.client.exception.CborSerializationException;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * TimeBasedBatchConsumptionTask class
 * </p>
 * <p>
 * The Task is run on background to check the jobs 
 * that were not submitted to the node
 * </p>
 * 
 * @author (Sotatek) joey.dao
 * @version 0.01
 * @category Background-task
 * @since 2023/06
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = false)
public class TimeBasedBatchConsumptionTask implements Runnable {

	// Service
	@Autowired
	BatchConsumptionService batchConsumptionService;

	@Override
	public void run() {
		try {
			// Make only one thread can use a instance of BatchConsumptionService
			// To ensure the consuming phase (based on time or based on tx max size) is
			// concurrently processed
			synchronized (batchConsumptionService) {
				batchConsumptionService.consumeBasedOnTime();
			}
		} catch (CborSerializationException e) {
			log.error(">>> Transaction serialization error: {}", e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
	}
}
