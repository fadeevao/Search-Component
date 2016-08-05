package uk.ac.susx.tag.neighbours;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileFormatValidatorTest {

//    @Test
//    public void testValidFormat() {
//        String regEx = "(([a-zA-Z0-9]+[\\/]*[a-zA-Z]*\\s*)+(\\d+\\.\\d+\\s*))+";
//        String n = "a/NNP a1/NNP 0.15 a2/NNP 0.14 a3/NNP 0.13 a4/NNP 0.12 a5/NNP 0.11";
//        String line = "jha/NNP sharma/NNP 0.23267 mishra/NNP 0.229307 sinha/NNP 0.226327 ghosh/NNP 0.223128 shukla/NNP 0.216498\n";
//        Pattern.matches(regEx, line);
//
//    }

    @Test
    public void testValidFormat() throws IOException {
        File file = FileUtils.toFile(Thread.currentThread().getContextClassLoader().getResource("neighbours/wiki-test"));
        List<String> fileContents = FileUtils.readLines(file);
        assertTrue(FileFormatValidator.isFileInAValidFormat(fileContents));

    }

    @Test
    public void testInvalidFormat() throws IOException {
        File file = FileUtils.toFile(Thread.currentThread().getContextClassLoader().getResource("neighbours/invalid-file-format"));
        List<String> fileContents = FileUtils.readLines(file);
        assertFalse(FileFormatValidator.isFileInAValidFormat(fileContents));

    }
}
