package uk.ac.susx.tag.lucenesearch.query_expansion.spellcheck;


import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class KeyboardDistanceSpellcheckTest {

    @Test
    public void testTermsAreReturnedWhenFrequenciesAreNotZero() throws IOException {
        FrequencyBasedSpellChecker frequencyBasedSpellChecker = new FrequencyBasedSpellChecker();
        KeyboardDistanceSpellCheck keyboardDistanceSpellCheck = new KeyboardDistanceSpellCheck(frequencyBasedSpellChecker);
        assertEquals("ought", keyboardDistanceSpellCheck.getBestSuggestion("kught"));
        assertEquals("library", keyboardDistanceSpellCheck.getBestSuggestion("livrary"));
        assertEquals("broccoli", keyboardDistanceSpellCheck.getBestSuggestion("vroccoli"));
        assertEquals("column", keyboardDistanceSpellCheck.getBestSuggestion("vilumn"));
        assertEquals("poppy", keyboardDistanceSpellCheck.getBestSuggestion("poppt"));
        assertEquals("interface", keyboardDistanceSpellCheck.getBestSuggestion("interfsce"));

    }

    @Test
    public void testNullIsReturned() throws IOException {
        FrequencyBasedSpellChecker frequencyBasedSpellChecker = new FrequencyBasedSpellChecker("/spellcheck/dictionary.txt", "/spellcheck/frequencies.csv");
        KeyboardDistanceSpellCheck keyboardDistanceSpellCheck = new KeyboardDistanceSpellCheck(frequencyBasedSpellChecker);
        assertNull(keyboardDistanceSpellCheck.getBestSuggestion("5iramisu"));
        assertNull(keyboardDistanceSpellCheck.getBestSuggestion("gjhsfhj"));
    }
}
