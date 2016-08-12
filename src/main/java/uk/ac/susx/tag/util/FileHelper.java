package uk.ac.susx.tag.util;


import org.apache.commons.io.FileUtils;
import uk.ac.susx.tag.inputData.InputData;
import uk.ac.susx.tag.inputData.TextInputData;

import java.io.File;
import java.io.IOException;

public class FileHelper {

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
/*
    public static InputData getInputDataFromDatumObject(Datum datum, String idKey, String mainBodyKey) {
        Key<String> mainBody = Key.of(mainBodyKey, RuntimeType.STRING);
        Key<String> documentId = Key.of(idKey, RuntimeType.STRING);
        return new InputData(datum.get(documentId), datum.get(mainBody));
    }*/

    private static String getFileExtension(File file) {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }


}
