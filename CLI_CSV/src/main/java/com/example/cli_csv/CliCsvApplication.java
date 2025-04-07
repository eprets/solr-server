package com.example.cli_csv;

import com.example.cli_csv.service.CsvProcessor;

public class CliCsvApplication {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Использование: java -jar CLI_CSV.jar <путь_к_CSV> <путь_к_маппингу>");
            return;
        }

        String csvPath = args[0];
        String mappingPath = args[1];

        AppConfig config = new AppConfig();
        String solrUrl = config.getSolrUrl();
        String solrCollection = config.getSolrCollection();

        CsvProcessor processor = new CsvProcessor(solrUrl, solrCollection, mappingPath);
        processor.processCsv(csvPath);
    }
}
