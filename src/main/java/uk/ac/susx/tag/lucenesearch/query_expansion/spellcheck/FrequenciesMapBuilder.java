package uk.ac.susx.tag.lucenesearch.query_expansion.spellcheck;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/*
Builds a map of frequencies for future use. Reference file is frequencies obtained from the Gutenberg Project:
https://en.wiktionary.org/wiki/Wiktionary:Frequency_lists
 */
public class FrequenciesMapBuilder {

    private static final String DEFAULT_FREQUENCIES_PATH = "spellcheck/frequencies.csv";
    private static final String WORD = "word";
    private static final String FREQUENCY = "frequency";


    public static Map<String, BigDecimal> getFrequenciesMap(String filePath) throws IOException {
        File file = FileUtils.toFile(Thread.currentThread().getContextClassLoader().getResource(filePath));
        Reader fileReader = new FileReader(file);

        Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(fileReader);
        Map<String, BigDecimal> frequenciesMap = new HashMap<>();

        for (CSVRecord record : records) {
            frequenciesMap.put(record.get(WORD), new BigDecimal(record.get(FREQUENCY)));
        }
        return frequenciesMap;
    }

    public static Map<String, BigDecimal> getFrequenciesMap() throws IOException {
        return getFrequenciesMap(DEFAULT_FREQUENCIES_PATH);
    }


}
