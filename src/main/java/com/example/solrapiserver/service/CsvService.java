package com.example.solrapiserver.service;

import com.example.solrapiserver.model.Book;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.RequiredArgsConstructor;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsvService {

    private final SolrClient solrClient;
    private final MapperService mapperService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${solr.collection}")
    private String collection;

    public void processCsv(MultipartFile file) throws Exception {
        List<Book> books = parseCsv(file.getInputStream());
        uploadToSolr(books);
    }

    private List<Book> parseCsv(InputStream inputStream) throws Exception {
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        MappingIterator<Book> it = csvMapper.readerFor(Book.class).with(schema).readValues(inputStream);
        return it.readAll();
    }

    private void uploadToSolr(List<Book> books) throws Exception {
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
                } else if (solrFieldName.endsWith("_f")) {
                    doc.addField(solrFieldName, fieldValue.asDouble());
                } else if (solrFieldName.endsWith("_b")) {
                    doc.addField(solrFieldName, fieldValue.asBoolean());
                } else if (solrFieldName.endsWith("_dt")) {
                    doc.addField(solrFieldName, fieldValue.asText() + "T00:00:00Z");
                } else {
                    doc.addField(solrFieldName, fieldValue.asText());
                }
            }

            solrClient.add(collection, doc);
        }
        solrClient.commit(collection);
    }
}
