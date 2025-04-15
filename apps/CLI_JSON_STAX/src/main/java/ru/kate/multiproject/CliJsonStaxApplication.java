package ru.kate.multiproject;

import ru.kate.multiproject.service.JsonProcessor;

public class CliJsonStaxApplication {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Использование: java -jar CLI_JSON.jar <путь_к_JSON> <путь_к_маппингу>");
            return;
        }

        String jsonPath = args[0];
        String mappingPath = args[1];

        AppConfig config = new AppConfig();
        String solrUrl = config.getSolrUrl();
        String collection = config.getSolrCollection();

        JsonProcessor processor = new JsonProcessor(solrUrl, collection, mappingPath);
        processor.validateParams(jsonPath);
        processor.processJson(jsonPath);
    }
}
