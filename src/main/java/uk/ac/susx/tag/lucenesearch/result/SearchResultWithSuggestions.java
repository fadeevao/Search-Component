package uk.ac.susx.tag.lucenesearch.result;

import uk.ac.susx.tag.lucenesearch.neighbours.NeighbourSuggestion;

import java.util.List;


public class SearchResultWithSuggestions {

    private NeighbourSuggestion searchTermSuggestions;

    private List<SearchResult> searchResults;

    private List<SearchResult> searchResultsAfterExpansion;

    public SearchResultWithSuggestions(NeighbourSuggestion searchTermSuggestions, List<SearchResult> searchResults) {
        this.searchTermSuggestions = searchTermSuggestions;
        this.searchResults = searchResults;
    }

    public SearchResultWithSuggestions(FinalSearchResults finalSearchResults) {
        this.searchResultsAfterExpansion = finalSearchResults.getSearchResultsAfterExpansion();
        this.searchResults = finalSearchResults.getSearchResults();
    }

    public SearchResultWithSuggestions(List<SearchResult> searchResults) {
        this.searchResults = searchResults;
    }

    public List<SearchResult> getSearchResultsAfterExpansion() {
        return searchResultsAfterExpansion;
    }

    public List<SearchResult> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<SearchResult> searchResults) {
        this.searchResults = searchResults;
    }

    public NeighbourSuggestion getSuggestionsWrapper() {
        return searchTermSuggestions;
    }

    public void setSearchTermSuggestions(NeighbourSuggestion searchTermSuggestions) {
        this.searchTermSuggestions = searchTermSuggestions;
    }
}
