package gui;

import caseloader.CaseLoader;
import caseloader.CaseLoaderEvents;
import caseloader.CaseSearchRequest;
import caseloader.credentials.websites.Kartoteka;
import caseloader.errors.CaseLoaderError;
import caseloader.kad.CourtsInfo;
import export.ExcelExporter;
import export.ExportException;
import export.Extension;
import export.UnsupportedExtensionException;
import gui.casestable.CaseFieldNamesMismatchException;
import gui.casestable.CaseModel;
import gui.casestable.CaseModelAppender;
import gui.casestable.TextFlowCell;
import gui.searchpanel.CaseTypeModel;
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
import org.controlsfx.control.MyCheckComboBox;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import util.MyLogger;
import util.ResourceControl;
import util.net.HttpDownloader;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class MainController {
    @FXML private MyCheckComboBox<String> courtComboCheckBox;
    //@FXML private ComboBox<String> courtsChoiceBox;
    @FXML private ComboBox<CaseTypeModel> caseType;
    @FXML private CheckBox withVKSInstances;
    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;
    @FXML private MySpinner minCost;
    @FXML private MySpinner searchLimit;
    @FXML private VBox searchPanel;
    @FXML private HBox rootNode;
    @FXML private TableView<CaseModel> casesTable;
    @FXML private MyProgressIndicator progressIndicator;
    @FXML private Button searchButton;
    @FXML private Button exportButton;

    private Thread sitesInitializerThread = null;

    private final static String EXPORT_PATH_PROPERTY = "exportDirectory";

    private final ResourceBundle res = ResourceBundle.getBundle("properties.gui_strings", new ResourceControl("UTF-8"));

    private final ObservableList<CaseModel> casesData = FXCollections.observableArrayList();
    private final ObservableList<String> courtsList = FXCollections.observableArrayList();

    private Stage stage;
    private CaseLoader<CaseModelAppender> caseLoader = null;
    private CaseSearchRequest currentRequest = null;

    private enum Mode {
        DEFAULT, SEARCHING
    }

    private Mode mode = Mode.DEFAULT;

    /**
     * Should be be called manually
     * @param stage
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Will be called automatically by FXML loader
     * Please note, that at this step stage == null
     */
    @FXML
    private void initialize() {
        //testLicense();

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            System.out.println("ERROR");
            e.printStackTrace();
            Dialogs.create().showException(
                    new UnexpectedException(
                            res.getString("unexpectedException"), e));

            stage.close();
        });

        CaseLoaderEvents.instance().onError.on(this::errorHandler);

        initializeCourtList();
        initializeCaseModel();
        initializeTableView();

        initializeSites();

        caseType.setItems(CaseTypeModel.getCollection());
        caseType.setValue(CaseTypeModel.getCollection().get(0));

        //not in fxml because of JavaFX bug
        //http://stackoverflow.com/questions/22992458/javafx-thread-with-progressindicator-not-spinning-work-done-in-non-fxthread
        Platform.runLater(() -> progressIndicator.setVisible(false));
    }

    private void initializeSites() {
        if (sitesInitializerThread != null && sitesInitializerThread.isAlive())
            sitesInitializerThread.interrupt();

        sitesInitializerThread = new Thread(() -> {
            try {
                Kartoteka.initialize();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        sitesInitializerThread.start();
    }

    private void testLicense() {
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int month = Calendar.getInstance().get(Calendar.MONTH);

        if (month != Calendar.AUGUST || day > 15) {
            Dialogs.create().message("Demo expired").showError();
            throw new RuntimeException("Licence expired");
        }
    }

    private void initializeCourtList() {
        //courtsChoiceBox.setItems(courtsList);
        courtComboCheckBox.setItems(courtsList);

        //new AutoCompleteComboBoxListener<>(courtsChoiceBox);

        CourtsInfo.courtsLoadedEvent.on(courtsList::addAll);

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

    private void errorHandler(CaseLoaderError error) {
        String errorMessage = error.getReason()
                .getLocalizedDescription(error.getDescription());

        Dialogs.create()
                .message(errorMessage)
                .showError();

        switch (error.getReason()) {
            case KAD_PAGE_ERROR:
                stopSearching();
                break;

            case COURTS_RETRIEVAL_ERROR: {
                Action action = Dialogs.create()
                        .message(res.getString("retryCourtsRetrieval"))
                        .actions(Dialog.Actions.YES, Dialog.Actions.NO)
                        .showConfirm();

                if (action == Dialog.Actions.YES)
                    initializeCourtList();

                break;
            }

            default:
                stage.close();
        }
    }

    public void onClose(WindowEvent event) {
        if (caseLoader != null) {
            if (mode == Mode.SEARCHING) {
                Action action = Dialogs.create()
                        .message(res.getString("onStopDialog"))
                        .actions(Dialog.Actions.YES, Dialog.Actions.NO)
                        .showConfirm();

                if (action != Dialog.Actions.YES) {
                    event.consume();
                    return;
                }
            }

            caseLoader.stopExecution();

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
        HttpDownloader.i().stop();
        MyLogger.close();
    }
    
    public void casesSearchClick(ActionEvent actionEvent) {
        if (!sitesInitialised()) {
            Action action = Dialogs.create()
                    .message(res.getString("sitesNotInitialised"))
                    .actions(Dialog.Actions.YES, Dialog.Actions.NO)
                    .showConfirm();

            if (action == Dialog.Actions.YES)
                initializeSites();

            return;
        }

        if (mode == Mode.SEARCHING) {
            Action action = Dialogs.create()
                    .message(res.getString("onStopDialog"))
                    .actions(Dialog.Actions.YES, Dialog.Actions.NO)
                    .showConfirm();

            if (action == Dialog.Actions.YES)
                stopSearching();
        } else {
            if (prepareRequest()) {
                mode = Mode.SEARCHING;
                searchButton.setText(res.getString("searchButtonPressed"));
                exportButton.setDisable(false);
                progressIndicator.reset();
                progressIndicator.setVisible(true);

                CaseLoaderEvents.instance().offSubscribers();
                CaseLoaderEvents.instance().onError.on(this::errorHandler);

                this.caseLoader = new CaseLoader<>();
                CaseModelAppender caseModelAppender = new CaseModelAppender(casesData);
                casesData.clear();

                caseLoader.events().totalCasesCountObtained.on(progressIndicator::setLimit);

                caseLoader.events().caseProcessed.on(() -> {
                    progressIndicator.add(1);
                });

                caseLoader.events().casesLoaded.on((data) -> {
                    mode = Mode.DEFAULT;
                    searchButton.setText(res.getString("searchButtonDefault"));
                    progressIndicator.setVisible(false);

                    String message = String.format(res.getString("loadingFinished"), casesData.size());

                    Dialogs.create()
                            .message(message)
                            .showInformation();
                });

                caseLoader.retrieveDataAsync(currentRequest, caseModelAppender);
            }
        }
    }

    private boolean sitesInitialised() {
        return Kartoteka.isInitialised();
    }

    private void stopSearching() {
        if (caseLoader != null)
            caseLoader.stopExecution();

        mode = Mode.DEFAULT;
        searchButton.setText(res.getString("searchButtonDefault"));
        progressIndicator.setVisible(false);
    }

    private boolean prepareRequest() {
        currentRequest = null;

        String courts[] = new String[courtComboCheckBox.getCheckModel().getSelectedItems().size()];
        courtComboCheckBox.getCheckModel().getSelectedItems().toArray(courts);
        //validate user input if one occurred (for ComboBox with editable=true)

        String dateFromStr = null;
        String dateToStr = null;

        if (dateFrom.getValue() != null)
            dateFromStr =
                    dateFrom.getValue().format(DateTimeFormatter.ISO_DATE)
                    + "T00:00:00";

        if (dateTo.getValue() != null)
            dateToStr =
                    dateTo.getValue().format(DateTimeFormatter.ISO_DATE)
                    + "T23:59:59";

        CaseSearchRequest.CaseType caseTypeValue = caseType.getValue().getType();
        boolean withVKSInstancesValue = withVKSInstances.isSelected();

        long minCostValue = minCost.getValue().longValue();

        int searchLimitValue = searchLimit.getValue().intValue();

        currentRequest = new CaseSearchRequest(
                courts,
                dateFromStr,
                dateToStr,
                caseTypeValue,
                withVKSInstancesValue,
                minCostValue,
                searchLimitValue);

        return true;
    }

    public void exportCasesToExcel(ActionEvent actionEvent) {
        if (mode == Mode.SEARCHING) {
            Action action = Dialogs.create()
                    .message(res.getString("onExportWhenSearching"))
                    .actions(Dialog.Actions.YES, Dialog.Actions.NO)
                    .showConfirm();

            if (action != Dialog.Actions.YES)
                return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export to Excel");

        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss");
        Date date = new Date();
        fileChooser.setInitialFileName("CaseFinder Report "
                + dateFormat.format(date));

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(Extension.Excel.getName(), Extension.Excel.getValue()),
                new FileChooser.ExtensionFilter(Extension.AncientExcel.getName(), Extension.AncientExcel.getValue()));

        //get last export's directory
        Preferences prefs = Preferences.userNodeForPackage(MainController.class);
        String exportDirectoryPath = prefs.get(EXPORT_PATH_PROPERTY, System.getProperty("user.dir"));

        File exportDirectory = new File(exportDirectoryPath);
        if (exportDirectory.exists() && exportDirectory.isDirectory() && exportDirectory.canWrite())
            fileChooser.setInitialDirectory(exportDirectory);


        File selectedFile = fileChooser.showSaveDialog(stage);

        if (selectedFile != null) {
            String extensionName = fileChooser.getSelectedExtensionFilter().getDescription();

            try {
                ExcelExporter exporter = new ExcelExporter(Extension.fromName(extensionName));
                exporter.export(currentRequest, casesData, selectedFile.getAbsolutePath());
            } catch (ExportException | UnsupportedExtensionException e) {
                Dialogs.create().showException(e);
            }

            //save last export's directory
            prefs.put(EXPORT_PATH_PROPERTY, selectedFile.getParent());
        }
    }
}
