package uk.ac.susx.tag.lucenesearch.query_expansion.spellcheck;


import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

public class KeyboardKeyDistanceCalculatorTest {

    @Test
    public void testDistanceMeasure() {
        KeyboardKeyDistanceCalculator calculator = new KeyboardKeyDistanceCalculator();
        String comparisonTerm = "light";
        assertTrue(calculator.getTermDistance(comparisonTerm, "loght") < calculator.getTermDistance(comparisonTerm, "kught"));
        assertTrue(calculator.getTermDistance(comparisonTerm, "aqswe") > calculator.getTermDistance(comparisonTerm, "kught"));
        assertTrue(calculator.getTermDistance(comparisonTerm, "l8t56") > calculator.getTermDistance(comparisonTerm, "l8ght"));
    }
}


