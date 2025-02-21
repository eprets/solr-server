package com.example.solrapiserver.service.impl;

import com.example.solrapiserver.model.Book;
import com.example.solrapiserver.service.BookService;
import lombok.RequiredArgsConstructor;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookSolrjServiceImpl implements BookService {

    private final SolrClient solrClient;

    @Value("${solr.collection}")
    private String collection;

    @Override
    public void addBook(Book book) throws Exception {
        solrClient.addBean(collection, book);
    }

    @Override
    public List<Book> searchBooks(String keyword) throws Exception {
        return List.of();
    }

    @Override
    public void deleteBookById(String id) throws Exception {

    }
}