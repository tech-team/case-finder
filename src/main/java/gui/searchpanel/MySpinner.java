package gui.searchpanel;

import gui.Main;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MySpinner extends HBox {
    @FXML private VBox buttonsBox;
    @FXML private Path arrowUpPath;
    @FXML private Path arrowDownPath;
    @FXML private TextField textField;
    @FXML private Button buttonUp;
    @FXML private Button buttonDown;

    private ObjectProperty<BigDecimal> step = new SimpleObjectProperty<>(BigDecimal.ONE);
    private ObjectProperty<BigDecimal> value = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private ObjectProperty<BigDecimal> min = new SimpleObjectProperty<>(null);
    private ObjectProperty<BigDecimal> max = new SimpleObjectProperty<>(null);
    private StringProperty filter = new SimpleStringProperty("-?[0-9]+");

    public MySpinner() {
        super();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/my_spinner.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @FXML
    private void initialize() {
        textField.heightProperty().addListener((ob, oldValue, newValue) -> {
            double newSize = newValue.doubleValue();

            Platform.runLater(() -> {
                buttonsBox.setMinHeight(newSize);
                buttonsBox.setPrefHeight(newSize);
                buttonsBox.setMaxHeight(newSize);

                buttonsBox.setMinWidth(newSize);
                buttonsBox.setPrefWidth(newSize);
                buttonsBox.setMaxWidth(newSize);

                //buttonsBox.spacingProperty().setValue(newSize % 2);
            });
        });

        textField.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.DOWN) {
                decrement();
                keyEvent.consume();
            }
            if (keyEvent.getCode() == KeyCode.UP) {
                increment();
                keyEvent.consume();
            }
        });

        textField.setText(value.getValue().toString());

        textField.textProperty().addListener((ObservableValue<? extends String> ob, String oldValue, String newValue) -> {
            if (newValue.isEmpty()) {
                //if (min.getValue() != null)
                //    value.setValue(min.getValue());
                return;
            }

            Pattern pattern = Pattern.compile(filter.getValue());
            Matcher matcher = pattern.matcher(newValue);
            if (matcher.matches()) {
                BigDecimal bdValue = new BigDecimal(newValue);

                if (min.getValue() != null && bdValue.compareTo(min.getValue()) == -1) //less than min
                    value.setValue(min.getValue());
                else if(max.getValue() != null && bdValue.compareTo(max.getValue()) == 1) //more than max
                    value.setValue(max.getValue());
                else
                    value.setValue(bdValue);
            }
            else
                textField.setText(oldValue);
        });

        value.addListener((ObservableValue<? extends BigDecimal> ob, BigDecimal oldValue, BigDecimal newValue) -> {
            textField.setText(newValue.toString());
        });

        textField.focusedProperty().addListener((ObservableValue<? extends Boolean> ob, Boolean oldValue, Boolean newValue) -> {
            if (textField.getText().isEmpty() && !newValue && min.getValue() != null) {
                value.setValue(min.getValue());
            }
        });

        drawTriangle(arrowUpPath, false);
        drawTriangle(arrowDownPath, true);
    }

    private void drawTriangle(Path path, boolean flipped) {
        final int ARROW_SIZE = 4;
        int sign = flipped ? 1 : -1;

        path.getElements().addAll(
                new MoveTo(-ARROW_SIZE, 0),
                new LineTo(ARROW_SIZE, 0),
                new LineTo(0, sign * ARROW_SIZE),
                new LineTo(-ARROW_SIZE, 0));

        path.setMouseTransparent(true);
    }

    public void increment() {
        value.setValue(value.getValue().add(step.getValue()));
    }

    public void decrement() {
        value.setValue(value.getValue().subtract(step.getValue()));
    }

    public void buttonUpAction(ActionEvent actionEvent) {
        increment();
    }

    public void buttonDownAction(ActionEvent actionEvent) {
        decrement();
    }

    public BigDecimal getStep() {
        return step.get();
    }

    public ObjectProperty<BigDecimal> stepProperty() {
        return step;
    }

    public void setStep(BigDecimal step) {
        this.step.set(step);
    }

    public BigDecimal getValue() {
        return value.get();
    }

    public ObjectProperty<BigDecimal> valueProperty() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value.set(value);
    }

    public BigDecimal getMin() {
        return min.get();
    }

    public ObjectProperty<BigDecimal> minProperty() {
        return min;
    }

    public void setMin(BigDecimal min) {
        this.min.set(min);
    }

    public BigDecimal getMax() {
        return max.get();
    }

    public ObjectProperty<BigDecimal> maxProperty() {
        return max;
    }

    public void setMax(BigDecimal max) {
        this.max.set(max);
    }

    public String getFilter() {
        return filter.get();
    }

    public StringProperty filterProperty() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter.set(filter);
    }
}
