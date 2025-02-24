//package com.example.solrapiserver.service;
//
//import lombok.RequiredArgsConstructor;
//import org.apache.solr.client.solrj.SolrClient;
//import org.apache.solr.client.solrj.SolrQuery;
//import org.apache.solr.client.solrj.response.QueryResponse;
//import org.apache.solr.common.SolrDocumentList;
//import org.apache.solr.common.SolrInputDocument;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//
//@Service
//@RequiredArgsConstructor
//public class BookServiceSolrj {
//
//    private final SolrClient solrClient;
//
//    @Value("${solr.collection}")
//    private String collection;
//
//    private static final Map<String, String> FIELDS = Map.of(
//            "id", "String",
//            "title", "String",
//            "authors", "String[]"
//    );
//
//    public void addBook(Map<String, Object> bookData) throws Exception {
//        SolrInputDocument doc = new SolrInputDocument();
//        for (Map.Entry<String, Object> entry : bookData.entrySet()) {
//            doc.addField(entry.getKey(), entry.getValue());
//        }
//        solrClient.add(collection, doc);
//        solrClient.commit(collection);
//    }
//
//    public List<Map<String, Object>> searchBooks(String keyword) throws Exception {
//        String queryStr = "*:*";
//        if (!keyword.isEmpty() && !"*".equals(keyword)) {
//            queryStr = "title:\"" + keyword + "\" OR authors:\"" + keyword + "\"";
//        }
//
//        SolrQuery query = new SolrQuery(queryStr);
//        QueryResponse response = solrClient.query(collection, query);
//        SolrDocumentList documents = response.getResults();
//
//        List<Map<String, Object>> books = new ArrayList<>();
//        for (var doc : documents) {
//            Map<String, Object> bookData = new HashMap<>();
//            bookData.put("id", doc.getFirstValue("id"));
//            bookData.put("title", doc.getFirstValue("title"));
//            bookData.put("authors", doc.getFieldValue("authors"));
//            books.add(bookData);
//        }
//        return books;
//    }
//
//    public void deleteBookById(String id) throws Exception {
//        solrClient.deleteById(collection, id);
//        solrClient.commit(collection);
//    }
//
//    public Map<String, String> getFields() {
//        return FIELDS;
//    }
//}
