package ru.kate.multiproject.solr;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import ru.kate.multiproject.service.MapperService;

import java.io.IOException;
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
        if (helper.ensureSolrAndCores()) return;

        booksJsonNodes.parallelStream()
                .forEach(bookFromBatch -> {
                    SolrInputDocument doc = new SolrInputDocument();
                    Iterator<String> fieldNames = bookFromBatch.fieldNames();
                    while (fieldNames.hasNext()) {
                        String jsonFieldName = fieldNames.next();
                        String solrFieldName = mapperService.getSolrFieldName(jsonFieldName);
                        JsonNode fieldValue = bookFromBatch.get(jsonFieldName);

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
                    try {
                        solrClient.add(collection, doc);
                    } catch (SolrServerException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        solrClient.commit(collection);
        System.out.println("Загрузка в Solr завершена.");
    }

    public String getCollection() {
        return collection;
    }
}
