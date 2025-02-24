//package com.example.solrapiserver.service;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class BookService {
//
//    private final RestTemplate restTemplate;
//
//    @Value("${solr.host}")
//    private String solrHost;
//
//    @Value("${solr.collection}")
//    private String collection;
//
//    public BookService(RestTemplate restTemplate) {
//        this.restTemplate = restTemplate;
//    }
//
//    // Добавление книги
//    public void addBook(Map<String, Object> book) throws Exception {
//        String url = solrHost + "/" + collection + "/update?commit=true";
//        Map<String, Object> doc = new HashMap<>();
//        doc.put("add", book);
//
//        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(doc);
//        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
//    }
//
//    // Поиск книг
//    public List<Map<String, Object>> searchBooks(String keyword) throws Exception {
//        String queryStr = "*:*";
//        if (keyword != null && !keyword.trim().isEmpty()) {
//            queryStr = "title:\"" + keyword + "\" OR authors:\"" + keyword + "\"";
//        }
//
//        String url = solrHost + "/" + collection + "/select?q=" + queryStr + "&wt=json";
//        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, null, Map.class);
//        Map<String, Object> result = response.getBody();
//        return (List<Map<String, Object>>) result.get("response");
//    }
//
//    // Удаление книги по ID
//    public void deleteBookById(String id) throws Exception {
//        String url = solrHost + "/" + collection + "/update?commit=true";
//        Map<String, Object> deleteDoc = new HashMap<>();
//        deleteDoc.put("delete", Map.of("id", id));
//
//        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(deleteDoc);
//        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
//    }
//}
