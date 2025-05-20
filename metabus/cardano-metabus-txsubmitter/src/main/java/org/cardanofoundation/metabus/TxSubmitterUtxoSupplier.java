package org.cardanofoundation.metabus;

import com.bloxbean.cardano.client.api.UtxoSupplier;
import com.bloxbean.cardano.client.api.common.OrderEnum;
import com.bloxbean.cardano.client.api.model.Utxo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TxSubmitterUtxoSupplier implements UtxoSupplier {

    private List<Utxo> utxos;

    public TxSubmitterUtxoSupplier(List<Utxo> utxos) {
        this.utxos = utxos;
    }

    @Override
    public List<Utxo> getPage(String address, Integer pageSize, Integer page, OrderEnum order) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Invalid page size: " + pageSize);
        } else if (page < 0) {
            throw new IllegalArgumentException("Invalid page number: " + page);
        } else {
            int fromIndex = page * pageSize;
            return this.utxos.size() <= fromIndex ? Collections.emptyList() : this.utxos.subList(fromIndex, Math.min(fromIndex + pageSize, this.utxos.size()));
        }
    }

    @Override
    public List<Utxo> getAll(String address) {
        return this.utxos;
    }

    @Override
    public Optional<Utxo> getTxOutput(String s, int i) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
