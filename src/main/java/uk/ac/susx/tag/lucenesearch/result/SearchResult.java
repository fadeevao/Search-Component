package uk.ac.susx.tag.lucenesearch.result;

import uk.ac.susx.tag.lucenesearch.query_expansion.highlighter.HighlightedTextFragment;

import java.util.List;

public class SearchResult {

    private String fileId;

    private String filePath;

    private double documentScore;

    private String documentText;

    private List<List<HighlightedTextFragment>> docFragments;

    private List<String> highlightedTerms;

    public SearchResult(String id, List<List<HighlightedTextFragment>> docFragments, String filePath, double documentScore) {
        this.fileId = id;
        this.docFragments = docFragments;
        this.filePath = filePath;
        this.documentScore = documentScore;
    }

    public List<List<HighlightedTextFragment>> getDocFragments() {
        return docFragments;
    }

    public String getFileId() {
        return fileId;
    }

    public String getFilePath() {
        return filePath;
    }

    public double getDocumentScore() {
        return documentScore;
    }

    public String getDocumentText() {
        return documentText;
    }

    public void setDocumentText(String documentText) {
        this.documentText = documentText;
    }

    public List<String> getHighlightedTerms() {
        return highlightedTerms;
    }

    public void setHighlightedTerms(List<String> highlightedTerms) {
        this.highlightedTerms = highlightedTerms;
    }

}
