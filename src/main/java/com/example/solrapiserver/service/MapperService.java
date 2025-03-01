package com.example.solrapiserver.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class MapperService {

    private final Map<String, String> fieldsMapping = new HashMap<>();
    private final Map<String, String> fieldsTypes = new HashMap<>();

    @PostConstruct
    private void loadFields() throws IOException {
        Properties properties = new Properties();
        try (InputStream inputStream = new ClassPathResource("fields.properties").getInputStream()) {
            properties.load(inputStream);
            for (String key : properties.stringPropertyNames()) {
                fieldsMapping.put(key, properties.getProperty(key));
                // Поскольку у нас есть только имена полей, мы можем добавить соответствующие типы (например, _ss, _f, _dt и т. д.)
                String fieldType = getFieldType(properties.getProperty(key));
                fieldsTypes.put(key, fieldType);
            }
        }
    }

    public String getSolrFieldName(String jsonFieldName) {
        return fieldsMapping.getOrDefault(jsonFieldName, jsonFieldName);
    }

    // Метод для получения всех полей
    public Map<String, String> getAllFields() {
        return fieldsMapping;
    }

    // Метод для получения типа поля Solr для конкретного JSON поля
    public String getSolrFieldType(String jsonFieldName) {
        return fieldsTypes.getOrDefault(jsonFieldName, "Unknown");
    }

    // Логика для определения типа поля на основе имени поля Solr (по суффиксу)
    private String getFieldType(String solrFieldName) {
        if (solrFieldName.endsWith("_ss")) {
            return "String (Sorted)";
        }
        else if (solrFieldName.endsWith("_s")) {
            return "String";
        }
        else if (solrFieldName.endsWith("_f")) {
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
