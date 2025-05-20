package org.cardanofoundation.metabus.service;

/**
 * <p>
 * The Queueing Service Class
 * </p>
 *
 * @author (Sotatek) joey.dao
 * @version 0.01
 * @category Service
 * @since 2023/08
 */
public interface QueueingService {
	/**
	 * <p>
	 * Send message to the Topic.
	 * </p>
	 * 
	 * @param <T> The POJO class type of the message
	 * @param message The content message.
	 * @param topic The target topic
	 * @param key The target key.
	 */
	<T> void sendMessage(final T message, final String topic, final String key);
}
