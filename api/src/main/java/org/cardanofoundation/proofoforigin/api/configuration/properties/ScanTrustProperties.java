package org.cardanofoundation.proofoforigin.api.configuration.properties;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Configuration
@ConfigurationProperties(prefix = "scan-trust")
public class ScanTrustProperties {
    String domain;
    String scmDataAsync;
    /**
     * Uri of the upload SCM Data Sync Api
     */
    String scmDataSync;
    String scmTaskState;
    String uatToken;
    int repeatTimes;
    int repeatInterval;
    int minBackoff;
    int maxBackoff;
}
