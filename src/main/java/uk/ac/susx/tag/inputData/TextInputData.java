package uk.ac.susx.tag.inputData;

/*
Represents PDF/text files as to give preview in the UI based on the path
 */
public class TextInputData extends InputData {

    private String path;

    public TextInputData(String id, String message, String path) {
        super(id, message);
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
