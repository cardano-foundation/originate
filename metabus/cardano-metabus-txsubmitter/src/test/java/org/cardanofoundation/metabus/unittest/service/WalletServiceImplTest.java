package org.cardanofoundation.metabus.unittest.service;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.cardanofoundation.metabus.configuration.TxSubmitterProperties;
import org.cardanofoundation.metabus.service.impl.WalletServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.crypto.CryptoException;
import com.bloxbean.cardano.client.crypto.bip32.HdKeyPair;
import com.bloxbean.cardano.client.crypto.cip1852.CIP1852;
import com.bloxbean.cardano.client.crypto.cip1852.DerivationPath;

/**
 * @Modified (sotatek) joey.dao
 * @since 2023/07
 */
@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private TxSubmitterProperties txSubmitterProperties;

    @InjectMocks
    @Spy
    private WalletServiceImpl walletService;

    public static String mnemonic = "kit color frog trick speak employ suit sort bomb goddess jewel primary spoil fade person useless measure manage warfare reduce few scrub beyond era";

    /**
     * <p>
     * Description:
     * Test for valid inputs:
     * Test the method with valid inputs for mnemonic and numberOfChild
     * and verify that the returned list of HdKeyPair objects is of the expected
     * size
     * and contains the expected values.
     * </p>
     */
    @Test
    public void testGetFirstNChildKeyPairs() {
        final int numberOfChild = 2;
        final List<HdKeyPair> expectedHdKeyPairs = Arrays.asList(
                getHdKeyPair(mnemonic, 0),
                getHdKeyPair(mnemonic, 1));
        final List<HdKeyPair> actualHdKeyPairs = walletService.getFirstNChildKeyPairs(mnemonic, numberOfChild);

        assertEquals(expectedHdKeyPairs.size(), actualHdKeyPairs.size());
        assertTrue(Arrays.equals(expectedHdKeyPairs.get(0).getPublicKey().getBytes(),
                actualHdKeyPairs.get(0).getPublicKey().getBytes()));
        assertTrue(Arrays.equals(expectedHdKeyPairs.get(1).getPublicKey().getBytes(),
                actualHdKeyPairs.get(1).getPublicKey().getBytes()));
    }

    /**
     * <p>
     * Description:
     * Test for invalid mnemonic:
     * Test the method with an invalid mnemonic input and verify that an appropriate
     * exception is thrown.
     * </p>
     * 
     */
    @Test
    public void testGetFirstNChildKeyPairsInvalidMnemonic() {
        final String invalidMnemonic = "invalid mnemonic";
        final int numberOfChild = 5;
        assertThrows(CryptoException.class, () -> walletService.getFirstNChildKeyPairs(invalidMnemonic, numberOfChild));
    }

    /**
     * <p>
     * Description:
     * Test for negative numberOfChild:
     * Test the method with a negative value for numberOfChild and verify that list
     * returns empty.
     * </p>
     * 
     */
    @Test
    public void testGetFirstNChildKeyPairsNegativeNumberOfChild() {
        final int negativeNumberOfChild = -5;
        assertEquals(0, walletService.getFirstNChildKeyPairs(mnemonic, negativeNumberOfChild).size());
    }

    /**
     * <p>
     * Description:
     * Test for zero numberOfChild:
     * Test the method with a value of zero for numberOfChild
     * and verify that an empty list is returned.
     * </p>
     */
    @Test
    void testGetFirstNChildKeyPairsZeroNumberOfChild() {
        final int zeroNumberOfChild = 0;
        assertEquals(0, walletService.getFirstNChildKeyPairs(mnemonic, zeroNumberOfChild).size());
    }

    /**
     * <p>
     * Description:
     * Test for valid inputs:
     * Test the method with valid inputs for hdKeyPairs and network and verify
     * that the returned map contains the expected keys and values.
     * </p>
     * 
     */
    @Test
    void testGetAddressesWithKeyPairValidInputs() {
        final List<HdKeyPair> hdKeyPairs = List.of(getHdKeyPair(mnemonic, 0), getHdKeyPair(mnemonic, 1));
        final Network network = Networks.preprod();
        final String expectedAddress = "addr_test1vqxnp3khzm7kcj9t23hskehat7428ghsenk0pfew4rqy5vq24rmml";
        final String expectedAddress2 = "addr_test1vp7t5p2wprpevnf8c2z56fyu4dulveyu83udsfggc2v37hsd6lg9m";
        final Map<Address, HdKeyPair> result = walletService.getAddressesWithKeyPair(hdKeyPairs, network);

        assertEquals(hdKeyPairs.size(), result.size());

        final boolean isAddressEqual = result.keySet().stream()
                .anyMatch((address) -> {
                    if (expectedAddress.equals(address.getAddress())) {
                        return true;
                    } else if (expectedAddress2.equals(address.getAddress())) {
                        return true;
                    } else {
                        return false;
                    }
                });
        final boolean isHdKeyPairEqual = hdKeyPairs.stream().allMatch(hdKeyPair -> result.containsValue(hdKeyPair));

        assertTrue(isHdKeyPairEqual);
        assertTrue(isAddressEqual);
    }

    /**
     * <p>
     * Description:
     * Test for empty hdKeyPairs:
     * Test the method with an empty list for hdKeyPairs
     * and verify that an empty map is returned.
     * </p>
     */
    @Test
    void testGetAddressesWithKeyPairEmptyHdKeyPairs() {
        final List<HdKeyPair> emptyHdKeyPairs = List.of();
        final Network network = Networks.preprod();

        final Map<Address, HdKeyPair> result = walletService.getAddressesWithKeyPair(emptyHdKeyPairs, network);

        assertEquals(0, result.size());
    }

    /**
     * <p>
     * Description:
     * Test for null hdKeyPairs:
     * Test the method with a null value for hdKeyPairs
     * and verify that an appropriate exception is thrown.
     * </p>
     * 
     */
    @Test
    void testGetAddressesWithKeyPairNullHdKeyPairs() {
        List<HdKeyPair> nullHdKeyPairs = null;
        Network network = Networks.preprod();

        assertThrows(NullPointerException.class, () -> walletService.getAddressesWithKeyPair(nullHdKeyPairs, network));
    }

    /**
     * <p>
     * Get HdKeyPair base on index and mnemonic
     * </p>
     * 
     * @param mnemonic The mnemonic
     * @param index    The index
     * @return new HdKeyPair
     */
    private HdKeyPair getHdKeyPair(final String mnemonic, final int index) {
        return new CIP1852().getKeyPairFromMnemonic(mnemonic,
                DerivationPath.createExternalAddressDerivationPath(index));
    }

}
