package uk.ac.susx.tag.lucenesearch.query_expansion.spellcheck;

import java.math.BigDecimal;

class SimilarityMetric {

    private BigDecimal frequency;

    private Integer levenshteinDistanceToOriginalTerm;

    public SimilarityMetric(BigDecimal frequency, Integer levenshteinDistanceToOriginalTerm) {
        this.frequency = frequency;
        this.levenshteinDistanceToOriginalTerm = levenshteinDistanceToOriginalTerm;
    }

    public Integer getLevenshteinDistanceToOriginalTerm() {
        return levenshteinDistanceToOriginalTerm;
    }

    public BigDecimal getFrequency() {
        return frequency;
    }

}
