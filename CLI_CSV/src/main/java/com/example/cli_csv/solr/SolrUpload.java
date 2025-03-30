package com.example.cli_csv.solr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import com.example.cli_csv.model.Book;
import com.example.cli_csv.service.MapperService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class SolrUpload {
    private final SolrClient solrClient;
    private final MapperService mapperService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String collection;

    public SolrUpload(String solrUrl, String mappingPath) {
        this.solrClient = new HttpSolrClient.Builder(solrUrl).build();
        this.mapperService = new MapperService(mappingPath);
        this.collection = "books";
    }

    public void uploadToSolr(List<Book> books) throws Exception {
        for (Book book : books) {
            JsonNode bookJsonNode = objectMapper.valueToTree(book);
            SolrInputDocument doc = new SolrInputDocument();

            Iterator<String> fieldNames = bookJsonNode.fieldNames();
            while (fieldNames.hasNext()) {
                String jsonFieldName = fieldNames.next();
                String solrFieldName = mapperService.getSolrFieldName(jsonFieldName);
                JsonNode fieldValue = bookJsonNode.get(jsonFieldName);

                if ("publication_date".equals(jsonFieldName)) {
                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = dateFormat.parse(fieldValue.asText());
                        String formattedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(date);
                        doc.addField(solrFieldName, formattedDate);
                    } catch (ParseException e) {
                        System.out.println("Error parsing date: " + fieldValue.asText());
                    }
                } else if (fieldValue.isArray()) {
                    fieldValue.forEach(value -> doc.addField(solrFieldName, value.asText()));
                } else {
                    doc.addField(solrFieldName, fieldValue.asText());
                }
            }

            solrClient.add(collection, doc);
        }
        solrClient.commit(collection);
    }
}
