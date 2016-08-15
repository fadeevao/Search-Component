package uk.ac.susx.tag.neighbours;


import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import uk.ac.susx.tag.neighbours.exception.InvalidFileFormatException;

import java.io.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NeighbourProcessorTest {

    private NeighbourProcessor neighbourProcessor;

    private final static int TERM_A_NEIGHBOUR_COUNT = 5;
    private final static int TERM_B_NEIGHBOUR_COUNT = 7;
    private final static String TERM_A = "a";
    private final static String TERM_B = "b";


    @Before
    public void setUp() {
        neighbourProcessor = new NeighbourProcessor();
    }


    @Test
    public void testProcessAllNeighbours() throws IOException, InvalidFileFormatException {
        File file = FileUtils.toFile(Thread.currentThread().getContextClassLoader().getResource("neighbours/wiki-test"));
        Map<String, List<Neighbour>> neighbourMap = neighbourProcessor.buildNeighbourMap(file);
        assertEquals(2, neighbourMap.size());
        Set<String> keySet = neighbourMap.keySet();
        assertTrue(keySet.contains(TERM_A));
        assertTrue(keySet.contains(TERM_B));

        List<Neighbour> aNeighbours = neighbourMap.get(TERM_A);
        List<Neighbour> bNeighbours = neighbourMap.get(TERM_B);

        assertEquals(TERM_A_NEIGHBOUR_COUNT, aNeighbours.size());
        assertEquals(TERM_B_NEIGHBOUR_COUNT, bNeighbours.size());

        testMapPopulatedWithCorrectNeighbours(aNeighbours, TERM_A, 0.16d);
        testMapPopulatedWithCorrectNeighbours(bNeighbours, TERM_B, 0.18d);
    }

    @Test
    public void testProcessNeighboursWithSpecificCount() throws IOException, InvalidFileFormatException {
        File file = FileUtils.toFile(Thread.currentThread().getContextClassLoader().getResource("neighbours/wiki-test"));
        int customNeighbourCount = 2;
        Map<String, List<Neighbour>> neighbourMap = neighbourProcessor.buildNeighbourMap(file, customNeighbourCount);
        assertEquals(2, neighbourMap.size());

        List<Neighbour> aNeighbours = neighbourMap.get(TERM_A);
        List<Neighbour> bNeighbours = neighbourMap.get(TERM_B);

        assertEquals(customNeighbourCount, aNeighbours.size());
        assertEquals(customNeighbourCount, bNeighbours.size());

        testMapPopulatedWithCorrectNeighbours(aNeighbours, TERM_A, 0.16d);
        testMapPopulatedWithCorrectNeighbours(bNeighbours, TERM_B, 0.18d);
    }

    /*
    Passes a parameter as neighbour count that is greater than the actual number of neighbours that exist
     */
    @Test
    public void testProcessMoreNeighboursThanExist() throws IOException, InvalidFileFormatException {
        File file = FileUtils.toFile(Thread.currentThread().getContextClassLoader().getResource("neighbours/wiki-test"));
        int customNeighbourCount = 100;
        Map<String, List<Neighbour>> neighbourMap = neighbourProcessor.buildNeighbourMap(file, customNeighbourCount);
        assertEquals(2, neighbourMap.size());

        List<Neighbour> aNeighbours = neighbourMap.get(TERM_A);
        List<Neighbour> bNeighbours = neighbourMap.get(TERM_B);

        assertEquals(TERM_A_NEIGHBOUR_COUNT, aNeighbours.size());
        assertEquals(TERM_B_NEIGHBOUR_COUNT, bNeighbours.size());

        testMapPopulatedWithCorrectNeighbours(aNeighbours, TERM_A, 0.16d);
        testMapPopulatedWithCorrectNeighbours(bNeighbours, TERM_B, 0.18d);

    }

    //@Test(expected = InvalidFileFormatException.class)
    public void testExceptionThrown() throws IOException, InvalidFileFormatException {
        File file = FileUtils.toFile(Thread.currentThread().getContextClassLoader().getResource("neighbours/invalid-file-format"));
        neighbourProcessor.buildNeighbourMap(file);
    }

    private void testMapPopulatedWithCorrectNeighbours(List<Neighbour> neighbours, String key, double similarityValue) {
        DecimalFormat df = new DecimalFormat("#.##");
        Double similarity;
        Double comparisonValue;
        for(Neighbour neighbour : neighbours) {
            assertEquals(key + (neighbours.indexOf(neighbour) + 1), neighbour.getTerm());

            similarity = neighbour.getSimilarity();
            comparisonValue = similarityValue - Double.parseDouble("0.01");
            comparisonValue = Double.valueOf(df.format(comparisonValue));
            assertEquals(comparisonValue, similarity);
            similarityValue = comparisonValue;
        }
    }

    @Test
    public void test() {
        File file = FileUtils.toFile(Thread.currentThread().getContextClassLoader().getResource("neighbours/test.neighbours"));
        Map<String, List<Neighbour>> neighbourMap = neighbourProcessor.buildNeighbourMap(file);
        System.out.println(neighbourMap.size());
    }



}
