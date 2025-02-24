package com.example.solrapiserver.controller;

import com.example.solrapiserver.service.BookServiceSolrj;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/books-solrj")
@RequiredArgsConstructor
@Tag(name = "Book API", description = "API для работы с книгами в Solr")
public class BookControllerSolrj {

    private final BookServiceSolrj bookService;

    @PostMapping
    @Operation(summary = "Добавить книгу", description = "Добавляет новую книгу в Solr")
    public String addBook(@RequestBody Map<String, Object> bookData) {
        try {
            bookService.addBook(bookData);
            return "Книга добавлена!";
        } catch (Exception e) {
            return "Ошибка: " + e.getMessage();
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Поиск книг", description = "Ищет книги по названию или автору")
    public List<Map<String, Object>> searchBooks(@RequestParam(value = "q", defaultValue = "") String q) {
        try {
            if (q.equals("*")) {
                q = "";
            }
            return bookService.searchBooks(q);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при поиске: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить книгу", description = "Удаляет книгу из Solr по ID")
    public String deleteBook(@PathVariable String id) {
        try {
            bookService.deleteBookById(id);
            return "Книга удалена!";
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при удалении: " + e.getMessage());
        }
    }

    @GetMapping("/fields")
    @Operation(summary = "Получить поля и типы данных", description = "Возвращает доступные поля и их типы данных")
    public Map<String, String> getFields() {
        return bookService.getFieldsMapping();

    }
}
