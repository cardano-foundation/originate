package org.cardanofoundation.proofoforigin.api.utils;

import com.nimbusds.jose.shaded.gson.*;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class GsonUtil {

    public static final Gson GSON = new Gson();

    public static final Gson GSON_WITH_DATE = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (value, type, context) ->
                            new JsonPrimitive(value.format(DateTimeFormatter.ISO_LOCAL_DATE))
            )
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (value, type, context) ->
                            new JsonPrimitive(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            )
            .registerTypeAdapter(LocalDate.class,
                    (JsonDeserializer<LocalDate>) (jsonElement, type, context) ->
                            LocalDate.parse(
                                    jsonElement.getAsJsonPrimitive().getAsString(),
                                    DateTimeFormatter.ISO_LOCAL_DATE
                            )
            )
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (jsonElement, type, context) ->
                            LocalDateTime.parse(
                                    jsonElement.getAsJsonPrimitive().getAsString(),
                                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
                            )
            )
            .create();

}
