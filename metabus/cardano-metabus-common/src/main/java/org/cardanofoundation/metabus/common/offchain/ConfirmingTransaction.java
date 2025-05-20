package org.cardanofoundation.metabus.common.offchain;

import java.time.Instant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * The Confirming Transaction POJO Class.
 * </p>
 *
 * @author (Sotatek) joey.dao
 * @version 0.01
 * @category Common-entity
 * @since 2023/08
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConfirmingTransaction {
	/**
	 * The txHash of the transaction
	 */
	String txHash;

	/**
	 * The every jobs that was contained in the transaction
	 */
	JobBatch jobBatch;

	/**
	 * The submitted date of the transaction.
	 */
	Instant submittedDate;

	/**
	 * Retry Counts for unexpected error
	 */
	@Setter(AccessLevel.NONE)
	Long retryCountsForUnexpectedError;

	/**
	 * <p>
	 * Subtract the retry count by one unit.
	 * </p>
	 */
	public void subtractTheRetryCountByOne() {
		this.retryCountsForUnexpectedError -= 1;
	}
}
