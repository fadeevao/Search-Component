package uk.ac.susx.tag.lucenesearch;


import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import uk.ac.susx.tag.exportData.CsvExporter;
import uk.ac.susx.tag.inputData.FileType;
import uk.ac.susx.tag.lucenesearch.exception.IndexingException;
import uk.ac.susx.tag.lucenesearch.neighbours.NeighbourSuggestion;
import uk.ac.susx.tag.lucenesearch.query_expansion.QueryBuilder;
import uk.ac.susx.tag.lucenesearch.query_expansion.highlighter.Highlighter;
import uk.ac.susx.tag.lucenesearch.result.FinalSearchResults;
import uk.ac.susx.tag.lucenesearch.result.SearchResult;
import uk.ac.susx.tag.lucenesearch.result.SearchResultWithSuggestions;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Searcher {

    private IndexSearcher indexSearcher;
    private Highlighter highlighter;

    private QueryParser queryParser;
    private Analyzer analyzer;
    private Directory indexDirectory;
    private QueryBuilder queryBuilder;
    private IndexReader indexReader;
    private FileType fileType; //need it for UI/highlighting

    final int maxNumberOfCachedQueries = 256;
    final long maxRamBytesUsed = 50 * 1024L * 1024L; // 50MB
    final LRUQueryCache queryCache = new LRUQueryCache(maxNumberOfCachedQueries, maxRamBytesUsed);
    final QueryCachingPolicy defaultCachingPolicy = new UsageTrackingQueryCachingPolicy();


    //TODO: pass in the analyzer depending on the document type
    public Searcher(String indexDirectoryPath) throws IOException {
        indexDirectory =
                FSDirectory.open(Paths.get(indexDirectoryPath));
        instantiateComponents();
    }

    /*
    This constructor is for testing purposes only as uses a RAM directory
     */
    public Searcher(Directory directory ) throws IOException {
        indexDirectory = directory;
        instantiateComponents();
    }

    private void instantiateComponents( ) throws IOException {
        indexReader = DirectoryReader.open(indexDirectory);
        indexSearcher = new IndexSearcher(indexReader);
        //set caching options
        indexSearcher.setQueryCache(queryCache);
        indexSearcher.setQueryCachingPolicy(defaultCachingPolicy);

        analyzer = new EnglishAnalyzer(CharArraySet.EMPTY_SET);
        queryParser = new QueryParser("contents",
                analyzer);
        queryParser.setDefaultOperator(QueryParser.Operator.OR);
        highlighter = new Highlighter(indexSearcher);
    }

    public SearchResultWithSuggestions searchFor(String query) throws ParseException, IOException, InvalidTokenOffsetsException {
        query = query.trim();
        if (query.contains("/")) {
            query = queryParser.escape(query);
        }
        Query searchQuery;
        int numberOfTermsInQuery = query.split(" ").length;
        if (numberOfTermsInQuery == 1) {
            searchQuery = queryParser.parse(queryBuilder.expandSingleTermQuery(query));
        } else if (query.endsWith("\"") && query.startsWith("\"") || numberOfTermsInQuery > 7) { //last condition added as we might get the query to be too noisy if we expand it
            searchQuery = queryParser.parse(query);
        }else {
            searchQuery = queryParser.parse(queryBuilder.expandPhraseQuery(query));
        }

        TopDocs docs =  indexSearcher.search(searchQuery, indexReader.numDocs() < 100000 ? indexReader.numDocs() : 100000);
        List<SearchResult> searchResults = highlighter.highlight(searchQuery, docs, fileType);

        SearchResultWithSuggestions searchResultWithSuggestions;
        if (queryBuilder.getNeighbourSearcher() != null) {
            NeighbourSuggestion queryExpansionTerms = queryBuilder.expandSearchBasedOnWhatWasMostRelevant(searchResults, query);
             searchResultWithSuggestions = new SearchResultWithSuggestions(queryExpansionTerms, searchResults);
        } else {
            searchResultWithSuggestions = new SearchResultWithSuggestions(searchResults);
        }

        return searchResultWithSuggestions;
    }


    public SearchResultWithSuggestions expandSearchResultsWithUserSelectedTerms(List<SearchResult> initialSearchResults, List<String> userSelectedTerms) throws ParseException, InvalidTokenOffsetsException, IOException {
        List<SearchResult> additionalSearchResults = new ArrayList<>();
        for (String term : userSelectedTerms) {
            additionalSearchResults.addAll(search(term));
        }
        FinalSearchResults finalSearchResults = new FinalSearchResults(initialSearchResults, additionalSearchResults);
        return new SearchResultWithSuggestions(finalSearchResults);
    }

    private List<SearchResult> search(String query) throws ParseException, IOException, InvalidTokenOffsetsException {
        if (query.contains("/")) {
            query = queryParser.escape(query);
        }
        Query q = queryParser.parse(queryParser.escape(query));
        TopDocs top = indexSearcher.search(q, indexReader.numDocs());
        return highlighter.highlight(q, top, fileType);
    }

    //This method must be used to set the query builder which is built separately (UI reasons)
    public void setQueryBuilder(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    //UI reasons - in order to give preview of the doc that is not PDf on a scrollable pane
    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

}
