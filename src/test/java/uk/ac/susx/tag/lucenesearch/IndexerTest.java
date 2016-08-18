package uk.ac.susx.tag.lucenesearch;


import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.ac.susx.tag.inputData.FileType;
import uk.ac.susx.tag.inputData.csv.CsvData;
import uk.ac.susx.tag.inputData.csv.CsvReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class IndexerTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testIndexingCsvFile() throws IOException {
        Directory ramDirectory = new RAMDirectory();
        Indexer indexer = new Indexer(ramDirectory);
        indexer.createIndex(getFacebookCsvData(), false);

        IndexReader reader = DirectoryReader.open(indexer.getIndexDirectory());
        assertEquals(4, reader. numDocs());

        Document documentOne = reader.document(0);
        assertEquals("1", documentOne.get("id"));
        assertNotNull(documentOne.get("contents")); //we should not store the contents

        assertEquals("2", reader.document(1).get("id"));
        assertEquals("3", reader.document(2).get("id"));
        assertEquals("4", reader.document(3).get("id"));

        exception.expect(IllegalArgumentException.class);
        reader.document(4).get("id");
    }

    @Test
    public void testIndexingPdfFilesInDirectory() throws IOException {
        Directory ramDirectory = new RAMDirectory();
        Indexer indexer = new Indexer(ramDirectory);
        indexer.createIndex("src/test/resources/pdf", FileType.PDF);
        IndexReader reader = DirectoryReader.open(indexer.getIndexDirectory());
        assertEquals(2, reader. numDocs());
    }

    @Test
    public void testIndexingTextFilesInDirectory() throws IOException {
        Directory ramDirectory = new RAMDirectory();
        Indexer indexer = new Indexer(ramDirectory);
        indexer.createIndex("src/test/resources/lucenedata/samplefiles", FileType.TEXT_FILE);
        IndexReader reader = DirectoryReader.open(indexer.getIndexDirectory());
        assertEquals(4, reader. numDocs());
        assertEquals(4, reader.getDocCount("path"));
        assertEquals(4, reader.getDocCount("id"));
        assertEquals(4, reader.getDocCount("contents"));


        assertNotNull(reader.document(0).get("path"));
        List<String> docs = new ArrayList<>();
        for (int i = 0; i <reader.numDocs(); i++) {
            docs.add(reader.document(i).get("id"));
        }

        assertTrue(docs.contains("3student.txt"));
        assertTrue(docs.contains("4moons.txt"));
        assertTrue(docs.contains("deer.txt"));
        assertTrue(docs.contains("goldfish.txt"));
        exception.expect(IllegalArgumentException.class);
        reader.document(4).get("id");
    }

    private CsvData getFacebookCsvData() throws IOException {
        File file = FileUtils.toFile(Thread.currentThread().getContextClassLoader().getResource("csv/facebook.csv"));
        CsvData csvData = CsvReader.getCsvData(file, "facebook/id", "facebook/message");
        return csvData;
    }
}
