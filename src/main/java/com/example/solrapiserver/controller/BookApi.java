package com.example.solrapiserver.controller;

import com.example.solrapiserver.model.Book;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/books")
@Tag(name = "Book API", description = "API для работы с книгами в Solr")
public interface BookApi {
    @PostMapping
    @Operation(summary = "Добавить книгу", description = "Добавляет новую книгу в Solr")
    String addBook(@RequestBody Book book);

    @GetMapping("/search")
    @Operation(summary = "Поиск книг", description = "Ищет книги по названию или автору")
    List<Book> searchBooks(@RequestParam(value = "q", defaultValue = "") String q);

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить книгу", description = "Удаляет книгу из Solr по ID")
    String deleteBook(@PathVariable String id);
}
