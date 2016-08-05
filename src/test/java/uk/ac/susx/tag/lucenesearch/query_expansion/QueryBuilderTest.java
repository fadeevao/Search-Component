package uk.ac.susx.tag.lucenesearch.query_expansion;


import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertTrue;

public class QueryBuilderTest {

    @Test
    public void expandSingleTermQuery() throws IOException {
        QueryBuilder queryBuilder = new QueryBuilder();
        String result = queryBuilder.expandSingleTermQuery("libraru");
        assertTrue(result.contains("library"));
        assertTrue(result.contains("libraru~0.8"));
    }
}
