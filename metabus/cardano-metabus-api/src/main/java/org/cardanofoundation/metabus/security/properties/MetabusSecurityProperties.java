package org.cardanofoundation.metabus.security.properties;

import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "metabus.security")
public class MetabusSecurityProperties {
    List<Authorization> endpointAuthorizations;
    List<JobTypeAuthorization> jobTypeAuthorizations;

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Authorization {
        String urlPattern;
        String method;
        List<String> roles;
    }

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class JobTypeAuthorization {
        List<String> roles;
        List<String> allowedJobTypes;
    }
}
