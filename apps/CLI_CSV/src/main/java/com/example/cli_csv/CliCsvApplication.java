package com.example.cli_csv;

import com.example.libs.service.CsvProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.example")
public class CliCsvApplication implements CommandLineRunner {

    private final CsvProcessor csvProcessor;

    public CliCsvApplication(CsvProcessor csvProcessor) {
        this.csvProcessor = csvProcessor;
    }

    public static void main(String[] args) {
        SpringApplication.run(CliCsvApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if (args.length != 3) {
            System.out.println("Использование: java -jar CLI_CSV.jar <путь_к_CSV> <путь_к_маппингу> <Solr_URL>");
            return;
        }

        String csvPath = args[0];
        String mappingPath = args[1];
        String solrUrl = args[2];

        // Создаем процессор и запускаем обработку CSV
        csvProcessor.processCsv(csvPath);
        System.out.println("CSV обработан и данные загружены в Solr.");
    }
}
