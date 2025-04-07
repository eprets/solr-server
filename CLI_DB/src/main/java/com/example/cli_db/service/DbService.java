package com.example.cli_db.service;

import com.example.cli_db.solr.SolrUpload;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class DbService {
    private final Connection connection;
    private final MapperService mapperService;
    private static final int BATCH_SIZE = 25;

    public DbService(String dbPropsPath, String mappingPath) throws Exception {
        Properties props = new Properties();
        props.load(new FileInputStream(dbPropsPath));
        Class.forName(props.getProperty("db.driver"));
        this.connection = DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.username"),
                props.getProperty("db.password")
        );
        this.mapperService = new MapperService(mappingPath);
    }

    public void initSchema() throws Exception {
        Database database = liquibase.database.DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new Liquibase("changelog/db.changelog-master.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.update((String) null);
    }

    public void menu(SolrUpload solrUpload) throws Exception {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n1. Импорт из файла\n2. Отправить в Solr\n3. Выход");
            String input = scanner.nextLine();

            switch (input) {
                case "1" -> {
                    System.out.print("Введите путь к JSON или CSV файлу: ");
                    importFromFile(scanner.nextLine());
                }
                case "2" -> solrUpload.uploadFromDb(connection);
                case "3" -> {
                    connection.close();
                    return;
                }
                default -> System.out.println("Неверный ввод");
            }
        }
    }

    public void importFromFile(String path) throws Exception {
        if (path.endsWith(".json")) {
            importFromJson(path);
        } else if (path.endsWith(".csv")) {
            importFromCsv(path);
        } else {
            System.out.println("Поддерживаются только .json и .csv");
        }
    }

    private void importFromJson(String path) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> books = mapper.readValue(new File(path), new TypeReference<>() {});
        insertBooksBatch(books);
    }

    private void importFromCsv(String path) throws Exception {
        List<Map<String, Object>> books = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String headerLine = reader.readLine();
            if (headerLine == null) return;

            String[] headers = headerLine.split(",");
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                Map<String, Object> book = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    book.put(headers[i].trim(), values[i].trim());
                }
                books.add(book);
            }
        }
        insertBooksBatch(books);
    }

    private void insertBooksBatch(List<Map<String, Object>> books) throws SQLException {
        if (books.isEmpty()) {
            System.out.println("Нет данных для импорта.");
            return;
        }

        List<String> dbFields = new ArrayList<>(mapperService.getFieldsMapping().keySet());

        String sql = "INSERT INTO books (" +
                String.join(", ", dbFields) +
                ") VALUES (" +
                dbFields.stream().map(f -> "?").collect(Collectors.joining(", ")) +
                ")";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int count = 0;

            for (Map<String, Object> book : books) {
                for (int i = 0; i < dbFields.size(); i++) {
                    String field = dbFields.get(i);
                    Object value = book.get(field);

                    if (value == null || value.toString().isBlank()) {
                        ps.setObject(i + 1, null);
                        continue;
                    }

                    try {
                        switch (field) {
                            case "price" -> ps.setFloat(i + 1, Float.parseFloat(value.toString()));
                            case "available" -> ps.setBoolean(i + 1, Boolean.parseBoolean(value.toString()));
                            case "publication_date" -> ps.setDate(i + 1, java.sql.Date.valueOf(value.toString()));
                            default -> ps.setString(i + 1, value.toString());
                        }
                    } catch (Exception e) {
                        System.out.println("Ошибка в поле " + field + ": " + value);
                        ps.setObject(i + 1, null);
                    }
                }

                ps.addBatch();
                count++;

                if (count % BATCH_SIZE == 0) {
                    ps.executeBatch();
                }
            }

            ps.executeBatch();
            System.out.println("Импорт завершён. Книг добавлено: " + count);
        }
    }
}
