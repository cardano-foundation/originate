package org.cardanofoundation.metabus.service;

import java.util.List;
import java.util.Map;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.crypto.bip32.HdKeyPair;

/**
 * @Modified (sotatek) joey.dao
 * @since 2023/07
 */
public interface WalletService {    
    /**
     * <p>
     * Get the Hd Key pair lists that is based on mnemonic and How many children you want to get 
     * </p>
     * 
     * @param mnemonic The mnemonic string 
     * @param numberOfChild The number of the children
     * @return HdKeyPair lists
     */
    List<HdKeyPair> getFirstNChildKeyPairs(final String mnemonic, final int numberOfChild);

    /**
     * <p>
     * Get the addresses that is belonging to list of HD Key Pair from an network
     * </p>
     * 
     * @param hdKeyPairs The HdKeyPair List
     * @param network The client network
     * @return The map of address and hdKeyPair
     */
    Map<Address, HdKeyPair> getAddressesWithKeyPair(final List<HdKeyPair> hdKeyPairs, final Network network);
}
