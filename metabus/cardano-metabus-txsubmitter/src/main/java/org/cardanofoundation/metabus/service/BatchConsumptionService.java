package org.cardanofoundation.metabus.service;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.cardanofoundation.metabus.common.offchain.Job;

import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * <p>
 * The Batch Consumption Service.
 * </p>
 * <p>
 * This interface will indicated
 * what methods is able to use to consume the job
 * evaluate the whole batch and submit to node.
 * </p>
 * 
 * @author (Sotatek) joey.dao
 * @version 0.01
 * @category Service-interface
 * @since 2023/06
 */
public interface BatchConsumptionService {

	/**
	 * <p>
	 * This method will consume the job and evaluate the whole batch.
	 * If the submission time of the batch is excess far from a configured time.
	 * The batch will be consumed to submit to the server.
	 * </p>
	 * 
	 * @throws CborSerializationException
	 * @throws InterruptedException
	 * @throws JsonProcessingException
	 */
	void consumeBasedOnTime() throws CborSerializationException, InterruptedException, JsonProcessingException, CborException, AddressExcepion;

	/**
	 * <p>
	 * This method will consume the job and evaluate the whole batch.
	 * If the serialized transaction's size is excess far from txMaxSize.
	 * The batch will be consumed to submit to the server.
	 * </p>
	 * 
	 * @param consumerRecord consumer record from kafka
	 * @throws CborSerializationException
	 * @throws InterruptedException
	 * @throws JsonProcessingException
	 */
	void consumeBasedOnTxMaxSize(final ConsumerRecord<String, Job> consumerRecord)
            throws CborSerializationException, JsonProcessingException, InterruptedException, CborException, AddressExcepion;
}
