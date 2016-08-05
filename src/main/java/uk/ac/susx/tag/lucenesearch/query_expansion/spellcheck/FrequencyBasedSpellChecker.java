package uk.ac.susx.tag.lucenesearch.query_expansion.spellcheck;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.*;
import uk.ac.susx.tag.util.Constants;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/*
http://www.ncbi.nlm.nih.gov/pmc/articles/PMC400516/
 */
public class FrequencyBasedSpellChecker {

    private SpellChecker spellChecker;
    private final static String DICTIONARY_PATH = "spellcheck/dictionary.txt";
    private final static String FREQUENCIES_PATH = "spellcheck/frequencies.csv";
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
        File file = FileUtils.toFile(Thread.currentThread().getContextClassLoader().getResource(dictionaryPath));
        spellChecker.indexDictionary(new PlainTextDictionary(new FileReader(file)), config, true);
        FREQUENCIES_MAP = FrequenciesMapBuilder.getFrequenciesMap(frequenciesPath);
    }

    public List<String> getBestSuggestionForEveryWordInQuery(String query)  {
        String[] queryTerms = query.split(" ");
        List<Suggestion> suggestionsList = new ArrayList<>();
        for (String term: queryTerms) {
            if (!Constants.termIsAConstant(term) && !Constants.termIsNamedEntity(term) && term.length() > 4) {
                String[] suggestedTerms = getSuggestions(term, 10);
                if (suggestedTerms != null) {
                    suggestionsList.add(new Suggestion(term, Arrays.asList(suggestedTerms)));
                }
            }
        }
        List<Similarity> similarities = new ArrayList<>();
        for (Suggestion suggestion: suggestionsList) {
            Map<String, SimilarityMetric> frequencyMap = new HashMap<>();
            for (String suggestedTerm: suggestion.getSuggestions()) {
                Integer distance = StringUtils.getLevenshteinDistance(suggestion.getOriginalTerm(), suggestedTerm);
                SimilarityMetric similarityMetric = new SimilarityMetric(FREQUENCIES_MAP.get(suggestedTerm), distance);
                frequencyMap.put(suggestedTerm, similarityMetric);
            }

            similarities.add(new Similarity(suggestion.getOriginalTerm(), frequencyMap, FREQUENCIES_MAP.get(suggestion.getOriginalTerm())));
        }

        List<String> bestSuggestions = new ArrayList<>();
        for(Similarity similarity : similarities) {
            String suggestion = getSuggestionBasedOnFrequency(similarity);
            if (suggestion != null) {
                bestSuggestions.add(getSuggestionBasedOnFrequency(similarity));
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
        int comparisonResult = new SimilarityMetricComparator().compare( originalTermMetric, bestSuggestionMetric );

        if(comparisonResult >= 0) {
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

    public String[] getSuggestions(String term,  int numberOfSuggestions)  {
        if (term.length() > 0 ) {
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

