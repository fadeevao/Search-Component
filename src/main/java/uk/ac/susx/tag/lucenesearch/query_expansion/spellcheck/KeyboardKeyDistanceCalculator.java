package uk.ac.susx.tag.lucenesearch.query_expansion.spellcheck;

/*
Idea taken from: http://stackoverflow.com/questions/2120646/keyboard-layout-library-to-find-neighboring-keys-given-an-input-key-java-prefer

 need to make sure that li >> ku is less distance than li >> ou (take diagonals into account)
 */
public class KeyboardKeyDistanceCalculator {

    private  static String keyboardChars = "1234567890-qwertyuiopasdfghjkl;zxcvbnm,.";

    private static double distance(char c1, char c2) {
        return Math.sqrt(Math.pow(colOf(c2)-colOf(c1),2)+Math.pow(rowOf(c2)-rowOf(c1),2));
    }

    private static int rowOf(char c) {
        return keyboardChars.indexOf(c) / 10;
    }

    private static int colOf(char c) {
        return keyboardChars.indexOf(c) % 10;
    }

    public static double getTermDistance(String word1, String word2) {
        double distance = 0.0;
        //if any of words is null, then we assume they are far from each other
        if (word1==null || word2==null) return 20;
        int indexLength = word1.length() >= word2.length() ? word2.length() : word1.length();
        for (int i = 0; i<indexLength; i++) {
            distance += (distance(word1.charAt(i), word2.charAt(i)));
        }
        return distance;
    }
}
