package org.example.service;

import com.example.libs.model.Book;
import com.example.libs.service.CsvProcessor;
import com.example.libs.solr.SolrUpload;

import java.util.List;

public class SolrUploadService {

    private final SolrUpload solrUpload;
    private final CsvProcessor csvProcessor;

    public SolrUploadService(String solrUrl, String mappingPath) {
        this.solrUpload = new SolrUpload(solrUrl, mappingPath);
        this.csvProcessor = new CsvProcessor(solrUrl, mappingPath);
    }

    public void uploadToSolr(String csvPath) throws Exception {
        // Обрабатываем CSV, чтобы получить список книг
        List<Book> books = csvProcessor.processCsv(csvPath); // Убедитесь, что метод processCsv возвращает список книг

        // Загружаем книги в Solr
        solrUpload.uploadToSolr(books);
    }
}
