package com.example.service;

import com.example.libs.solr.SolrUpload;

public class SolrUploadService {

    private final SolrUpload solrUpload;

    public SolrUploadService(String solrUrl, String mappingPath) {
        this.solrUpload = new SolrUpload(solrUrl, mappingPath);
    }

    public void uploadToSolr(String csvPath) {
        // Логика для загрузки данных в Solr
        solrUpload.uploadToSolr(csvPath);
    }
}
