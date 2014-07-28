package gui;

import caseloader.CaseLoader;
import caseloader.CaseSearchRequest;
import caseloader.kad.CourtsInfo;
import export.ExcelExporter;
import export.ExportException;
import export.Extension;
import export.UnsupportedExtensionException;
import gui.casestable.CaseFieldNamesMismatchException;
import gui.casestable.CaseModel;
import gui.casestable.TextFlowCell;
import gui.searchpanel.AutoCompleteComboBoxListener;
import gui.searchpanel.MySpinner;
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
import javafx.stage.WindowEvent;
import org.controlsfx.dialog.Dialogs;
import util.CaseModelAppender;
import util.ResourceControl;

import java.io.File;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class MainController {
    @FXML private ComboBox<String> courtsChoiceBox;
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
    private CaseLoader<CaseModelAppender> caseLoader = null;

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

        new AutoCompleteComboBoxListener<>(courtsChoiceBox);

        CourtsInfo.courtsLoadedEvent.on(courts -> {
            courtsList.add("Любой");
            courtsList.addAll(courts);
        });

        CourtsInfo.retrieveCourtsAsync();
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

    public void onClose(WindowEvent event) {
        System.out.println("onClose");
        if (caseLoader != null)
            caseLoader.stopExecution();
    }
    
    public void casesSearchClick(ActionEvent actionEvent) {
        if (mode == Mode.SEARCHING) {
            caseLoader.stopExecution();
            mode = Mode.DEFAULT;
            searchButton.setText(res.getString("searchButtonDefault"));
            return;
        }

        mode = Mode.SEARCHING;
        searchButton.setText(res.getString("searchButtonPressed"));

        /*Random random = new Random();
        Integer caseId = random.nextInt(100);
        long days = random.nextInt(50);

        double cost = random.nextDouble() * 10000;

//        casesData.add(new CaseModel("А" + caseId, "http://google.com", LocalDate.now().minusDays(days).toString(), "Саша", "Петя", cost, "Административное", "Никита"));

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
        finishProgress.play();*/


        // real data

        this.caseLoader = new CaseLoader<>();
        CaseModelAppender caseModelAppender = new CaseModelAppender(casesData);
        casesData.clear();

        caseLoader.casesLoaded.on((data) -> {
            // TODO: Probably react somehow
        });

        caseLoader.retrieveDataAsync(new CaseSearchRequest(), caseModelAppender);

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
