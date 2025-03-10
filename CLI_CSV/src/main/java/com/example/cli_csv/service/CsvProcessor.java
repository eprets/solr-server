package com.example.cli_csv.service;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.example.cli_csv.model.Book;
import com.example.cli_csv.solr.SolrUpload;

import java.io.File;
import java.util.List;

public class CsvProcessor {
    private final SolrUpload solrUploader;

    public CsvProcessor(String solrUrl, String mappingPath) {
        this.solrUploader = new SolrUpload(solrUrl, mappingPath);
    }

    public void processCsv(String csvPath) {
        try {
            File csvFile = new File(csvPath);
            CsvMapper csvMapper = new CsvMapper();
            CsvSchema schema = CsvSchema.emptySchema().withHeader();
            MappingIterator<Book> iterator = csvMapper.readerFor(Book.class).with(schema).readValues(csvFile);

            List<Book> books = iterator.readAll();
            solrUploader.uploadToSolr(books);
            System.out.println("Загрузка завершена!");
        } catch (Exception e) {
            System.err.println("Ошибка обработки CSV: " + e.getMessage());
        }
    }
}
