package com.example.cli_json_stax.service;

import com.example.cli_json_stax.solr.SolrUpload;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonProcessor {
    private final SolrUpload solrUploader;
    private final String mappingPath;
    private static final int BATCH_SIZE = 50; // Размер пакета для загрузки в Solr

    public JsonProcessor(String solrUrl, String mappingPath) {
        this.solrUploader = new SolrUpload(solrUrl, mappingPath);
        this.mappingPath = mappingPath;
    }

    public void validateParams(String jsonPath) {
        // Валидация файлов перед обработкой
        validateFile(jsonPath, "JSON");
        validateFile(mappingPath, "mapping");

        // Проверка доступности Solr и ядра
        if (!solrUploader.checkSolrAvailability()) {
            throw new RuntimeException("Solr is not available. Exiting.");
        }

        if (solrUploader.checkCoreAvailability()) {
            System.out.println("Core " + solrUploader.getCollection() + " not found. Trying create core...");
            solrUploader.createCore();
        }
    }

    public void processJson(String jsonPath) {
        Integer counter = 0;
        try {
            System.out.println("Start JSON...");
            File jsonFile = new File(jsonPath);

            List<JsonNode> booksBatch = new ArrayList<>();
            JsonFactory jsonFactory = new JsonFactory();
            try (JsonParser jsonParser = jsonFactory.createParser(new FileInputStream(jsonFile))) {
                while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                    if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
                        try {
                            JsonNode bookJsonNode = parseJsonToNode(jsonParser);
                            booksBatch.add(bookJsonNode);
                            if (booksBatch.size() >= BATCH_SIZE) {
                                solrUploader.uploadToSolr(booksBatch);
                                booksBatch.clear();
                                counter++;
                                System.out.println("Document processed: " + counter * BATCH_SIZE);
                            }
                        } catch (Exception e) {
                            System.out.println("Error uploading book to Solr: " + e.getMessage());
                        }
                    }
                }
                if (!booksBatch.isEmpty()) {
                    solrUploader.uploadToSolr(booksBatch);
                }
            } catch (JsonParseException e) {
                System.out.println("Invalid JSON format: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("Error reading JSON: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected error while processing JSON: " + e.getMessage());
            }

            System.out.println("JSON processing completed!");
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        } finally {
            System.out.println("Program finished.");
        }
    }

    private void validateFile(String filePath, String fileType) throws IllegalArgumentException {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Invalid " + fileType + " file path: " + filePath);
        }
    }

    private JsonNode parseJsonToNode(JsonParser jsonParser) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        jsonParser.setCodec(objectMapper);
        return objectMapper.readTree(jsonParser);
    }
}