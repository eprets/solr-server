package com.example.solrapiserver.service.impl;

import com.example.solrapiserver.model.Book;
import com.example.solrapiserver.service.BookService;
import lombok.RequiredArgsConstructor;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Primary
public class BookServiceImpl implements BookService {

    private final SolrClient solrClient;

    @Value("${solr.collection}")
    private String collection;

    @Override
    public void addBook(Book book) throws Exception {
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", book.getId());
        doc.addField("title", book.getTitle());
        doc.addField("authors", book.getAuthors());
        solrClient.add(collection, doc);
        solrClient.commit(collection);
    }

    @Override
    public List<Book> searchBooks(String keyword) throws Exception {
        String queryStr = "*:*";
        if (keyword != null && !keyword.trim().isEmpty() && !"*".equals(keyword)) {
            queryStr = "title:\"" + keyword + "\" OR authors:\"" + keyword + "\"";
        }
        System.out.println("Query: " + queryStr);

        SolrQuery query = new SolrQuery(queryStr);
        try {
            QueryResponse response = solrClient.query(collection, query);
            SolrDocumentList documents = response.getResults();

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

    @Override
    public void deleteBookById(String id) throws Exception {
        solrClient.deleteById(collection, id);
        solrClient.commit(collection);
    }
}
