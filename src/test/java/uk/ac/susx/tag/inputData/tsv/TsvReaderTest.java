package uk.ac.susx.tag.inputData.tsv;


import org.apache.commons.io.FileUtils;
import org.junit.Test;
import uk.ac.susx.tag.inputData.InputData;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TsvReaderTest {

    @Test
    public void testTsvReader() throws IOException {
        File file = FileUtils.toFile(Thread.currentThread().getContextClassLoader().getResource("tsv/trump.tsv"));
        List<InputData> data = TsvReader.getTsvData(file);
        assertEquals(5, data.size());
    }
}
