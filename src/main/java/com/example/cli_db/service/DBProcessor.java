package com.example.cli_db.service;

import com.example.cli_db.solr.SolrUpload;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class DBProcessor {
    private final SolrUpload solrUploader;
    private final String mappingPath;
    private final Properties dbProperties;
    private static final int BATCH_SIZE = 10;

    public DBProcessor(String solrUrl, String mappingPath, String dbPropsPath) {
        this.solrUploader = new SolrUpload(solrUrl, mappingPath);
        this.mappingPath = mappingPath;
        this.dbProperties = loadDBProperties(dbPropsPath);
    }

    private Properties loadDBProperties(String dbPropsPath) {
        Properties props = new Properties();
        try (FileInputStream inputStream = new FileInputStream(dbPropsPath)) {
            props.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки DB свойств: " + e.getMessage());
        }
        return props;
    }

    public void processDB() {
        Connection connection = null;
        try {
            // Загрузка драйвера H2
            Class.forName("org.h2.Driver");

            String dbUrl = dbProperties.getProperty("db.url");
            String dbUser = dbProperties.getProperty("db.user");
            String dbPassword = dbProperties.getProperty("db.password");

            connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            System.out.println("Подключение к H2 БД успешно установлено.");

            // Выполнение запроса к таблице books
            String query = "SELECT * FROM books";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            List<Map<String, Object>> booksBatch = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> book = new HashMap<>();
                // Предполагается, что таблица books имеет следующие колонки
                book.put("id", rs.getObject("id"));
                book.put("title", rs.getObject("title"));
                book.put("authors", rs.getObject("authors"));
                book.put("publisher", rs.getObject("publisher"));
                book.put("publication_date", rs.getObject("publication_date"));
                book.put("isbn", rs.getObject("isbn"));
                book.put("language", rs.getObject("language"));
                book.put("genre", rs.getObject("genre"));
                book.put("description", rs.getObject("description"));
                book.put("price", rs.getObject("price"));
                book.put("available", rs.getObject("available"));
                book.put("keywords", rs.getObject("keywords"));

                booksBatch.add(book);
                if (booksBatch.size() >= BATCH_SIZE) {
                    solrUploader.uploadDocuments(booksBatch);
                    booksBatch.clear();
                }
            }
            if (!booksBatch.isEmpty()) {
                solrUploader.uploadDocuments(booksBatch);
            }
            System.out.println("Обработка данных из БД завершена!");
        } catch (Exception e) {
            System.out.println("Ошибка при обработке БД: " + e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch(SQLException e) {
                System.out.println("Ошибка закрытия соединения с БД: " + e.getMessage());
            }
        }
    }
}
