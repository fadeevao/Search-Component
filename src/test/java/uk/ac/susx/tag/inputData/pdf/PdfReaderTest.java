package uk.ac.susx.tag.inputData.pdf;


import org.apache.commons.io.FileUtils;
import org.junit.Test;
import uk.ac.susx.tag.inputData.InputData;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PdfReaderTest {

    @Test
    public void testPdfReadingUsualFont() throws IOException {
        File file = FileUtils.toFile(Thread.currentThread().getContextClassLoader().getResource("pdf/pdf_file.pdf"));
        InputData inputData = PdfReader.getPdfData(file);
        assertEquals("pdf_file.pdf", inputData.getId());
        assertTrue(inputData.getMessage().contains("Test"));
    }
}
