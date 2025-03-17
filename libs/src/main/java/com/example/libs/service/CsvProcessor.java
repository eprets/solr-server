package com.example.libs.service;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.example.libs.model.Book;
import com.example.libs.solr.SolrUpload;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class CsvProcessor {
    private final SolrUpload solrUploader;

    public CsvProcessor(SolrUpload solrUploader) {
        this.solrUploader = solrUploader;
    }

    public List<Book> processCsv(String csvPath) {
        System.out.println("Start CSV...");

        File csvFile = new File(csvPath);
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader();

        try (MappingIterator<Book> iterator = csvMapper.readerFor(Book.class).with(schema).readValues(csvFile)) {
            List<Book> books = iterator.readAll();
            solrUploader.uploadToSolr(books);
            return books;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
