package uk.ac.susx.tag.lucenesearch.query_expansion.spellcheck;

import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class FrequenciesMapBuilderTest {

    @Test
    public void testMapBuilder() throws IOException {
        HashMap<String, BigDecimal> map = (HashMap<String, BigDecimal>) new FrequenciesMapBuilder().getFrequenciesMap("/spellcheck/frequencies-test-set.csv");
        assertEquals(5, map.size());
        assertEquals(new BigDecimal("923053"), map.get("men"));
        assertEquals(new BigDecimal("922130"), map.get("own"));
        assertEquals(new BigDecimal("899673"), map.get("never"));
        assertEquals(new BigDecimal("889691"), map.get("most"));
        assertEquals(new BigDecimal("887917"), map.get("old"));
    }
}
