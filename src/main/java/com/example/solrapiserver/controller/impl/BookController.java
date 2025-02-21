package com.example.solrapiserver.controller.impl;

import com.example.solrapiserver.controller.BookApi;
import com.example.solrapiserver.model.Book;
import com.example.solrapiserver.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BookController implements BookApi {

    private final BookService bookService;

    @Override
    public String addBook(Book book) {
        try {
            bookService.addBook(book);
            return "Книга добавлена!";
        } catch (Exception e) {
            return "Ошибка: " + e.getMessage();
        }
    }

    @Override
    public List<Book> searchBooks(String q) {
        try {
            if (q.equals("*")) {
                q = "";
            }
            return bookService.searchBooks(q);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при поиске: " + e.getMessage());
        }
    }

    @Override
    public String deleteBook(String id) {
        try {
            bookService.deleteBookById(id);
            return "Книга удалена!";
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при удалении: " + e.getMessage());
        }
    }
}
