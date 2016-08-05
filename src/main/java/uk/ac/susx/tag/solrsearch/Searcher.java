package uk.ac.susx.tag.solrsearch;


import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import java.io.IOException;

public class Searcher {

    private SolrClient solr = null;

    public Searcher (String solrURL)
    {
        solr = (HttpSolrClient) SolrClientFactory.getInstance().createClient(solrURL);
    }

    public SolrDocumentList searchForTerm(String term) {
        SolrQuery query = new SolrQuery();
        query.setQuery(term);
        QueryResponse rsp = null;
        try
        {
            rsp = solr.query( query );
        }
        catch (SolrServerException e)
        {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rsp.getResults();
    }
}

