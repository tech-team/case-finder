package gui.searchpanel;

import gui.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MyProgressIndicator extends VBox {
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label progressLabel;

    private Integer limit = null;
    private int value;

    public MyProgressIndicator() {
        super();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/my_progress_indicator.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        this.getStylesheets().add("/css/my_progress_indicator.css");

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @FXML
    private void initialize() {
        if (progressIndicator.isIndeterminate())
            progressLabel.setVisible(false);

        progressIndicator.indeterminateProperty().addListener((ob, oldValue, newValue) -> {
            progressLabel.setVisible(!newValue);
        });
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void add(int value) {
        this.updateProgress(this.value + value);
    }

    public void subtract(int value) {
        this.updateProgress(this.value - value);
    }

    public void updateProgress(int newValue) {
        if (newValue < 0)
            value = 0;
        else if (limit != null && newValue > limit)
            value = limit;
        else
            value = newValue;

        if (limit != null && limit != 0) {
            progressIndicator.setProgress(((double) value) / limit);
            progressLabel.setText(String.format("%d/%d", value, limit));
        }
    }

    public void setIndeterminate() {
        progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
    }

    public void reset() {
        progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        value = 0;
        limit = null;
    }
}
