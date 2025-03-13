package com.example.service;

import com.example.libs.service.CsvProcessor;

public class CsvProcessorService {

    private final CsvProcessor csvProcessor;

    public CsvProcessorService(String solrUrl, String mappingPath) {
        this.csvProcessor = new CsvProcessor(solrUrl, mappingPath);
    }

    public void processCsv(String csvPath) {
        csvProcessor.processCsv(csvPath);
    }
}
