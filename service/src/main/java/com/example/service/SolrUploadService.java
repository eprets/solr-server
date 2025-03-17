package com.example.service;

import com.example.libs.model.Book;
import com.example.libs.solr.SolrUpload;
import com.example.libs.service.CsvProcessor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SolrUploadService {

    private final SolrUpload solrUpload;
    private final CsvProcessor csvProcessor;

    public SolrUploadService(SolrUpload solrUpload, CsvProcessor csvProcessor) {
        this.solrUpload = solrUpload;
        this.csvProcessor = csvProcessor;
    }

    public void uploadToSolr(String csvPath) throws Exception {
        List<Book> books = csvProcessor.processCsv(csvPath);
        solrUpload.uploadToSolr(books);
    }
}
