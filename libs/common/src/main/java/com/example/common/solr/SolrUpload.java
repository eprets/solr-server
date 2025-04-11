package com.example.common.solr;

import com.example.common.service.MapperService;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.net.HttpURLConnection;
import java.net.URL;

public class SolrUpload {
    private final SolrClient solrClient;
    private final MapperService mapperService;
    private final String collection;
    private final String solrUrl;

    public SolrUpload(String solrUrl, String collection, String mappingPath) {
        this.solrClient = new HttpSolrClient.Builder(solrUrl).build();
        this.mapperService = new MapperService(mappingPath);
        this.collection = collection;
        this.solrUrl = solrUrl;
    }

    public boolean checkSolrAvailability() {
        try {
            URL url = new URL(solrUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            System.out.println("Solr server is not available: " + e.getMessage());
            return false;
        }
    }

    public boolean checkCoreAvailability() {
        try {
            URL url = new URL(solrUrl + "/admin/cores?action=STATUS&core=" + collection);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            System.out.println("Error checking core availability: " + e.getMessage());
            return false;
        }
    }

    public boolean createCore() {
        try {
            URL url = new URL(solrUrl + "/admin/cores?action=CREATE&name=" + collection + "&configSet=sample_techproducts_configs");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            // Проверяем, что коллекция была успешно создана
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            System.out.println("Error creating core: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void uploadToSolr(List<JsonNode> booksJsonNodes) throws Exception {
        // Проверка наличия коллекции перед загрузкой
        if (!checkCoreAvailability()) {
            System.out.println("Core not found. Creating core...");
            if (!createCore()) {
                System.out.println("Failed to create core. Exiting.");
                return;
            } else {
                System.out.println("Core created successfully.");
            }
        }

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
    }

    public String getCollection() {
        return collection;
    }
}
