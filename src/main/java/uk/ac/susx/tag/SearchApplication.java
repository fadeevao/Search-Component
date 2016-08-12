package uk.ac.susx.tag;


import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import uk.ac.susx.tag.exportData.CsvExporter;
import uk.ac.susx.tag.inputData.FileType;
import uk.ac.susx.tag.inputData.csv.CsvData;
import uk.ac.susx.tag.lucenesearch.Indexer;
import uk.ac.susx.tag.lucenesearch.Searcher;
import uk.ac.susx.tag.lucenesearch.neighbours.NeighbourSearcher;
import uk.ac.susx.tag.lucenesearch.query_expansion.QueryBuilder;
import uk.ac.susx.tag.lucenesearch.result.SearchResultWithSuggestions;
import uk.ac.susx.tag.neighbours.NeighbourProcessor;

import java.io.Console;
import java.io.IOException;

public class SearchApplication {
    public static void main(String[] args) throws IOException, InvalidTokenOffsetsException, ParseException {
        CsvExporter csvExporter = new CsvExporter();

        Console console = System.console();
        if (console == null) {
            System.err.println("No console.");
            System.exit(1);
        }
        System.out.println("Welcome to the search application");
        System.out.println("1. Enter a path to the index directory");
        String indexDirectoryPath = console.readLine();


        System.out.println("2. Enter the format of the file you  want to index: " + StringUtils.join(FileType.values(), ","));
        FileType fileType = FileType.valueOf(console.readLine());
        boolean workingWithCsv = fileType.equals(FileType.CSV);
        String idKey = "";
        String messageKey = "";
        if(workingWithCsv) {
            System.out.println("Enter id key");
            idKey = console.readLine();

            System.out.println("Enter message key");
            messageKey = console.readLine();
        }

        System.out.println("3. Enter a path to searchable file/directory");
        String searchableFilesLocation = console.readLine();

        System.out.println("4. Do you want to upload distributional semantics model? Y/N");
        boolean useDisSemanticsModel = console.readLine().toLowerCase().equals("y") ? true : false;


        QueryBuilder queryBuilder = new QueryBuilder();
        boolean separateSpellingSuggestions= false;
        if(useDisSemanticsModel) {
            System.out.println("Enter a path to the the model");
            String neighboursFilePath = console.readLine();

            System.out.println("Enter a number of neighbours to analyse:");
            Integer numberOfNeighboursToAnalyze = Integer.parseInt(console.readLine());

            System.out.println("Do you want to separate spelling suggestions? Y/N");
            separateSpellingSuggestions = console.readLine().toLowerCase().equals("y") ? true : false;

            NeighbourProcessor neighbourProcessor = new NeighbourProcessor();
            NeighbourSearcher neighbourSearcher = new NeighbourSearcher(
                    neighbourProcessor.buildNeighbourMap(neighboursFilePath, numberOfNeighboursToAnalyze*5),
                    numberOfNeighboursToAnalyze,
                    separateSpellingSuggestions);
            queryBuilder.setNeighbourSearcher(neighbourSearcher);

        }



        Indexer indexer = new Indexer(indexDirectoryPath);
        CsvData csvData = null;
        if (workingWithCsv) {
            csvData =indexer.createIndex(searchableFilesLocation, idKey, messageKey);
        } else {
            indexer.createIndex(indexDirectoryPath, fileType);
        }
        System.out.println("Index created");
        Searcher searcher = new Searcher(indexDirectoryPath);
        searcher.setQueryBuilder(queryBuilder);

        System.out.println("Enter a search term to commence search, csv file will be downloaded to your current directory");
        performSearch(csvExporter, console, workingWithCsv, csvData, searcher, useDisSemanticsModel, separateSpellingSuggestions);

        boolean searchAgain = askWhetherToContinueSearch(console);
        while (searchAgain) {
            System.out.println("Enter a search term,csv file will be downloaded to your current directory");
            performSearch(csvExporter, console, workingWithCsv, csvData, searcher, useDisSemanticsModel, useDisSemanticsModel);
            searchAgain = askWhetherToContinueSearch(console);
        }
        return;

    }

    private static boolean askWhetherToContinueSearch(Console console) {
        System.out.println("> Search again? Y/N (if N then the program will terminate");
        return console.readLine().toLowerCase().equals("y") ? true : false;
    }

    private static void performSearch(CsvExporter csvExporter, Console console, boolean workingWithCsv, CsvData csvData, Searcher searcher, boolean useDisSemanticsModel, boolean separateSpellingSuggestions) throws ParseException, IOException, InvalidTokenOffsetsException {
        String searchQuery = console.readLine();
        SearchResultWithSuggestions searchResultWithSuggestions = searcher.searchFor(searchQuery);
        System.out.println("> Found " + searchResultWithSuggestions.getSearchResults().size() + " results, now exporting to csv");
        exportCsv(csvExporter, workingWithCsv, csvData, searchQuery, searchResultWithSuggestions);
        displaySearchSuggestionsBasedOnTheModel(useDisSemanticsModel, separateSpellingSuggestions, searchResultWithSuggestions);
    }

    private static void displaySearchSuggestionsBasedOnTheModel(boolean useDisSemanticsModel, boolean separateSpellingSuggestions, SearchResultWithSuggestions searchResultWithSuggestions) {
        if (useDisSemanticsModel) {
            System.out.println("> Suggested search terms based on the model uploaded: ");
            searchResultWithSuggestions.getSuggestionsWrapper().getNeighbourSuggestions().forEach(System.out::println);
            if (separateSpellingSuggestions) {
                System.out.println("> Spelling variations: ");
                searchResultWithSuggestions.getSuggestionsWrapper().getSpellingVariations().forEach(System.out::println);
            }
        }
    }


    private static void exportCsv(CsvExporter csvExporter, boolean workingWithCsv, CsvData csvData, String searchQuery, SearchResultWithSuggestions searchResultWithSuggestions) throws IOException {
        String exportComplete = "CSV export complete";
        csvExporter.setSearchResults(searchResultWithSuggestions.getSearchResults());
        csvExporter.setSearchTerms(searchQuery);
        if (workingWithCsv) {
            csvExporter.writeCsvDataToNewCsv(csvData);
        } else {
            csvExporter.writeDataToCsv();
        }
        System.out.println(exportComplete);
    }
}
