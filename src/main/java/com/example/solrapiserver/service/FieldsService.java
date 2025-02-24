package com.example.solrapiserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class FieldsService {

    private final Map<String, String> fieldsMapping = new HashMap<>();

    @PostConstruct
    private void loadFields() throws IOException {
        Properties properties = new Properties();
        try (InputStream inputStream = new ClassPathResource("fields.properties").getInputStream()) {
            properties.load(inputStream);
            for (String key : properties.stringPropertyNames()) {
                fieldsMapping.put(key, properties.getProperty(key));
            }
        }
    }

    public Map<String, String> getFields() {
        return fieldsMapping;
    }

}
