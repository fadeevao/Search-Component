package uk.ac.susx.tag.lucenesearch.exception;


import org.apache.lucene.search.IndexSearcher;

public class IndexingException extends Exception {
    public IndexingException(String msg) {
        super(msg);
    }
}
