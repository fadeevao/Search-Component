package uk.ac.susx.tag.lucenesearch;


import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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
import uk.ac.susx.tag.lucenesearch.query_expansion.highlighter.HighlightedTextFragment;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.types.RuntimeType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

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
        indexer.createIndex("src/test/resources/pdf", FileType.PDF, null, null);
        IndexReader reader = DirectoryReader.open(indexer.getIndexDirectory());
        assertEquals(2, reader. numDocs());
    }

    @Test
    public void testIndexingTextFilesInDirectory() throws IOException {
        Directory ramDirectory = new RAMDirectory();
        Indexer indexer = new Indexer(ramDirectory);
        indexer.createIndex("src/test/resources/lucenedata/samplefiles", FileType.TEXT_FILE, null, null);
        IndexReader reader = DirectoryReader.open(indexer.getIndexDirectory());
        assertEquals(4, reader. numDocs());

        assertNotNull(reader.document(0).get("path"));
        assertEquals("3student.txt", reader.document(0).get("id"));
        assertEquals("4moons.txt", reader.document(1).get("id"));
        assertEquals("deer.txt", reader.document(2).get("id"));
        assertEquals("goldfish.txt", reader.document(3).get("id"));

        exception.expect(IllegalArgumentException.class);
        reader.document(4).get("id");
    }

    @Test
    public void testIndexingDatumObjects() throws IOException {
        Key<String> mainBody = Key.of("body", RuntimeType.STRING);
        Key<String> id = Key.of("id", RuntimeType.STRING);
        Datum datumOne = new Datum();
        datumOne = datumOne
                .with(mainBody, "once upon a time")
                .with(id, "one");
        Datum datumTwo = new Datum();
        datumTwo = datumTwo
                .with(mainBody, "a woodcutter lived happily with his wife")
                .with(id, "two");


        Directory ramDirectory = new RAMDirectory();
        Indexer indexer = new Indexer(ramDirectory);
        indexer.createIndex(new ArrayList<Datum>(Arrays.asList(datumOne, datumTwo)), "id", "body");
        IndexReader reader = DirectoryReader.open(indexer.getIndexDirectory());
        assertEquals(2, reader. numDocs());
        assertEquals("one", reader.document(0).get("id"));
        assertEquals("two", reader.document(1).get("id"));
    }

    private CsvData getFacebookCsvData() throws IOException {
        File file = FileUtils.toFile(Thread.currentThread().getContextClassLoader().getResource("csv/facebook.csv"));
        CsvData csvData = CsvReader.getCsvData(file, "facebook/id", "facebook/message");
        return csvData;
    }
}
