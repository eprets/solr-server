package com.example.cli_db.solr;

import com.example.cli_db.service.MapperService;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

import java.text.SimpleDateFormat;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class SolrUpload {
    private final SolrClient solrClient;
    private final MapperService mapperService;
    private final String collection;
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
        } catch (Exception e) {
            System.out.println("Solr сервер недоступен: " + e.getMessage());
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
        } catch (Exception e) {
            System.out.println("Ошибка проверки доступности core: " + e.getMessage());
            return false;
        }
    }

    public boolean createCore() {
        try {
            URL url = new URL(solrUrl + "/admin/cores?action=CREATE&name=" + collection + "&configSet=sample_techproducts_configs");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            System.out.println("Ошибка создания core: " + e.getMessage());
            return false;
        }
    }

    // Метод для загрузки документов в Solr. Принимает список карт с данными книги.
    public void uploadDocuments(List<Map<String, Object>> books) {
        if (!checkCoreAvailability()) {
            System.out.println("Core не найден. Создаём core...");
            if (!createCore()) {
                System.out.println("Не удалось создать core. Загрузка прервана.");
                return;
            } else {
                System.out.println("Core успешно создан.");
            }
        }
        try {
            for (Map<String, Object> book : books) {
                SolrInputDocument doc = new SolrInputDocument();
                for (Map.Entry<String, Object> entry : book.entrySet()) {
                    String dbFieldName = entry.getKey();
                    Object value = entry.getValue();
                    String solrFieldName = mapperService.getSolrFieldName(dbFieldName);
                    if ("publication_date".equals(dbFieldName) && value != null) {
                        try {
                            Date date;
                            if (value instanceof Date) {
                                date = (Date) value;
                            } else {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                date = dateFormat.parse(value.toString());
                            }
                            String formattedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(date);
                            doc.addField(solrFieldName, formattedDate);
                        } catch (Exception e) {
                            System.out.println("Ошибка преобразования даты для значения: " + value);
                        }
                    } else {
                        doc.addField(solrFieldName, value);
                    }
                }
                solrClient.add(collection, doc);
            }
            solrClient.commit(collection);
            System.out.println("Загружено документов в Solr: " + books.size());
        } catch (Exception e) {
            System.out.println("Ошибка при загрузке в Solr: " + e.getMessage());
        }
    }
}
