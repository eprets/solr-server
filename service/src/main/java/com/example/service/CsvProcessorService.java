package com.example.service;

import com.example.libs.service.CsvProcessor;
import org.springframework.stereotype.Service;

@Service


public class CsvProcessorService {

    private final CsvProcessor csvProcessor;

    public CsvProcessorService(CsvProcessor csvProcessor) {
        this.csvProcessor = csvProcessor;
    }

    public void processCsv(String csvPath) {
        csvProcessor.processCsv(csvPath);
    }
}
