package com.example.solrapiserver.service;

import com.example.solrapiserver.model.Book;

import java.util.List;

public interface BookService {
    void addBook(Book book) throws Exception;

    List<Book> searchBooks(String keyword) throws Exception;

    void deleteBookById(String id) throws Exception;
}
