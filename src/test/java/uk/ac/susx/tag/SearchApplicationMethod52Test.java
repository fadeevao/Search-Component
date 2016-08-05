package uk.ac.susx.tag;


import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.junit.Test;
import uk.ac.susx.tag.lucenesearch.exception.IndexingException;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.types.RuntimeType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/*
Test class to demonstrate how SearchComponent can be used within Method52 application
 */
public class SearchApplicationMethod52Test {

    private final static String ID_KEY = "id";
    private final static String MAIN_BODY_KEY = "body";

    @Test
    public void testSearchComponentIntegrationWithoutNeighboursFilePresent() throws ParseException, InvalidTokenOffsetsException, IOException, IndexingException {
        SearchApplicationMethod52 searchApplicationMethod52 = new SearchApplicationMethod52();
        String indexDirectoryPath = "src/test/resources/testing-index-directory";

        Key<String> id = Key.of(ID_KEY, RuntimeType.STRING);
        Key<String> mainBody = Key.of(MAIN_BODY_KEY, RuntimeType.STRING);
        List<Datum> searchData = getSearchData(id, mainBody);
        List<Datum> relevantDocs = searchApplicationMethod52.integrationWithoutNeighbourFilePresent(searchData, ID_KEY, MAIN_BODY_KEY, indexDirectoryPath, "once");
        assertEquals(1, relevantDocs.size());
        assertEquals("once upon a time", relevantDocs.get(0).get(mainBody));
        assertEquals("one", relevantDocs.get(0).get(id));
    }

    @Test
    public void testSearchComponentIntegrationWithNeighboursFilePresent() throws ParseException, InvalidTokenOffsetsException, IOException, IndexingException {
        SearchApplicationMethod52 searchApplicationMethod52 = new SearchApplicationMethod52();
        String indexDirectoryPath = "src/test/resources/testing-index-directory";
        String neighbourFilePath = "src/test/resources/neighbours/wiki-test";

        Key<String> id = Key.of(ID_KEY, RuntimeType.STRING);
        Key<String> mainBody = Key.of(MAIN_BODY_KEY, RuntimeType.STRING);
        List<Datum> searchData = getSearchData(id, mainBody);
        List<Datum> relevantDocs = searchApplicationMethod52.integrationWithNeighbourFilePresent(searchData, ID_KEY, MAIN_BODY_KEY, indexDirectoryPath, "once", neighbourFilePath);
        assertEquals(1, relevantDocs.size());
        assertEquals("once upon a time", relevantDocs.get(0).get(mainBody));
        assertEquals("one", relevantDocs.get(0).get(id));
    }


    private List<Datum> getSearchData(Key<String> id, Key<String> mainBody) {
        Datum datumOne = new Datum();
        datumOne = datumOne
                .with(mainBody, "once upon a time")
                .with(id, "one");
        Datum datumTwo = new Datum();
        datumTwo = datumTwo
                .with(mainBody, "a woodcutter lived happily with his wife")
                .with(id, "two");
       return new ArrayList<>(Arrays.asList(datumOne, datumTwo));
    }
}
