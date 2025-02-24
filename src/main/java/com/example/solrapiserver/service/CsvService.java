package com.example.solrapiserver.service;

import com.example.solrapiserver.model.Book;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.RequiredArgsConstructor;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
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

    // Чтение CSV файла и конвертация в объект Book
    private List<Book> parseCsv(InputStream inputStream) throws Exception {
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = CsvSchema
                .emptySchema()
                .withHeader()
                .withColumnSeparator(',')
                .withQuoteChar('"')
                .withArrayElementSeparator(",");

        MappingIterator<Book> it = csvMapper.readerFor(Book.class).with(schema).readValues(inputStream);

        List<Book> books = new ArrayList<>();
        while (it.hasNext()) {
            books.add(it.next());
        }
        return books;
    }

    // Основной метод обработки CSV
    public void processCsv(MultipartFile file) throws Exception {
        List<Book> books = parseCsv(file.getInputStream());
        uploadToSolr(books);
    }

    // Загрузка данных в Solr
    private void uploadToSolr(List<Book> books) throws Exception {
        for (Book book : books) {
            SolrInputDocument doc = new SolrInputDocument();

            // Используем свойства непосредственно из fields.properties
            doc.addField("id", book.getId());
            doc.addField("title", book.getTitle());
            doc.addField("authors_ss", book.getAuthors());
            doc.addField("publisher", book.getPublisher());
            doc.addField("publication_date_dt", book.getPublicationDate());
            doc.addField("isbn", book.getIsbn());
            doc.addField("language", book.getLanguage());
            doc.addField("genre", book.getGenre());
            doc.addField("description", book.getDescription());
            doc.addField("price_f", book.getPrice());
            doc.addField("available_b", book.isAvailable());
            doc.addField("keywords_ss", book.getKeywords());

            solrClient.add(collection, doc);
        }
        solrClient.commit(collection, true, true);
    }
}
