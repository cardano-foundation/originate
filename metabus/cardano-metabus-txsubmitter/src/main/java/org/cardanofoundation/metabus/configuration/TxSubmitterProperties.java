package org.cardanofoundation.metabus.configuration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigInteger;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "cardano-metabus-txsubmitter")
public class TxSubmitterProperties {

    @NotNull
    Connection connection;
    @NotNull
    String network;
    @NotNull
    Wallet wallet;
    @NotNull
    BigInteger metadatumLabel;
    @NotNull
    String metadataVersion;
    @NotNull
    BigInteger txOut;
    @NotNull
    BigInteger batchConsumptionBoundaryTime;
    @NotNull
    Integer numberOfDerivedAddresses;
    @NotNull
    BigInteger waitingTimeToReConsume;
    @NotNull
    Integer numberOfRetryPullingUtxo;
    @NotNull
    String offchainBucket;
    @NotNull
    Long txSubmissionRetryDelayDuration;

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Connection {
        @NotNull
        Socket socket;
    }

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Socket {
        @NotBlank
        String path;
    }

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Wallet {
        @NotBlank
        String mnemonic;
    }

}
