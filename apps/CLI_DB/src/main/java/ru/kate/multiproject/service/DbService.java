package ru.kate.multiproject.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import liquibase.Liquibase;
import liquibase.database.Database;
import ru.kate.multiproject.solr.SolrDbUpload;
import ru.kate.multiproject.solr.SolrUpload;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import com.fasterxml.jackson.core.type.TypeReference;

import org.springframework.util.StopWatch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class DbService {
    private final SolrDbUpload solrUploader;
    private final String solrUrl;
    private final String collection;
    private final Connection connection;
    private final MapperService mapperService;
    private static final int BATCH_SIZE = 25;

    public DbService(String dbPropsPath, SolrDbUpload solrUploader, String solrUrl, String collection, String mappingPath) throws Exception {
        this.solrUploader = solrUploader;
        this.solrUrl = solrUrl;
        this.collection = collection;
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

    public void validateParams(String mappingPath) {
        validateFile(mappingPath, "mapping");

        SolrUpload helper = new SolrUpload(solrUrl, collection);
        if (helper.ensureSolrAndCores()) {
            throw new RuntimeException("Solr или core недоступны. Загрузка отменена.");
        }
        if (helper.checkCoreAvailability()) {
            System.out.println("Core " + solrUploader.getCollection() + " not found. Trying create core...");
            helper.createCore();
        }
    }

    private void validateFile(String filePath, String fileType) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Неверный путь до " + fileType + " файла: " + filePath);
        }
    }


    public void initSchema() throws Exception {
        Database database = liquibase.database.DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new Liquibase("changelog/db.changelog-master.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.update((String) null);
    }

    public void menu(SolrDbUpload solrUpload) throws Exception {
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

    /*public void importFromFile(String path) throws Exception {
        if (path.endsWith(".json")) {
            importFromJson(path);
        } else if (path.endsWith(".csv")) {
            importFromCsv(path);
        } else {
            System.out.println("Поддерживаются только .json и .csv");
        }
    }*/

    public void importFromFile(String path) throws Exception {
        StopWatch stopWatch = new StopWatch("Импорт книги");
        stopWatch.start("Обработка файла");

        if (path.endsWith(".json")) {
            importFromJson(path);
        } else if (path.endsWith(".csv")) {
            importFromCsv(path);
        } else {
            System.out.println("Поддерживаются только .json и .csv");
            return;
        }

        stopWatch.stop();
        System.out.println("Время импорта: " + stopWatch.getTotalTimeMillis() + " мс");
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

        StopWatch stopWatch = new StopWatch("Вставка в базу");
        stopWatch.start();

        List<String> dbFields = new ArrayList<>(mapperService.getFieldsMapping().keySet());

        String sql = "MERGE INTO books (" +
                String.join(", ", dbFields) +
                ") KEY(id) VALUES (" +
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

            stopWatch.stop();

            System.out.println("Импорт завершён. Книг добавлено: " + count);

            System.out.println("Время вставки в базу: " + stopWatch.getTotalTimeMillis() + " мс");
        }
    }
}
