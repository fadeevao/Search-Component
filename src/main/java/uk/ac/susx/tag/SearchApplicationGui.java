package uk.ac.susx.tag;


import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import uk.ac.susx.tag.exportData.CsvExporter;
import uk.ac.susx.tag.inputData.FileType;
import uk.ac.susx.tag.inputData.csv.CsvData;
import uk.ac.susx.tag.lucenesearch.Indexer;
import uk.ac.susx.tag.lucenesearch.neighbours.NeighbourSuggestion;
import uk.ac.susx.tag.lucenesearch.result.SearchResult;
import uk.ac.susx.tag.lucenesearch.Searcher;
import uk.ac.susx.tag.lucenesearch.query_expansion.QueryBuilder;
import uk.ac.susx.tag.lucenesearch.query_expansion.highlighter.HighlightedTextFragment;
import uk.ac.susx.tag.lucenesearch.result.SearchResultWithSuggestions;
import uk.ac.susx.tag.neighbours.NeighbourProcessor;
import uk.ac.susx.tag.lucenesearch.neighbours.NeighbourSearcher;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class SearchApplicationGui extends Application {

    private Searcher searcher;
    private FileType fileType;
    private final Insets DEFAULT_PADDING = new Insets(10, 20, 10, 20);
    private String csvMessage;
    private String csvMessageId;
    private QueryBuilder queryBuilder;
    private String indexDirectoryPath;
    private int numberOfNeighboursToSuggest;
    private boolean separateSpellingSuggestions = true;
    private boolean disSemanticsModelUploaded = false;
    private Pagination pagination;
    private Label noResultsFoundLabel;
    private Label resultsFoundLabel;
    private ChoiceBox fileTypeChoiceBox;
    private Button openDirectoryButton;
    private Label directoryMustBeEmptyLabel;
    private boolean exportResultsToCsv;
    private Button downloadCsv;

    private CsvExporter csvExporter;
    private CsvData csvData;
    private Scene searchScene;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread thread, Throwable t) {
                System.out.println("Exception thrown "  + t.getMessage());
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Exception Dialog");
                alert.setHeaderText("Unexpected exception occurred");

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                t.printStackTrace(pw);
                String exceptionText = sw.toString();

                Label label = new Label("The exception stacktrace was:");

                Label textArea = new Label(exceptionText);

                GridPane.setVgrow(textArea, Priority.ALWAYS);
                GridPane.setHgrow(textArea, Priority.ALWAYS);

                GridPane expContent = new GridPane();
                expContent.setMaxWidth(Double.MAX_VALUE);
                expContent.add(label, 0, 0);
                expContent.add(textArea, 0, 1);
                alert.getDialogPane().setExpandableContent(expContent);
                alert.showAndWait();
            }

        });
        primaryStage.setTitle("Search Application");
        queryBuilder = new QueryBuilder();


        GridPane inputAdminGrid = new GridPane();
        inputAdminGrid.setVgap(5);
        inputAdminGrid.setHgap(10);
        inputAdminGrid.setPadding(DEFAULT_PADDING);

        directoryMustBeEmptyLabel = new Label("directory must be empty!");
        directoryMustBeEmptyLabel.setFont(Font.font(9));
        directoryMustBeEmptyLabel.setTextFill(Color.RED);

        final DirectoryChooser directoryChooser = new DirectoryChooser();
        Label indexDirectoryLabel = new Label("1. Select a directory for indexing the files");
        Button indexDirectoryButton = new Button("Select index directory");
        indexDirectoryButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                File directory = directoryChooser.showDialog(primaryStage);
                if (directory != null) {
//                    if (directory.list().length >= 1) {
//                        inputAdminGrid.add(directoryMustBeEmptyLabel, 1, 0);
//                    } else {
                        indexDirectoryPath = directory.getPath();
                        inputAdminGrid.getChildren().remove(directoryMustBeEmptyLabel);
                        fileTypeChoiceBox.setDisable(false);
                    //}
                }
            }
        });


        final TextField csvMessageIdTextField = new TextField();
        final TextField csvMessageTextField = new TextField();
        Label csvMessageIdLabel = new Label("CSV message id");
        Label csvMessageLabel = new Label("CSV message field");
        GridPane grid = new GridPane();
        grid.setPadding(DEFAULT_PADDING);
        grid.setHgap(10);
        grid.setVgap(5);
        grid.add(csvMessageIdLabel, 1, 1);
        grid.add(csvMessageIdTextField, 2, 1);

        grid.add(csvMessageLabel, 1, 2);
        grid.add(csvMessageTextField, 2, 2);
        Button doneEnteringFieldValues = new Button("Done");
        grid.add(doneEnteringFieldValues, 2, 3);

        grid.setVisible(false);

        inputAdminGrid.add(indexDirectoryLabel, 0, 0);
        inputAdminGrid.add(indexDirectoryButton, 2, 0);
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setPercentWidth(60);
        inputAdminGrid.getColumnConstraints().add(0, columnConstraints);

        inputAdminGrid.setPadding(DEFAULT_PADDING);

        Button startSearchButton = new Button("Start searching");
        startSearchButton.setDisable(true);
        openDirectoryButton = new Button("Select");
        openDirectoryButton.setDisable(true);

        Label fileTypeSelectionLabel = new Label("2. select the file type");
        List<String> choiceList = new ArrayList<>();
        choiceList.add("choose file type");
        for (FileType fileType : FileType.values()) {
            choiceList.add(fileType.toString());
        }
        fileTypeChoiceBox = new ChoiceBox(FXCollections.observableArrayList(choiceList));
        fileTypeChoiceBox.setDisable(true);
        fileTypeChoiceBox.getSelectionModel().selectFirst();
        fileTypeChoiceBox.setTooltip(new Tooltip("Select the file type"));

        fileTypeChoiceBox.setOnAction(event -> {
            if (!fileTypeChoiceBox.getValue().equals("choose file type")) {
                fileType = FileType.valueOf((String) fileTypeChoiceBox.getValue());
                if (fileType.equals(FileType.CSV)) {
                    inputAdminGrid.add(grid, 0, 2);
                    grid.setVisible(true);
                    startSearchButton.setDisable(true);
                    openDirectoryButton.setDisable(true);
                } else {
                    grid.setVisible(false);
                    inputAdminGrid.getChildren().remove(grid);
                    openDirectoryButton.setDisable(false);
                }
            }

        });
        inputAdminGrid.add(fileTypeSelectionLabel, 0, 1);
        inputAdminGrid.add(fileTypeChoiceBox, 2, 1);

        doneEnteringFieldValues.setOnAction(event -> {
            csvMessage = csvMessageTextField.getText();
            csvMessageId = csvMessageIdTextField.getText();
            openDirectoryButton.setDisable(false);
        });

        Label singleFileOrDirectoryLabel = new Label("3. My searchable items are represented in a:");
        HBox radioButtonBoxFileOrDirectory = new HBox();
        radioButtonBoxFileOrDirectory.setSpacing(5);
        ToggleGroup groupFileOrDirectory = new ToggleGroup();


        final RadioButton[] singleFile = {new RadioButton("single file")};
        singleFile[0].setToggleGroup(groupFileOrDirectory);

        RadioButton directory = new RadioButton("directory");
        directory.setToggleGroup(groupFileOrDirectory);
        directory.setSelected(true);
        radioButtonBoxFileOrDirectory.getChildren().addAll(singleFile[0], directory);
        inputAdminGrid.add(singleFileOrDirectoryLabel, 0, 3);
        inputAdminGrid.add(radioButtonBoxFileOrDirectory, 2, 3);


        final boolean[] singleFileSelection = new boolean[1];
        singleFileSelection[0] = false;
        groupFileOrDirectory.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                RadioButton chk = (RadioButton) newValue.getToggleGroup().getSelectedToggle(); // Cast object to radio button
                if (chk.getText().toString().equals("single file")) {
                    singleFileSelection[0] = true;
                } else {
                    singleFileSelection[0] = false;
                }
            }
        });


        Label directorySelectLabel = new Label("4. select directory with searchable files or a single file");
        openDirectoryButton.setOnAction(event -> selectDirectoryToIndex(singleFileSelection, primaryStage, startSearchButton));
        inputAdminGrid.add(directorySelectLabel, 0, 4);
        inputAdminGrid.add(openDirectoryButton, 2, 4);

        Label disSemanticsLabel = new Label("5. Use distributional semantics model to improve search results");

        HBox radioButtonBox = new HBox();
        radioButtonBox.setSpacing(5);
        ToggleGroup group = new ToggleGroup();

        RadioButton yes = new RadioButton("yes");
        yes.setToggleGroup(group);

        RadioButton no = new RadioButton("no");
        no.setToggleGroup(group);
        no.setSelected(true);
        radioButtonBox.getChildren().addAll(yes, no);

        inputAdminGrid.add(disSemanticsLabel, 0, 5);
        inputAdminGrid.add(radioButtonBox, 2, 5);

        Button selectDisSemanticsFileButton = new Button("Upload model");
        selectDisSemanticsFileButton.setDisable(true);
        FileChooser disSemanticsFileChooser = new FileChooser();
        selectDisSemanticsFileButton.setOnAction(event -> {
            File file = disSemanticsFileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                NeighbourProcessor neighbourProcessor = new NeighbourProcessor();
                try {
                    NeighbourSearcher neighbourSearcher = new NeighbourSearcher(neighbourProcessor.buildNeighbourMap(file, numberOfNeighboursToSuggest * 5), numberOfNeighboursToSuggest, separateSpellingSuggestions);
                    startSearchButton.setDisable(false);
                    queryBuilder = new QueryBuilder(neighbourSearcher);
                    disSemanticsModelUploaded = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        HBox boxForNumberOfNeighbour = new HBox();
        boxForNumberOfNeighbour.setSpacing(5);
        boxForNumberOfNeighbour.setVisible(false);
        TextField numberOfNeighboursTextField = new TextField();
        Label labelForNumberOfNeighbours = new Label("number of neighbours to process");
        boxForNumberOfNeighbour.getChildren().addAll(labelForNumberOfNeighbours, numberOfNeighboursTextField);
        numberOfNeighboursTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    numberOfNeighboursTextField.setText(newValue.replaceAll("[^\\d]", ""));
                } else {

                    selectDisSemanticsFileButton.setDisable(false);

                    numberOfNeighboursToSuggest = Integer.parseInt(numberOfNeighboursTextField.getText());
                }
            }
        });

        HBox boxForFilteringSuggestions = new HBox();
        boxForFilteringSuggestions.setSpacing(5);
        boxForFilteringSuggestions.setVisible(false);
        Label labelForFilteringSuggestion = new Label("Separate spelling variations");
        CheckBox checkBoxForFilteringSuggestions = new CheckBox();
        checkBoxForFilteringSuggestions.setSelected(true);
        checkBoxForFilteringSuggestions.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    separateSpellingSuggestions = true;
                } else {
                    separateSpellingSuggestions = false;
                }
            }
        });
        boxForFilteringSuggestions.getChildren().addAll(labelForFilteringSuggestion, checkBoxForFilteringSuggestions);
        inputAdminGrid.add(boxForFilteringSuggestions, 0, 7);


        HBox disSemanticsSelectionBox = new HBox();
        disSemanticsSelectionBox.getChildren().add(selectDisSemanticsFileButton);
        inputAdminGrid.add(disSemanticsSelectionBox, 0, 8);
        inputAdminGrid.add(boxForNumberOfNeighbour, 0, 6);
        selectDisSemanticsFileButton.setVisible(false);

        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1) {
                RadioButton chk = (RadioButton) t1.getToggleGroup().getSelectedToggle(); // Cast object to radio button
                if (chk.getText().toString().equals("yes")) {
                    startSearchButton.setDisable(true);
                    selectDisSemanticsFileButton.setVisible(true);
                    boxForNumberOfNeighbour.setVisible(true);
                    boxForFilteringSuggestions.setVisible(true);
                } else {
                    startSearchButton.setDisable(false);
                    try {
                        queryBuilder = new QueryBuilder();
                        selectDisSemanticsFileButton.setVisible(false);
                        boxForNumberOfNeighbour.setVisible(false);
                        boxForFilteringSuggestions.setVisible(false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        HBox boxForDownloadingCsv = new HBox();
        boxForDownloadingCsv.setSpacing(10);
        Label downloadResultsAsCsvLabel = new Label("6. Download results as CSV");
        CheckBox checkboxForDownloadingResultsAsCsv = new CheckBox();
        boxForDownloadingCsv.getChildren().addAll(downloadResultsAsCsvLabel, checkboxForDownloadingResultsAsCsv);
        inputAdminGrid.add(boxForDownloadingCsv, 0, 9);
        checkboxForDownloadingResultsAsCsv.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    exportResultsToCsv = true;
                } else {
                    exportResultsToCsv = false;
                }
            }
        });



        BorderPane searchScreen = new BorderPane();
        TextField userQueryField = new TextField();
        userQueryField.setPrefWidth(700);
        Button searchButton = new Button("Search!");
        Pane searchScreenRoot = new HBox();
        searchScreenRoot.setPadding(DEFAULT_PADDING);
        searchScreen.setTop(searchScreenRoot);

        Pane searchResultsPane = new VBox();
        searchResultsPane.setPadding(DEFAULT_PADDING);
        searchScreen.setCenter(searchResultsPane);
        searchButton.setOnAction(event -> performSearch(primaryStage, searchResultsPane, userQueryField));

        BorderPane enterScreenRoot = new BorderPane();
        Scene initialScene = new Scene(enterScreenRoot, 800, 500);
        searchScreenRoot.getChildren().addAll(userQueryField, searchButton);

        HBox navigationButtonsBox = new HBox();
        navigationButtonsBox.setSpacing(10);
        navigationButtonsBox.setPadding(DEFAULT_PADDING);
        Button backButton = new Button("Start again");

        backButton.setOnAction(event -> primaryStage.setScene(initialScene));
        searchScreen.setBottom(navigationButtonsBox);

        downloadCsv = new Button("Download CSV");
        downloadCsv.setVisible(false);
        downloadCsv.setOnAction(event -> {
            try {
                if (fileType.equals(FileType.CSV)) {
                    csvExporter.writeCsvDataToNewCsv(csvData);
                } else {
                    csvExporter.writeDataToCsv();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        navigationButtonsBox.getChildren().add(backButton);
        navigationButtonsBox.getChildren().add(downloadCsv);


        startSearchButton.setOnAction(event -> {
                startSearch(primaryStage, searchScreen);

        });
        startSearchButton.setAlignment(Pos.CENTER);
        startSearchButton.setMaxWidth(200);
        startSearchButton.setMaxHeight(120);


        AnchorPane anchorpane = new AnchorPane();
        anchorpane.getChildren().addAll(inputAdminGrid);
        AnchorPane.setTopAnchor(inputAdminGrid, 8.0);
        AnchorPane.setLeftAnchor(inputAdminGrid, 8.0);
        AnchorPane.setRightAnchor(inputAdminGrid, 8.0);


        enterScreenRoot.setCenter(startSearchButton);
        enterScreenRoot.setTop(anchorpane);

        primaryStage.setScene(initialScene);

        searchScene = new Scene(searchScreen, 1000, 1000);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    private void selectDirectoryToIndex(boolean[] singleFile, Stage primaryStage, Button startSearchButton) {
        File fileOrDirectory;
        if (singleFile[0]) {
            FileChooser fileChooser = new FileChooser();
            fileOrDirectory = fileChooser.showOpenDialog(primaryStage);
        } else {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            fileOrDirectory = directoryChooser.showDialog(primaryStage);
        }

        if (fileOrDirectory != null) {
            try {
                Indexer indexer = new Indexer(indexDirectoryPath);
                //if filetype is not CSV then csvMessageId and csvMessage both will be null
                if (!fileType.equals(FileType.CSV)) {
                    indexer.createIndex(fileOrDirectory.getPath(), fileType);
                } else {
                    csvData = indexer.createIndex(fileOrDirectory.getPath(), csvMessageId, csvMessage);
                }
                startSearchButton.setDisable(false);
            } catch (IOException e) {

            }
        }
    }

    private void startSearch(Stage primaryStage, BorderPane searchScreen) {
        primaryStage.setScene(searchScene);
        try {
            searcher = new Searcher(indexDirectoryPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        searcher.setQueryBuilder(queryBuilder);
        searcher.setFileType(fileType);

    }

    private void performSearch(Stage primaryStage, Pane searchResultsPane, TextField textField) {
        Label displayingSearchResultsForExpandedQuery = new Label("Displaying search results based on added expansion terms");
        Button buttonSearchOriginalResults = new Button("Show original search results");
        HBox boxForShowOriginalResultsButton = new HBox();
        boxForShowOriginalResultsButton.setSpacing(10);
        boxForShowOriginalResultsButton.getChildren().addAll(displayingSearchResultsForExpandedQuery, buttonSearchOriginalResults);
        try {
            primaryStage.setWidth(1000);
            primaryStage.setHeight(1200);
            searchResultsPane.getChildren().clear();
            SearchResultWithSuggestions searchResultWithSuggestions = searcher.searchFor(textField.getText());
            List<SearchResult> searchResults = searchResultWithSuggestions.getSearchResults();
            if (exportResultsToCsv) {
                csvExporter = new CsvExporter(searchResults, textField.getText());
            }
            if (disSemanticsModelUploaded) {
                NeighbourSuggestion neighbourSuggestion = searchResultWithSuggestions.getSuggestionsWrapper();
                List<String> suggestions = neighbourSuggestion.getNeighbourSuggestions();
                if (suggestions != null && !suggestions.isEmpty()) {
                    VBox labelAndSuggestionsBox = new VBox();
                    labelAndSuggestionsBox.setSpacing(5);
                    Button searchAgain = new Button("Search again");
                    searchAgain.setDisable(true);
                    Label searchForMore = new Label("Search for terms used in similar context:");
                    labelAndSuggestionsBox.getChildren().add(searchForMore);
                    List<String> userSelectedSuggestions = new ArrayList<>();
                    FlowPane suggestionsPane = new FlowPane();
                    suggestionsPane.setPrefWrapLength(400);
                    CheckBox[] suggestionCheckBoxes = new CheckBox[suggestions.size()];
                    for (int i = 0; i < suggestionCheckBoxes.length; i++) {
                        CheckBox box = suggestionCheckBoxes[i] = new CheckBox(suggestions.get(i));
                        box.setPadding(new Insets(5, 10, 5, 10));
                        box.selectedProperty().addListener((observable, oldValue, newValue) -> observeCheckBoxChanges(searchAgain, userSelectedSuggestions, box, newValue));
                    }

                    List<String> spellingVariations = new ArrayList<>(neighbourSuggestion.getSpellingVariations());
                    if (separateSpellingSuggestions && !spellingVariations.isEmpty()) {
                        VBox spellingVariationLabelAndSuggBox = new VBox();
                        Label label = new Label("Search for spelling variations:");
                        spellingVariationLabelAndSuggBox.getChildren().add(label);
                        CheckBox[] spellcheckVariationCheckboxes = new CheckBox[spellingVariations.size()];
                        FlowPane spellingVariationsPane = new FlowPane();
                        spellingVariationsPane.setPrefWrapLength(400);
                        for (int i = 0; i < spellcheckVariationCheckboxes.length; i++) {
                            CheckBox box = spellcheckVariationCheckboxes[i] = new CheckBox(spellingVariations.get(i));
                            box.setPadding(new Insets(5, 10, 5, 10));
                            box.selectedProperty().addListener((observable, oldValue, newValue) -> observeCheckBoxChanges(searchAgain, userSelectedSuggestions, box, newValue));
                        }
                        spellingVariationsPane.getChildren().addAll(spellcheckVariationCheckboxes);
                        spellingVariationLabelAndSuggBox.getChildren().add(spellingVariationsPane);
                        spellingVariationLabelAndSuggBox.setPadding(DEFAULT_PADDING);
                        searchResultsPane.getChildren().addAll(spellingVariationLabelAndSuggBox);
                    }

                    suggestionsPane.getChildren().addAll(suggestionCheckBoxes);
                    labelAndSuggestionsBox.getChildren().add(suggestionsPane);
                    labelAndSuggestionsBox.getChildren().add(searchAgain);
                    labelAndSuggestionsBox.setPadding(DEFAULT_PADDING);
                    searchResultsPane.getChildren().addAll(labelAndSuggestionsBox);

                    searchAgain.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            try {
                                //display results
                                SearchResultWithSuggestions finalResults = searcher.expandSearchResultsWithUserSelectedTerms(searchResults, userSelectedSuggestions);
                                List<SearchResult> searchResultsAfterExpansion = finalResults.getSearchResultsAfterExpansion();
                                if (exportResultsToCsv) {
                                    List<SearchResult> searchResultsCombined = new ArrayList<>();
                                    searchResultsCombined.addAll(finalResults.getSearchResults());
                                    searchResultsCombined.addAll(finalResults.getSearchResultsAfterExpansion());
                                    csvExporter = new CsvExporter(searchResultsCombined, StringUtils.join(userSelectedSuggestions, "_"));
                                }
                                buttonSearchOriginalResults.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
                                        searchResultsPane.getChildren().remove(boxForShowOriginalResultsButton);
                                        createPagination(searchResultsPane, textField, finalResults.getSearchResults(), true);

                                    }
                                });

                                if (!searchResultsPane.getChildren().contains(boxForShowOriginalResultsButton)) {
                                    searchResultsPane.getChildren().add(boxForShowOriginalResultsButton);
                                }
                                createPagination(searchResultsPane, textField, searchResultsAfterExpansion, false);


                            } catch (ParseException e) {
                                e.printStackTrace();
                            } catch (InvalidTokenOffsetsException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }

            //pagination
            createPagination(searchResultsPane, textField, searchResults, true);
            if (exportResultsToCsv) {
                downloadCsv.setVisible(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (InvalidTokenOffsetsException e) {
            e.printStackTrace();
        }

    }

    private void createPagination(Pane searchResultsPane, TextField textField, List<SearchResult> searchResults, boolean initialSearch) {

        searchResultsPane.getChildren().remove(noResultsFoundLabel);
        searchResultsPane.getChildren().remove(pagination);
        searchResultsPane.getChildren().remove(resultsFoundLabel);
        if (searchResults.isEmpty() && initialSearch) {
            noResultsFoundLabel = new Label("No matching results were found for: " + textField.getText());
            noResultsFoundLabel.setPadding(DEFAULT_PADDING);
            searchResultsPane.getChildren().add(noResultsFoundLabel);

        }  else if (searchResults.isEmpty() && !initialSearch) {
            noResultsFoundLabel = new Label("No results were found when expanding the search query");
            noResultsFoundLabel.setPadding(DEFAULT_PADDING);
            searchResultsPane.getChildren().add(noResultsFoundLabel);
        } else {

            if (initialSearch) {
                 resultsFoundLabel = new Label("Found " + searchResults.size() + " results for: " + textField.getText());
            } else {
                resultsFoundLabel = new Label("Found " + searchResults.size() + " results when performing search expansion");
            }
            int divisionResult = searchResults.size() / itemsPerPage();
            int numberOfPages = searchResults.size() % itemsPerPage() == 0 ? divisionResult : divisionResult + 1;
            pagination = new Pagination(numberOfPages, 0);

            pagination.setPageFactory((Integer pageIndex) -> createPage(pageIndex, searchResults));
            pagination.setPadding(DEFAULT_PADDING);
            pagination.setCache(true);
            searchResultsPane.getChildren().addAll(resultsFoundLabel, pagination);
        }
    }

    private void observeCheckBoxChanges(Button searchAgain, List<String> userSelectedSuggestions, CheckBox box, Boolean newValue) {
        if (newValue) {
            userSelectedSuggestions.add(box.getText());
            searchAgain.setDisable(false);
        } else if (newValue == false) {
            userSelectedSuggestions.remove(box.getText());
        }
        if (userSelectedSuggestions.isEmpty()) {
            searchAgain.setDisable(true);
        }
    }

    private int itemsPerPage() {
        return 5;
    }

    /*
    Creates a page and highlights relevant to the search term words
     */
    public Pane createPage(int pageIndex, List<SearchResult> searchResults) {

        VBox searchResultsPane = new VBox();

        int fromIndex = pageIndex * itemsPerPage();
        int toIndex = Math.min(fromIndex + itemsPerPage(), searchResults.size());
        searchResultsPane.getChildren().add(new Label());
        for (int i = fromIndex; i < toIndex; i++) {
            if (i < searchResults.size()) {
                SearchResult result = searchResults.get(i);

                VBox boxForLabelAndPreview = new VBox();

                Label idLabel = new Label(result.getFileId());
                idLabel.setPadding(DEFAULT_PADDING);
                Label docTextLabel = new Label(result.getDocumentText());
                ScrollPane sp = new ScrollPane();
                sp.setContent(docTextLabel);
                sp.setFitToHeight(true);
                boxForLabelAndPreview.getChildren().add(idLabel);

                idLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {

                        if (boxForLabelAndPreview.getChildren().contains(sp)) {
                            boxForLabelAndPreview.getChildren().remove(sp);
                        } else if (fileType.equals(FileType.PDF) || fileType.equals(FileType.TEXT_FILE)) {
                            File file = new File(result.getFilePath());
                            try {
                                Desktop.getDesktop().open(file);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            boxForLabelAndPreview.getChildren().add(sp);
                        }
                    }
                });
                idLabel.setStyle("-fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 12pt;");
                searchResultsPane.getChildren().add(boxForLabelAndPreview);
                String boldStyle = "-fx-font-weight: bold;";
                for (List<HighlightedTextFragment> text : result.getDocFragments()) {
                    TextFlow flow = new TextFlow();
                    flow.setMaxWidth(400);
                    for (HighlightedTextFragment fragment : text) {
                        //highlight the right ones and combine in 1 label
                        Text fragmentText = new Text(fragment.getTextFragment());
                        if (fragment.isHighlighted()) {
                            fragmentText.setStyle(boldStyle);
                        }
                        flow.getChildren().add(fragmentText);
                        flow.setPadding(DEFAULT_PADDING);

                        flow.setMaxWidth(Double.MAX_VALUE);
                        flow.setMaxHeight(Double.MAX_VALUE);
                    }
                    searchResultsPane.getChildren().add(flow);
                }
            }
        }
        searchResultsPane.getChildren().add(new Label());
        return searchResultsPane;
    }


    public void method() throws IOException {
        System.out.println(Thread.currentThread().getName());
        throw new RuntimeException();
    }
}
