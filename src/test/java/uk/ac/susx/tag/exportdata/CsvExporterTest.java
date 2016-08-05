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

public class CsvExporterTest {

    @Test
    public void testExport() throws IOException {
        List<SearchResult> searchResults = new ArrayList<>();
        SearchResult result = new SearchResult("1", null, "path", 5.5);
        searchResults.add(result);
        CsvExporter csvExporter = new CsvExporter(searchResults, "search");
        csvExporter.writeDataToCsv();

    }

    @Test
    public void testExportCsvDataFile() throws IOException {
        File file = FileUtils.toFile(Thread.currentThread().getContextClassLoader().getResource("csv/facebook.csv"));
        CsvData csvData = CsvReader.getCsvData(file, "facebook/id", "facebook/message");
        List<SearchResult> searchResults = new ArrayList<>();
        SearchResult result = new SearchResult("1", null, "path", 5.5);
        searchResults.add(result);
        SearchResult result2 = new SearchResult("3", null, "path", 5.5);
        searchResults.add(result2);
        CsvExporter csvExporter = new CsvExporter(searchResults, "search");
        csvExporter.writeCsvDataToNewCsv(csvData);
    }
}
