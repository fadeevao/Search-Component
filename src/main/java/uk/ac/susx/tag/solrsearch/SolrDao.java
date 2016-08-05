package uk.ac.susx.tag.solrsearch;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocumentList;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * Puts documents into Solr and retrieves them
 */
public class SolrDao<T> {

    private SolrClient solr = null;

    public SolrDao (String solrURL)
    {
        solr = (HttpSolrClient) SolrClientFactory.getInstance().createClient(solrURL);
        //add solr config?
    }

    public SolrDao(SolrClient solr) {
        this.solr = solr;
    }

    public void addCollection(Collection<T> dao)
    {
        try
        {
            UpdateResponse rsp = solr.addBeans(dao);
            solr.commit();
            System.out.println ("Added documents to solr. Time taken = " + rsp.getElapsedTime() + ". " + rsp.toString());
        }
        catch (SolrServerException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public SolrDocumentList readAll ()
    {
        SolrQuery query = new SolrQuery();
        query.setQuery( "*:*" );

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

    public void addDoc (SolrItem doc)
    {
        addCollection(createSingletonSet(doc));
    }

    private Collection<T> createSingletonSet(SolrItem doc) {
        if (doc == null)
            return Collections.emptySet();
        return Collections.singleton((T) doc);
    }

    public SolrClient getSolr() {
        return solr;
    }
}
