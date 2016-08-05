package uk.ac.susx.tag.inputData;

/*
Represents any  sort of input data be it a text file or csv fields that represent the  message and id of the file (which can also
 be a path to the file)
 */
public class InputData {
    private String id;
    private String message;

    public InputData(String id, String message) {
        this.id=id;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }
}
