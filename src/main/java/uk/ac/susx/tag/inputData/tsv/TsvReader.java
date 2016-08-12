package uk.ac.susx.tag.inputData.tsv;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import uk.ac.susx.tag.inputData.InputData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TsvReader {

    public static List<InputData> getTsvData(File file)  {
        List<String> rows = new ArrayList<>();
        LineIterator it = null;
        try {
            it = FileUtils.lineIterator(file, "UTF-8");
        } catch (IOException e) {
            return null;
        }
        try {
            while (it.hasNext()) {
                rows.add(it.nextLine());
            }
        } finally {
            LineIterator.closeQuietly(it);
        }

        return convertRowsToInputData(rows);
    }

    private static List<InputData> convertRowsToInputData(List<String> rows) {
        List<InputData> inputDataList = new ArrayList<>();
        int i = 1;
        for (String string : rows) {
            inputDataList.add(new InputData(String.valueOf(i), string.replaceAll("\t", " ")));
            i++;
        }
        return inputDataList;
    }
}
