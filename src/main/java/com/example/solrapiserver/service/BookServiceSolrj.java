package com.example.solrapiserver.service;

import lombok.RequiredArgsConstructor;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BookServiceSolrj {

    private final SolrClient solrClient;
    private final FieldsService fieldsService;

    @Value("${solr.collection}")
    private String collection;

    public void addBook(Map<String, Object> bookData) throws Exception {
        SolrInputDocument doc = new SolrInputDocument();
        Map<String, String> fieldsMapping = fieldsService.getFields();

        for (Map.Entry<String, Object> entry : bookData.entrySet()) {
            String solrField = fieldsMapping.getOrDefault(entry.getKey(), entry.getKey());
            doc.addField(solrField, entry.getValue());
        }

        solrClient.add(collection, doc);
        solrClient.commit(collection);
    }

    public List<Map<String, Object>> searchBooks(String keyword) throws Exception {
        String queryStr = "*:*";
        if (!keyword.isEmpty() && !"*".equals(keyword)) {
            queryStr = "title:\"" + keyword + "\" OR authors:\"" + keyword + "\"";
        }

        SolrQuery query = new SolrQuery(queryStr);
        QueryResponse response = solrClient.query(collection, query);
        SolrDocumentList documents = response.getResults();

        Map<String, String> fieldsMapping = fieldsService.getFields();
        Map<String, String> reverseMapping = new HashMap<>();
        for (Map.Entry<String, String> entry : fieldsMapping.entrySet()) {
            reverseMapping.put(entry.getValue(), entry.getKey());
        }

        List<Map<String, Object>> books = new ArrayList<>();
        for (var doc : documents) {
            Map<String, Object> bookData = new HashMap<>();
            for (String solrField : doc.getFieldNames()) {
                String mappedField = reverseMapping.getOrDefault(solrField, solrField);
                bookData.put(mappedField, doc.getFieldValue(solrField));
            }
            books.add(bookData);
        }
        return books;
    }

    public void deleteBookById(String id) throws Exception {
        solrClient.deleteById(collection, id);
        solrClient.commit(collection);
    }

    public Map<String, String> getFieldsMapping() {
        return fieldsService.getFields();
    }
}
