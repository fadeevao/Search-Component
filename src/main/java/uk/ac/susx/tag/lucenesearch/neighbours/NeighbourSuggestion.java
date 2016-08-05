package uk.ac.susx.tag.lucenesearch.neighbours;


import java.util.List;
import java.util.Set;

/*
Represents 2 lists that are derived from dist. semantics model - one with neighbour suggestions and the other with the spelling variations for the original query term
 */
public class NeighbourSuggestion {
    private List<String> neighbourSuggestions;
    private Set<String> spellingVariations;

    public NeighbourSuggestion(List<String> neighbourSuggestions, Set<String> spellingVariations) {
        this.neighbourSuggestions = neighbourSuggestions;
        this.spellingVariations = spellingVariations;
    }

    public List<String> getNeighbourSuggestions() {
        return neighbourSuggestions;
    }

    public Set<String> getSpellingVariations() {
        return spellingVariations;
    }
}
