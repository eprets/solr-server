package com.example.cli_csv.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MapperService {
    private final Map<String, String> fieldsMapping = new HashMap<>();

    public MapperService(String mappingPath) {
        loadMapping(mappingPath);
    }

    private void loadMapping(String mappingPath) {
        Properties properties = new Properties();
        try (FileInputStream inputStream = new FileInputStream(mappingPath)) {
            properties.load(inputStream);
            for (String key : properties.stringPropertyNames()) {
                fieldsMapping.put(key, properties.getProperty(key));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error mapping: " + e.getMessage());
        }
    }

    public String getSolrFieldName(String jsonFieldName) {
        return fieldsMapping.getOrDefault(jsonFieldName, jsonFieldName);
    }
}

