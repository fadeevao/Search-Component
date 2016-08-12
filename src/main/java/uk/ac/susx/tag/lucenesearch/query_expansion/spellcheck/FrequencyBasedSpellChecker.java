package uk.ac.susx.tag.lucenesearch.query_expansion.spellcheck;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import uk.ac.susx.tag.util.Constants;
import uk.ac.susx.tag.util.NamedEntityIdentifier;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/*
Idea inspired by: http://www.ncbi.nlm.nih.gov/pmc/articles/PMC400516/
 */
public class FrequencyBasedSpellChecker {

    private SpellChecker spellChecker;
    private NamedEntityIdentifier namedEntityIdentifier;
    private final static String DICTIONARY_PATH = "/spellcheck/dictionary.txt";
    private final static String FREQUENCIES_PATH = "/spellcheck/frequencies.csv";
    public final Map<String, BigDecimal> FREQUENCIES_MAP;

    /*
    default way to create a class using the default dictionary
     */
    public FrequencyBasedSpellChecker() throws IOException {
        this(DICTIONARY_PATH);
    }

    public FrequencyBasedSpellChecker(String dictionaryPath) throws IOException {
        this(dictionaryPath, FREQUENCIES_PATH);
    }

    /*
   Store index in RAM rather than in a specific directory
   This constructor is used for testing
    */
    public FrequencyBasedSpellChecker(String dictionaryPath, String frequenciesPath) throws IOException {
        Directory indexDirectory = new RAMDirectory();
        spellChecker = new SpellChecker(indexDirectory);
        spellChecker.setAccuracy(0.5f);

        Analyzer analyzer = new EnglishAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        InputStream inputStream = getClass().getResourceAsStream(dictionaryPath);
        spellChecker.indexDictionary(new PlainTextDictionary(inputStream), config, true);
        FREQUENCIES_MAP = new FrequenciesMapBuilder().getFrequenciesMap(frequenciesPath);
        namedEntityIdentifier = new NamedEntityIdentifier();
    }

    public String getBestSuggestion(String term) {
        Suggestion suggestion = null;
        if (!Constants.termIsAConstant(term) && !namedEntityIdentifier.termIsNamedEntity(term)) {
            String[] suggestedTerms = getSuggestions(term, 10);
            if (suggestedTerms != null) {
                suggestion = new Suggestion(term, Arrays.asList(suggestedTerms));
            }
        }


        Map<String, SimilarityMetric> frequencyMap = new HashMap<>();
        if (suggestion != null) {
            for (String suggestedTerm : suggestion.getSuggestions()) {
                Integer distance = StringUtils.getLevenshteinDistance(suggestion.getOriginalTerm(), suggestedTerm);
                SimilarityMetric similarityMetric = new SimilarityMetric(FREQUENCIES_MAP.get(suggestedTerm), distance);
                frequencyMap.put(suggestedTerm, similarityMetric);
            }

            Similarity similarity = new Similarity(suggestion.getOriginalTerm(), frequencyMap, FREQUENCIES_MAP.get(suggestion.getOriginalTerm()));


            String suggestedTerm = getSuggestionBasedOnFrequency(similarity);
            if (suggestedTerm != null) {
                return getSuggestionBasedOnFrequency(similarity);
            }
        }
        return term;

    }

    public List<String> getBestSuggestionForEveryWordInQuery(String query) {
        List<String> bestSuggestions = new ArrayList<>();
        if (query !=null) {
            String[] queryTerms = query.split(" ");
            for (String term : queryTerms) {
                if (term.length() != 0) {
                    bestSuggestions.add(getBestSuggestion(term));
                }
            }
        }
        return bestSuggestions;
    }

    private String getSuggestionBasedOnFrequency(Similarity similarity) {
        BigDecimal originalTermFrequency = similarity.getOriginalTermFrequency();
        List<Map.Entry<String, SimilarityMetric>> bestSuggestions = compareFrequenciesOfSuggestedWordsAndReturnTheMostLikelyOne(similarity.getSuggestionSimilarityMetrics());
        if (originalTermFrequency == null) {
            if (bestSuggestions.size() > 0) {
                return bestSuggestions.get(0).getKey();
            } else {
                return null;
            }
        }
        SimilarityMetric originalTermMetric = new SimilarityMetric(originalTermFrequency.multiply(new BigDecimal("1.5")), 0);

        SimilarityMetric bestSuggestionMetric = bestSuggestions.get(0).getValue();
        int comparisonResult = new SimilarityMetricComparator().compare(originalTermMetric, bestSuggestionMetric);

        if (comparisonResult >= 0) {
            return similarity.getOriginalTerm();
        }
        return bestSuggestions.get(0).getKey();
    }

    private List<Map.Entry<String, SimilarityMetric>> compareFrequenciesOfSuggestedWordsAndReturnTheMostLikelyOne(Map<String, SimilarityMetric> suggestionFrequencies) {
        //pick top 3 suggestions
        List<Map.Entry<String, SimilarityMetric>> bestSuggestions = suggestionFrequencies.entrySet()
                .stream()
                .filter(e -> e.getValue() != null)
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue(new SimilarityMetricComparator())))
                .collect(Collectors.toList());

        return bestSuggestions;
    }

    public String[] getSuggestions(String term, int numberOfSuggestions) {
        if (term.length() > 0) {
            try {
                return spellChecker.suggestSimilar(term, numberOfSuggestions);
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    public boolean wordExists(String term) {
        try {
            return spellChecker.exist(term);
        } catch (IOException e) {
            return false;
        }
    }
}

