package uk.ac.susx.tag.lucenesearch;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;

public class LuceneTest {
    private RAMDirectory ramDirectory;
    private IndexWriter indexWriter;

    public LuceneTest() {
        ramDirectory = new RAMDirectory();
    }

    public IndexWriter getIndexWriterWithRamDirectory(Analyzer analyzer) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        return new IndexWriter(ramDirectory, config);
    }

    public IndexWriter getIndexWriterWithRamDirectory() throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(new EnglishAnalyzer());
        return new IndexWriter(ramDirectory, config);
    }
}
