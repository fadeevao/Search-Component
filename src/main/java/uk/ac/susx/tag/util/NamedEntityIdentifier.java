package uk.ac.susx.tag.util;


import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

import java.io.IOException;
import java.io.InputStream;

public class NamedEntityIdentifier {

    private final String MODEL_PATH = "/named-entity/en-ner-person.bin";
    private NameFinderME nameFinder;

    public NamedEntityIdentifier() {
        InputStream modelIn;
        TokenNameFinderModel model = null;
        modelIn = getClass().getResourceAsStream(MODEL_PATH);
        try {
            model = new TokenNameFinderModel(modelIn);
        } catch (IOException e) {
            e.printStackTrace();
        }
        nameFinder = new NameFinderME(model);
    }

    public boolean termIsNamedEntity(String term) {
        Span nameSpans[];
        String termCapitalized = org.apache.commons.lang3.StringUtils.capitalize(term);
        String sentence[] = new String[]{termCapitalized};
        nameSpans = nameFinder.find(sentence);
        return nameSpans.length > 0;
    }
}
