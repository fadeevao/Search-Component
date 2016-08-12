package uk.ac.susx.tag.lucenesearch.query_expansion.spellcheck;

import uk.ac.susx.tag.util.Constants;
import uk.ac.susx.tag.util.NamedEntityIdentifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KeyboardDistanceSpellCheck {

    private FrequencyBasedSpellChecker frequencyBasedSpellChecker;
    private NamedEntityIdentifier namedEntityIdentifier;

    public KeyboardDistanceSpellCheck(FrequencyBasedSpellChecker frequencyBasedSpellChecker) {
        this.frequencyBasedSpellChecker = frequencyBasedSpellChecker;
        namedEntityIdentifier = new NamedEntityIdentifier();

    }

    public List<String> getBestSuggestionForEveryWordInQuery(String query) {
        String[] queryTerms = query.split(" ");
        List<String> suggestions = new ArrayList<>();
        for (String term: queryTerms) {
            if ( term.length() > 4 && !Constants.termIsAConstant(term) && !namedEntityIdentifier.termIsNamedEntity(term)) {
                suggestions.add(getBestSuggestion(term));
            } else {
                suggestions.add("");
            }
        }
        return suggestions;
    }

    /*
    Returns the closest (keyboard-spelling-wise) word to the typed in term
    Can return null if none of the suggested/similar words are in the frequencies list

    1. gets suggestions from the dictionary
    2. selects the suggestions that are keyboard-wise distance the closest to the original term
     */
    public String getBestSuggestion(String term) {

        if (frequencyBasedSpellChecker.wordExists(term)) { return term; }
        String[] suggestions = frequencyBasedSpellChecker.getSuggestions(term, 30); //get suggestions for the original term from the dictionary
        List<KeyboardDistance> distances = new ArrayList<>();
        for (String suggestion : suggestions) {
            if (suggestion.length() == term.length()) {
                double calculatedDistance = KeyboardKeyDistanceCalculator.getTermDistance(term, suggestion);
                if (calculatedDistance < 4.0) { //eliminate results where distance is too big as they are unlikely to be useful
                    distances.add(new KeyboardDistance(suggestion, calculatedDistance, frequencyBasedSpellChecker.FREQUENCIES_MAP.get(suggestion)));
                }
            }
        }
        Collections.sort(distances);

        return !distances.isEmpty() ? distances.get(0).getTerm() : null;
    }
}