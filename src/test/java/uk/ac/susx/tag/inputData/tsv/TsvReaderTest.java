package uk.ac.susx.tag.inputData.tsv;


import org.apache.commons.io.FileUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.junit.Test;
import uk.ac.susx.tag.inputData.InputData;
import uk.ac.susx.tag.lucenesearch.Indexer;
import uk.ac.susx.tag.lucenesearch.Searcher;
import uk.ac.susx.tag.lucenesearch.exception.IndexingException;
import uk.ac.susx.tag.lucenesearch.query_expansion.QueryBuilder;
import uk.ac.susx.tag.lucenesearch.result.SearchResultWithSuggestions;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TsvReaderTest {

    @Test
    public void testTsvReader() throws IOException {
        File file = FileUtils.toFile(Thread.currentThread().getContextClassLoader().getResource("tsv/trump.tsv"));
        List<InputData> data = TsvReader.getTsvData(file);
        assertEquals(5, data.size());
    }

    @Test
    public void testTsvReaderu() throws IOException, InvalidTokenOffsetsException, ParseException, IndexingException {
        String indexDirectoryPath = "src/test/resources/testing-index-directory";
        Indexer indexer = new Indexer(indexDirectoryPath);

        File file = FileUtils.toFile(Thread.currentThread().getContextClassLoader().getResource("tsv/big.tsv"));
        List<InputData> data = TsvReader.getTsvData(file);
        for (InputData data1 : data) {
            indexer.indexData(data1);
        }
        indexer.close();

        Searcher searcher = new Searcher(indexDirectoryPath);
        QueryBuilder queryBuilder = new QueryBuilder();
        searcher.setQueryBuilder(queryBuilder);
        SearchResultWithSuggestions searchResultWithSuggestions = searcher.searchFor("hillary clinton");
        searchResultWithSuggestions.getSearchResults();
    }


}
