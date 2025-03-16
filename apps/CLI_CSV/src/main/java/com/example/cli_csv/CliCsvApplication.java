package org.example.cli_csv;

import com.example.libs.service.CsvProcessor;
import com.example.libs.solr.SolrUpload;

public class CliCsvApplication {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Использование: java -jar CLI_CSV.jar <путь_к_CSV> <путь_к_маппингу> <Solr_URL>");
            return;
        }

        String csvPath = args[0];
        String mappingPath = args[1];
        String solrUrl = args[2];

        // Создаем процессор и запускаем обработку CSV
        CsvProcessor processor = new CsvProcessor(solrUrl, mappingPath);
        processor.processCsv(csvPath);
    }
}
