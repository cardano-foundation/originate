package org.cardanofoundation.metabus.service;

import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.crypto.bip32.HdKeyPair;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.metadata.Metadata;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadata;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.transaction.spec.TransactionOutput;
import com.bloxbean.cardano.yaci.helper.model.TxResult;
import org.cardanofoundation.metabus.common.entities.UnconfirmedTxJPA;
import org.cardanofoundation.metabus.common.offchain.Job;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TransactionService {
    Transaction buildTransaction(String senderPaymentAddress, List<Utxo> txInList, TransactionOutput txOut, Metadata metadata);
    Mono<TxResult> submitTransaction(Transaction transaction) throws CborSerializationException;
    Transaction signTransaction(HdKeyPair hdKeyPair, Transaction transaction);
    UnconfirmedTxJPA saveUnconfirmedTx(List<Job> jobs, CBORMetadata metadata, String transactionHash);
}
