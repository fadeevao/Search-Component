package uk.ac.susx.tag.lucenesearch.query_expansion.spellcheck;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FrequencyBasedSpellCheckerTest {

   private static FrequencyBasedSpellChecker frequencyBasedSpellChecker;

    @BeforeClass
    public static void setUp() throws IOException {
        frequencyBasedSpellChecker = new FrequencyBasedSpellChecker();
    }

    @Test
    public void testSpellCheckOnASingleWord() throws IOException {
        List<String> bestSuggestions = frequencyBasedSpellChecker.getBestSuggestionForEveryWordInQuery("erth");
        assertEquals(1, bestSuggestions.size());
        assertEquals("earth", bestSuggestions.get(0));

        bestSuggestions = frequencyBasedSpellChecker.getBestSuggestionForEveryWordInQuery("wster");
        assertEquals(1, bestSuggestions.size());
        assertEquals("water", bestSuggestions.get(0));

        bestSuggestions = frequencyBasedSpellChecker.getBestSuggestionForEveryWordInQuery("mther");
        assertEquals(1, bestSuggestions.size());
        assertEquals("mother", bestSuggestions.get(0));

        bestSuggestions = frequencyBasedSpellChecker.getBestSuggestionForEveryWordInQuery("brther");
        assertEquals(1, bestSuggestions.size());
        assertEquals("brother", bestSuggestions.get(0));

        bestSuggestions = frequencyBasedSpellChecker.getBestSuggestionForEveryWordInQuery("fatthr");
        assertEquals(1, bestSuggestions.size());
        assertEquals("father", bestSuggestions.get(0));

        bestSuggestions = frequencyBasedSpellChecker.getBestSuggestionForEveryWordInQuery("mooon");
        assertEquals(1, bestSuggestions.size());
        assertEquals("moon", bestSuggestions.get(0));

        bestSuggestions = frequencyBasedSpellChecker.getBestSuggestionForEveryWordInQuery("escaep");
        assertEquals(1, bestSuggestions.size());
        assertEquals("escape", bestSuggestions.get(0));
    }

    @Test
    public void testSpellCheckOnMultipleWordQueryBothWordsNotInFrequencyMap() throws IOException {
        List<String> bestSuggestions = frequencyBasedSpellChecker.getBestSuggestionForEveryWordInQuery("mther erth");
        assertEquals(2, bestSuggestions.size());
        assertEquals("mother", bestSuggestions.get(0));
        assertEquals("earth", bestSuggestions.get(1));
    }

    @Test
    public void testSpellCheckOnASingleWordWhichIsPresentInFrequencyMap() throws IOException {
        List<String> bestSuggestions = frequencyBasedSpellChecker.getBestSuggestionForEveryWordInQuery("come");
        assertEquals(1, bestSuggestions.size());
        assertEquals("come", bestSuggestions.get(0));

        bestSuggestions = frequencyBasedSpellChecker.getBestSuggestionForEveryWordInQuery("same");
        assertEquals(1, bestSuggestions.size());
        assertEquals("same", bestSuggestions.get(0));
    }

    @Test
    public void testEdgeCases() throws IOException {
        List<String> bestSuggestions = frequencyBasedSpellChecker.getBestSuggestionForEveryWordInQuery("");
        assertEquals(0, bestSuggestions.size());

        bestSuggestions = frequencyBasedSpellChecker.getBestSuggestionForEveryWordInQuery(null);
        assertEquals(0, bestSuggestions.size());
    }
}
