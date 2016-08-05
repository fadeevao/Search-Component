package uk.ac.susx.tag.inputData.csv;


import uk.ac.susx.tag.inputData.InputData;

import java.util.ArrayList;
import java.util.List;
/*
Keeps the list of messages and their ids and all other data that was present in a csv file
 */
public class CsvData {
    private List<InputData> messages;

    private List<String[]> csvLines;

    private ArrayList<String> headers;

    public CsvData(List<InputData> messages, List<String[]> csvLines, ArrayList<String> headers) {
        this.messages = messages;
        this.csvLines = csvLines;
        this.headers = headers;
    }

    public List<InputData> getMessages() {
        return messages;
    }

    public ArrayList<String> getHeaders() {
        return headers;
    }


    public List<String[]> getCsvLines() {
        return csvLines;
    }
}
