package uk.ac.susx.tag.lucenesearch.query_expansion.highlighter;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.search.postingshighlight.PostingsHighlighter;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.search.vectorhighlight.FieldQuery;
import uk.ac.susx.tag.inputData.FileType;
import uk.ac.susx.tag.lucenesearch.result.SearchResult;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Highlights the section of the document that contains the given query term
 */
public class Highlighter {

    private IndexSearcher indexSearcher;


    public Highlighter(IndexSearcher indexSearcher) {
        this.indexSearcher = indexSearcher;

    }

    public List<SearchResult> highlight(Query query, TopDocs hits, FileType fileType) throws IOException, InvalidTokenOffsetsException {
        List<SearchResult> searchResults = new ArrayList<>();
        PostingsHighlighter highlighter = new CustomPostingsHighlighter(250);
        int indexLength = hits.scoreDocs.length > 1000 ? 1000 : hits.scoreDocs.length;
        String[] fragments;
        if (indexLength < hits.scoreDocs.length) {
            ScoreDoc[] docs = Arrays.copyOfRange(hits.scoreDocs, 0, indexLength);
            TopDocs topdocs = new TopDocs(indexLength, docs, hits.getMaxScore());
            fragments = highlighter.highlight("contents", query, indexSearcher, topdocs, 3);
        } else {
            fragments = highlighter.highlight("contents", query, indexSearcher, hits, 3);
        }

        //highlight for first 1000 docs, leave the rest with no highlighted results - as otherwise highlighting is not efficient
        List<List<List<HighlightedTextFragment>>> allHighlightedFragments = new ArrayList<>();
        for (int i = 0; i< indexLength; i++) {
            String[] docPassages = fragments[i].split("\\$FRAGMENT\\$");
            List<String> relevantFragments = new ArrayList<>();
            for (int j = 0; j < docPassages.length; j++) {
                if ((docPassages[j] != null)) {
                    System.out.println("> Text fragment: " + (docPassages[j]));
                    relevantFragments.add(docPassages[j]);
                }
            }
            allHighlightedFragments.add(getHighlighterTextFragments(relevantFragments));
        }

        for (int i = 0; i< hits.scoreDocs.length; i++) {
            int id = hits.scoreDocs[i].doc;
            Document doc = indexSearcher.doc(id);
            String filePath = doc.get("path");
            List<List<HighlightedTextFragment>> highlightedFragmentsForThisDocument = i< indexLength && !allHighlightedFragments.get(i).isEmpty() ? allHighlightedFragments.get(i) : new ArrayList<List<HighlightedTextFragment>>();
            SearchResult result = new SearchResult(doc.get("id"), highlightedFragmentsForThisDocument, filePath, hits.scoreDocs[i].score);
            if (fileType != null && !fileType.equals(FileType.PDF)) {
                result.setDocumentText(doc.get("contents"));
            }
            searchResults.add(result);
        }

        return searchResults;
    }

    private List<List<HighlightedTextFragment>> getHighlighterTextFragments(List<String> fragments) {
        List<List<HighlightedTextFragment>> stringFragments = new ArrayList<>();
        for (String fragment : fragments) {
            stringFragments.add(getTextFragments(fragment.replaceAll("\n", " ")));
        }
        return  stringFragments;
    }

    //Purely for the UI
    private List<HighlightedTextFragment> getTextFragments(String line) {
        Pattern p = Pattern.compile("%(\\S+)%");
        Matcher m = p.matcher(line);
        String[] a = StringUtils.splitByWholeSeparatorPreserveAllTokens(line, "%");

        ArrayList<String> highlightedParts = new ArrayList<>();
        while (m.find() )
        {
            String highlightedPart = m.group().replaceAll("%", "");
            highlightedParts.add(highlightedPart);
        }

        List<HighlightedTextFragment> fragments = new ArrayList<>();
        for (String str : a) {
            if (highlightedParts.contains(str)) {
                fragments.add(new HighlightedTextFragment(str, true));
            } else {
                fragments.add(new HighlightedTextFragment(str, false));
            }
        }
        return fragments;
    }

}
