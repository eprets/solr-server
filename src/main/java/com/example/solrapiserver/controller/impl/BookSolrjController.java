package com.example.solrapiserver.controller.impl;

import com.example.solrapiserver.controller.BookSolrjApi;
import com.example.solrapiserver.model.Book;
import com.example.solrapiserver.service.BookService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BookSolrjController implements BookSolrjApi {

    private final BookService bookService;

    public BookSolrjController(@Qualifier(value = "bookSolrjServiceImpl") BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public String addBook(Book book) {
        try {
            bookService.addBook(book);
            return "Книга добавлена!";
        } catch (Exception e) {
            return "Книга добавлена!";
        }
    }

    @Override
    public List<Book> searchBooks(String q) {
        return List.of();
    }

    @Override
    public String deleteBook(String id) {
        return "";
    }
}
