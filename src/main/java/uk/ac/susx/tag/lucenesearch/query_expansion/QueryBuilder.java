package uk.ac.susx.tag.lucenesearch.query_expansion;

import uk.ac.susx.tag.lucenesearch.neighbours.NeighbourSearcher;
import uk.ac.susx.tag.lucenesearch.neighbours.NeighbourSuggestion;
import uk.ac.susx.tag.lucenesearch.query_expansion.highlighter.HighlightedTextFragment;
import uk.ac.susx.tag.lucenesearch.query_expansion.spellcheck.FrequencyBasedSpellChecker;
import uk.ac.susx.tag.lucenesearch.query_expansion.spellcheck.KeyboardDistanceSpellCheck;
import uk.ac.susx.tag.lucenesearch.query_expansion.spellcheck.KeyboardKeyDistanceCalculator;
import uk.ac.susx.tag.lucenesearch.query_expansion.spellcheck.chatspeak.ChatspeakSuggest;
import uk.ac.susx.tag.lucenesearch.result.SearchResult;
import uk.ac.susx.tag.util.Constants;
import uk.ac.susx.tag.util.NamedEntityIdentifier;
import uk.ac.susx.tag.util.Stemmer;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/*
Deals with expanding the query with frequency suggestions keyboard distance suggestions

desired example (not implemented):
query : coping with overcrowded prisons
• normal form : #combine( coping with
    overcrowded #syn( prisons prison ) )
• expanded form : #combine( coping with overcrowded #syn( prisons prison inmate inmates jail jails detention detentions prisoner prisoners detainee detainees ) )
 */
public class QueryBuilder {

    private FrequencyBasedSpellChecker frequencyBasedSpellChecker;
    private KeyboardDistanceSpellCheck keyboardDistanceSpellCheck;
    private KeyboardKeyDistanceCalculator calculator;
    private NamedEntityIdentifier namedEntityIdentifier;

    private NeighbourSearcher neighbourSearcher;

    private ChatspeakSuggest chatspeakSuggest;

    public QueryBuilder() throws IOException {
        frequencyBasedSpellChecker = new FrequencyBasedSpellChecker();
        keyboardDistanceSpellCheck = new KeyboardDistanceSpellCheck(frequencyBasedSpellChecker);
        calculator = new KeyboardKeyDistanceCalculator();
        namedEntityIdentifier = new NamedEntityIdentifier();
        chatspeakSuggest = new ChatspeakSuggest();
    }

    public QueryBuilder(NeighbourSearcher neighbourSearcher) throws IOException {
        this();
        this.neighbourSearcher = neighbourSearcher;
    }

    /*
    Expands a multi term query with suggestions
     */
    public String expandPhraseQuery(String query) {
        String[] queryTerms = query.split(" ");
        List<String> spellcheckerSuggestions  = frequencyBasedSpellChecker.getBestSuggestionForEveryWordInQuery(query);
        List<String> keyboardSuggestions = keyboardDistanceSpellCheck.getBestSuggestionForEveryWordInQuery(query);

        StringBuilder stringBuilder = new StringBuilder(query).append(" ");
        appendToQueryString(queryTerms, spellcheckerSuggestions, stringBuilder);
        appendToQueryString(queryTerms, keyboardSuggestions, stringBuilder);
        appendChatspeakSuggestions(query, stringBuilder);
        return stringBuilder.toString();

    }

    /*
    Expands a single term query with suggestions adding a boost to keyboard suggestion and 'fuzzifying' the original term
    Original term is left as it is if it's a constant (eg a pronoun), a name or is less than 5 chars in length
     */
    public String expandSingleTermQuery(String query) {
        if ( query.length() <= 4 || Constants.termIsAConstant(query) || namedEntityIdentifier.termIsNamedEntity(query)) {
            return query;
        }
        StringBuilder stringBuilder = new StringBuilder(query);


       if (query.length() > 4) {
           stringBuilder.append("~0.8^4 "); //we append tilde (makes the search fuzzy) for the case when  original term is >  chars as otherwise results are too noisy
       } else {
           stringBuilder.append(" ");
       }

       List<String> spellcheckSuggestions  = frequencyBasedSpellChecker.getBestSuggestionForEveryWordInQuery(query);
       List<String> keyboardSuggestions = keyboardDistanceSpellCheck.getBestSuggestionForEveryWordInQuery(query);


       if (!spellcheckSuggestions.isEmpty() && spellcheckSuggestions.get(0) != null) {
           double distance = calculator.getTermDistance(keyboardSuggestions.get(0), spellcheckSuggestions.get(0));
           //if most frequent word is distance-wise too far from the input term, then we do not include it in a query
           //however, we do include it in a query if no keyboard suggestion was generated and null was returned (distance is returned as 20)
           if (distance < 4.5 || distance == 20) {
               stringBuilder.append(spellcheckSuggestions.get(0))
                       .append(" ");
           }
       }


       if (!keyboardSuggestions.isEmpty() && keyboardSuggestions.get(0) != null) {
           stringBuilder.append(keyboardSuggestions.get(0))
                   .append("^2 "); //boost the correctly spelled word
       }

        appendChatspeakSuggestions(query, stringBuilder);

       return  stringBuilder.toString();
   }

    /*
    Appends chatspeak suggestions if there are any to the original query
     */
    private void appendChatspeakSuggestions(String query, StringBuilder stringBuilder) {
        List<String> chatSpeakSuggestions = chatspeakSuggest.getChatspeakSuggestions(query);
        if (chatSpeakSuggestions!=null) {
            for (String sugg : chatSpeakSuggestions) {
                stringBuilder.append(sugg)
                        .append(" ");
            }
        }
    }


    private void appendToQueryString(String[] queryTerms, List<String> suggestions, StringBuilder stringBuilder) {
        for (String suggestion : suggestions) {
            if (suggestion != null || !suggestion.equals("")) {
                stringBuilder.append(suggestion)
                        .append(" ");
            } else {
                stringBuilder.append(queryTerms[suggestions.indexOf(suggestion)])
                        .append(" ");

            }
        }
    }

    /*
    Get the highlighted terms from the results and pass them on to the neighbourSearcher that will find neighbours for the highlighted terms
    and spelling variations
     */
    public NeighbourSuggestion expandSearchBasedOnWhatWasMostRelevant(List<SearchResult> searchResults, String originalQuery) {
        Set<String> highlightedTerms = new HashSet<>(); //add highlighted terms to a set to avoid duplicates
        int indexSize = searchResults.size() < 100 ? searchResults.size() : 100;
        for (int i = 0; i< indexSize; i++) {
            for (List<HighlightedTextFragment> highlightedTextFragments : searchResults.get(i).getDocFragments()) {
                for (HighlightedTextFragment highlightedTextFragment : highlightedTextFragments) {
                    String textFragment = highlightedTextFragment.getTextFragment().toLowerCase();
                    if (!highlightedTerms.contains(textFragment) && highlightedTextFragment.isHighlighted() && !originalQuery.equals(textFragment) && !Constants.termIsAConstant(textFragment)) {
                            highlightedTerms.add(textFragment);
                    }
                }
            }
        }
        if (highlightedTerms.isEmpty()) {
            highlightedTerms.add(originalQuery);
        }

        return neighbourSearcher.generateQueryTermsBasedOnTheNeighboursOfHighlightedWords(highlightedTerms, originalQuery);
    }

    public NeighbourSearcher getNeighbourSearcher() {
        return neighbourSearcher;
    }

    public void setNeighbourSearcher(NeighbourSearcher neighbourSearcher) {
        this.neighbourSearcher = neighbourSearcher;
    }
}
