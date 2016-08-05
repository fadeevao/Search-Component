package uk.ac.susx.tag.lucenesearch.query_expansion.highlighter;


/*
Needed for the UI to make fragments that are relevant to the search query bold
 */
public class HighlightedTextFragment {
    private boolean highlighted;
    private String TextFragment;

    public HighlightedTextFragment(String textFragment, boolean highlighted) {
        TextFragment = textFragment;
        this.highlighted = highlighted;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public String getTextFragment() {
        return TextFragment;
    }

    public void setTextFragment(String textFragment) {
        TextFragment = textFragment;
    }
}
