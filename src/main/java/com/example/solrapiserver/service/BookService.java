package com.example.solrapiserver.service;

import com.example.solrapiserver.model.Book;
import lombok.RequiredArgsConstructor;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookService {

    private final SolrClient solrClient;

    @Value("${solr.collection}")
    private String collection;

    private final RestTemplate restTemplate;

    // Добавление книги в Solr
    public void addBook(Book book) throws Exception {
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", book.getId());        // id книги
        doc.addField("title", book.getTitle());  // название книги
        doc.addField("authors", book.getAuthors()); // авторы книги
        doc.addField("publisher", book.getPublisher());
        doc.addField("publication_date", book.getPublicationDate());
        doc.addField("isbn", book.getIsbn());
        doc.addField("language", book.getLanguage());
        doc.addField("genre", book.getGenre());
        doc.addField("description", book.getDescription());
        doc.addField("price", book.getPrice());
        doc.addField("available", book.isAvailable());
        doc.addField("keywords", book.getKeywords());

        solrClient.add(collection, doc);
        solrClient.commit(collection);
    }

    // Поиск книг в Solr
    public List<Book> searchBooks(String keyword) throws Exception {
        String queryStr = "*:*";
        if (keyword != null && !keyword.trim().isEmpty() && !"*".equals(keyword)) {
            queryStr = "title:\"" + keyword + "\" OR authors:\"" + keyword + "\"";
        }
        System.out.println("Query: " + queryStr);

        SolrQuery query = new SolrQuery(queryStr);
        try {
            QueryResponse response = solrClient.query(collection, query);
            var documents = response.getResults();

            System.out.println("Number of documents found: " + documents.getNumFound());

            List<Book> books = new ArrayList<>();
            documents.forEach(doc -> books.add(new Book(
                    (String) doc.getFirstValue("id"),
                    (String) doc.getFirstValue("title"),
                    (List<String>) doc.getFieldValue("authors"),
                    (String) doc.getFirstValue("publisher"),
                    (String) doc.getFirstValue("publication_date"),
                    (String) doc.getFirstValue("isbn"),
                    (String) doc.getFirstValue("language"),
                    (String) doc.getFirstValue("genre"),
                    (String) doc.getFirstValue("description"),
                    (Double) doc.getFirstValue("price"),
                    (Boolean) doc.getFirstValue("available"),
                    (List<String>) doc.getFieldValue("keywords")
            )));
            return books;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Ошибка при выполнении запроса к Solr: " + e.getMessage());
        }
    }

    // Удаление книги по ID
    public void deleteBookById(String id) throws Exception {
        solrClient.deleteById(collection, id);
        solrClient.commit(collection);
    }

}
