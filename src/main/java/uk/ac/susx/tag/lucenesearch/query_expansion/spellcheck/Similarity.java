package uk.ac.susx.tag.lucenesearch.query_expansion.spellcheck;


import java.math.BigDecimal;
import java.util.Map;

/*
Used for frequency based spellcheck
 */
public class Similarity {
    private String originalTerm;
    private Map<String, SimilarityMetric> suggestionSimilarityMetrics;

    private BigDecimal originalTermFrequency;

    public Similarity(String originalTerm, Map<String, SimilarityMetric> suggestionSimilarityMetrics, BigDecimal originalTermFrequency) {
        this.originalTerm = originalTerm;
        this.suggestionSimilarityMetrics = suggestionSimilarityMetrics;
        this.originalTermFrequency = originalTermFrequency;
    }

    public String getOriginalTerm() {
        return originalTerm;
    }

    public Map<String, SimilarityMetric> getSuggestionSimilarityMetrics() {
        return suggestionSimilarityMetrics;
    }

    public BigDecimal getOriginalTermFrequency() {
        return originalTermFrequency;
    }

}
