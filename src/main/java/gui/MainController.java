package gui;

import gui.casestable.CaseModel;
import gui.casestable.TextFlowCell;
import gui.searchpanel.MySpinner;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import util.ExcelExporter;

import java.io.File;
import java.time.LocalDate;
import java.util.Random;

public class MainController {
    @FXML private MySpinner costSpinner;
    @FXML private MySpinner searchLimitSpinner;
    @FXML private VBox searchPanel;
    @FXML private HBox rootNode;
    @FXML private TableView<CaseModel> casesTable;
    @FXML private TableColumn<CaseModel, Integer> casesTable_Id;
    @FXML private TableColumn<CaseModel, LocalDate> casesTable_createdDate;
    @FXML private TableColumn<CaseModel, String> casesTable_plaintiff;
    @FXML private TableColumn<CaseModel, String> casesTable_defendant;
    @FXML private TableColumn<CaseModel, Double> casesTable_cost;

    private ObservableList<CaseModel> casesData = FXCollections.observableArrayList();

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        casesTable_Id.setCellValueFactory(cellData -> cellData.getValue().id.asObject());
        casesTable_createdDate.setCellValueFactory(cellData -> cellData.getValue().createdDate);
        casesTable_plaintiff.setCellValueFactory(cellData -> cellData.getValue().plaintiff);

        casesTable_plaintiff.setCellFactory(p -> new TextFlowCell<>());

        casesTable_defendant.setCellValueFactory(cellData -> cellData.getValue().defendant);
        casesTable_cost.setCellValueFactory(cellData -> cellData.getValue().cost.asObject());

        casesTable.setItems(casesData);
    }

    public void casesSearchClick(ActionEvent actionEvent) {
        //test data
        Random random = new Random();
        int caseId = random.nextInt(100);
        long days = random.nextInt(50);

        double cost = random.nextDouble() * 10000;

        casesData.add(new CaseModel(caseId, LocalDate.now().minusDays(days), "http://google.com", "Петя", cost));

        // real data

//        CaseModelAppender caseModelAppender = new CaseModelAppender(casesData);
//        casesData.clear();
//
//        CaseLoader<CaseModelAppender> caseLoader = new CaseLoader<>();
//        caseLoader.setKadRequest(new KadSearchRequest());
//
//        //TODO:
//        caseLoader.setOutputContainer(caseModelAppender);
//        caseLoader.setMinCost(minCost);
//        caseLoader.setSearchLimit(searchLimit);
//
//        Thread casesLoaderThread = new Thread(caseLoader);
//        casesLoaderThread.start();

    }

    public void exportCasesToExcel(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export to Excel");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel Files", "*.xls"));

        File selectedFile = fileChooser.showSaveDialog(stage);
        if (selectedFile != null) {
            ExcelExporter.export(casesData, selectedFile.getAbsolutePath());
        }
    }
}
