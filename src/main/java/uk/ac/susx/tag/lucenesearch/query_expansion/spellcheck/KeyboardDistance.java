package uk.ac.susx.tag.lucenesearch.query_expansion.spellcheck;


import java.math.BigDecimal;

public class KeyboardDistance implements Comparable {

    private String term;
    private Double distance;
    private BigDecimal termFrequency;

    public KeyboardDistance(String term, Double distance, BigDecimal termFrequency) {
        this.term = term;
        this.distance = distance;
        this.termFrequency = termFrequency;
    }

    public String getTerm() {
        return term;
    }

    public Double getDistance() {
        return distance;
    }

    public BigDecimal getTermFrequency() {
        return termFrequency;
    }

    @Override
    public int compareTo(Object o) {
        KeyboardDistance distance1 = (KeyboardDistance) o;
        if (this.distance == distance1.getDistance()) {
            return 0;
        }
        return this.distance > distance1.getDistance() ? 1 : -1;
    }
}
