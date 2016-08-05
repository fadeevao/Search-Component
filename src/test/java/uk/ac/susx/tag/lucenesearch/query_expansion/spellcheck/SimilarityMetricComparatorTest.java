package uk.ac.susx.tag.lucenesearch.query_expansion.spellcheck;


import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class SimilarityMetricComparatorTest {

    @Test
    public void testComparatorHigherScoreToTheItemWithLowerEditDistance() {
        SimilarityMetric similarityMetricOne = new SimilarityMetric(new BigDecimal("10"), 1);
        SimilarityMetric similarityMetricTwo = new SimilarityMetric(new BigDecimal("10"), 2);
        Map<String,  SimilarityMetric> similarities = new HashMap<>();
        similarities.put("test1", similarityMetricOne);
        similarities.put("test2", similarityMetricTwo);
        List<Map.Entry<String, SimilarityMetric>> bestSuggestions = similarities.entrySet()
                .stream()
                .filter(e -> e.getValue() != null)
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue(new SimilarityMetricComparator())))
                .collect(Collectors.toList());
        assertEquals(bestSuggestions.get(0).getValue(), similarityMetricOne);
        assertEquals(bestSuggestions.get(1).getValue(), similarityMetricTwo);
    }

    @Test
    public void testComparatorHigherScoreToTheItemWIthGreaterFrequencySameDistance() {
        SimilarityMetric similarityMetricOne = new SimilarityMetric(new BigDecimal("11"), 1);
        SimilarityMetric similarityMetricTwo = new SimilarityMetric(new BigDecimal("10"), 1);
        Map<String,  SimilarityMetric> similarities = new HashMap<>();
        similarities.put("test1", similarityMetricOne);
        similarities.put("test2", similarityMetricTwo);
        List<Map.Entry<String, SimilarityMetric>> bestSuggestions = similarities.entrySet()
                .stream()
                .filter(e -> e.getValue() != null)
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue(new SimilarityMetricComparator())))
                .collect(Collectors.toList());
        assertEquals(bestSuggestions.get(0).getValue(), similarityMetricOne);
        assertEquals(bestSuggestions.get(1).getValue(), similarityMetricTwo);
    }

    @Test
    public void testComparatorHighestScoreMixedValues() {
        SimilarityMetric similarityMetricOne = new SimilarityMetric(new BigDecimal("11"), 2);
        SimilarityMetric similarityMetricTwo = new SimilarityMetric(new BigDecimal("10"), 1);
        Map<String,  SimilarityMetric> similarities = new HashMap<>();
        similarities.put("test1", similarityMetricOne);
        similarities.put("test2", similarityMetricTwo);
        List<Map.Entry<String, SimilarityMetric>> bestSuggestions = similarities.entrySet()
                .stream()
                .filter(e -> e.getValue() != null)
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue(new SimilarityMetricComparator())))
                .collect(Collectors.toList());
        assertEquals(bestSuggestions.get(0).getValue(), similarityMetricTwo);
        assertEquals(bestSuggestions.get(1).getValue(), similarityMetricOne);
    }
}
