package uk.ac.susx.tag.solrsearch;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
Creates a single instance of Solr client for reuse in the app
 */
public class SolrClientFactory {

    Map<String, SolrClient> urlToSolrClient = new ConcurrentHashMap<String, SolrClient>();
    static SolrClientFactory instance = new SolrClientFactory();

    public static SolrClientFactory getInstance()
    {
        return instance;
    }

    private SolrClientFactory() {}

    public SolrClient createClient (String solrURL)
    {
        if (urlToSolrClient.containsKey(solrURL))
            return urlToSolrClient.get(solrURL);

        SolrClient client = new HttpSolrClient.Builder(solrURL).build();
        urlToSolrClient.put(solrURL, client);
        return client;
    }
}
