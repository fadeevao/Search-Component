package uk.ac.susx.tag.lucenesearch.neighbours;

import org.apache.commons.lang3.StringUtils;
import uk.ac.susx.tag.lucenesearch.query_expansion.spellcheck.chatspeak.ChatspeakSuggest;
import uk.ac.susx.tag.neighbours.Neighbour;
import uk.ac.susx.tag.util.Constants;
import uk.ac.susx.tag.util.Stemmer;

import java.util.*;


public class NeighbourSearcher {

    private Map<String, List<Neighbour>> neighbourMap;
    private int numberOfSuggestions;
    private boolean separateSpellingVariations;
    private ChatspeakSuggest chatspeakSuggest;
    private int numberOfSpellingVariationsToSuggest;
    private int editDistance;

    private final static Integer DEFAULT_NUMBER_OF_NEIGHBOURS = 3;
    private final static Integer DEFAULT_NUMBER_OF_SPELLING_SUGGESTIONS = 5;
    private final static Integer DEFAULT_EDIT_DISTANCE = 3;

    public NeighbourSearcher(Map<String, List<Neighbour>> map, int numberOfSuggestions) {
        this(map, numberOfSuggestions, false, DEFAULT_NUMBER_OF_SPELLING_SUGGESTIONS, DEFAULT_EDIT_DISTANCE);
    }

    /*
    Default number of suggestions for every term that we generate is 3
     */
    public NeighbourSearcher(Map<String, List<Neighbour>> map) {
        this(map, DEFAULT_NUMBER_OF_NEIGHBOURS, false, DEFAULT_NUMBER_OF_SPELLING_SUGGESTIONS, DEFAULT_EDIT_DISTANCE);
    }

    public NeighbourSearcher(Map<String, List<Neighbour>> map, int numberOfSuggestions, boolean separateSpellingVariations, int numberOfSpellingVariationsToSuggest, int editDistance) {
        this.neighbourMap = map;
        this.numberOfSuggestions = numberOfSuggestions;
        this.separateSpellingVariations = separateSpellingVariations;
        chatspeakSuggest = new ChatspeakSuggest();
        this.numberOfSpellingVariationsToSuggest = numberOfSpellingVariationsToSuggest;
        this.editDistance = editDistance;
    }

    public NeighbourSearcher(Map<String, List<Neighbour>> map, int numberOfSuggestions, boolean separateSpellingVariations) {
        this.neighbourMap = map;
        this.numberOfSuggestions = numberOfSuggestions;
        this.separateSpellingVariations = separateSpellingVariations;
        chatspeakSuggest = new ChatspeakSuggest();
        this.numberOfSpellingVariationsToSuggest = DEFAULT_NUMBER_OF_SPELLING_SUGGESTIONS;
        this.editDistance = DEFAULT_EDIT_DISTANCE;
    }

    /*
    Take the words that have been highlighted in a search process and find their neighbours which can further be used
    for expanding the search
     */
    public NeighbourSuggestion generateQueryTermsBasedOnTheNeighboursOfHighlightedWords(Set<String> termsForNeighbourGeneration, String originalQuery) {
        List<String> queryExpansionTerms = new ArrayList<>();
        Set<String> spellingVariations = new HashSet<>();
        for (String term : termsForNeighbourGeneration) {
            if (neighbourMap.get(term) != null) {
                populateSuggestionLists(originalQuery, queryExpansionTerms, spellingVariations, term);
            }
        }

        if (queryExpansionTerms.isEmpty()) {
            populateSuggestionLists(originalQuery, queryExpansionTerms, spellingVariations, originalQuery);
        }
        return new NeighbourSuggestion(queryExpansionTerms, spellingVariations);
    }

    private void populateSuggestionLists(String originalTerm, List<String> queryExpansionTerms, Set<String> spellingVariations, String term) {
        List<Neighbour> neighbours = neighbourMap.get(term);
        NeighbourSuggestion sugg = filterNeighbourList(neighbours != null ? neighbours : new ArrayList<Neighbour>(), originalTerm);
        if (separateSpellingVariations) {
            spellingVariations.addAll(sugg.getSpellingVariations());
        }
        queryExpansionTerms.addAll(sugg.getNeighbourSuggestions());
    }

    /*
    Filter out pronouns and stopwords as we do not want to suggest those.
    Also measure a distance from suggested neighbour to the original term and is the distance is small enough add that
    suggestion to the list of spelling variations (can be error prone in some cases)
     */
    private NeighbourSuggestion filterNeighbourList(List<Neighbour> neighbours, String originalTerm) {
        List<String> filteredNeighbours = new ArrayList<>();
        Set<String> spellingVariations = new HashSet<>();
        List<String> chatspeakSuggestions = chatspeakSuggest.getChatspeakSuggestions(originalTerm);
        if (chatspeakSuggestions!=null) {
            spellingVariations.addAll(chatspeakSuggestions);
        }
        boolean termAdded = false;
        for (Neighbour neighbour : neighbours) {
            if (separateSpellingVariations) {
                if (spellingVariations.size() < numberOfSpellingVariationsToSuggest && StringUtils.getLevenshteinDistance(originalTerm, neighbour.getTerm()) <= editDistance) {
                    spellingVariations.add(neighbour.getTerm());
                    termAdded = true;
                }
            }
            if (!Constants.termIsAConstant(neighbour.getTerm()) && !termAdded && filteredNeighbours.size() < numberOfSuggestions) {
                filteredNeighbours.add(neighbour.getTerm());
            }
            termAdded = false;
        }

        return new NeighbourSuggestion(filteredNeighbours, spellingVariations);
    }

    public Map<String, List<Neighbour>> getNeighbourMap() {
        return neighbourMap;
    }
}
