package ru.kate.multiproject;

import ru.kate.multiproject.service.JsonProcessor;

public class CliJsonStaxApplication {
    public static void main(String[] args) {
        if (args.length < 2 || args.length > 4) {
            System.out.println("Использование: java -jar CLI_JSON.jar <путь_к_JSON> <путь_к_маппингу> [batch_size] [thread_count]");
            return;
        }

        String jsonPath = args[0];
        String mappingPath = args[1];

        int batchSize = 100;
        int threadCount = 3;

        if (args.length >= 3) {
            try {
                batchSize = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: batch_size должен быть числом. Используется значение по умолчанию: 100");
            }
        }

        if (args.length == 4) {
            try {
                threadCount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: thread_count должен быть числом. Используется значение по умолчанию: 3");
            }
        }

        AppConfig config = new AppConfig();
        String solrUrl = config.getSolrUrl();
        String collection = config.getSolrCollection();

        JsonProcessor processor = new JsonProcessor(solrUrl, collection, mappingPath, threadCount, batchSize);
        processor.validateParams(jsonPath);
        processor.processJson(jsonPath);
    }
}
