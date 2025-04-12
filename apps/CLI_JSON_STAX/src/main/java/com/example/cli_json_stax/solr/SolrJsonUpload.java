package com.example.cli_json_stax.solr;

import com.example.common.service.MapperService;
import com.example.common.solr.SolrUpload;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class SolrJsonUpload {
    private final SolrClient solrClient;
    private final MapperService mapperService;
    private final String collection;
    private final String solrUrl;

    public SolrJsonUpload(String solrUrl, String collection, String mappingPath) {
        this.solrClient = new HttpSolrClient.Builder(solrUrl).build();
        this.mapperService = new MapperService(mappingPath);
        this.collection = collection;
        this.solrUrl = solrUrl;
    }

    public void uploadToSolr(List<JsonNode> booksJsonNodes) throws Exception {
        SolrUpload helper = new SolrUpload(solrUrl, collection);
        if (!helper.ensureSolrAndCore()) return;

        for (JsonNode book : booksJsonNodes) {
            SolrInputDocument doc = new SolrInputDocument();
            Iterator<String> fieldNames = book.fieldNames();

            while (fieldNames.hasNext()) {
                String jsonFieldName = fieldNames.next();
                String solrFieldName = mapperService.getSolrFieldName(jsonFieldName);
                JsonNode fieldValue = book.get(jsonFieldName);

                if ("publication_date".equals(jsonFieldName)) {
                    try {
                        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(fieldValue.asText());
                        String formatted = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(date);
                        doc.addField(solrFieldName, formatted);
                    } catch (ParseException e) {
                        System.out.println("Ошибка парсинга даты: " + fieldValue.asText());
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
        System.out.println("Загрузка в Solr завершена.");
    }

    public String getCollection() {
        return collection;
    }
}
