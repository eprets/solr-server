package com.example.cli_csv.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    private String id;
    private String title;
    private List<String> authors;
    private String publisher;
    @JsonProperty("publication_date")
    private String publicationDate;
    private String isbn;
    private String language;
    private String genre;
    private String description;
    private Double price;
    private Boolean available;
    private List<String> keywords;
}
