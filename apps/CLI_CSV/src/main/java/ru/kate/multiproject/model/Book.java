package ru.kate.multiproject.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Book {
    @JsonProperty("id")
    private String id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("authors")
    private List<String> authors;

    @JsonProperty("publisher")
    private String publisher;

    @JsonProperty("publication_date")
    private String publicationDate;

    @JsonProperty("isbn")
    private String isbn;

    @JsonProperty("language")
    private String language;

    @JsonProperty("genre")
    private String genre;

    @JsonProperty("description")
    private String description;

    @JsonProperty("price")
    private Double price;

    @JsonProperty("available")
    private Boolean available;

    @JsonProperty("keywords")
    private List<String> keywords;

    public Book() {}
}