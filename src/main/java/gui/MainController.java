package gui;

import gui.casestable.Case;
import gui.casestable.TextFlowCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import util.ExcelExporter;

import java.io.File;
import java.time.LocalDate;
import java.util.Random;

public class MainController {
    @FXML private HBox rootNode;
    @FXML private TableView<Case> casesTable;
    @FXML private TableColumn<Case, Integer> casesTable_Id;
    @FXML private TableColumn<Case, LocalDate> casesTable_createdDate;
    @FXML private TableColumn<Case, String> casesTable_plaintiff;
    @FXML private TableColumn<Case, String> casesTable_defendant;
    @FXML private TableColumn<Case, Double> casesTable_cost;

    private ObservableList<Case> casesData = FXCollections.observableArrayList();

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
        casesTable_Id.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        casesTable_createdDate.setCellValueFactory(cellData -> cellData.getValue().createdDateProperty());
        casesTable_plaintiff.setCellValueFactory(cellData -> cellData.getValue().plaintiffProperty());

        casesTable_plaintiff.setCellFactory(p -> new TextFlowCell<>());

        casesTable_defendant.setCellValueFactory(cellData -> cellData.getValue().defendantProperty());
        casesTable_cost.setCellValueFactory(cellData -> cellData.getValue().costProperty().asObject());

        casesTable.setItems(casesData);
    }

    public void casesSearchClick(ActionEvent actionEvent) {
        //test data
        Random random = new Random();
        int caseId = random.nextInt(100);
        long days = random.nextInt(50);

        double cost = random.nextDouble() * 10000;

        casesData.add(new Case(caseId, LocalDate.now().minusDays(days), "http://google.com", "Петя", cost));

        //real data
        /*
        CaseModelAppender CaseModelAppender = new CaseModelAppender(casesData);
        casesData.clear();

        CaseLoader caseLoader = new CaseLoader();
        caseLoader.setKadRequest(new KadSearchRequest(...));

        //TODO:
        //caseLoader.setCaseModelAppender(CaseModelAppender);
        //caseLoader.setMinCost(minCost);
        //caseLoader.setSearchLimit(searchLimit);

        Thread casesLoaderThread = new Thread(caseLoader);
        casesLoaderThread.start();
        */
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
