package gui;

import caseloader.CaseSearchRequest;
import caseloader.kad.CourtsInfo;
import export.ExcelExporter;
import export.ExportException;
import export.Extension;
import export.UnsupportedExtensionException;
import gui.casestable.CaseFieldNamesMismatchException;
import gui.casestable.CaseModel;
import gui.casestable.TextFlowCell;
import gui.searchpanel.MySpinner;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.dialog.Dialogs;
import proxy.ProxyUpdater;
import util.ResourceControl;

import java.io.File;
import java.time.LocalDate;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class MainController {
    @FXML private ChoiceBox<String> courtsChoiceBox;
    @FXML private MySpinner minCost;
    @FXML private MySpinner searchLimit;
    @FXML private VBox searchPanel;
    @FXML private HBox rootNode;
    @FXML private TableView<CaseModel> casesTable;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button searchButton;

    private final static String EXPORT_PATH_PROPERTY = "exportDirectory";

    private ResourceBundle res = ResourceBundle.getBundle("properties.gui_strings", new ResourceControl("UTF-8"));

    private ObservableList<CaseModel> casesData = FXCollections.observableArrayList();
    private ObservableList<String> courtsList = FXCollections.observableArrayList();

    private Stage stage;

    private enum Mode {
        DEFAULT, SEARCHING
    }

    private Mode mode = Mode.DEFAULT;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        initializeCourtList();
        initializeCaseModel();
        initializeProgressIndicator();
        initializeTableView();
    }

    private void initializeCourtList() {
        courtsChoiceBox.setItems(courtsList);

        CourtsInfo.courtsLoadedEvent.on(courts -> {
            courtsList.setAll(courts);
        });

        ProxyUpdater proxyUpdater = new ProxyUpdater();
        proxyUpdater.start();

        CourtsInfo.retrieveCourtsAsync().start();
    }

    private void initializeCaseModel() {
        try {
            CaseModel.loadTitles();
        }
        catch (CaseFieldNamesMismatchException e) {
            Dialogs.create().showException(e);
            System.exit(1);
        }
    }

    @SuppressWarnings("unchecked")
    private void initializeTableView() {
        //disables cell height jitter
        casesTable.setRowFactory(table -> {
            TableRow<CaseModel> row = new TableRow<>();
            row.setMinHeight(30);
            row.setPrefHeight(30);
            row.setMaxHeight(30);

            return row ;
        });

        for (Map.Entry<String, String> field: CaseModel.FIELD_NAMES.entrySet()) {
            TableColumn col = new TableColumn();
            col.setText(field.getValue());

            col.setCellValueFactory(new PropertyValueFactory(field.getKey()));
            col.setCellFactory(p -> new TextFlowCell<>());

            casesTable.getColumns().add(col);
        }

        casesTable.setItems(casesData);
    }

    private void initializeProgressIndicator() {
        progressIndicator.progressProperty().addListener((ov, t, newValue) -> {
            if (newValue.doubleValue() >= 1) {
                Text text = (Text) progressIndicator.lookup(".percentage");
                text.setText(res.getString("done"));
            }
        });
    }

    public void casesSearchClick(ActionEvent actionEvent) {
        //test data
        Random random = new Random();
        Integer caseId = random.nextInt(100);
        long days = random.nextInt(50);

        double cost = random.nextDouble() * 10000;

        casesData.add(new CaseModel("А" + caseId, "http://google.com", LocalDate.now().minusDays(days).toString(), "Саша", "Петя", cost, "Административное", "Никита"));

        mode = Mode.SEARCHING;
        searchButton.setText(res.getString("searchButtonPressed"));
        progressIndicator.setProgress(0);
        progressIndicator.setVisible(true);
        final double[] progress = {(double) 0};

        Timeline fiveSecondsWonder = new Timeline(new KeyFrame(Duration.seconds(0.1), event -> {
            progress[0] = progress[0] + 0.01;
            progressIndicator.setProgress(progress[0]);
        }));
        fiveSecondsWonder.setCycleCount(100);
        fiveSecondsWonder.play();


        Timeline finishProgress = new Timeline(new KeyFrame(Duration.seconds(10), event -> {
            mode = Mode.DEFAULT;
            searchButton.setText(res.getString("searchButtonDefault"));
        }));
        finishProgress.setCycleCount(1);
        finishProgress.play();


        // real data

//        CaseModelAppender caseModelAppender = new CaseModelAppender(casesData);
//        casesData.clear();
//
//        CaseLoader<CaseModelAppender> caseLoader = new CaseLoader<>();
//        caseLoader.casesLoaded.on((data) -> {
//            // TODO: Probably react somehow
//        });
//
//        Thread casesLoaderThread =
//                caseLoader.retrieveDataAsync(new CaseSearchRequest(), caseModelAppender);
//        casesLoaderThread.start();

    }

    public void exportCasesToExcel(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export to Excel");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(Extension.Excel.getName(), Extension.Excel.getValue()),
                new FileChooser.ExtensionFilter(Extension.AncientExcel.getName(), Extension.AncientExcel.getValue()));

        //get last export's directory
        Preferences prefs = Preferences.userNodeForPackage(MainController.class);
        String exportDirectory = prefs.get(EXPORT_PATH_PROPERTY, System.getProperty("user.dir"));
        fileChooser.setInitialDirectory(new File(exportDirectory));


        File selectedFile = fileChooser.showSaveDialog(stage);

        if (selectedFile != null) {
            String extensionName = fileChooser.getSelectedExtensionFilter().getDescription();

            try {
                CaseSearchRequest request = new CaseSearchRequest(
                        new String[]{"courtName"},
                        "10.10.2014",
                        "20.20.2014",
                        CaseSearchRequest.CaseType.A,
                        true,
                        100,
                        200);

                ExcelExporter.export(request, casesData, selectedFile.getAbsolutePath(), Extension.fromName(extensionName));
            } catch (ExportException | UnsupportedExtensionException e) {
                Dialogs.create().showException(e);
            }

            //save last export's directory
            prefs.put(EXPORT_PATH_PROPERTY, selectedFile.getParent());
        }
    }
}
