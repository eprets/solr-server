package com.example.cli_json_stax;

import com.example.cli_json_stax.service.JsonProcessor;

public class CliJsonStaxApplication {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Использование: java -jar CLI_JSON.jar <путь_к_JSON> <путь_к_маппингу> <Solr_URL>");
            return;
        }

        String jsonPath = args[0];
        String mappingPath = args[1];
        String solrUrl = args[2];

        JsonProcessor processor = new JsonProcessor(solrUrl, mappingPath);
        processor.processJson(jsonPath);
    }
}