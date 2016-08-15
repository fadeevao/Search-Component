package uk.ac.susx.tag.lucenesearch.neighbours;


import org.apache.commons.io.FileUtils;
import org.junit.Test;
import uk.ac.susx.tag.lucenesearch.neighbours.NeighbourSearcher;
import uk.ac.susx.tag.neighbours.Neighbour;
import uk.ac.susx.tag.neighbours.NeighbourProcessor;
import uk.ac.susx.tag.neighbours.exception.InvalidFileFormatException;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static junit.framework.TestCase.assertTrue;

public class NeighbourSearcherTest {

    @Test
    public void testGenerateNeighbours() throws IOException, InvalidFileFormatException {
        NeighbourProcessor neighbourProcessor = new NeighbourProcessor();
        File file = FileUtils.toFile(Thread.currentThread().getContextClassLoader().getResource("neighbours/wordvec.neighbours"));
        Map<String, List<Neighbour>> neighbourMap = neighbourProcessor.buildNeighbourMap(file);
        NeighbourSearcher neighbourSearcher = new NeighbourSearcher(neighbourMap, 3, true);
        Set<String> set = new HashSet<>();
        set.addAll(Arrays.asList("examiner"));
        NeighbourSuggestion neighbourSuggestion  = neighbourSearcher.generateQueryTermsBasedOnTheNeighboursOfHighlightedWords(set, "examiners");
        assertTrue(!neighbourSuggestion.getNeighbourSuggestions().isEmpty());
    }

    @Test
    public void testGenerateNeighboursWithSpellingVariationsSeparated() {
        NeighbourProcessor neighbourProcessor = new NeighbourProcessor();
        File file = FileUtils.toFile(Thread.currentThread().getContextClassLoader().getResource("neighbours/wordvec.neighbours"));
        Map<String, List<Neighbour>> neighbourMap = neighbourProcessor.buildNeighbourMap(file, 15);
        NeighbourSearcher neighbourSearcher = new NeighbourSearcher(neighbourMap, 3, true);
        NeighbourSuggestion neighbours = neighbourSearcher.generateQueryTermsBasedOnTheNeighboursOfHighlightedWords(Collections.<String>emptySet(), "examiner");
        assertTrue(neighbours.getNeighbourSuggestions().isEmpty());
    }

    @Test
    public void testSpellingVariationsWithChatspeakSuggestions() {
        NeighbourProcessor neighbourProcessor = new NeighbourProcessor();
        File file = FileUtils.toFile(Thread.currentThread().getContextClassLoader().getResource("neighbours/wordvec.neighbours"));
        Map<String, List<Neighbour>> neighbourMap = neighbourProcessor.buildNeighbourMap(file, 15);
        NeighbourSearcher neighbourSearcher = new NeighbourSearcher(neighbourMap, 3, true);
        Set<String> set = new HashSet<>();
        set.addAll(Arrays.asList("thank"));
        NeighbourSuggestion neighbours = neighbourSearcher.generateQueryTermsBasedOnTheNeighboursOfHighlightedWords(set, "thank you");
        assertTrue(neighbours.getSpellingVariations().contains("thanx"));
        assertTrue(neighbours.getSpellingVariations().contains("ta"));
    }
}
