package com.example.cli_csv.service;


import com.example.cli_csv.solr.SolrCsvUpload;
import com.example.common.solr.SolrUpload;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.example.cli_csv.model.Book;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CsvProcessor {
    private final SolrCsvUpload solrUploader;
    private final String solrUrl;
    private final String collection;
    private static final int BATCH_SIZE = 12;

    public CsvProcessor(String solrUrl, String collection, String mappingPath) {
        this.solrUploader = new SolrCsvUpload(solrUrl, collection, mappingPath);
        this.collection = collection;
        this.solrUrl = solrUrl;
    }
    public void validateParams(String mappingPath) {
        validateFile(mappingPath, "mapping");

        SolrUpload helper = new SolrUpload(solrUrl, collection);
        if (helper.ensureSolrAndCores()) {
            throw new RuntimeException("Solr или core недоступны. Загрузка отменена.");
        }
        if (helper.checkCoreAvailability()) {
            System.out.println("Core " + solrUploader.getCollection() + " not found. Trying create core...");
            helper.createCore();
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

            List<Book> booksBatch = new ArrayList<>();
            int counter = 0;

            try (MappingIterator<Book> iterator = csvMapper.readerFor(Book.class).with(schema).readValues(csvFile)) {
                while (iterator.hasNext()) {
                    Book book = iterator.next();
                    booksBatch.add(book);

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
            }

            System.out.println("CSV processing completed!");

        } catch (Exception e) {
            System.out.println("Error processing CSV: " + e.getMessage());
        } finally {
            System.out.println("Program finished.");
        }
    }

}
