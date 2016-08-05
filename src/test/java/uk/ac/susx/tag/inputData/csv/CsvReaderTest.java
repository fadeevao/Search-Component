package uk.ac.susx.tag.inputData.csv;


import org.apache.commons.io.FileUtils;
import org.junit.Test;
import uk.ac.susx.tag.inputData.InputData;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CsvReaderTest {

    @Test
    public void testCsvReading() throws IOException {
        File file = FileUtils.toFile(Thread.currentThread().getContextClassLoader().getResource("csv/facebook.csv"));
        CsvData csvData = CsvReader.getCsvData(file, "facebook/id", "facebook/message");
        List<InputData> messages = csvData.getMessages();
        for (int i =0; i<messages.size(); i++) {
            assertEquals(messages.get(i).getId(), String.valueOf(i+1));
        }

        assertTrue(csvData.getMessages().get(0).getMessage().contains("Boris"));
        assertTrue(csvData.getMessages().get(3).getMessage().contains("Boris"));
    }
}
