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

        solrClient.add(collection, doc);         // добавляем в Solr
        solrClient.commit(collection);           // коммитим изменения
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
                    (List<String>) doc.getFieldValue("authors")
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

    // Обработка загрузки CSV в Solr
    public void uploadCsvToSolr(List<Book> books) throws Exception {
        for (Book book : books) {
            SolrInputDocument doc = new SolrInputDocument();
            doc.addField("id", book.getId());
            doc.addField("title", book.getTitle());
            doc.addField("authors", book.getAuthors());

            solrClient.add(collection, doc);
        }
        solrClient.commit(collection);
    }

    // Метод для добавления документа в Solr через HTTP
    public void addBookViaHttp(Book book) throws Exception {
        String url = "http://localhost:8983/solr/" + collection + "/update?commit=true";
        Map<String, Object> bookDoc = Map.of(
                "id", book.getId(),
                "title", book.getTitle(),
                "authors", book.getAuthors()
        );
        restTemplate.postForObject(url, bookDoc, String.class);
    }
}
