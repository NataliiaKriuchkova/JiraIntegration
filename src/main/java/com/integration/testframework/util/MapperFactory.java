package com.integration.testframework.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * Central factory for Jackson {@link ObjectMapper} instances so every layer
 * (clients, transformers, tests) shares identical (de)serialization behavior.
 */
public final class MapperFactory {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private static final XmlMapper XML_MAPPER = (XmlMapper) new XmlMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private MapperFactory() {
    }

    public static ObjectMapper json() {
        return JSON_MAPPER;
    }

    public static XmlMapper xml() {
        return XML_MAPPER;
    }
}
