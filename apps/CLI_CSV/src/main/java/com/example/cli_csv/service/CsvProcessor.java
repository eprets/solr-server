package com.example.cli_csv.service;


import com.example.cli_csv.solr.SolrCsvUpload;
import com.example.common.solr.SolrUpload;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.example.cli_csv.model.Book;


import java.io.File;
import java.util.List;

public class CsvProcessor {
    private final SolrCsvUpload solrUploader;
    private final String solrUrl;
    private final String collection;

    public CsvProcessor(String solrUrl, String collection, String mappingPath) {
        this.solrUploader = new SolrCsvUpload(solrUrl, collection, mappingPath);
        this.collection = collection;
        this.solrUrl = solrUrl;
    }
    public void validateParams(String mappingPath) {
        validateFile(mappingPath, "mapping");

        SolrUpload helper = new SolrUpload(solrUrl, collection);
        if (!helper.ensureSolrAndCore()) {
            throw new RuntimeException("Solr или core недоступны. Загрузка отменена.");
        }
    }
    private void validateFile(String filePath, String fileType) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Неверный путь до " + fileType + " файла: " + filePath);
        }
    }

    public void processCsv(String csvPath) {
        try {
            System.out.println("Start CSV...");

            File csvFile = new File(csvPath);
            CsvMapper csvMapper = new CsvMapper();
            CsvSchema schema = CsvSchema.emptySchema().withHeader();

            try (MappingIterator<Book> iterator = csvMapper.readerFor(Book.class).with(schema).readValues(csvFile)) {
                List<Book> books = iterator.readAll();
                solrUploader.uploadToSolr(books);
            }

            System.out.println("The end good!");
        } catch (Exception e) {
            System.out.println("Error CSV: " + e.getMessage());
        }
    }
}
