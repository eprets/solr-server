package ru.kate.multiproject.solr;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import ru.kate.multiproject.service.MapperService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SolrDbUpload {
    private final SolrClient solrClient;
    private final MapperService mapperService;
    private final String collection;
    private final String solrUrl;
    private static final int BATCH_SIZE = 25;

    public SolrDbUpload(String solrUrl, String collection, String mappingPath) {
        this.solrClient = new HttpSolrClient.Builder(solrUrl).build();

        this.mapperService = new MapperService(mappingPath);
        this.collection = collection;
        this.solrUrl = solrUrl;
    }

    public void uploadFromDb(Connection conn) throws Exception {
        SolrUpload helper = new SolrUpload(solrUrl, collection);
        if (helper.ensureSolrAndCores()) return;

        String sql = "SELECT * FROM books";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            List<SolrInputDocument> batch = new ArrayList<>();
            int totalUploaded = 0;

            while (rs.next()) {
                SolrInputDocument doc = new SolrInputDocument();
                ResultSetMetaData meta = rs.getMetaData();

                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    String column = meta.getColumnName(i);
                    String solrField = mapperService.getSolrFieldName(column.toLowerCase());

                    Object value = rs.getObject(i);
                    //System.out.printf(" Чтение колонки: '%s' => Solr поле: '%s' | Значение: %s%n", column, solrField, value);

                    if (value instanceof Clob) {
                        Clob clob = (Clob) value;
                        value = clob.getSubString(1, (int) clob.length());
                    }
                    if (value != null) {
                        doc.addField(solrField, value);
                    }
                }

                batch.add(doc);

                if (batch.size() >= BATCH_SIZE) {
                    /*System.out.println("📦 Отправка документов в Solr:");
                    for (SolrInputDocument d : batch) {
                        System.out.println("️-> " + d);
                    }*/
                    solrClient.add(collection, batch);
                    solrClient.commit(collection);
                    totalUploaded += batch.size();
                    System.out.println("Загружено в Solr: " + totalUploaded + " документов...");
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                solrClient.add(collection, batch);
                solrClient.commit(collection);
                totalUploaded += batch.size();
                System.out.println("Загружено в Solr: " + totalUploaded + " документов (финальный батч).");
            }

            if (totalUploaded == 0) {
                System.out.println("Нет данных для отправки в Solr.");
            } else {
                System.out.println("Всего загружено в Solr: " + totalUploaded + " документов.");
            }
        }
    }
    public String getCollection() {
        return collection;
    }
}
