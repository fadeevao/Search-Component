package uk.ac.susx.tag.util;


import opennlp.tools.stemmer.PorterStemmer;

public class Stemmer {

    private final static PorterStemmer porterStemmer = new PorterStemmer();

    public static String stem(String word) {
        return porterStemmer.stem(word);
    }

}
