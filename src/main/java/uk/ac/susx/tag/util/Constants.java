package uk.ac.susx.tag.util;


import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class Constants {
    public final static List<String> STOPWORDS = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8",
            "9", "000", "$", "£",
            "about", "after", "all", "also", "an", "and",
            "another", "any", "are", "as", "at", "be",
            "because", "been", "before", "being", "between",
            "both", "but", "by", "came", "can", "come",
            "could", "did", "do", "does", "each", "else",
            "for", "from", "get", "got", "has", "had",
            "he", "have", "her", "here", "him", "himself",
            "his", "how", "if", "in", "into", "is", "it",
            "its", "just", "like", "make", "many", "me",
            "might", "more", "most", "much", "must", "my",
            "never", "now", "of", "on", "only", "or",
            "other", "our", "out", "over", "re", "said",
            "same", "see", "should", "since", "so", "some",
            "still", "such", "take", "than", "that", "the",
            "their", "them", "then", "there", "these",
            "they", "this", "those", "through", "to", "too",
            "under", "up", "use", "very", "want", "was",
            "way", "we", "well", "were", "what", "when",
            "where", "which", "while", "who", "will",
            "with", "would", "you", "your",
            "a", "b", "c", "d", "e", "f", "g", "h", "i",
            "j", "k", "l", "m", "n", "o", "p", "q", "r",
            "s", "t", "u", "v", "w", "x", "y", "z");

    public final static List<String> POSESSIVE_PRONOUNS = Arrays.asList("my", "mine", "our", "ours",
            "its", "his", "her", "hers", "their", "theirs", "your", "yours");

    public final static List<String> PRONOUNS = Arrays.asList("I", "me", "you", "she", "he", "it", "we", "they", "them", "him", "her", "us");

    public final static List<String> PUNCTUATION = Arrays.asList(".", ",", "?", "!", ":", ";", "£", "%", "*");

    public static boolean termIsAConstant(String term) {
        return STOPWORDS.contains(term) || POSESSIVE_PRONOUNS.contains(term) || PUNCTUATION.contains(term) || PRONOUNS.contains(term);
    }

    public static boolean termIsNamedEntity(String term) {
        InputStream modelIn;
        Span nameSpans[];
        TokenNameFinderModel model = null;
        String termCapitalized = org.apache.commons.lang3.StringUtils.capitalize(term);
        try {

            File file = FileUtils.toFile(Thread.currentThread().getContextClassLoader().getResource("named-entity/en-ner-person.bin"));
            modelIn = new FileInputStream(file);
            model = new TokenNameFinderModel(modelIn);
        } catch (IOException e) {
            e.printStackTrace();
        }
        NameFinderME nameFinder = new NameFinderME(model);
        String sentence[] = new String[]{termCapitalized};
        nameSpans = nameFinder.find(sentence);
        return nameSpans.length > 0;
    }
}
