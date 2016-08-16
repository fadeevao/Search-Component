package uk.ac.susx.tag.lucenesearch.query_expansion.spellcheck.chatspeak;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class ChatspeakMapBuilder {

    private static final String DEFAULT_CHATSPEAK_DICTIONARY_PATH = "/spellcheck/chatspeak/chatspeak.csv";
    private static final String TERM = "term";
    private static final String ABBREVIATION = "abbreviation";


    public Map<String, List<String>> getChatspeakMap(String filePath) {
        InputStream in = getClass().getResourceAsStream(filePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        Iterable<CSVRecord> records = null;
        try {
            records = CSVFormat.EXCEL.withHeader().parse(reader);
        } catch (IOException e) {
            return  new HashMap<String, List<String>>();
        }
        Map<String, List<String>> chatspeakMap = new HashMap<>();

        for (CSVRecord record : records) {
            chatspeakMap.put(record.get(TERM), new ArrayList<String>(Arrays.asList(record.get(ABBREVIATION).split(","))));
        }
        return chatspeakMap;
    }

    public Map<String, List<String>> getChatspeakMap() {
        return getChatspeakMap(DEFAULT_CHATSPEAK_DICTIONARY_PATH);
    }
}
