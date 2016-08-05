package uk.ac.susx.tag;

import java.io.*;

public class MainTest {
    //public static void main(String[] args) throws IOException {
        /*
        SolrDao<SolrItem> dao = new SolrDao<>("http://localhost:8983/solr/testcore");
        File file = FileUtils.toFile(Thread.currentThread().getContextClassLoader().getResource(""));
        if (file.isDirectory()) {
//            List<File> filesInFolder = Files.walk(Paths.get("/path/to/folder"))
//                    .filter(Files::isRegularFile)
//                    .map(Path::toFile)
//                    .collect(Collectors.toList());
//            List<TextFile> textFiles = new ArrayList<TextFile>();
//            for (File fileInFolder : filesInFolder) {
//                String fileContent = FileUtils.readFileToString(fileInFolder);
//                textFiles.add(new TextFile(fileInFolder.getCanonicalPath(), fileContent));
//            }
//            dao.addCollection(FileHelper.getSolrItemFromInputData(textFiles));

        } else {
            CsvData data = CsvReader.getCsvData(file, "facebook/id", "facebook/message");
            dao.addCollection(FileHelper.getSolrItemFromInputData(data.getMessages()));
        }

        SolrDocumentList list = dao.readAll();
        System.out.println(list.size());
        for (SolrDocument doc : list) {
            System.out.println(doc.get("content"));
        }

        Searcher searcher = new Searcher("http://localhost:8983/solr/testcore");
        SolrDocumentList solrDocuments = searcher.searchForTerm("Boris");
        System.out.println(solrDocuments.size());
        */

/*
        File file = FileUtils.toFile(Thread.currentThread().getContextClassLoader().getResource("frequencies2.csv"));
        String content = FileUtils.readFileToString(file);
        //Use a BufferedReader to read from actual Text fil
        String csv = content.replaceAll(",\\r+", "\n");

        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("MyCSV.csv")));
        out.println(csv);
        out.close();


        File fileCsv = FileUtils.toFile(Thread.currentThread().getContextClassLoader().getResource("MyCSV.csv"));
        Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(new FileReader(fileCsv));
        List<CSVRecord> csvFields = new ArrayList<CSVRecord>();
        for (CSVRecord record : records) {
            csvFields.add(record);
        }
        System.out.println(csvFields.size());

*/

    //}
}
