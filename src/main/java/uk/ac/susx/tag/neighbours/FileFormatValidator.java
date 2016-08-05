package uk.ac.susx.tag.neighbours;


import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileFormatValidator {

    private static final String REG_EX = "(([a-zA-Z0-9]+[\\/]*[a-zA-Z]*\\s*)+(\\d+\\.\\d+\\s*\\n*))+";

    public static boolean isFileInAValidFormat(List<String> fileContents) {
        for (String line: fileContents) {
            if (!Pattern.matches(REG_EX, line)) {
                return false;
            }
        }
        return true;
    }
}
