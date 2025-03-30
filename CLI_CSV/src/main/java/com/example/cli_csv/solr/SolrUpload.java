package com.example.cli_csv.solr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import com.example.cli_csv.service.MapperService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
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
    private static final int BATCH_SIZE = 100;
    private final String solrUrl;


    public SolrUpload(String solrUrl, String mappingPath) {
        this.solrClient = new HttpSolrClient.Builder(solrUrl).build();
        this.mapperService = new MapperService(mappingPath);
        this.collection = "books";
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
            return false;
        }
    }

    public void uploadToSolr(List<JsonNode> booksJsonNodes) throws Exception {
        int totalBooks = booksJsonNodes.size();

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

        for (int i = 0; i < totalBooks; i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, totalBooks);
            List<JsonNode> batch = booksJsonNodes.subList(i, end);

            for (JsonNode bookJsonNode : batch) {
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

    public String getCollection() {
        return collection;
    }
}
