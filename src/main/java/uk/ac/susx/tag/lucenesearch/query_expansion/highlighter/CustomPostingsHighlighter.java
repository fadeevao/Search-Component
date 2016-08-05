package uk.ac.susx.tag.lucenesearch.query_expansion.highlighter;

import org.apache.lucene.search.postingshighlight.DefaultPassageFormatter;
import org.apache.lucene.search.postingshighlight.Passage;
import org.apache.lucene.search.postingshighlight.PassageFormatter;
import org.apache.lucene.search.postingshighlight.PostingsHighlighter;
import org.apache.lucene.search.spans.SpanWeight;


public class CustomPostingsHighlighter extends PostingsHighlighter {

    public CustomPostingsHighlighter(int length) {
        super(length);
    }

    @Override
    protected org.apache.lucene.search.postingshighlight.PassageFormatter getFormatter(java.lang.String field) {
        return new DefaultPassageFormatter("%", "%", "$FRAGMENT$", false);

    }
}
