package uk.ac.susx.tag.exportdata;


import org.apache.commons.io.FileUtils;
import org.junit.Test;
import uk.ac.susx.tag.exportData.CsvExporter;
import uk.ac.susx.tag.inputData.csv.CsvData;
import uk.ac.susx.tag.inputData.csv.CsvReader;
import uk.ac.susx.tag.lucenesearch.result.SearchResult;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

public class CsvExporterTest {

    private final static String ID_KEY = "facebook/id";
    private final String MESSAGE_KEY = "facebook/message";

    private final static  String ID_KEY_OUT = "id";
    private final static String SCORE_KEY_OUT = "score";

    @Test
    public void testExport() throws IOException {
        List<SearchResult> searchResults = new ArrayList<>();
        SearchResult result = new SearchResult("1", null, "path", 5.5);
        searchResults.add(result);
        CsvExporter csvExporter = new CsvExporter(searchResults, "search1");
        csvExporter.writeDataToCsv();

        File outputFile = FileUtils.toFile(Thread.currentThread().getContextClassLoader().getResource("export/search_results_for_search1.csv"));
        CsvData exportCsvData = CsvReader.getCsvData(outputFile, ID_KEY_OUT, SCORE_KEY_OUT);
        assertEquals(1, exportCsvData.getCsvLines().size());
        assertEquals(2, exportCsvData.getHeaders().size());
        assertEquals(1, exportCsvData.getMessages().size());

        String[] line = exportCsvData.getCsvLines().get(0);
        assertEquals("1", line[0]);
        assertEquals("5.5", line[1]);
    }

    @Test
    public void testExportCsvDataFile() throws IOException {
        File file = FileUtils.toFile(Thread.currentThread().getContextClassLoader().getResource("csv/facebook.csv"));
        CsvData csvData = CsvReader.getCsvData(file, ID_KEY, MESSAGE_KEY);
        List<SearchResult> searchResults = new ArrayList<>();
        SearchResult result = new SearchResult("1", null, "path", 5.5);
        searchResults.add(result);
        SearchResult result2 = new SearchResult("3", null, "path", 6.5);
        searchResults.add(result2);
        CsvExporter csvExporter = new CsvExporter(searchResults, "search2");
        csvExporter.writeCsvDataToNewCsv(csvData);

        File outputFile = FileUtils.toFile(Thread.currentThread().getContextClassLoader().getResource("export/search_results_for_search2.csv"));
        CsvData exportCsvData = CsvReader.getCsvData(outputFile, ID_KEY, SCORE_KEY_OUT);
        assertEquals(2, exportCsvData.getCsvLines().size());
        assertTrue(exportCsvData.getHeaders().contains("score"));
        assertEquals(2, exportCsvData.getMessages().size());

        String[] line = exportCsvData.getCsvLines().get(0);
        assertEquals("1", line[1]);
        assertEquals("5.5", line[6]);

        String[] line2 = exportCsvData.getCsvLines().get(1);
        assertEquals("3", line2[1]);
        assertEquals("6.5", line2[6]);
    }
}
