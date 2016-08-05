package uk.ac.susx.tag.util;


import org.apache.commons.io.FileUtils;
import uk.ac.susx.tag.inputData.InputData;
import uk.ac.susx.tag.inputData.TextInputData;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.types.RuntimeType;
import uk.ac.susx.tag.solrsearch.SolrItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileHelper {

    public static List<SolrItem> getSolrItemFromInputData(List<? extends InputData> messages) {
        List<SolrItem> indexableItems = new ArrayList<SolrItem>();
        for (InputData message : messages) {
            indexableItems.add(new SolrItem(message));
        }
        return indexableItems;
    }

    public static InputData getInputDataFromFile(File file) {
        if(getFileExtension(file).equals("pdf")) {
            return null;
        }
        String fileContents = null;
        try {
            fileContents = FileUtils.readFileToString(file);
        } catch (IOException e) {
            return null;
        }
        return new TextInputData(file.getName(), fileContents, file.getPath());
    }

    public static InputData getInputDataFromDatumObject(Datum datum, String idKey, String mainBodyKey) {
        Key<String> mainBody = Key.of(mainBodyKey, RuntimeType.STRING);
        Key<String> documentId = Key.of(idKey, RuntimeType.STRING);
        return new InputData(datum.get(documentId), datum.get(mainBody));
    }

    private static String getFileExtension(File file) {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }


}