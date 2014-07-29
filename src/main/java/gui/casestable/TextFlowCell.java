package gui.casestable;

import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;
import gui.Main;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import util.HypertextNode;
import util.HypertextParser;

import java.util.List;

public class TextFlowCell<S, T> extends TableCell<S, T> {
    private final TextFlow textFlow;
    private TableRow tableRow;

    public TextFlowCell() {
        this.textFlow = new TextFlow();

        setAlignment(Pos.CENTER_LEFT);
        setGraphic(textFlow);
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        }
        else {
            setGraphic(textFlow);

            ObservableValue<T> ov = getTableColumn().getCellObservableValue(getIndex());
            String value = ov.getValue().toString();

            textFlow.getChildren().clear();
            textFlow.setMaxWidth(Integer.MAX_VALUE);
            textFlow.setPrefWidth(Integer.MAX_VALUE);
            textFlow.setMinWidth(Integer.MAX_VALUE);

            List<HypertextNode> nodes = HypertextParser.parse(value);
            for (HypertextNode node: nodes) {
                if (node.getType() == HypertextNode.Type.LINK) {
                    Hyperlink link = new Hyperlink(node.getValue());

                    link.setOnAction(actionEvent -> {
                        HostServicesFactory.getInstance(Main.instance)
                                .showDocument(value);
                    });

                    textFlow.getChildren().add(link);
                }
                else {
                    Text text = new Text(node.getValue());
                    text.setTextAlignment(TextAlignment.LEFT);
                    text.setWrappingWidth(Double.MAX_VALUE);
                    textFlow.getChildren().add(text);
                }
            }

            //wait for row to be initialized
            //and set handler once
            if (tableRow == null && (tableRow = getTableRow()) != null) {
                tableRow.selectedProperty().addListener((ob, oldValue, newValue) -> {
                    updateColor(newValue);
                });
            }

            //update every time
            if (tableRow != null)
                updateColor(tableRow.selectedProperty().get());
        }
    }

    private void updateColor(boolean selected) {
        for (Node node: textFlow.getChildren()) {
            if (selected)
                node.setStyle("-fx-text-fill:white;-fx-fill:white");
            else
                node.setStyle("");
        }
    }
}
