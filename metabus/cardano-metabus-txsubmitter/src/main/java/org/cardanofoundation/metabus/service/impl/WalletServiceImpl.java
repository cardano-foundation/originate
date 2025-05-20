package org.cardanofoundation.metabus.service.impl;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.cardanofoundation.metabus.configuration.TxSubmitterProperties;
import org.cardanofoundation.metabus.service.WalletService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.address.AddressProvider;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.crypto.bip32.HdKeyPair;
import com.bloxbean.cardano.client.crypto.cip1852.CIP1852;
import com.bloxbean.cardano.client.crypto.cip1852.DerivationPath;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * @Modified (sotatek) joey.dao
 * @since 2023/07
 */
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@EnableConfigurationProperties(value = { TxSubmitterProperties.class })
public class WalletServiceImpl implements WalletService {

    @Override
    public List<HdKeyPair> getFirstNChildKeyPairs(final String mnemonic, final int numberOfChild) {
        final List<HdKeyPair> childHdKeyPairs = new LinkedList<>();
        childHdKeyPairs.addAll(IntStream.range(0, numberOfChild).boxed()
                .map(childIndex -> new CIP1852().getKeyPairFromMnemonic(mnemonic,
                        DerivationPath.createExternalAddressDerivationPath(childIndex)))
                .collect(Collectors.toList()));

        return childHdKeyPairs;
    }

    @Override
    public Map<Address, HdKeyPair> getAddressesWithKeyPair(final List<HdKeyPair> hdKeyPairs, final Network network) {
        final Map<Address, HdKeyPair> addressesMap = new LinkedHashMap<>();

        hdKeyPairs.stream().forEach(
                keyPair -> addressesMap.put(AddressProvider.getEntAddress(keyPair.getPublicKey(), network), keyPair));

        return addressesMap;
    }
}
