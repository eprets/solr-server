package com.example.cli_csv.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.example.cli_csv.model.Book;
import com.example.cli_csv.solr.SolrUpload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class CsvProcessor {
    private final SolrUpload solrUploader;
    private final String mappingPath;

    public CsvProcessor(String solrUrl, String mappingPath) {
        this.solrUploader = new SolrUpload(solrUrl, mappingPath);
        this.mappingPath = mappingPath;
    }

    public void processCsv(String jsonPath) {
        try {
            System.out.println("Start JSON...");

            // Проверка доступности Solr и ядра
            if (!solrUploader.checkSolrAvailability()) {
                System.out.println("Solr is not available. Exiting.");
                return;
            }

            if (solrUploader.checkCoreAvailability()) {
                System.out.println("Core " + solrUploader.getCollection() + " not found. Creating core...");
                solrUploader.createCore();
            }

            File jsonFile = new File(jsonPath);
            if (!jsonFile.exists() || !jsonFile.isFile()) {
                System.out.println("Invalid JSON file path: " + jsonPath);
                return;
            }

            File mappingFile = new File(mappingPath);
            if (!mappingFile.exists() || !mappingFile.isFile()) {
                System.out.println("Invalid mapping file path: " + mappingPath);
                return;
            }

            JsonFactory jsonFactory = new JsonFactory();
            try (JsonParser jsonParser = jsonFactory.createParser(new FileInputStream(jsonFile))) {
                while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                    if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
                        try {
                            JsonNode bookJsonNode = parseJsonToNode(jsonParser);
                            solrUploader.uploadToSolr(List.of(bookJsonNode)); // Отправка данных в Solr
                        } catch (Exception e) {
                            System.out.println("Error uploading book to Solr: " + e.getMessage());
                        }
                    }
                }
            }catch (JsonParseException e) {
                System.out.println("Invalid JSON format: " + e.getMessage());
            }catch (IOException e) {
                System.out.println("Error reading JSON: " + e.getMessage());
            }catch (Exception e) {
                System.out.println("Unexpected error while processing JSON: " + e.getMessage());
            }

            System.out.println("JSON processing completed!");
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        } finally {
            System.out.println("Program finished.");
        }
    }

    private JsonNode parseJsonToNode(JsonParser jsonParser) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        jsonParser.setCodec(objectMapper);
        return objectMapper.readTree(jsonParser);
    }
}
