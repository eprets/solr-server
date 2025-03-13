package com.example.libs.solr;

import org.apache.solr.client.solrj.impl.HttpSolrClient;

public class SolrConfig {
    private final HttpSolrClient solrClient;

    public SolrConfig(String solrUrl) {
        this.solrClient = new HttpSolrClient.Builder(solrUrl).build();
    }

    public HttpSolrClient getSolrClient() {
        return solrClient;
    }
}
