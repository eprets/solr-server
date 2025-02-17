package com.example.solrapiserver.service;

import com.example.solrapiserver.model.Book;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.RequiredArgsConstructor;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsvService {

    private final SolrClient solrClient;

    @Value("${solr.collection}")
    private String collection;

    private List<Book> parseCsv(InputStream inputStream) throws Exception {
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader().withColumnSeparator(',');

        schema = schema.withQuoteChar('"');

        MappingIterator<Book> it = csvMapper.readerFor(Book.class).with(schema).readValues(inputStream);

        List<Book> books = new ArrayList<>();
        while (it.hasNext()) {
            books.add(it.next());
        }
        return books;
    }

    public void processCsv(MultipartFile file) throws Exception {
        System.out.println("Processing CSV file...");
        List<Book> books = parseCsv(file.getInputStream());
        System.out.println("Parsed " + books.size() + " books.");
        uploadToSolr(books);
    }

    private void uploadToSolr(List<Book> books) throws Exception {
        for (Book book : books) {
            System.out.println("Uploading book: " + book.getTitle());
            SolrInputDocument doc = new SolrInputDocument();
            doc.addField("id", book.getId());
            doc.addField("title", book.getTitle());
            doc.addField("author", book.getAuthor());

            solrClient.add(collection, doc);
        }
        solrClient.commit(collection);
    }
}
