package gui;

import gui.casestable.Case;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDate;
import java.util.Random;

public class MainController {
    @FXML private TableView<Case> casesTable;
    @FXML private TableColumn<Case, Integer> casesTable_Id;
    @FXML private TableColumn<Case, LocalDate> casesTable_createdDate;
    @FXML private TableColumn<Case, String> casesTable_plaintiff;
    @FXML private TableColumn<Case, String> casesTable_defendant;
    @FXML private TableColumn<Case, Double> casesTable_cost;

    private ObservableList<Case> casesData = FXCollections.observableArrayList();

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        casesTable_Id.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        casesTable_createdDate.setCellValueFactory(cellData -> cellData.getValue().createdDateProperty());
        casesTable_plaintiff.setCellValueFactory(cellData -> cellData.getValue().plaintiffProperty());
        casesTable_defendant.setCellValueFactory(cellData -> cellData.getValue().defendantProperty());
        casesTable_cost.setCellValueFactory(cellData -> cellData.getValue().costProperty().asObject());

        casesTable.setItems(casesData);
    }

    public void casesSearchClick(ActionEvent actionEvent) {
        Random random = new Random();
        int caseId = random.nextInt(100);
        long days = random.nextInt(50);

        double cost = random.nextDouble() * 10000;

        casesData.add(new Case(caseId, LocalDate.now().minusDays(days), "http://google.com", "Петя", cost));
    }
}
