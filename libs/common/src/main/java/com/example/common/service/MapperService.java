package com.example.common.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class MapperService {
    private final Map<String, String> fieldsMapping = new LinkedHashMap<>();

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
            throw new RuntimeException("Ошибка загрузки маппинга: " + e.getMessage());
        }
    }

    public Map<String, String> getFieldsMapping() {
        return fieldsMapping;
    }

    public String getSolrFieldName(String dbFieldName) {
        return fieldsMapping.getOrDefault(dbFieldName, dbFieldName);
    }
}
