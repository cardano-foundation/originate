package org.cardanofoundation.metabus.unittest.services.impl;

import org.cardanofoundation.metabus.security.config.SecurityConfig;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@ImportAutoConfiguration(exclude = SecurityConfig.class)
public class TestConfig {

}
