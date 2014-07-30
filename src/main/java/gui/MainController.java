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
import gui.searchpanel.MyProgressIndicator;
import gui.searchpanel.MySpinner;
import javafx.application.Platform;
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
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
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
    @FXML private MyProgressIndicator progressIndicator;
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
        initializeTableView();

        //not in fxml because of JavaFX bug
        //http://stackoverflow.com/questions/22992458/javafx-thread-with-progressindicator-not-spinning-work-done-in-non-fxthread
        Platform.runLater(() -> progressIndicator.setVisible(false));
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
        //localize placeholder
        casesTable.setPlaceholder(new Text(res.getString("casesTablePlaceholder")));

        //disables cell height jitter
        casesTable.setRowFactory(table -> {
            TableRow<CaseModel> row = new TableRow<>();
            row.setMinHeight(30);
            row.setPrefHeight(30);
            row.setMaxHeight(30);

            return row;
        });

        for (Map.Entry<String, String> field: CaseModel.FIELD_NAMES.entrySet()) {
            TableColumn col = new TableColumn();
            col.setText(field.getValue());

            col.setCellValueFactory(new PropertyValueFactory(field.getKey()));
            col.setCellFactory(p -> new TextFlowCell<>());

            casesTable.getColumns().add(col);
        }

        //testing TextFlow cells:
        /*
        CaseModel caseModel = new CaseModel();
        caseModel.setNumber("some really long string");
        caseModel.setUrl("http://google.com");
        caseModel.setCreatedDate("some really long string");
        caseModel.setPlaintiff("some really long string");
        caseModel.setDefendant("some really long string");
        caseModel.setCaseType("some really long string");
        caseModel.setCourt("some really long string");
        caseModel.setCost(100500.0);

        casesData.add(caseModel);
        */

        casesTable.setItems(casesData);
    }

    public void onClose(WindowEvent event) {
        if (caseLoader != null) {
            if (mode == Mode.SEARCHING) {
                Action action = Dialogs.create()
                        .message(res.getString("onStopDialog"))
                        .actions(Dialog.Actions.YES, Dialog.Actions.NO)
                        .showConfirm();

                if (action == Dialog.Actions.YES)
                    caseLoader.stopExecution();
                else {
                    event.consume();
                    return;
                }
            }

            if (!casesData.isEmpty()) {
                Dialogs.CommandLink saveAction = new Dialogs.CommandLink(
                        res.getString("saveAction"),
                        res.getString("saveActionComment"));

                Dialogs.CommandLink exitAction = new Dialogs.CommandLink(
                        res.getString("exitAction"),
                        res.getString("exitActionComment"));

                Dialogs.CommandLink cancelAction = new Dialogs.CommandLink(
                        res.getString("cancelAction"),
                        res.getString("cancelActionComment"));

                Action action = Dialogs.create()
                        .message(res.getString("onCloseDialog"))
                        .showCommandLinks(saveAction, saveAction, exitAction, cancelAction);

                if (action == saveAction) {
                    exportCasesToExcel(null);
                } else if (action != exitAction) {
                    //cancel or cross button
                    event.consume();
                }
            }
        }
    }
    
    public void casesSearchClick(ActionEvent actionEvent) {
        if (mode == Mode.SEARCHING) {
            if (caseLoader != null) {
                Action action = Dialogs.create()
                        .message(res.getString("onStopDialog"))
                        .actions(Dialog.Actions.YES, Dialog.Actions.NO)
                        .showConfirm();

                if (action == Dialog.Actions.YES)
                    caseLoader.stopExecution();
                else
                    return;
            }

            mode = Mode.DEFAULT;
            searchButton.setText(res.getString("searchButtonDefault"));
            progressIndicator.setVisible(false);
            return;
        }

        mode = Mode.SEARCHING;
        searchButton.setText(res.getString("searchButtonPressed"));
        progressIndicator.reset();
        progressIndicator.setVisible(true);

        this.caseLoader = new CaseLoader<>();
        CaseModelAppender caseModelAppender = new CaseModelAppender(casesData);
        casesData.clear();

        caseLoader.totalCasesCountObtained.on(progressIndicator::setLimit);

        caseLoader.caseProcessed.on(() -> {
            progressIndicator.add(1);
        });

        caseLoader.casesLoaded.on((data) -> {
            mode = Mode.DEFAULT;
            searchButton.setText(res.getString("searchButtonDefault"));

            String message = String.format(res.getString("loadingFinished"), casesData.size());

            Dialogs.create()
                    .message(message)
                    .showInformation();
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
