package com.example.cli_json_stax.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.cli_json_stax.model.Book;
import com.example.cli_json_stax.solr.SolrUpload;
import org.codehaus.stax2.XMLInputFactory2;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
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
            XMLInputFactory2 factory = (XMLInputFactory2) javax.xml.stream.XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(new FileInputStream(jsonFile));

            JsonFactory jsonFactory = new JsonFactory();
            JsonParser jsonParser = jsonFactory.createParser(new FileInputStream(jsonFile));

            ObjectMapper objectMapper = new ObjectMapper();
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
                    Book book = objectMapper.readValue(jsonParser, Book.class);
                    try {
                        solrUploader.uploadToSolr(List.of(book));
                    } catch (Exception e) {
                        System.out.println("Error uploading book to Solr: " + e.getMessage());
                    }
                }
            }

            System.out.println("JSON processing completed!");
        } catch (IOException | XMLStreamException e) {
            System.out.println("Error JSON: " + e.getMessage());
        } finally {
            System.out.println("Program finished.");
        }
    }
}
