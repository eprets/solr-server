package com.example.cli_db;

import com.example.cli_db.service.DBProcessor;

public class CliDbApplication {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Использование: java -jar CLI_DB.jar <путь_к_маппингу> <Solr_URL> <путь_к_DB_props>");
            return;
        }

        String mappingPath = args[0];
        String solrUrl = args[1];
        String dbPropsPath = args[2];

        DBProcessor processor = new DBProcessor(solrUrl, mappingPath, dbPropsPath);
        processor.processDB();
    }
}
