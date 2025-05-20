package org.cardanofoundation.metabus.service;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.api.model.ProtocolParams;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.yaci.core.protocol.chainsync.messages.Point;

import java.util.List;

public interface LocalNodeService {
    List<Utxo> queryUTXOs(Address address);
    ProtocolParams queryProtocolParam();
    Point queryChainPoint();
}
