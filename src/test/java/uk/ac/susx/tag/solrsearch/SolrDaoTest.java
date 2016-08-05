package uk.ac.susx.tag.solrsearch;


import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.JettySolrRunner;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SolrDaoTest {

    static SolrClient solr;

    static JettySolrRunner jettySolrRunner;



    @Before
    public void setUpSolr() throws Exception {

//        System.setProperty("solr.solr.home", "src/test/resources/solr");
//        System.setProperty("solr.data.dir", "target/test-classes/solr/data");
//
//
//        // Instruct Solr to keep the index in memory, for faster testing.
//        System.setProperty("solr.directoryFactory", "solr.RAMDirectoryFactory");
//        String solrDir = "src/test/resources/solr";
//
//
//
//        jettySolrRunner = new JettySolrRunner("src/test/resources/solr", "/solr", 8080);
//        jettySolrRunner.start();
//
//        solr = new HttpSolrClient.Builder("http://localhost:8080/solr").build();
    }

    @Test
    public void testAddDocumentsAndReadAll() throws IOException, SolrServerException {
        SolrDao<SolrItem> dao = new SolrDao<>("http://localhost:8983/solr/testcore");
        List<SolrItem> documentCollection = getDocumentCollection();
        dao.getSolr().deleteByQuery("*:*");
        dao.addCollection(documentCollection);
        assertEquals(2, dao.readAll().size());
    }

    private List<SolrItem> getDocumentCollection() {
        List<SolrItem> collection = new ArrayList<>();
        collection.add(new SolrItem("1", "doc1"));
        collection.add(new SolrItem("2", "doc2"));
        return collection;
    }


}
