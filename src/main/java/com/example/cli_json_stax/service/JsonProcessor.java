package com.example.cli_json_stax.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.cli_json_stax.model.Book;
import com.example.cli_json_stax.solr.SolrUpload;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JsonProcessor {
    private final SolrUpload solrUploader;

    public JsonProcessor(String solrUrl, String mappingPath) {
        this.solrUploader = new SolrUpload(solrUrl, mappingPath);
    }

    public void processJson(String jsonPath) {
        try {
            System.out.println("Start JSON...");

            File jsonFile = new File(jsonPath);
            JsonFactory factory = new JsonFactory();
            JsonParser parser = factory.createParser(jsonFile);

            ObjectMapper objectMapper = new ObjectMapper();

            // Чтение JSON с использованием StAX
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IOException("Expected start of JSON array");
            }

            while (parser.nextToken() != JsonToken.END_ARRAY) {
                Book book = objectMapper.readValue(parser, Book.class);
                solrUploader.uploadToSolr(List.of(book));
            }

            System.out.println("The end good!");
        } catch (Exception e) {
            System.out.println("Error JSON: " + e.getMessage());
        }
    }
}
