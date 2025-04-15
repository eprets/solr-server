package ru.kate.multiproject;


import ru.kate.multiproject.service.DbService;
import ru.kate.multiproject.solr.SolrDbUpload;

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

            SolrDbUpload solrDbUpload = new SolrDbUpload(solrUrl, collection, mapperPath);
            DbService dbService = new DbService(dbPropsPath, solrDbUpload, solrUrl, collection, mapperPath);
            dbService.validateParams(mapperPath);
            dbService.initSchema();
            dbService.menu(solrDbUpload);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
