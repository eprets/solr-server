package ru.kate.multiproject.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.kate.multiproject.solr.SolrJsonUpload;
import ru.kate.multiproject.solr.SolrUpload;
import org.springframework.util.StopWatch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class JsonProcessor {
    private final SolrJsonUpload solrUploader;
    private final String mappingPath;
    private final String solrUrl;
    private final String collection;
    private static final int BATCH_SIZE = 1000;
    private final int threadCount;

    private int counter = 0;

    public JsonProcessor(String solrUrl, String collection, String mappingPath) {
        this(solrUrl, collection, mappingPath, 2);
    }

    public JsonProcessor(String solrUrl, String collection, String mappingPath, int threadCount) {
        this.solrUploader = new SolrJsonUpload(solrUrl, collection, mappingPath);
        this.mappingPath = mappingPath;
        this.solrUrl = solrUrl;
        this.collection = collection;
        this.threadCount = threadCount;
    }

    public void validateParams(String jsonPath) {
        validateFile(jsonPath, "JSON");
        validateFile(mappingPath, "mapping");

        SolrUpload helper = new SolrUpload(solrUrl, collection);
        if (!helper.checkSolrAvailability()) {
            throw new RuntimeException("Solr недоступен. Завершение работы.");
        }

        if (helper.checkCoreAvailability()) {
            System.out.println("Core " + solrUploader.getCollection() + " не найден. Создаём core...");
            helper.createCore();
        }
    }

    public void processJson(String jsonPath) {
        StopWatch stopWatch = new StopWatch("Обработка JSON");
        stopWatch.start("Чтение и загрузка данных");

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        int totalBooks = 0;
        long[] totalUploadTimeMs = {0};

        try {
            System.out.println("Начинаем обработку JSON...");
            File jsonFile = new File(jsonPath);

            List<JsonNode> booksBatch = new ArrayList<>();
            JsonFactory jsonFactory = new JsonFactory();

            try (JsonParser jsonParser = jsonFactory.createParser(new FileInputStream(jsonFile))) {
                if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
                    System.out.println("Неверный формат JSON: ожидался массив объектов.");
                    return;
                }

                while (jsonParser.nextToken() == JsonToken.START_OBJECT) {
                    try {
                        JsonNode bookJsonNode = parseJsonToNode(jsonParser);
                        booksBatch.add(bookJsonNode);
                    } catch (Exception e) {
                        System.out.println("Ошибка при разборе объекта JSON: " + e.getMessage());
                    }

                    if (booksBatch.size() >= BATCH_SIZE) {
                        List<JsonNode> batchToUpload = new ArrayList<>(booksBatch);
                        booksBatch.clear();

                        totalBooks += batchToUpload.size();

                        executorService.submit(() -> {
                            long uploadStart = System.currentTimeMillis();
                            try {
                                solrUploader.uploadToSolr(batchToUpload);
                            } catch (Exception e) {
                                System.out.println("Ошибка при загрузке батча в Solr: " + e.getMessage());
                            }
                            long uploadEnd = System.currentTimeMillis();
                            synchronized (totalUploadTimeMs) {
                                totalUploadTimeMs[0] += (uploadEnd - uploadStart);
                            }
                            synchronized (this) {
                                counter++;
                            }
                            System.out.println("Пакет загружен (" + batchToUpload.size() + " книг).");
                        });
                    }
                }

                if (!booksBatch.isEmpty()) {
                    List<JsonNode> batchToUpload = new ArrayList<>(booksBatch);
                    booksBatch.clear();

                    totalBooks += batchToUpload.size();

                    executorService.submit(() -> {
                        long uploadStart = System.currentTimeMillis();
                        try {
                            solrUploader.uploadToSolr(batchToUpload);
                        } catch (Exception e) {
                            System.out.println("Ошибка при загрузке последней партии в Solr: " + e.getMessage());
                        }
                        long uploadEnd = System.currentTimeMillis();
                        synchronized (totalUploadTimeMs) {
                            totalUploadTimeMs[0] += (uploadEnd - uploadStart);
                        }
                        synchronized (this) {
                            counter++;
                        }
                        System.out.println("Финальный пакет загружен (" + batchToUpload.size() + " книг).");
                    });
                }

            } catch (JsonParseException e) {
                System.out.println("Ошибка разбора JSON: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("Ошибка чтения JSON: " + e.getMessage());
            }

            System.out.println("Обработка JSON завершена!");

        } catch (Exception e) {
            System.out.println("Непредвиденная ошибка: " + e.getMessage());
        } finally {
            executorService.shutdown();
            try {
                executorService.awaitTermination(1, TimeUnit.HOURS);
            } catch (InterruptedException e) {
                System.out.println("Ожидание завершения потоков прервано: " + e.getMessage());
            }

            stopWatch.stop();
            System.out.println("Общее время обработки JSON: " + stopWatch.getTotalTimeMillis() + " мс");

            if (totalBooks > 0 && counter > 0) {
                double avgMsPerRequest = (double) totalUploadTimeMs[0] / counter;
                double avgMsPerBook = (double) totalUploadTimeMs[0] / totalBooks;

                System.out.println("Статистика загрузки:");
                System.out.printf("Общее время загрузки в Solr: %d мс%n", totalUploadTimeMs[0]);
                System.out.printf("Среднее время на один запрос (пакет): %.2f мс%n", avgMsPerRequest);
                System.out.printf("Среднее время на одну книгу: %.2f мс%n", avgMsPerBook);
            }

            System.out.println("Программа завершила работу.");
        }
    }

    private void validateFile(String filePath, String fileType) throws IllegalArgumentException {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Некорректный путь к файлу " + fileType + ": " + filePath);
        }
    }

    private JsonNode parseJsonToNode(JsonParser jsonParser) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        jsonParser.setCodec(objectMapper);
        return objectMapper.readTree(jsonParser);
    }
}
