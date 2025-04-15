package ru.kate.multiproject;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class AppConfig {
    private final Map<String, Object> config;

    public AppConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.yaml")) {
            if (input == null) {
                throw new RuntimeException("Файл application.yaml не найден.");
            }
            Yaml yaml = new Yaml();
            config = yaml.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка чтения application.yaml: " + e.getMessage(), e);
        }
    }

    public String getSolrUrl() {
        Map<String, String> solr = (Map<String, String>) config.get("solr");
        return solr.get("url");
    }

    public String getSolrCollection() {
        Map<String, String> solr = (Map<String, String>) config.get("solr");
        return solr.get("collection");
    }
}
