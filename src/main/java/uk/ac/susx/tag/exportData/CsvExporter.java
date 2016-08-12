package uk.ac.susx.tag.exportData;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import uk.ac.susx.tag.inputData.InputData;
import uk.ac.susx.tag.inputData.csv.CsvData;
import uk.ac.susx.tag.lucenesearch.result.SearchResult;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
Class responsible for creating a csv file with search results that contain a user-entered search term.
If initially user uploaded a csv as a searchable file, search results will be
appended to the csv file - rows that contain documents not relevant to the search term will not be included
 */
public class CsvExporter {

    private List<SearchResult> searchResults;

    private String searchTerms;
    private static final String NEW_LINE_SEPARATOR = "\n";

    private static final Object [] FILE_HEADER = {"id","score"};

    public CsvExporter(List<SearchResult> searchResults, String query) {
        this.searchResults = searchResults;
        this.searchTerms = query;
    }

    public  CsvExporter() {}

    public void writeDataToCsv() throws IOException {

        FileWriter fileWriter = null;

        CSVPrinter csvFilePrinter = null;
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);

        try {
            fileWriter = new FileWriter("search_results_for_"+searchTerms+".csv");
            csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
            csvFilePrinter.printRecord(FILE_HEADER);
            //write all search results to the csv file
            for (SearchResult result : searchResults) {
                List<String> resultToWrite = new ArrayList<String>();
                resultToWrite.add(result.getFileId());
                resultToWrite.add(String.valueOf(result.getDocumentScore()));
                csvFilePrinter.printRecord(resultToWrite);
            }
        } finally {

            closeWriterAndFlush(fileWriter, csvFilePrinter);
        }
    }

    public void writeCsvDataToNewCsv(CsvData csvData) throws IOException{
        FileWriter fileWriter = null;
        csvData.getHeaders().add("score");

        CSVPrinter csvFilePrinter = null;
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);

        try {
            fileWriter = new FileWriter("search_results_for_"+searchTerms+".csv");
            csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
            csvFilePrinter.printRecord(csvData.getHeaders());

            for (InputData inputData : csvData.getMessages()) {
                for (SearchResult searchResult : searchResults) {
                    if (inputData.getId().equals(searchResult.getFileId())) {
                        List<String> resultToWrite = new ArrayList<String>();
                        int index = csvData.getMessages().indexOf(inputData);
                        addIndividualFieldsToTheFile(csvData, resultToWrite, index);
                        resultToWrite.add(String.valueOf(searchResult.getDocumentScore()));
                        csvFilePrinter.printRecord(resultToWrite);
                        break;
                    }
                }
            }
        } finally {

            closeWriterAndFlush(fileWriter, csvFilePrinter);
        }
    }

    private void closeWriterAndFlush(FileWriter fileWriter, CSVPrinter csvFilePrinter) {
        try {
            fileWriter.flush();
            fileWriter.close();
            csvFilePrinter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addIndividualFieldsToTheFile(CsvData csvData, List<String> resultToWrite, int index) {
        String[] lines = csvData.getCsvLines().get(index);
        for (String line : lines) {
            resultToWrite.add(line);
        }
    }

    public void setSearchResults(List<SearchResult> searchResults) {
        this.searchResults = searchResults;
    }

    public void setSearchTerms(String searchTerms) {
        this.searchTerms = searchTerms;
    }

}
