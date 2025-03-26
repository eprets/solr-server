package com.example.cli_json_stax.solr;

import com.example.cli_json_stax.service.MapperService;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.net.HttpURLConnection;
import java.net.URL;

public class SolrUpload {
    private final SolrClient solrClient;
    private final MapperService mapperService;
    private final String collection;
    private static final int BATCH_SIZE = 100;
    private final String solrUrl;

    public SolrUpload(String solrUrl, String mappingPath) {
        this.solrClient = new HttpSolrClient.Builder(solrUrl).build();
        this.mapperService = new MapperService(mappingPath);
        this.collection = "books"; // Убедись, что коллекция называется "books"
        this.solrUrl = solrUrl; // Сохраняем URL Solr-сервера
    }

    // Метод для проверки доступности Solr
    public boolean checkSolrAvailability() {
        try {
            // Теперь отправляем запрос на главную страницу Solr
            URL url = new URL(solrUrl );
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            // Проверяем, что сервер возвращает код 200 OK
            return responseCode == HttpURLConnection.HTTP_OK; // 200 OK
        } catch (IOException e) {
            System.out.println("Solr server is not available: " + e.getMessage());
            return false;
        }
    }


    // Метод для проверки существования ядра
    public boolean checkCoreAvailability() {
        try {
            // Проверяем существование ядра, отправив запрос на /admin/cores?action=STATUS
            URL url = new URL(solrUrl + "/admin/cores?action=STATUS&core=" + collection);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            return responseCode == HttpURLConnection.HTTP_OK; // 200 OK
        } catch (IOException e) {
            System.out.println("Error checking core availability: " + e.getMessage());
            return false;
        }
    }

    // Метод для создания ядра пока не рабочий
    public void createCore() throws IOException {
        // Проверка, существует ли коллекция
        String checkCoreUrl = solrUrl + "/admin/cores?action=STATUS&core=" + collection;
        HttpURLConnection checkConnection = (HttpURLConnection) new URL(checkCoreUrl).openConnection();
        checkConnection.setRequestMethod("GET");
        int checkResponseCode = checkConnection.getResponseCode();

        if (checkResponseCode != HttpURLConnection.HTTP_OK) {
            // Коллекция не существует, создаём её
            String createCoreUrl = solrUrl + "/admin/cores?action=CREATE&name=" + collection + "&configSet=sample_techproducts_configs";
            HttpURLConnection connection = (HttpURLConnection) new URL(createCoreUrl).openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Core " + collection + " created successfully.");
            } else {
                System.out.println("Error creating core: " + responseCode);
            }
        } else {
            System.out.println("Core " + collection + " already exists.");
        }
    }



    // Основной метод для загрузки данных в Solr
    public void uploadToSolr(List<JsonNode> booksJsonNodes) throws Exception {
        int totalBooks = booksJsonNodes.size();
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

    public String getCollection() {
        return collection;
    }
}
