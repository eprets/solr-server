package com.example.solrapiserver.service;

import com.example.solrapiserver.model.Book;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;

@Service
@RequiredArgsConstructor
public class BookService {

    private final SolrClient solrClient;
    private final MapperService mapperService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${solr.collection}")
    private String collection;

    public SolrDocumentList searchBooks(String query) throws Exception {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(query);
        QueryResponse response = solrClient.query(collection, solrQuery);
        return response.getResults();
    }

    public void addBook(Book book) throws Exception {
        JsonNode bookJsonNode = objectMapper.valueToTree(book);
        SolrInputDocument doc = new SolrInputDocument();

        Iterator<String> fieldNames = bookJsonNode.fieldNames();
        while (fieldNames.hasNext()) {
            String jsonFieldName = fieldNames.next();
            String solrFieldName = mapperService.getSolrFieldName(jsonFieldName);
            JsonNode fieldValue = bookJsonNode.get(jsonFieldName);

            if (fieldValue.isArray()) {
                fieldValue.forEach(value -> doc.addField(solrFieldName, value.asText()));
            } else if (solrFieldName.endsWith("_f")) {
                doc.addField(solrFieldName, fieldValue.asDouble());
            } else if (solrFieldName.endsWith("_b")) {
                doc.addField(solrFieldName, fieldValue.asBoolean());
            } else if (solrFieldName.endsWith("_dt")) {
                String dateValue = fieldValue.asText();
                // Преобразуем дату в нужный формат
                String formattedDate = formatDateForSolr(dateValue);
                doc.addField(solrFieldName, formattedDate);
            } else {
                doc.addField(solrFieldName, fieldValue.asText());
            }
        }

        solrClient.add(collection, doc);
        solrClient.commit(collection);
    }

    public void deleteBook(String id) throws Exception {
        solrClient.deleteById(collection, id);
        solrClient.commit(collection);
    }

    // Метод для преобразования даты в формат, который понимает Solr
    private String formatDateForSolr(String date) {
        try {
            // Преобразуем строку в дату
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date parsedDate = sdf.parse(date);
            // Переводим дату в формат, который использует Solr
            SimpleDateFormat solrDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            return solrDateFormat.format(parsedDate);
        } catch (ParseException e) { return null; }
    }
}
