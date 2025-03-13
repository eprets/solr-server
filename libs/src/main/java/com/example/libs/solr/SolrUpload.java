package com.example.libs.solr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import com.example.libs.model.Book;
import com.example.libs.service.MapperService;

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

                if (fieldValue.isArray()) {
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
