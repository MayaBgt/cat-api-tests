package com.mb.api.tests.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;

public class TestDataLoader {

    private static final ObjectMapper mapper = new ObjectMapper();
    public static JsonNode loadJson(String fileName) throws IOException {
        InputStream inputStream = TestDataLoader.class.getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new IOException("File not found in resources: " + fileName);
        }
        return mapper.readTree(inputStream);
    }
}
