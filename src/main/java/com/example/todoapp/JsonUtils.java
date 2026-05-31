package com.example.todoapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Serialisation et désérialisation JSON .
 */
public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Constructeur privé.
     */
    private JsonUtils() {
    }

    /**
     * Sérialiser un objet Java en JSON.
     *
     * @param o object to serialize.
     * @return JSON representation of the object.
     */
    public static String serialize(Object o) throws JsonProcessingException {
        return MAPPER.writeValueAsString(o);
    }

    /**
     * désérialiser un JSON en objet Java.
     * @param json  
     * @param clazz 
     * @param <T>   
     * @return      
     */
    public static <T> T deserialize(String json, Class<T> clazz) throws IOException {
        return MAPPER.readValue(json, clazz);
    }
}
