package com.example.cli_json.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.cli_json.model.Book;
import com.example.cli_json.solr.SolrUpload;

import java.io.File;
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
            ObjectMapper objectMapper = new ObjectMapper();

            // Чтение JSON из файла
            List<Book> books = objectMapper.readValue(jsonFile, objectMapper.getTypeFactory().constructCollectionType(List.class, Book.class));
            solrUploader.uploadToSolr(books);

            System.out.println("The end good!");
        } catch (Exception e) {
            System.out.println("Error JSON: " + e.getMessage());
        }
    }
}
