package uk.ac.susx.tag.inputData.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import uk.ac.susx.tag.inputData.InputData;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/*
Reads in a csv file and constructs a CsvData object.
 */
public class CsvReader {

    public static CsvData getCsvData(File file, String idKey, String messageKey)  {
        Reader fileReader = null;
        List<InputData> messages = new ArrayList<>();
        List<String[]> csvLines = new ArrayList<>();
        Set<String> headers;
        try {
            fileReader = new FileReader(file);
            CSVParser parser = new CSVParser(fileReader, CSVFormat.EXCEL.withHeader());
            headers = parser.getHeaderMap().keySet();
                    List<CSVRecord> records = parser.getRecords();
            for (CSVRecord record : records) {
                messages.add(new InputData(record.get(idKey), record.get(messageKey)));
                csvLines.add(recordData(record, headers));
            }
        } catch (FileNotFoundException e) {
           return null;
        } catch (IOException ex) {
            return null;
        }
        return new CsvData(messages, csvLines, new ArrayList<>(headers));
    }

    public static CsvData getCsvData(List<File> files, String key, String messageKey) {
        List<InputData> messages = new ArrayList<>();
        List<String[]> csvLines = new ArrayList<>();
        List<String> headers = new ArrayList<>();
        for (File file : files) {
            CsvData singleCsv = getCsvData(file, key, messageKey);
            messages.addAll(singleCsv.getMessages());
            csvLines.addAll(singleCsv.getCsvLines());
            headers = singleCsv.getHeaders();
        }
        return new CsvData(messages, csvLines, headers);

    }

    private  static String[] recordData(CSVRecord record, Set<String> headers) {
        String[] fields = new String[headers.size()];
        for (int i = 0; i< headers.size(); i++) {
            fields[i] = record.get(i);
        }
        return  fields;
    }
}
