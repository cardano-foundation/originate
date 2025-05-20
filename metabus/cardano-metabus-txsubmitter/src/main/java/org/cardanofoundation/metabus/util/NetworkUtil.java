package org.cardanofoundation.metabus.util;


import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.common.model.Networks;

public class NetworkUtil {
    public static Network getNetwork(String network){
        switch (network){
            case "mainnet":
                return Networks.mainnet();
            case "preprod":
                return Networks.preprod();
            case "testnet":
                return Networks.testnet();
            case "preview":
                return Networks.preview();
            default:
                throw new RuntimeException("Invalid network string");
        }
    }
}
