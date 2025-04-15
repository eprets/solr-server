package ru.kate.multiproject.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.kate.multiproject.solr.SolrJsonUpload;
import ru.kate.multiproject.solr.SolrUpload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonProcessor {
    private final SolrJsonUpload solrUploader;
    private final String mappingPath;
    private final String solrUrl;
    private final String collection;
    private static final int BATCH_SIZE = 12;

    public JsonProcessor(String solrUrl, String collection, String mappingPath) {
        this.solrUploader = new SolrJsonUpload(solrUrl, collection, mappingPath);
        this.mappingPath = mappingPath;
        this.solrUrl = solrUrl;
        this.collection = collection;
    }

    public void validateParams(String jsonPath) {
        validateFile(jsonPath, "JSON");
        validateFile(mappingPath, "mapping");

        SolrUpload helper = new SolrUpload(solrUrl, collection);
        if (!helper.checkSolrAvailability()) {
            throw new RuntimeException("Solr is not available. Exiting.");
        }

        if (helper.checkCoreAvailability()) {
            System.out.println("Core " + solrUploader.getCollection() + " not found. Trying create core...");
            helper.createCore();
        }
    }

    public void processJson(String jsonPath) {
        int counter = 0;
        try {
            System.out.println("Start JSON...");
            File jsonFile = new File(jsonPath);

            List<JsonNode> booksBatch = new ArrayList<>();
            JsonFactory jsonFactory = new JsonFactory();

            try (JsonParser jsonParser = jsonFactory.createParser(new FileInputStream(jsonFile))) {
                if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
                    System.out.println("Invalid JSON: expected array of objects.");
                    return;
                }

                while (jsonParser.nextToken() == JsonToken.START_OBJECT) {
                    try {
                        JsonNode bookJsonNode = parseJsonToNode(jsonParser);
                        booksBatch.add(bookJsonNode);
                    } catch (Exception e) {
                        System.out.println("Error parsing JSON object: " + e.getMessage());
                    }

                    if (booksBatch.size() >= BATCH_SIZE) {
                        try {
                            solrUploader.uploadToSolr(booksBatch);
                            counter++;
                            System.out.println("Batch processed: " + (counter * BATCH_SIZE));
                        } catch (Exception e) {
                            System.out.println("Error uploading batch to Solr: " + e.getMessage());
                        } finally {
                            booksBatch.clear();
                        }
                    }
                }

                if (!booksBatch.isEmpty()) {
                    try {
                        solrUploader.uploadToSolr(booksBatch);
                        System.out.println("Final batch processed: " + booksBatch.size());
                    } catch (Exception e) {
                        System.out.println("Error uploading final batch to Solr: " + e.getMessage());
                    }
                }

            } catch (JsonParseException e) {
                System.out.println("Invalid JSON format: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("Error reading JSON: " + e.getMessage());
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