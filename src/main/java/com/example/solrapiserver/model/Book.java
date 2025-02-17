package com.example.solrapiserver.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.solr.client.solrj.beans.Field;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Field("id")
    private String id;

    @Field("title")
    private List<String> title;

    @Field("author")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> author;
}
