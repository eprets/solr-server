package com.example.libs.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
public class MapperService {

    private final Map<String, String> fieldsMapping = new HashMap<>();
    private final Map<String, String> reverseFieldsMapping = new HashMap<>();
    private final Map<String, String> fieldsTypes = new HashMap<>();

    private void loadFields(String mappingPath) throws IOException {
        Properties properties = new Properties();
        try (InputStream inputStream = new ClassPathResource(mappingPath).getInputStream()) {
            properties.load(inputStream);
            for (String key : properties.stringPropertyNames()) {
                String solrField = properties.getProperty(key);
                fieldsMapping.put(key, solrField);
                reverseFieldsMapping.put(solrField, key);
                String fieldType = getFieldType(solrField);
                fieldsTypes.put(key, fieldType);
            }
        }
    }

    public String getSolrFieldName(String jsonFieldName) {
        return fieldsMapping.getOrDefault(jsonFieldName, jsonFieldName);
    }

    public String getJsonFieldName(String solrFieldName) {
        return reverseFieldsMapping.getOrDefault(solrFieldName, solrFieldName);
    }

    public Map<String, String> getAllFields() {
        return fieldsMapping;
    }

    public String getSolrFieldType(String jsonFieldName) {
        return fieldsTypes.getOrDefault(jsonFieldName, "Unknown");
    }

    private String getFieldType(String solrFieldName) {
        if (solrFieldName.endsWith("_ss")) {
            return "String (Sorted)";
        } else if (solrFieldName.endsWith("_s")) {
            return "String";
        } else if (solrFieldName.endsWith("_f")) {
            return "Float";
        } else if (solrFieldName.endsWith("_b")) {
            return "Boolean";
        } else if (solrFieldName.endsWith("_dt")) {
            return "Date";
        } else {
            return "Unknown";
        }
    }
}
