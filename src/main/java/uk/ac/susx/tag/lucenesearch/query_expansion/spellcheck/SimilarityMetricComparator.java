package uk.ac.susx.tag.lucenesearch.query_expansion.spellcheck;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;


public class SimilarityMetricComparator implements Comparator<SimilarityMetric> {

    @Override
    public int compare(SimilarityMetric a, SimilarityMetric b) {
        BigDecimal aWeight = calculateWeight(a);
        BigDecimal bWeight = calculateWeight(b);
        BigDecimal result = aWeight.subtract(bWeight);
        result = result.multiply(new BigDecimal("100"));
        int resInt = result.intValue();
        return resInt;
    }

    /*
    The formula is : frequency*0.5 + 1/distance
     */
    private BigDecimal calculateWeight(SimilarityMetric similarityMetric) {
        BigDecimal frequency = similarityMetric.getFrequency() == null ? new BigDecimal("0") : similarityMetric.getFrequency().multiply(new BigDecimal("0.5"));
        BigDecimal distanceMeasure;
        //Levenshtein distance is 0 in case when we compare original term to some other term
        if (similarityMetric.getLevenshteinDistanceToOriginalTerm() == 0) {
            distanceMeasure = new BigDecimal("1.0").divide(new BigDecimal("0.5"), 4, RoundingMode.HALF_EVEN);
        } else {
            distanceMeasure = new BigDecimal("1.0").divide(new BigDecimal(similarityMetric.getLevenshteinDistanceToOriginalTerm()), 4, RoundingMode.HALF_EVEN);
        }
        return frequency.add(distanceMeasure).setScale(4, RoundingMode.HALF_EVEN);
    }
}
