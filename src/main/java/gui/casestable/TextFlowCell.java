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

        setAlignment(Pos.CENTER);
        setGraphic(textFlow);
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        //wait for row to be initialized
        if (tableRow == null && getTableRow() != null) {
            tableRow = getTableRow();
            tableRow.selectedProperty().addListener((ob, oldValue, newValue) -> {
                for (Node node: textFlow.getChildren()) {
                    if (newValue)
                        node.setStyle("-fx-text-fill:white;-fx-fill:white");
                    else
                        node.setStyle("");
                }
            });
        }

        if (empty) {
            setText(null);
            setGraphic(null);
        }
        else {
            setGraphic(textFlow);

            ObservableValue<T> ov = getTableColumn().getCellObservableValue(getIndex());
            String value = ov.getValue().toString();

            textFlow.getChildren().clear();
            textFlow.setMaxWidth(Double.MAX_VALUE);
            textFlow.setPrefWidth(Double.MAX_VALUE);
            textFlow.setMinWidth(Double.MAX_VALUE);
            textFlow.setTextAlignment(TextAlignment.LEFT);

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
        }
    }
}
