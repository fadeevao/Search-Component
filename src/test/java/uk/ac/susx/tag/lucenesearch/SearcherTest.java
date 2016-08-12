package uk.ac.susx.tag.lucenesearch;


import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;
import org.mockito.Mockito;
import uk.ac.susx.tag.inputData.FileType;
import uk.ac.susx.tag.inputData.InputData;
import uk.ac.susx.tag.lucenesearch.exception.IndexingException;
import uk.ac.susx.tag.lucenesearch.neighbours.NeighbourSearcher;
import uk.ac.susx.tag.lucenesearch.neighbours.NeighbourSuggestion;
import uk.ac.susx.tag.lucenesearch.query_expansion.QueryBuilder;
import uk.ac.susx.tag.lucenesearch.result.SearchResult;
import uk.ac.susx.tag.lucenesearch.result.SearchResultWithSuggestions;
import uk.ac.susx.tag.neighbours.exception.InvalidFileFormatException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class SearcherTest {

    @Test
    public void testTermSearchForPhrase() throws IOException, ParseException, InvalidTokenOffsetsException, IndexingException {
        Searcher searcher = initialiseSearcherWithSampleTexts();
        List<SearchResult> topHits = searcher.searchFor("\"hit a deer\"").getSearchResults();
        assertEquals(1, topHits.size());

        topHits = searcher.searchFor("\"each other\"").getSearchResults();
        assertEquals(2, topHits.size());

        topHits = searcher.searchFor("\"EACH OTHER\"").getSearchResults();
        assertEquals(2, topHits.size());

        topHits = searcher.searchFor("\"EACH oThEr\"").getSearchResults();
        assertEquals(2, topHits.size());

        topHits = searcher.searchFor("\"I could tell he was trying to decide if he should believe me\"").getSearchResults();
        assertEquals(1, topHits.size());
    }

    private Searcher initialiseSearcherWithSampleTexts() throws IOException {
        Directory ramDirectory = new RAMDirectory();
        Indexer indexer = new Indexer(ramDirectory);
        indexer.createIndex("src/test/resources/lucenedata/samplefiles", FileType.TEXT_FILE);
        Searcher searcher = new Searcher(ramDirectory);
        QueryBuilder queryBuilder = new QueryBuilder();
        searcher.setQueryBuilder(queryBuilder);
        return searcher;
    }


    @Test
    public void testStopWords() throws IOException, InvalidTokenOffsetsException, ParseException, IndexingException {
        Searcher searcher = initialiseSearcherWithSampleTexts();
        List<SearchResult> topHits = searcher.searchFor("and").getSearchResults();
        assertEquals(4, topHits.size());
    }

    @Test
    public void testPluralTerms() throws IOException, InvalidTokenOffsetsException, ParseException, IndexingException {
        Directory ramDirectory = new RAMDirectory();
        Indexer indexer = new Indexer(ramDirectory);
        InputData inputData = new InputData("1", "soldiers");
        indexer.indexData(inputData);
        indexer.close();
        Searcher searcher = new Searcher(ramDirectory);
        QueryBuilder queryBuilder = new QueryBuilder();
        searcher.setQueryBuilder(queryBuilder);
        List<SearchResult> topHits = searcher.searchFor("soldier").getSearchResults();
        assertEquals(1, topHits.size());
    }

    @Test
    public void testSuggestionsGeneratedForSingleTermQuery() throws IOException, InvalidTokenOffsetsException, ParseException, InvalidFileFormatException, IndexingException {
        Directory ramDirectory = new RAMDirectory();
        generateData(ramDirectory);
        Searcher searcher = new Searcher(ramDirectory);

        NeighbourSearcher neighbourSearcher = Mockito.mock(NeighbourSearcher.class);
        when(neighbourSearcher.generateQueryTermsBasedOnTheNeighboursOfHighlightedWords(any(), any())).thenReturn(new NeighbourSuggestion(Arrays.asList("troops"), Collections.EMPTY_SET));
        QueryBuilder queryBuilder = new QueryBuilder(neighbourSearcher);
        searcher.setQueryBuilder(queryBuilder);

        SearchResultWithSuggestions searchResultWithSuggestions = searcher.searchFor("soldier");
        List<SearchResult> initialResults = searchResultWithSuggestions.getSearchResults();
        assertEquals(2, initialResults.size()); //more results here as we make the term fuzzy TODO???
        List<String> suggestions = searchResultWithSuggestions.getSuggestionsWrapper().getNeighbourSuggestions();
        List<String> userSelection = new ArrayList<>(Arrays.asList(suggestions.get(0)));

        SearchResultWithSuggestions finalResults = searcher.expandSearchResultsWithUserSelectedTerms(searchResultWithSuggestions.getSearchResults(),userSelection);
        assertEquals(finalResults.getSearchResults(), initialResults);
        assertEquals("5", finalResults.getSearchResultsAfterExpansion().get(0).getFileId());
    }

    @Test
    public void testSuggestionsGeneratedForMultipleWordQuery() throws IOException, InvalidTokenOffsetsException, ParseException, IndexingException {
        Directory ramDirectory = new RAMDirectory();
        generateData(ramDirectory);
        Searcher searcher = new Searcher(ramDirectory);

        NeighbourSearcher neighbourSearcher = Mockito.mock(NeighbourSearcher.class);
        when(neighbourSearcher.generateQueryTermsBasedOnTheNeighboursOfHighlightedWords(any(), any())).thenReturn(new NeighbourSuggestion(Arrays.asList("troops", "always"), Collections.EMPTY_SET));
        QueryBuilder queryBuilder = new QueryBuilder(neighbourSearcher);
        searcher.setQueryBuilder(queryBuilder);

        SearchResultWithSuggestions searchResultWithSuggestions = searcher.searchFor("soldier is forever");

        List<SearchResult> initialResults = searchResultWithSuggestions.getSearchResults();
        assertEquals(3, initialResults.size());
        List<String> suggestions = searchResultWithSuggestions.getSuggestionsWrapper().getNeighbourSuggestions();
        List<String> userSelection = new ArrayList<>();
        userSelection.add(suggestions.get(0));
        userSelection.add(suggestions.get(1));

        SearchResultWithSuggestions finalResults = searcher.expandSearchResultsWithUserSelectedTerms(searchResultWithSuggestions.getSearchResults(),userSelection);
        assertEquals(finalResults.getSearchResults(), initialResults);
        assertEquals("5", finalResults.getSearchResultsAfterExpansion().get(0).getFileId());
        assertEquals("6", finalResults.getSearchResultsAfterExpansion().get(1).getFileId());
    }

    @Test
    public void testNoResultsReturnedWhenUserSelectsAdditionalTerms() throws IOException, InvalidTokenOffsetsException, ParseException, InvalidFileFormatException, IndexingException {
        Directory ramDirectory = new RAMDirectory();
        generateData(ramDirectory);
        Searcher searcher = new Searcher(ramDirectory);

        NeighbourSearcher neighbourSearcher = Mockito.mock(NeighbourSearcher.class);
        when(neighbourSearcher.generateQueryTermsBasedOnTheNeighboursOfHighlightedWords(any(), any())).thenReturn(new NeighbourSuggestion(Arrays.asList("summer", "time"), Collections.EMPTY_SET));
        QueryBuilder queryBuilder = new QueryBuilder(neighbourSearcher);
        searcher.setQueryBuilder(queryBuilder);

        SearchResultWithSuggestions searchResultWithSuggestions = searcher.searchFor("soldier is forever");

        List<SearchResult> initialResults = searchResultWithSuggestions.getSearchResults();
        assertEquals(3, initialResults.size());
        List<String> suggestions = searchResultWithSuggestions.getSuggestionsWrapper().getNeighbourSuggestions();
        List<String> userSelection = new ArrayList<>();
        userSelection.add(suggestions.get(0));
        userSelection.add(suggestions.get(1));

        SearchResultWithSuggestions finalResults = searcher.expandSearchResultsWithUserSelectedTerms(searchResultWithSuggestions.getSearchResults(),userSelection);
        assertEquals(finalResults.getSearchResults(), initialResults);
        //as what user selected is not anywhere in the documents
        assertTrue(finalResults.getSearchResultsAfterExpansion().isEmpty());
    }

    @Test
    public void testCharacterEscape() throws IOException, InvalidTokenOffsetsException, ParseException, IndexingException {
        Directory ramDirectory = new RAMDirectory();
        generateData(ramDirectory);
        Searcher searcher = new Searcher(ramDirectory);
        QueryBuilder queryBuilder = new QueryBuilder();
        searcher.setQueryBuilder(queryBuilder);
        searcher.searchFor("ok/");
    }

    @Test
    public void testResultsReturnedAsExpected() throws IOException, InvalidTokenOffsetsException, ParseException {
        Directory ramDirectory = new RAMDirectory();
        generateHillaryData(ramDirectory);
        Searcher searcher = new Searcher(ramDirectory);
        QueryBuilder queryBuilder = new QueryBuilder();
        searcher.setQueryBuilder(queryBuilder);
        SearchResultWithSuggestions results = searcher.searchFor("hillary clinton");
        assertEquals("2", results.getSearchResults().get(0).getFileId());
    }

    private void generateHillaryData(Directory ramDirectory) throws IOException {
        Indexer indexer = new Indexer(ramDirectory);
        InputData inputData = new InputData("1", "bullary clinton");
        indexer.indexData(inputData);
        InputData inputData1 = new InputData("2", "hillary clinton");
        indexer.indexData(inputData1);
        indexer.close();
    }

    private void generateData(Directory ramDirectory) throws IOException {
        Indexer indexer = new Indexer(ramDirectory);
        InputData inputData = new InputData("1", "solders fighting");
        indexer.indexData(inputData);
        InputData inputData1 = new InputData("2", "soldiers are good");
        indexer.indexData(inputData1);
        InputData inputData2 = new InputData("3", "solders forever");
        indexer.indexData(inputData2);
        InputData inputData3 = new InputData("4", "soldier for life");
        indexer.indexData(inputData3);
        InputData inputData4 = new InputData("5", "troops work");
        indexer.indexData(inputData4);
        InputData inputData5 = new InputData("6", "always work");
        indexer.indexData(inputData5);
        indexer.close();
    }
}
