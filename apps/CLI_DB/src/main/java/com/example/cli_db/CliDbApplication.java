package com.example.cli_db;

import com.example.cli_db.service.DbService;
import com.example.cli_db.solr.SolrDbUpload;
import com.example.common.AppConfig;

public class CliDbApplication {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Использование: java -jar CLI_DB.jar <mapper.properties> <db.properties>");
            return;
        }

        String mapperPath = args[0];
        String dbPropsPath = args[1];

        try {
            AppConfig config = new AppConfig();
            String solrUrl = config.getSolrUrl();
            String collection = config.getSolrCollection();

            DbService dbService = new DbService(dbPropsPath, mapperPath);
            dbService.initSchema();
            SolrDbUpload solrDbUpload = new SolrDbUpload(solrUrl, collection, mapperPath);
            dbService.menu(solrDbUpload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
