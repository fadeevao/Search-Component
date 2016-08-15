package uk.ac.susx.tag;

/*
This class gives an example of how to integrate the SearchComponent with the Method52.
See the test class for example (SearchApplicationMethod52Test)
The order of creating and setting the fields is dictated by the developed UI so might not make much sense when using SearchComponent
independently of the UI..
All objects are created in methods while can be moved into constructor - depends on the use cases

Overall what needs to be provided:
- path to the directory where index will be store
- path to the neighbours file if using one - good to use if later want to offer search suggestions, otherwise not much point really
- id and main body keys in Datum objects in order to index them properly
 */

import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import uk.ac.susx.tag.lucenesearch.Indexer;
import uk.ac.susx.tag.lucenesearch.Searcher;
import uk.ac.susx.tag.lucenesearch.exception.IndexingException;
import uk.ac.susx.tag.lucenesearch.neighbours.NeighbourSearcher;
import uk.ac.susx.tag.lucenesearch.query_expansion.QueryBuilder;
import uk.ac.susx.tag.lucenesearch.result.SearchResult;
import uk.ac.susx.tag.lucenesearch.result.SearchResultWithSuggestions;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.types.RuntimeType;
import uk.ac.susx.tag.neighbours.Neighbour;
import uk.ac.susx.tag.neighbours.NeighbourProcessor;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchApplicationMethod52 {

    public List<Datum> integrationWithNeighbourFilePresent(List<Datum> objectsToSearch, String idKey, String messageIdKey,  String indexDirectoryPath, String searchTerm, String neighbourFilePath) throws IOException, InvalidTokenOffsetsException, ParseException, IndexingException, org.apache.lucene.queryparser.classic.ParseException {
        Indexer indexer = new Indexer(indexDirectoryPath); //can also pass in a directory object in the constructor (normally RAMDirectory) but that's not efficient for big collections of data
        indexer.createIndex(objectsToSearch, idKey, messageIdKey);
        Searcher searcher = new Searcher(indexDirectoryPath); //if creating a RAMDirectory, then pass it into the Searcher's constructor

        NeighbourProcessor neighbourProcessor = new NeighbourProcessor();
        File neighbourFile = new File(neighbourFilePath);
        Map<String, List<Neighbour>> neighbourMap = neighbourProcessor.buildNeighbourMap(neighbourFile);
        NeighbourSearcher neighbourSearcher = new NeighbourSearcher(neighbourMap, 5, true);
        QueryBuilder queryBuilder = new QueryBuilder(neighbourSearcher);
        searcher.setQueryBuilder(queryBuilder);

        SearchResultWithSuggestions searchResultWithSuggestions = searcher.searchFor(searchTerm);
        List<SearchResult> searchResults = searchResultWithSuggestions.getSearchResults();

        return getRelevantToSearchTermDatumObjects(objectsToSearch, idKey, searchResults);
    }

    public List<Datum> integrationWithoutNeighbourFilePresent(List<Datum> objectsToSearch, String idKey, String messageIdKey, String indexDirectoryPath, String searchTerm) throws IOException, InvalidTokenOffsetsException, ParseException, IndexingException, org.apache.lucene.queryparser.classic.ParseException {
        Indexer indexer = new Indexer(indexDirectoryPath); //can also pass in a directory object in the constructor (normally RAMDirectory) but that's not efficient for big collections of data
        indexer.createIndex(objectsToSearch, idKey, messageIdKey);
        Searcher searcher = new Searcher(indexDirectoryPath); //if creating a RAMDirectory, then pass it into the Searcher's constructor

        QueryBuilder queryBuilder = new QueryBuilder();
        searcher.setQueryBuilder(queryBuilder);

        SearchResultWithSuggestions searchResultWithSuggestions = searcher.searchFor(searchTerm);
        List<SearchResult> searchResults = searchResultWithSuggestions.getSearchResults();

        return getRelevantToSearchTermDatumObjects(objectsToSearch, idKey, searchResults);
    }

    /*
    Goes through the list of results and selects the datum objects that match the doc id of the search result and adds datums
    to the list of relevant docs. Also appends a score to the datum object as a new key
     */

    private List<Datum> getRelevantToSearchTermDatumObjects(List<Datum> objectsToSearch, String idKey, List<SearchResult> searchResults) {
        List<Datum> relevantDocs = new ArrayList<>();
        for (SearchResult result : searchResults) {
            Datum datum = getDatumObjectWithKey(objectsToSearch, idKey, result.getFileId());
            Key<Double> documentScore = Key.of("searchScore", RuntimeType.DOUBLE); //not sure if this is the right way to add a new key to a datum object
            datum = datum.with(documentScore, result.getDocumentScore());
            relevantDocs.add(datum);
        }
        return relevantDocs;
    }

    /*
    Finds a Datum object in the collection of docs that has the same id as a document referenced in a search result;
    Can add "filters" for other keys such as dates, gender etc. (eg if datum.get(documentId).equals(searchResultId) && datum.get(genderKey).equals("male") then ... )
     */

    private Datum getDatumObjectWithKey(List<Datum> datumObjects, String idKey, String searchResultId) {
        Key<String> documentId = Key.of(idKey, RuntimeType.STRING);
        for (Datum datum : datumObjects) {
            if (datum.get(documentId).equals(searchResultId)) {
                return datum;
            }
        }
        return null;
    }
}

