package org.cardanofoundation.metabus.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JsonParser {

    @Autowired
    ObjectMapper objectMapper;

    public  <T> T parseJsonStringToObject(String jsonString, TypeReference<T> typeReference) {
        T object = null;
        try {
            object = objectMapper.readValue(jsonString, typeReference);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return object;
    }

}
