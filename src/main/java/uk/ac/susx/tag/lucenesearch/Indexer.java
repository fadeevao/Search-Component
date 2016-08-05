package uk.ac.susx.tag.lucenesearch;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import uk.ac.susx.tag.inputData.FileType;
import uk.ac.susx.tag.inputData.csv.CsvData;
import uk.ac.susx.tag.inputData.InputData;
import uk.ac.susx.tag.inputData.TextInputData;
import uk.ac.susx.tag.inputData.csv.CsvReader;
import uk.ac.susx.tag.inputData.pdf.PdfReader;
import uk.ac.susx.tag.inputData.tsv.TsvReader;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.util.FileHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Indexer {

    private IndexWriter writer;

    private Directory indexDirectory;

    private Analyzer analyzer = new EnglishAnalyzer(CharArraySet.EMPTY_SET);

    //TODO pass in the analyzer
    public Indexer(String indexDirectoryPath, IndexWriterConfig.OpenMode openMode) throws IOException {
        //this directory will contain the indices
        indexDirectory =
                FSDirectory.open(Paths.get(indexDirectoryPath));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(openMode);
        //create the indexer
        writer = new IndexWriter(indexDirectory, config);
    }

    public Indexer(String indexDirectoryPath) throws IOException {
        this(indexDirectoryPath, IndexWriterConfig.OpenMode.CREATE);
    }

    /*
    For testing purposes or small document collections only - uses RAM directory for indexing
     */
    public Indexer(Directory directory) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setCommitOnClose(true);
        config.setRAMBufferSizeMB(48);
        config.setUseCompoundFile(false);
        indexDirectory = directory;
        writer = new IndexWriter(indexDirectory, config);
    }


    public void close() throws CorruptIndexException, IOException{
        writer.close();
    }

    private Document getDocument(InputData inputData) throws IOException{
        Document document = new Document();

        String fileContent = inputData.getMessage();

        Field contentField = new Field("contents",
                fileContent, getFieldTypeStoringOffsets());

        //index file's id/path unless we specifically specify path as in the case of indexing a text file
        Field idField = new Field("id",
                inputData.getId(),
                TextField.TYPE_STORED);

        if (inputData instanceof TextInputData) {
            //index file path
            Field filePathField = new Field("path",
                    ((TextInputData) inputData).getPath(),
                    TextField.TYPE_STORED);
            document.add(filePathField);
        }


        document.add(contentField);
        document.add(idField);

        return document;
    }

    public void createIndex(CsvData csvData, boolean indexMultipleFiles) throws IOException {
        for (InputData message : csvData.getMessages()) {
            indexData(message);
        }
        if (!indexMultipleFiles) close();
    }

    public void createIndex(List<Datum> datumList, String idKey, String mainBodyKey) throws IOException {
        for (Datum datum : datumList) {
            indexData(FileHelper.getInputDataFromDatumObject(datum, idKey, mainBodyKey));
        }
        close();
    }

    //Must call this.close() after this method has been invoked for N times
    public void indexData(InputData inputData) throws IOException{
        if (inputData != null) {
            Document document = getDocument(inputData);
            writer.addDocument(document);
        }
    }

    //TODO: add support for .doc/.docx files?
    public void createIndex(String dataDirPath, FileType fileType, String idKey, String messageKey)
            throws IOException{
        //get all files in the data directory
        List<File> files = Files.walk(Paths.get(dataDirPath))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());

        for (File file : files) {
            if(!file.isDirectory()
                    && !file.isHidden()
                    && file.exists()
                    && file.canRead()){
                if (fileType.equals(FileType.TEXT_FILE)) {
                    indexData(FileHelper.getInputDataFromFile(file));
                } else if (fileType.equals(FileType.PDF)) {
                    indexData(PdfReader.getPdfData(file));
                } else if (fileType.equals(FileType.CSV)) {
                    createIndex(CsvReader.getCsvData(file, idKey, messageKey), true);
                } else if (fileType.equals(FileType.TSV)) {
                    List<InputData> data = TsvReader.getTsvData(file);
                    if (data != null) {
                        for (InputData data1 : data) {
                            indexData(data1);
                        }
                    }
                }
            }
        }
        close();
    }

    private FieldType getFieldTypeStoringOffsets() {
        FieldType offsetsType = new FieldType(TextField.TYPE_STORED);
        offsetsType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        return offsetsType;
    }

    public Directory getIndexDirectory() {
        return indexDirectory;
    }

}
