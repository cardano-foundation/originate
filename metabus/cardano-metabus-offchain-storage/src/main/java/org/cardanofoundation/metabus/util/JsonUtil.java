package org.cardanofoundation.metabus.util;

import org.erdtman.jcs.JsonCanonicalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);

    public String canonicalizeFromText(String jsonText) throws IOException {
        JsonCanonicalizer jc = new JsonCanonicalizer(jsonText);
        String canonicalized = jc.getEncodedString();
        LOGGER.debug("Canonicalized: " + canonicalized);
        return canonicalized;
    }
}
