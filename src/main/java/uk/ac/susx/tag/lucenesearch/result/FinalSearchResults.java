package uk.ac.susx.tag.lucenesearch.result;


import java.util.List;

public class FinalSearchResults {
    private List<SearchResult> searchResults;

    private List<SearchResult> searchResultsAfterExpansion;

    public FinalSearchResults(List<SearchResult> searchResults, List<SearchResult> searchResultsAfterExpansion) {
        this.searchResults = searchResults;
        this.searchResultsAfterExpansion = searchResultsAfterExpansion;
    }

    public List<SearchResult> getSearchResults() {
        return searchResults;
    }

    public List<SearchResult> getSearchResultsAfterExpansion() {
        return searchResultsAfterExpansion;
    }
}
