package com.example.cli_db;

import com.example.cli_db.service.DbService;
import com.example.cli_db.solr.SolrUpload;

public class CliDbApplication {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Использование: java -jar CLI_DB.jar <mapper.properties> <solr_url> <db.properties>");
            return;
        }

        String mapperPath = args[0];
        String solrUrl = args[1];
        String dbPropsPath = args[2];

        try {
            DbService dbService = new DbService(dbPropsPath, mapperPath);
            dbService.initSchema();
            SolrUpload solrUpload = new SolrUpload(solrUrl, mapperPath);
            dbService.menu(solrUpload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
