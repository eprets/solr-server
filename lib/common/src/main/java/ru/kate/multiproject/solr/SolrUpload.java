package ru.kate.multiproject.solr;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SolrUpload {
    private final String solrUrl;
    private final String collection;

    public SolrUpload(String solrUrl, String collection) {
        this.solrUrl = solrUrl;
        this.collection = collection;
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

    public boolean ensureSolrAndCores() {
        if (!checkSolrAvailability()) {
            System.out.println("Solr недоступен. Загрузка отменена.");
            return true;
        }

        if (!checkCoreAvailability()) {
            System.out.println("Ядро не найдено. Попытка создать...");
            if (!createCore()) {
                System.out.println("Не удалось создать ядро. Загрузка отменена.");
                return true;
            } else {
                System.out.println("Ядро успешно создано.");
            }
        }
        return false;

    }
}
