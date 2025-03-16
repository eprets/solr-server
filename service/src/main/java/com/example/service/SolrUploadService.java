package com.example.service;

import com.example.libs.model.Book;
import com.example.libs.service.CsvProcessor;
import com.example.libs.solr.SolrUpload;
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
        // Обрабатываем CSV, чтобы получить список книг
        List<Book> books = csvProcessor.processCsv(csvPath); // Убедитесь, что метод processCsv возвращает список книг

        // Загружаем книги в Solr
        solrUpload.uploadToSolr(books);
    }
}
