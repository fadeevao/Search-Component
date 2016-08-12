package uk.ac.susx.tag.lucenesearch.query_expansion.spellcheck;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/*
Builds a map of frequencies for future use. Reference file is frequencies obtained from the Gutenberg Project:
https://en.wiktionary.org/wiki/Wiktionary:Frequency_lists
 */
public class FrequenciesMapBuilder {

    private static final String DEFAULT_FREQUENCIES_PATH = "/spellcheck/frequencies.csv";
    private static final String WORD = "word";
    private static final String FREQUENCY = "frequency";


    public Map<String, BigDecimal> getFrequenciesMap(String filePath) throws IOException {
        InputStream in = getClass().getResourceAsStream(filePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(reader);
        Map<String, BigDecimal> frequenciesMap = new HashMap<>();

        for (CSVRecord record : records) {
            frequenciesMap.put(record.get(WORD), new BigDecimal(record.get(FREQUENCY)));
        }
        return frequenciesMap;
    }

    public Map<String, BigDecimal> getFrequenciesMap() throws IOException {
        return getFrequenciesMap(DEFAULT_FREQUENCIES_PATH);
    }


}
