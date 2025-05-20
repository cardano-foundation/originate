package org.cardanofoundation.metabus.service;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.api.model.Utxo;
import org.cardanofoundation.metabus.common.entities.UnconfirmedTxJPA;

import java.util.List;
import java.util.Optional;

public interface UtxoService {
    /**
     * Get utxos that is unused by our backend application of a specified address.
     * Because there might be some utxos that we query from node that is already used by previous transaction but that
     * transaction is not on-chain yet.
     * @param address
     * @return List<Utxo>
     */
    List<Utxo> getUnusedUtxosSortByAmount(Address address);
    Optional<Utxo> getGreatestUtxo(List<Utxo> utxos);
    void saveUsedUtxo(List<Utxo> usedUtxo, Address address, UnconfirmedTxJPA unconfirmedTxJPA);
}
